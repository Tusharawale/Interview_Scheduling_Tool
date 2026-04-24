package com.example.authadmin.controller;

import com.example.authadmin.dto.MeetingDtos;
import com.example.authadmin.service.MeetingChatRateLimiter;
import com.example.authadmin.service.MeetingChatService;
import com.example.authadmin.service.MeetingPresenceService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;

/**
 * WebSocket handlers for single global meeting (no room IDs).
 */
@Controller
public class MeetingWsController {

	private final SimpMessagingTemplate messagingTemplate;
	private final MeetingPresenceService presenceService;
	private final MeetingChatService meetingChatService;
	private final MeetingChatRateLimiter meetingChatRateLimiter;

	public MeetingWsController(SimpMessagingTemplate messagingTemplate,
	                            MeetingPresenceService presenceService,
	                            MeetingChatService meetingChatService,
	                            MeetingChatRateLimiter meetingChatRateLimiter) {
		this.messagingTemplate = messagingTemplate;
		this.presenceService = presenceService;
		this.meetingChatService = meetingChatService;
		this.meetingChatRateLimiter = meetingChatRateLimiter;
	}

	@MessageMapping("/meeting/signal")
	public void signal(MeetingDtos.WsEnvelope message) {
		if (message.getType() == null) message.setType("signal");
		if (message.getTs() == null) message.setTs(Instant.now());
		messagingTemplate.convertAndSend("/topic/meeting/signal", message);
	}

	@MessageMapping("/meeting/chat")
	public void chat(MeetingDtos.WsEnvelope message, SimpMessageHeaderAccessor headers) {
		if (message.getType() == null) message.setType("chat");
		if (message.getTs() == null) message.setTs(Instant.now());

		MeetingDtos.ChatPayload payload = extractPayload(message.getPayload());
		if (payload != null && payload.getText() != null && !payload.getText().isBlank()) {
			if (!meetingChatRateLimiter.allowTextMessage(headers.getSessionId())) {
				return;
			}
		}

		meetingChatService.saveFromEnvelope(message);
		messagingTemplate.convertAndSend("/topic/meeting/chat", message);
	}

	private static MeetingDtos.ChatPayload extractPayload(Object raw) {
		if (raw instanceof MeetingDtos.ChatPayload p) return p;
		if (raw instanceof java.util.Map<?, ?> map) {
			MeetingDtos.ChatPayload p = new MeetingDtos.ChatPayload();
			Object t = map.get("text");
			Object fn = map.get("fileName");
			Object fu = map.get("fileUrl");
			if (t != null) p.setText(String.valueOf(t));
			if (fn != null) p.setFileName(String.valueOf(fn));
			if (fu != null) p.setFileUrl(String.valueOf(fu));
			return p;
		}
		return null;
	}

	@MessageMapping("/meeting/typing")
	public void typing(MeetingDtos.TypingPayload payload) {
		if (payload == null) return;
		messagingTemplate.convertAndSend("/topic/meeting/typing", payload);
	}

	@MessageMapping("/meeting/end")
	public void end(MeetingDtos.WsEnvelope message) {
		if (message.getType() == null) message.setType("system");
		if (message.getKind() == null) message.setKind("end");
		if (message.getTs() == null) message.setTs(Instant.now());
		messagingTemplate.convertAndSend("/topic/meeting/signal", message);
	}

	@MessageMapping("/meeting/presence/join")
	public void join(MeetingDtos.PresencePayload payload, SimpMessageHeaderAccessor headers) {
		String sessionId = headers.getSessionId();
		presenceService.upsert(
			sessionId,
			payload == null ? null : payload.getClientId(),
			payload == null ? null : payload.getRole(),
			payload == null ? null : payload.getName(),
			payload == null ? null : payload.getUserId()
		);
		messagingTemplate.convertAndSend("/topic/meeting/presence", presenceService.list());
	}

	@MessageMapping("/meeting/control")
	public void control(MeetingDtos.MeetingControlCommand command, SimpMessageHeaderAccessor headers) {
		if (command == null) return;
		String sessionId = headers == null ? null : headers.getSessionId();
		MeetingPresenceService.Participant sender = presenceService.getBySessionId(sessionId);
		String senderRole = sender == null ? "" : String.valueOf(sender.role());
		if (!"admin".equalsIgnoreCase(senderRole)) {
			return;
		}
		if (command.getTs() == null) command.setTs(Instant.now());
		if (command.getAdminClientId() == null && sender != null) command.setAdminClientId(sender.clientId());
		if (command.getAdminUserId() == null && sender != null) command.setAdminUserId(sender.userId());
		if (command.getTargetClientId() == null || command.getTargetClientId().isBlank()) return;
		messagingTemplate.convertAndSend("/topic/meeting/control", command);
	}

	@MessageMapping("/meeting/control/ack")
	public void controlAck(MeetingDtos.MeetingControlAck ack) {
		if (ack == null) return;
		if (ack.getTs() == null) ack.setTs(Instant.now());
		messagingTemplate.convertAndSend("/topic/meeting/control/ack", ack);
	}
}
