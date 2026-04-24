package com.example.authadmin.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Opaque tokens returned after admin login; required for POST /meeting/start and /meeting/end.
 */
@Service
public class MeetingAdminTokenService {

    private final Map<String, Instant> tokens = new ConcurrentHashMap<>();

    public String issueToken() {
        purgeExpired();
        String t = UUID.randomUUID().toString();
        tokens.put(t, Instant.now().plus(12, ChronoUnit.HOURS));
        return t;
    }

    public boolean validate(String token) {
        if (token == null || token.isBlank()) return false;
        purgeExpired();
        Instant exp = tokens.get(token.trim());
        if (exp == null) return false;
        if (Instant.now().isAfter(exp)) {
            tokens.remove(token.trim());
            return false;
        }
        return true;
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        for (Iterator<Map.Entry<String, Instant>> it = tokens.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Instant> e = it.next();
            if (now.isAfter(e.getValue())) it.remove();
        }
    }
}
