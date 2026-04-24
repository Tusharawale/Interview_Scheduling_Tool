package com.example.authadmin.config;

import com.example.authadmin.service.MeetingPresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEvents {

	private final MeetingPresenceService presenceService;
	private final SimpMessagingTemplate messagingTemplate;

	public WebSocketEvents(MeetingPresenceService presenceService, SimpMessagingTemplate messagingTemplate) {
		this.presenceService = presenceService;
		this.messagingTemplate = messagingTemplate;
	}

	@EventListener
	public void onDisconnect(SessionDisconnectEvent event) {
		presenceService.remove(event.getSessionId());
		messagingTemplate.convertAndSend("/topic/meeting/presence", presenceService.list());
	}
}

