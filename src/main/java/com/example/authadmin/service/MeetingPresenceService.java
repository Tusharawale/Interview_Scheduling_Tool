package com.example.authadmin.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MeetingPresenceService {

	public record Participant(String sessionId, String clientId, String role, String name, Integer userId) {}

	private final Map<String, Participant> bySessionId = new ConcurrentHashMap<>();

	public void upsert(String sessionId, String clientId, String role, String name, Integer userId) {
		if (sessionId == null) return;
		if (clientId == null || clientId.isBlank()) return;
		String safeRole = role == null ? "user" : role;
		String safeName = (name == null || name.isBlank()) ? safeRole : name;
		bySessionId.put(sessionId, new Participant(sessionId, clientId, safeRole, safeName, userId));
	}

	public void remove(String sessionId) {
		if (sessionId == null) return;
		bySessionId.remove(sessionId);
	}

	public Participant getBySessionId(String sessionId) {
		if (sessionId == null) return null;
		return bySessionId.get(sessionId);
	}

	public List<Participant> list() {
		return new ArrayList<>(bySessionId.values());
	}
}

