package com.example.authadmin.service;

import com.example.authadmin.dto.InterviewUpgradeDtos;
import com.example.authadmin.dto.MeetingDtos;
import com.example.authadmin.entity.InterviewBooking;
import com.example.authadmin.entity.InterviewSession;
import com.example.authadmin.repository.InterviewBookingRepository;
import com.example.authadmin.repository.InterviewSessionRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple adapter around an external Whisper HTTP service.
 * If app.stt.whisper.url is not configured, STT endpoints will return 503 errors.
 */
@Service
public class MeetingSttService {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final InterviewBookingRepository bookingRepository;
    private final InterviewSessionRepository sessionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final InterviewUpgradeService interviewUpgradeService;
    private final Map<Integer, Set<String>> processedChunkKeys = new ConcurrentHashMap<>();
    private final Map<Integer, List<MeetingDtos.TranscriptSegment>> segmentCache = new ConcurrentHashMap<>();

    @Value("${app.stt.whisper.url:}")
    private String whisperUrl;

    public MeetingSttService(InterviewBookingRepository bookingRepository,
                             InterviewSessionRepository sessionRepository,
                             SimpMessagingTemplate messagingTemplate,
                             InterviewUpgradeService interviewUpgradeService) {
        this.bookingRepository = bookingRepository;
        this.sessionRepository = sessionRepository;
        this.messagingTemplate = messagingTemplate;
        this.interviewUpgradeService = interviewUpgradeService;
    }

    public Map<String, Object> acceptChunk(Integer bookingId, byte[] audioBytes, String mimeType, String clientId, String chunkSeq) {
        if (bookingId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "bookingId is required");
        }
        InterviewBooking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        InterviewSession session = sessionRepository.findByBooking(booking).orElseGet(() -> {
            InterviewSession s = new InterviewSession();
            s.setBooking(booking);
            return s;
        });
        String chunkKey = (clientId == null ? "unknown" : clientId) + ":" + (chunkSeq == null ? "" : chunkSeq);
        if (chunkSeq != null && !chunkSeq.isBlank()) {
            Set<String> keys = processedChunkKeys.computeIfAbsent(bookingId, k -> java.util.Collections.synchronizedSet(new HashSet<>()));
            if (!keys.add(chunkKey)) {
                Map<String, Object> dupe = new HashMap<>();
                dupe.put("bookingId", bookingId);
                dupe.put("duplicate", true);
                dupe.put("ackKey", chunkKey);
                return dupe;
            }
        }

        WhisperResult wr = callWhisper(audioBytes, mimeType, false);
        String partial = wr.text;
        String existing = session.getTranscriptText() == null ? "" : session.getTranscriptText();
        String merged = (existing + " " + (partial == null ? "" : partial)).trim();
        session.setTranscriptText(merged);
        sessionRepository.save(session);

        if (wr.segments != null && !wr.segments.isEmpty()) {
            List<MeetingDtos.TranscriptSegment> cached = segmentCache.computeIfAbsent(bookingId, k -> new ArrayList<>());
            cached.addAll(wr.segments);
        }

        // Push transcript updates to STOMP for real-time UI.
        Map<String, Object> transcriptPush = new HashMap<>();
        transcriptPush.put("bookingId", bookingId);
        transcriptPush.put("partialTranscript", partial);
        transcriptPush.put("fullTranscript", merged);
        transcriptPush.put("segments", segmentCache.getOrDefault(bookingId, List.of()));
        messagingTemplate.convertAndSend("/topic/meeting/stt/" + bookingId, transcriptPush);

