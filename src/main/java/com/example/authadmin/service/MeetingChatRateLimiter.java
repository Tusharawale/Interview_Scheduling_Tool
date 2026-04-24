package com.example.authadmin.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple per-WebSocket-session rate limit for chat text (spam protection).
 */
@Component
public class MeetingChatRateLimiter {

    private static final long MIN_INTERVAL_MS = 600;

    private final Map<String, Long> lastTextAt = new ConcurrentHashMap<>();

    public boolean allowTextMessage(String wsSessionId) {
        if (wsSessionId == null) return true;
        long now = System.currentTimeMillis();
        Long prev = lastTextAt.put(wsSessionId, now);
        if (prev == null) return true;
        return now - prev >= MIN_INTERVAL_MS;
    }
}
