package com.example.authadmin.controller;

import com.example.authadmin.service.MeetingSttService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/meeting")
public class MeetingSttController {

    private final MeetingSttService meetingSttService;

    public MeetingSttController(MeetingSttService meetingSttService) {
        this.meetingSttService = meetingSttService;
    }

    @PostMapping(value = "/{bookingId}/stt/chunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadChunk(@PathVariable("bookingId") Integer bookingId,
                                                           @RequestParam("audio") MultipartFile audioFile,
                                                           @RequestParam(value = "clientId", required = false) String clientId,
                                                           @RequestParam(value = "chunkSeq", required = false) String chunkSeq) throws Exception {
        byte[] bytes = audioFile.getBytes();
        String mimeType = audioFile.getContentType();
        Map<String, Object> result = meetingSttService.acceptChunk(bookingId, bytes, mimeType, clientId, chunkSeq);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{bookingId}/stt/finalize")
    public ResponseEntity<Map<String, Object>> finalizeTranscript(@PathVariable("bookingId") Integer bookingId) {
        Map<String, Object> body = meetingSttService.finalizeTranscript(bookingId);
        return ResponseEntity.ok(body);
    }
}