        // Also compute score and push score updates.
        try {
            InterviewUpgradeDtos.CommunicationScoreRequest req = new InterviewUpgradeDtos.CommunicationScoreRequest();
            req.setBookingId(bookingId);
            req.setTranscriptText(merged);
            int dur = session.getSpeakingDurationSeconds() == null ? 60 : Math.max(10, session.getSpeakingDurationSeconds());
            req.setSpeakingDurationSeconds(dur);
            InterviewUpgradeDtos.CommunicationScoreResponse score = interviewUpgradeService.scoreCommunication(req);
            Map<String, Object> scorePush = new HashMap<>();
            scorePush.put("bookingId", bookingId);
            scorePush.put("communicationScore", score.getCommunicationScore());
            scorePush.put("behavioralScore", score.getBehavioralScore());
            scorePush.put("technicalScore", score.getTechnicalScore());
            scorePush.put("finalScore", score.getFinalScore());
            messagingTemplate.convertAndSend("/topic/meeting/score/" + bookingId, scorePush);
        } catch (Exception ignored) {}

        Map<String, Object> out = new HashMap<>();
        out.put("bookingId", bookingId);
        out.put("partialTranscript", partial);
        out.put("fullTranscript", merged);
        out.put("ackKey", chunkKey);
        out.put("segments", segmentCache.getOrDefault(bookingId, List.of()));
        return out;
    }

    public Map<String, Object> finalizeTranscript(Integer bookingId) {
        if (bookingId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "bookingId is required");
        }
        InterviewBooking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        InterviewSession session = sessionRepository.findByBooking(booking)
            .orElseGet(() -> {
                InterviewSession s = new InterviewSession();
                s.setBooking(booking);
                return s;
            });
        String text = session.getTranscriptText() == null ? "" : session.getTranscriptText();
        Map<String, Object> out = new HashMap<>();
        out.put("bookingId", bookingId);
        out.put("transcriptText", text);
        out.put("segments", segmentCache.getOrDefault(bookingId, List.of()));
        out.put("avgConfidence", avgConfidence(segmentCache.getOrDefault(bookingId, List.of())));
        return out;
    }

    private WhisperResult callWhisper(byte[] audioBytes, String mimeType, boolean finalRequest) {
        if (whisperUrl == null || whisperUrl.isBlank()) {
            return WhisperResult.empty();
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("audio", Base64.getEncoder().encodeToString(audioBytes));
            body.put("mimeType", mimeType == null ? "audio/webm" : mimeType);
            body.put("final", finalRequest);
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(whisperUrl))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            HttpResponse<String> resp = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                return WhisperResult.empty();
            }
            com.fasterxml.jackson.databind.JsonNode node =
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(resp.body());
            WhisperResult out = new WhisperResult();
            out.text = node.path("text").asText("");
            com.fasterxml.jackson.databind.JsonNode segs = node.path("segments");
            if (segs.isArray()) {
                for (int i = 0; i < segs.size(); i++) {
                    com.fasterxml.jackson.databind.JsonNode s = segs.get(i);
                    MeetingDtos.TranscriptSegment ts = new MeetingDtos.TranscriptSegment();
                    ts.setSpeaker(s.path("speaker").asText("speaker-1"));
                    ts.setText(s.path("text").asText(""));
                    if (s.has("confidence")) ts.setConfidence(s.path("confidence").asDouble(0.0));
                    if (s.has("start")) ts.setStartSec(s.path("start").asDouble(0.0));
                    if (s.has("end")) ts.setEndSec(s.path("end").asDouble(0.0));
                    out.segments.add(ts);
                }
            }
            return out;
        } catch (Exception e) {
            return WhisperResult.empty();
        }
    }

    private static Double avgConfidence(List<MeetingDtos.TranscriptSegment> segments) {
        if (segments == null || segments.isEmpty()) return null;
        double sum = 0;
        int c = 0;
        for (MeetingDtos.TranscriptSegment s : segments) {
            if (s == null || s.getConfidence() == null) continue;
            sum += s.getConfidence();
            c++;
        }
        return c == 0 ? null : (sum / c);
    }

    private static class WhisperResult {
        String text = "";
        List<MeetingDtos.TranscriptSegment> segments = new ArrayList<>();
        static WhisperResult empty() { return new WhisperResult(); }
    }
}

