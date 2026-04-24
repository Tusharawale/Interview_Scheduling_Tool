package com.example.authadmin.service;

import com.example.authadmin.dto.InterviewUpgradeDtos;
import com.example.authadmin.entity.*;
import com.example.authadmin.repository.*;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InterviewUpgradeService {
    private static final Set<String> FILLERS = Set.of("um", "uh", "hmm", "like", "basically", "actually", "you know");
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final InterviewBookingRepository bookingRepository;
    private final InterviewSessionRepository sessionRepository;
    private final RankingWeightRepository rankingWeightRepository;
    private final UserRepository userRepository;
    private final ProfileAnalyticsService profileAnalyticsService;
    private final CodingChallengeRepository codingChallengeRepository;
    private final CodingSubmissionRepository codingSubmissionRepository;
    private final FinalReportRepository finalReportRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;

    @Value("${app.coding.judge0.url:}")
    private String judge0Url;

    @Value("${app.coding.judge0.api-key:}")
    private String judge0ApiKey;

    public InterviewUpgradeService(InterviewBookingRepository bookingRepository,
                                   InterviewSessionRepository sessionRepository,
                                   RankingWeightRepository rankingWeightRepository,
                                   UserRepository userRepository,
                                   ProfileAnalyticsService profileAnalyticsService,
                                   CodingChallengeRepository codingChallengeRepository,
                                   CodingSubmissionRepository codingSubmissionRepository,
                                   FinalReportRepository finalReportRepository,
                                   FileStorageService fileStorageService,
                                   EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.sessionRepository = sessionRepository;
        this.rankingWeightRepository = rankingWeightRepository;
        this.userRepository = userRepository;
        this.profileAnalyticsService = profileAnalyticsService;
        this.codingChallengeRepository = codingChallengeRepository;
        this.codingSubmissionRepository = codingSubmissionRepository;
        this.finalReportRepository = finalReportRepository;
        this.fileStorageService = fileStorageService;
        this.emailService = emailService;
    }

    public InterviewUpgradeDtos.CommunicationScoreResponse scoreCommunication(InterviewUpgradeDtos.CommunicationScoreRequest req) {
        if (req.getBookingId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "bookingId is required");
        if (req.getTechnicalScore() != null && (req.getTechnicalScore() < 0 || req.getTechnicalScore() > 100)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "technicalScore must be between 0 and 100");
        }
        if (req.getBehavioralScore() != null && (req.getBehavioralScore() < 0 || req.getBehavioralScore() > 100)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "behavioralScore must be between 0 and 100");
        }
        InterviewBooking booking = bookingRepository.findById(req.getBookingId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        InterviewSession session = sessionRepository.findByBooking(booking).orElseGet(InterviewSession::new);
        session.setBooking(booking);
        String transcript = req.getTranscriptText() == null ? "" : req.getTranscriptText().trim();
        int durationSec = req.getSpeakingDurationSeconds() == null || req.getSpeakingDurationSeconds() <= 0 ? 60 : req.getSpeakingDurationSeconds();

        List<String> tokens = Arrays.stream(transcript.toLowerCase().split("\\s+"))
            .map(s -> s.replaceAll("[^a-z]", ""))
            .filter(s -> !s.isBlank())
            .toList();
        int words = tokens.size();
        int fillerCount = (int) tokens.stream().filter(FILLERS::contains).count();
        double wpm = durationSec > 0 ? (words * 60.0) / durationSec : 0.0;

        double fillerRatio = words > 0 ? (fillerCount * 1.0 / words) : 1.0;
        double fillerPenalty = clamp(fillerRatio * 120.0, 0, 35);
        double speedPenalty = speedPenalty(wpm);
        double clarity = clamp(100.0 - fillerPenalty - speedPenalty, 0, 100);
        double confidence = clamp(100.0 - fillerPenalty * 1.2 - speedPenalty * 0.6, 0, 100);
        double communication = clamp(clarity * 0.55 + confidence * 0.45, 0, 100);

        if (req.getTechnicalScore() != null) session.setTechnicalScore(round2(req.getTechnicalScore()));
        if (req.getBehavioralScore() != null) session.setBehavioralScore(round2(req.getBehavioralScore()));
        session.setTranscriptText(transcript);
        session.setSpeakingDurationSeconds(durationSec);
        session.setWordsCount(words);
        session.setFillerWordsCount(fillerCount);
        session.setSpeakingSpeedWpm(round2(wpm));
        session.setClarityScore(round2(clarity));
        session.setConfidenceScore(round2(confidence));
        session.setCommunicationScore(round2(communication));
        sessionRepository.save(session);
        recalculateFinalScoreForUser(booking.getUser().getId());
        return toCommunicationResponse(session);
    }

    public InterviewUpgradeDtos.RankingWeightResponse getWeights() {
        RankingWeight w = getOrCreateWeights();
        return toWeightDto(w);
    }

    public InterviewUpgradeDtos.RankingWeightResponse updateWeights(InterviewUpgradeDtos.RankingWeightRequest req) {
        RankingWeight w = getOrCreateWeights();
        double communication = req.getCommunicationWeight() == null ? w.getCommunicationWeight().doubleValue() : req.getCommunicationWeight();
        double technical = req.getTechnicalWeight() == null ? w.getTechnicalWeight().doubleValue() : req.getTechnicalWeight();
        double behavioral = req.getBehavioralWeight() == null ? w.getBehavioralWeight().doubleValue() : req.getBehavioralWeight();
        double profile = req.getProfileWeight() == null ? w.getProfileWeight().doubleValue() : req.getProfileWeight();
        if (communication < 0 || technical < 0 || behavioral < 0 || profile < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "weights cannot be negative");
        }
        double sum = communication + technical + behavioral + profile;
        if (sum <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "weights sum must be > 0");
        w.setCommunicationWeight(round2(communication * 100.0 / sum));
        w.setTechnicalWeight(round2(technical * 100.0 / sum));
        w.setBehavioralWeight(round2(behavioral * 100.0 / sum));
        w.setProfileWeight(round2(profile * 100.0 / sum));
        rankingWeightRepository.save(w);
        recalculateAllFinalScores();
        return toWeightDto(w);
    }

    public InterviewUpgradeDtos.RankingResponse buildRanking() {
        RankingWeight w = getOrCreateWeights();
        List<InterviewUpgradeDtos.CandidateRankingRow> rows = userRepository.findAll().stream()
            .map(u -> buildRankingRow(u, w))
            .sorted(Comparator.comparing(InterviewUpgradeDtos.CandidateRankingRow::getFinalScore).reversed())
            .collect(Collectors.toList());
        InterviewUpgradeDtos.RankingResponse resp = new InterviewUpgradeDtos.RankingResponse();
        resp.setWeights(toWeightDto(w));
        resp.setCandidates(rows);
        return resp;
    }

    public List<InterviewUpgradeDtos.CodingChallengeResponse> listActiveChallenges() {
        return codingChallengeRepository.findByActiveTrueOrderByIdDesc().stream().map(this::toChallengeDto).toList();
    }

    public List<InterviewUpgradeDtos.CodingChallengeResponse> listAllChallenges() {
        return codingChallengeRepository.findAll().stream()
            .sorted(Comparator.comparing(CodingChallenge::getId).reversed())
            .map(this::toChallengeDto).toList();
    }

    public Map<String, Object> judge0Status() {
        Map<String, Object> out = new LinkedHashMap<>();
        boolean enabled = judge0Url != null && !judge0Url.isBlank();
        out.put("enabled", enabled);
        out.put("url", enabled ? judge0Url : "");
        if (!enabled) {
            out.put("reachable", false);
            out.put("message", "Judge0 not configured. Using built-in evaluator fallback.");
            return out;
        }
        try {
            String base = judge0Url.endsWith("/") ? judge0Url : judge0Url + "/";
            HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(base + "languages"))
                .timeout(Duration.ofSeconds(6))
                .header("Accept", "application/json");
            if (judge0ApiKey != null && !judge0ApiKey.isBlank()) {
                b.header("X-RapidAPI-Key", judge0ApiKey);
            }
            HttpResponse<String> resp = HTTP_CLIENT.send(b.GET().build(), HttpResponse.BodyHandlers.ofString());
            boolean ok = resp.statusCode() >= 200 && resp.statusCode() < 300;
            out.put("reachable", ok);
            out.put("httpStatus", resp.statusCode());
            out.put("message", ok ? "Judge0 is reachable." : "Judge0 responded with non-success status.");
            return out;
        } catch (Exception e) {
            out.put("reachable", false);
            out.put("message", "Judge0 connection failed: " + e.getMessage());
            return out;
        }
    }

    public InterviewUpgradeDtos.CodingChallengeResponse saveChallenge(InterviewUpgradeDtos.CodingChallengeRequest req) {
        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge title is required");
        }
        if (req.getDescription() == null || req.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge description is required");
        }
        CodingChallenge c = new CodingChallenge();
        c.setTitle(defaultText(req.getTitle(), "Untitled challenge"));
        c.setDescription(defaultText(req.getDescription(), ""));
        c.setLanguage(defaultText(req.getLanguage(), "java"));
        c.setStarterCode(req.getStarterCode());
        c.setExpectedOutput(req.getExpectedOutput());
        c.setTimeLimitSeconds(req.getTimeLimitSeconds() == null ? 2 : req.getTimeLimitSeconds());
        c.setMemoryLimitMb(req.getMemoryLimitMb() == null ? 256 : req.getMemoryLimitMb());
        c.setActive(req.getActive() == null || req.getActive());
        codingChallengeRepository.save(c);
        return toChallengeDto(c);
    }

    public InterviewUpgradeDtos.CodingSubmissionResponse submitCode(InterviewUpgradeDtos.CodingSubmissionRequest req) {
        if (req.getBookingId() == null || req.getChallengeId() == null || req.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "bookingId, challengeId and userId are required");
        }
        InterviewBooking booking = bookingRepository.findById(req.getBookingId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        User user = userRepository.findById(req.getUserId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        CodingChallenge challenge = codingChallengeRepository.findById(req.getChallengeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
        if (!challenge.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge is inactive");
        }
        if (!Objects.equals(booking.getUser().getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "bookingId does not belong to userId");
        }
        if (req.getSourceCode() == null || req.getSourceCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sourceCode cannot be empty");
        }

        CodingEvalResult eval = evaluateCode(req, challenge);

        CodingSubmission s = new CodingSubmission();
        s.setBooking(booking);
        s.setChallenge(challenge);
        s.setUser(user);
        s.setLanguage(defaultText(req.getLanguage(), challenge.getLanguage()));
        s.setSourceCode(req.getSourceCode());
        s.setStdoutText(eval.stdoutText);
        s.setExecutionTimeMs(eval.executionTimeMs);
        s.setScore(round2(eval.score));
        s.setStatus(eval.status);
        codingSubmissionRepository.save(s);

        InterviewSession session = sessionRepository.findByBooking(booking).orElseGet(InterviewSession::new);
        session.setBooking(booking);
        session.setTechnicalScore(round2(eval.score));
        sessionRepository.save(session);
        recalculateFinalScoreForUser(user.getId());

        InterviewUpgradeDtos.CodingSubmissionResponse resp = new InterviewUpgradeDtos.CodingSubmissionResponse();
        resp.setId(s.getId());
        resp.setBookingId(booking.getId());
        resp.setChallengeId(challenge.getId());
        resp.setUserId(user.getId());
        resp.setStatus(s.getStatus());
        resp.setExecutionTimeMs(s.getExecutionTimeMs());
        resp.setScore(s.getScore());
        resp.setStdoutText(s.getStdoutText());
        return resp;
    }

    public InterviewUpgradeDtos.FinalReportResponse finalReport() {
        InterviewUpgradeDtos.RankingResponse ranking = buildRanking();
        List<InterviewSession> sessions = sessionRepository.findAll();
        List<CodingSubmission> submissions = codingSubmissionRepository.findAll();

        InterviewUpgradeDtos.FinalReportResponse r = new InterviewUpgradeDtos.FinalReportResponse();
        r.setGeneratedAt(java.time.LocalDateTime.now().toString());
        r.setTotalUsers(userRepository.count());
        r.setTotalInterviewSessions(sessions.size());
        r.setTotalCodingSubmissions(submissions.size());
        r.setAverageCommunication(avg(sessions.stream().map(InterviewSession::getCommunicationScore).toList()));
        r.setAverageTechnical(avg(sessions.stream().map(InterviewSession::getTechnicalScore).toList()));
        r.setAverageBehavioral(avg(sessions.stream().map(InterviewSession::getBehavioralScore).toList()));
        r.setAverageFinal(avg(sessions.stream().map(InterviewSession::getFinalScore).toList()));
        r.setAcceptedSubmissions((int) submissions.stream().filter(s -> "ACCEPTED".equalsIgnoreCase(s.getStatus())).count());
        r.setTopCandidates(ranking.getCandidates().stream().limit(10).toList());

        Map<String, Long> statusCounts = submissions.stream()
            .collect(Collectors.groupingBy(s -> defaultText(s.getStatus(), "UNKNOWN"), Collectors.counting()));
        List<InterviewUpgradeDtos.NameValue> statusList = statusCounts.entrySet().stream()
            .map(e -> new InterviewUpgradeDtos.NameValue(e.getKey(), e.getValue().doubleValue()))
            .sorted(Comparator.comparing(InterviewUpgradeDtos.NameValue::getName))
            .toList();
        r.setCodingStatusDistribution(statusList);

        List<InterviewUpgradeDtos.NameValue> scoreBands = List.of(
            new InterviewUpgradeDtos.NameValue("0-39", countBand(ranking.getCandidates(), 0, 39.999)),
            new InterviewUpgradeDtos.NameValue("40-59", countBand(ranking.getCandidates(), 40, 59.999)),
            new InterviewUpgradeDtos.NameValue("60-79", countBand(ranking.getCandidates(), 60, 79.999)),
            new InterviewUpgradeDtos.NameValue("80-100", countBand(ranking.getCandidates(), 80, 100))
        );
        r.setFinalScoreBands(scoreBands);

        List<InterviewUpgradeDtos.CommunicationBehavioralPoint> scatter = (ranking.getCandidates() == null ? List.<InterviewUpgradeDtos.CandidateRankingRow>of() : ranking.getCandidates())
            .stream()
            .map(row -> {
                InterviewUpgradeDtos.CommunicationBehavioralPoint p = new InterviewUpgradeDtos.CommunicationBehavioralPoint();
                p.setUserId(row.getUserId());
                p.setUsername(row.getUsername());
                p.setDisplayName(row.getDisplayName());
                p.setCommunication(row.getCommunicationScore() == null ? 0.0 : row.getCommunicationScore().doubleValue());
                p.setBehavioral(row.getBehavioralScore() == null ? 0.0 : row.getBehavioralScore().doubleValue());
                p.setFinalScore(row.getFinalScore() == null ? 0.0 : row.getFinalScore().doubleValue());
                return p;
            })
            .toList();
        r.setCommunicationBehavioralScatter(scatter);
        return r;
    }

    public InterviewUpgradeDtos.PublishReportResponse publishFinalReport(boolean sendEmails) {
        InterviewUpgradeDtos.FinalReportResponse report = finalReport();
        try {
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(report);
            Path root = fileStorageService.getUploadRoot();
            Path reportsDir = root.resolve("reports");
            Files.createDirectories(reportsDir);
            long ts = System.currentTimeMillis();
            String pdfFileName = "final-report-" + ts + ".pdf";
            Path pdfTarget = reportsDir.resolve(pdfFileName);
            writeFinalReportPdf(report, pdfTarget);
            String pdfRelPath = "reports/" + pdfFileName;

            FinalReport fr = new FinalReport();
            fr.setTotalUsers((int) report.getTotalUsers());
            fr.setReportPath(pdfRelPath);
            fr.setReportJson(json);
            finalReportRepository.save(fr);

            int emailed = 0;
            if (sendEmails && report.getTopCandidates() != null && !report.getTopCandidates().isEmpty()) {
                Map<Integer, User> userById = userRepository.findAll().stream()
                    .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
                int total = (int) report.getTotalUsers();
                // email all ranked candidates, not just top 10
                List<InterviewUpgradeDtos.CandidateRankingRow> allRows = buildRanking().getCandidates();
                for (int i = 0; i < allRows.size(); i++) {
                    InterviewUpgradeDtos.CandidateRankingRow row = allRows.get(i);
                    User u = userById.get(row.getUserId());
                    if (u == null || u.getEmail() == null || u.getEmail().isBlank()) continue;
                    emailService.sendUserPositionEmailWithAttachment(
                        u.getEmail(),
                        defaultText(u.getUsername(), "User"),
                        i + 1,
                        total <= 0 ? allRows.size() : total,
                        row.getFinalScore() == null ? 0.0 : row.getFinalScore().doubleValue(),
                        pdfTarget.toFile()
                    );
                    emailed++;
                }
            }

            InterviewUpgradeDtos.PublishReportResponse resp = new InterviewUpgradeDtos.PublishReportResponse();
            resp.setReportId(fr.getId());
            resp.setReportJsonPath(null);
            resp.setReportPdfPath(pdfRelPath);
            resp.setEmailedUsers(emailed);
            resp.setGeneratedAt(fr.getGeneratedAt() == null ? java.time.LocalDateTime.now().toString() : fr.getGeneratedAt().toString());
            return resp;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to publish report: " + e.getMessage());
        }
    }

    public InterviewUpgradeDtos.UserPositionResponse latestUserPosition(Integer userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        FinalReport fr = finalReportRepository.findTopByOrderByGeneratedAtDesc()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No published report available"));
        try {
            String json = defaultText(fr.getReportJson(), "");
            if (json.isBlank()) throw new IllegalStateException("Empty report");
            com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
            com.fasterxml.jackson.databind.JsonNode top = root.path("topCandidates");
            // Use current ranking for complete position if top list doesn't include user
            List<InterviewUpgradeDtos.CandidateRankingRow> all = buildRanking().getCandidates();
            int pos = -1;
            double score = 0.0;
            for (int i = 0; i < all.size(); i++) {
                if (Objects.equals(all.get(i).getUserId(), userId)) {
                    pos = i + 1;
                    score = all.get(i).getFinalScore() == null ? 0.0 : all.get(i).getFinalScore().doubleValue();
                    break;
                }
            }
            if (pos < 0 && top.isArray()) {
                for (int i = 0; i < top.size(); i++) {
                    com.fasterxml.jackson.databind.JsonNode row = top.get(i);
                    if (row.path("userId").asInt(-1) == userId) {
                        pos = i + 1;
                        score = row.path("finalScore").asDouble(0.0);
                        break;
                    }
                }
            }
            InterviewUpgradeDtos.UserPositionResponse resp = new InterviewUpgradeDtos.UserPositionResponse();
            resp.setUserId(user.getId());
            resp.setUsername(user.getUsername());
            resp.setPosition(pos > 0 ? pos : null);
            resp.setTotalCandidates((int) userRepository.count());
            resp.setFinalScore(score);
            resp.setGeneratedAt(fr.getGeneratedAt() == null ? null : fr.getGeneratedAt().toString());
            return resp;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read report: " + e.getMessage());
        }
    }

    private CodingEvalResult evaluateCode(InterviewUpgradeDtos.CodingSubmissionRequest req, CodingChallenge challenge) {
        if (judge0Url != null && !judge0Url.isBlank()) {
            CodingEvalResult remote = tryJudge0(req, challenge);
            if (remote != null) return remote;
        }
        String source = defaultText(req.getSourceCode(), "");
        String expected = defaultText(challenge.getExpectedOutput(), "").trim();
        String language = defaultText(req.getLanguage(), challenge.getLanguage()).toLowerCase();

        int complexityBonus = Math.min(20, Math.max(0, source.length() / 80));
        boolean expectedFound = !expected.isBlank() && source.toLowerCase().contains(expected.toLowerCase());
        boolean hasMain = source.contains("main(") || source.contains("def ") || source.contains("function ");
        double base = hasMain ? 35 : 20;
        double outputScore = expectedFound ? 45 : 20;
        double qualityScore = complexityBonus;
        if ("java".equals(language) && source.contains("System.out")) qualityScore += 5;
        if ("python".equals(language) && source.contains("print(")) qualityScore += 5;
        if (language.contains("js") && source.contains("console.log")) qualityScore += 5;
        double score = clamp(base + outputScore + qualityScore, 0, 100);
        CodingEvalResult result = new CodingEvalResult();
        result.executionTimeMs = 120 + (int) Math.min(1200, source.length() * 0.8);
        result.stdoutText = expectedFound ? expected : "Evaluation fallback mode: expected output not matched in source.";
        result.score = score;
        result.status = expectedFound ? "ACCEPTED" : "PARTIAL";
        return result;
    }

    private CodingEvalResult tryJudge0(InterviewUpgradeDtos.CodingSubmissionRequest req, CodingChallenge challenge) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("source_code", defaultText(req.getSourceCode(), ""));
            body.put("language_id", languageId(defaultText(req.getLanguage(), challenge.getLanguage())));
            body.put("stdin", defaultText(req.getStdin(), ""));
            body.put("expected_output", defaultText(challenge.getExpectedOutput(), ""));
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
            HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(judge0Url.endsWith("/") ? judge0Url + "submissions?base64_encoded=false&wait=true" : judge0Url + "/submissions?base64_encoded=false&wait=true"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json");
            if (judge0ApiKey != null && !judge0ApiKey.isBlank()) {
                b.header("X-RapidAPI-Key", judge0ApiKey);
            }
            HttpResponse<String> resp = HTTP_CLIENT.send(b.POST(HttpRequest.BodyPublishers.ofString(json)).build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) return null;
            com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(resp.body());
            CodingEvalResult out = new CodingEvalResult();
            String statusDesc = node.path("status").path("description").asText("UNKNOWN");
            boolean accepted = "Accepted".equalsIgnoreCase(statusDesc);
            out.status = accepted ? "ACCEPTED" : "FAILED";
            out.executionTimeMs = (int) (node.path("time").asDouble(0.0) * 1000);
            out.stdoutText = node.path("stdout").asText("");
            out.score = accepted ? 100.0 : 35.0;
            return out;
        } catch (Exception ignore) {
            return null;
        }
    }

    private int languageId(String language) {
        String l = language == null ? "" : language.toLowerCase();
        if (l.contains("java")) return 62;
        if (l.contains("python")) return 71;
        if (l.contains("javascript") || l.equals("js")) return 63;
        return 62;
    }

    private InterviewUpgradeDtos.CandidateRankingRow buildRankingRow(User u, RankingWeight w) {
        List<InterviewBooking> bookings = bookingRepository.findByUser(u);
        List<InterviewSession> sessions = bookings.stream()
            .map(b -> sessionRepository.findByBooking(b).orElse(null))
            .filter(Objects::nonNull)
            .toList();
        double comm = avg(sessions.stream().map(InterviewSession::getCommunicationScore).toList());
        double tech = avg(sessions.stream().map(InterviewSession::getTechnicalScore).toList());
        double beh = avg(sessions.stream().map(InterviewSession::getBehavioralScore).toList());
        double profile = profileAnalyticsService.buildUserAnalytics(u.getId()).getProfileCompleteness();
        double finalScore = weighted(comm, tech, beh, profile, w);

        InterviewUpgradeDtos.CandidateRankingRow row = new InterviewUpgradeDtos.CandidateRankingRow();
        row.setUserId(u.getId());
        row.setUsername(u.getUsername());
        row.setDisplayName(profileAnalyticsService.buildUserAnalytics(u.getId()).getDisplayName());
        row.setCommunicationScore(round2(comm));
        row.setTechnicalScore(round2(tech));
        row.setBehavioralScore(round2(beh));
        row.setProfileScore(round2(profile));
        row.setFinalScore(round2(finalScore));
        return row;
    }

    private void recalculateFinalScoreForUser(Integer userId) {
        RankingWeight w = getOrCreateWeights();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;
        List<InterviewBooking> bookings = bookingRepository.findByUser(user);
        double profile = profileAnalyticsService.buildUserAnalytics(userId).getProfileCompleteness();
        for (InterviewBooking b : bookings) {
            InterviewSession s = sessionRepository.findByBooking(b).orElse(null);
            if (s == null) continue;
            double comm = s.getCommunicationScore() == null ? 0 : s.getCommunicationScore().doubleValue();
            double tech = s.getTechnicalScore() == null ? 0 : s.getTechnicalScore().doubleValue();
            double beh = s.getBehavioralScore() == null ? 0 : s.getBehavioralScore().doubleValue();
            s.setFinalScore(round2(weighted(comm, tech, beh, profile, w)));
            sessionRepository.save(s);
        }
    }

    private void recalculateAllFinalScores() {
        userRepository.findAll().forEach(u -> recalculateFinalScoreForUser(u.getId()));
    }

    private double weighted(double comm, double tech, double beh, double profile, RankingWeight w) {
        return (comm * w.getCommunicationWeight().doubleValue()
            + tech * w.getTechnicalWeight().doubleValue()
            + beh * w.getBehavioralWeight().doubleValue()
            + profile * w.getProfileWeight().doubleValue()) / 100.0;
    }

    private RankingWeight getOrCreateWeights() {
        RankingWeight w = rankingWeightRepository.findById(1).orElse(null);
        if (w != null) return w;
        RankingWeight nw = new RankingWeight();
        nw.setId(1);
        nw.setCommunicationWeight(BigDecimal.valueOf(35));
        nw.setTechnicalWeight(BigDecimal.valueOf(40));
        nw.setBehavioralWeight(BigDecimal.valueOf(15));
        nw.setProfileWeight(BigDecimal.valueOf(10));
        return rankingWeightRepository.save(nw);
    }

    private InterviewUpgradeDtos.CommunicationScoreResponse toCommunicationResponse(InterviewSession s) {
        InterviewUpgradeDtos.CommunicationScoreResponse resp = new InterviewUpgradeDtos.CommunicationScoreResponse();
        resp.setSessionId(s.getId());
        resp.setBookingId(s.getBooking().getId());
        resp.setUserId(s.getBooking().getUser().getId());
        resp.setWordsCount(s.getWordsCount() == null ? 0 : s.getWordsCount());
        resp.setFillerWordsCount(s.getFillerWordsCount() == null ? 0 : s.getFillerWordsCount());
        resp.setSpeakingSpeedWpm(s.getSpeakingSpeedWpm());
        resp.setClarityScore(s.getClarityScore());
        resp.setConfidenceScore(s.getConfidenceScore());
        resp.setCommunicationScore(s.getCommunicationScore());
        resp.setTechnicalScore(s.getTechnicalScore());
        resp.setBehavioralScore(s.getBehavioralScore());
        resp.setFinalScore(s.getFinalScore());
        return resp;
    }

    private InterviewUpgradeDtos.RankingWeightResponse toWeightDto(RankingWeight w) {
        InterviewUpgradeDtos.RankingWeightResponse resp = new InterviewUpgradeDtos.RankingWeightResponse();
        resp.setCommunicationWeight(w.getCommunicationWeight());
        resp.setTechnicalWeight(w.getTechnicalWeight());
        resp.setBehavioralWeight(w.getBehavioralWeight());
        resp.setProfileWeight(w.getProfileWeight());
        return resp;
    }

    private InterviewUpgradeDtos.CodingChallengeResponse toChallengeDto(CodingChallenge c) {
        InterviewUpgradeDtos.CodingChallengeResponse r = new InterviewUpgradeDtos.CodingChallengeResponse();
        r.setId(c.getId());
        r.setTitle(c.getTitle());
        r.setDescription(c.getDescription());
        r.setLanguage(c.getLanguage());
        r.setStarterCode(c.getStarterCode());
        r.setTimeLimitSeconds(c.getTimeLimitSeconds());
        r.setMemoryLimitMb(c.getMemoryLimitMb());
        r.setActive(c.isActive());
        return r;
    }

    private static String defaultText(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }

    private static double speedPenalty(double wpm) {
        if (wpm <= 0) return 30;
        if (wpm < 90) return 20;
        if (wpm <= 170) return 0;
        if (wpm <= 220) return 12;
        return 24;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private static BigDecimal round2(double d) {
        return BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP);
    }

    private static double avg(List<BigDecimal> values) {
        List<BigDecimal> filtered = values.stream().filter(Objects::nonNull).toList();
        if (filtered.isEmpty()) return 0.0;
        BigDecimal sum = filtered.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(filtered.size()), 4, RoundingMode.HALF_UP).doubleValue();
    }

    private static double countBand(List<InterviewUpgradeDtos.CandidateRankingRow> rows, double min, double max) {
        return rows.stream().filter(x -> {
            double v = x.getFinalScore() == null ? 0.0 : x.getFinalScore().doubleValue();
            return v >= min && v <= max;
        }).count();
    }

    private static void writeFinalReportPdf(InterviewUpgradeDtos.FinalReportResponse report, Path output) throws java.io.IOException, DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, new java.io.FileOutputStream(output.toFile()));
        document.open();
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font hFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font body = FontFactory.getFont(FontFactory.HELVETICA, 10);

        document.add(new Paragraph("Final Interview Report", titleFont));
        document.add(new Paragraph("Generated: " + defaultText(report.getGeneratedAt(), "-"), body));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Summary", hFont));
        document.add(new Paragraph("Total Users: " + report.getTotalUsers(), body));
        document.add(new Paragraph("Interview Sessions: " + report.getTotalInterviewSessions(), body));
        document.add(new Paragraph("Coding Submissions: " + report.getTotalCodingSubmissions(), body));
        document.add(new Paragraph("Average Communication: " + String.format("%.2f", report.getAverageCommunication()), body));
        document.add(new Paragraph("Average Technical: " + String.format("%.2f", report.getAverageTechnical()), body));
        document.add(new Paragraph("Average Behavioral: " + String.format("%.2f", report.getAverageBehavioral()), body));
        document.add(new Paragraph("Average Final: " + String.format("%.2f", report.getAverageFinal()), body));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Top Candidates", hFont));
        List<InterviewUpgradeDtos.CandidateRankingRow> top = report.getTopCandidates() == null ? List.of() : report.getTopCandidates();
        for (int i = 0; i < Math.min(20, top.size()); i++) {
            InterviewUpgradeDtos.CandidateRankingRow r = top.get(i);
            document.add(new Paragraph(
                "#" + (i + 1) + " " + defaultText(r.getDisplayName(), defaultText(r.getUsername(), "User " + r.getUserId())) +
                    " | Final: " + (r.getFinalScore() == null ? "0.00" : r.getFinalScore().setScale(2, RoundingMode.HALF_UP)),
                body
            ));
        }

        try {
            byte[] avgBar = buildAverageScoresBarPng(report);
            if (avgBar != null && avgBar.length > 0) {
                document.add(new Paragraph(" ", body));
                document.add(new Paragraph("Average Scores by Test", hFont));
                Image img = Image.getInstance(avgBar);
                img.scaleToFit(520, 280);
                img.setAlignment(Image.ALIGN_CENTER);
                document.add(img);
            }

            byte[] png = buildCommBehScatterPng(report.getCommunicationBehavioralScatter());
            if (png != null && png.length > 0) {
                document.add(new Paragraph(" ", body));
                document.add(new Paragraph("Communication vs Behavioral (All Users)", hFont));
                Image img = Image.getInstance(png);
                img.scaleToFit(520, 320);
                img.setAlignment(Image.ALIGN_CENTER);
                document.add(img);
            }

            byte[] bandBar = buildFinalBandBarPng(report.getFinalScoreBands());
            if (bandBar != null && bandBar.length > 0) {
                document.add(new Paragraph(" ", body));
                document.add(new Paragraph("Final Score Distribution", hFont));
                Image img = Image.getInstance(bandBar);
                img.scaleToFit(520, 280);
                img.setAlignment(Image.ALIGN_CENTER);
                document.add(img);
            }
        } catch (Exception ignored) {
            // Keep PDF generation robust even if chart rendering fails.
        }
        document.close();
    }

    private static byte[] buildAverageScoresBarPng(InterviewUpgradeDtos.FinalReportResponse report) throws java.io.IOException {
        CategoryChart chart = new CategoryChartBuilder()
            .width(900)
            .height(420)
            .title("Average Scores")
            .xAxisTitle("Test")
            .yAxisTitle("Score")
            .build();
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setYAxisMin(0.0);
        chart.getStyler().setYAxisMax(100.0);
        chart.addSeries(
            "Average",
            List.of("Communication", "Technical", "Behavioral", "Final"),
            List.of(
                report.getAverageCommunication(),
                report.getAverageTechnical(),
                report.getAverageBehavioral(),
                report.getAverageFinal()
            )
        );
        return BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG);
    }

    private static byte[] buildCommBehScatterPng(List<InterviewUpgradeDtos.CommunicationBehavioralPoint> points) throws java.io.IOException {
        if (points == null || points.isEmpty()) return new byte[0];
        double[] x = points.stream().mapToDouble(p -> p.getCommunication() == null ? 0.0 : p.getCommunication()).toArray();
        double[] y = points.stream().mapToDouble(p -> p.getBehavioral() == null ? 0.0 : p.getBehavioral()).toArray();
        XYChart chart = new XYChartBuilder()
            .width(900)
            .height(520)
            .title("Communication vs Behavioral")
            .xAxisTitle("Communication score")
            .yAxisTitle("Behavioral score")
            .build();
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setDefaultSeriesRenderStyle(org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Scatter);
        chart.getStyler().setXAxisMin(0.0);
        chart.getStyler().setXAxisMax(100.0);
        chart.getStyler().setYAxisMin(0.0);
        chart.getStyler().setYAxisMax(100.0);
        chart.getStyler().setMarkerSize(7);
        chart.getStyler().setChartTitleVisible(true);
        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setPlotContentSize(0.92);
        chart.getStyler().setPlotMargin(8);
        chart.addSeries("Users", x, y);
        return BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG);
    }

    private static byte[] buildFinalBandBarPng(List<InterviewUpgradeDtos.NameValue> bands) throws java.io.IOException {
        if (bands == null || bands.isEmpty()) return new byte[0];
        List<String> labels = bands.stream().map(InterviewUpgradeDtos.NameValue::getName).toList();
        List<Double> values = bands.stream().map(InterviewUpgradeDtos.NameValue::getValue).toList();
        CategoryChart chart = new CategoryChartBuilder()
            .width(900)
            .height(420)
            .title("Final Score Bands")
            .xAxisTitle("Band")
            .yAxisTitle("Candidates")
            .build();
        chart.getStyler().setLegendVisible(false);
        chart.addSeries("Candidates", labels, values);
        return BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG);
    }

    private static class CodingEvalResult {
        String status;
        int executionTimeMs;
        double score;
        String stdoutText;
    }
}

