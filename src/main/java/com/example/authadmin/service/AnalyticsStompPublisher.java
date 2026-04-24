package com.example.authadmin.service;

import com.example.authadmin.dto.AnalyticsDtos;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Pushes analytics updates over STOMP after profile mutations (Chart.js clients can refresh).
 */
@Component
public class AnalyticsStompPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final ProfileAnalyticsService profileAnalyticsService;

    public AnalyticsStompPublisher(SimpMessagingTemplate messagingTemplate,
                                   ProfileAnalyticsService profileAnalyticsService) {
        this.messagingTemplate = messagingTemplate;
        this.profileAnalyticsService = profileAnalyticsService;
    }

    public void publishAfterProfileChange(Integer userId) {
        if (userId == null) return;
        try {
            AnalyticsDtos.UserAnalyticsResponse body = profileAnalyticsService.buildUserAnalytics(userId);
            messagingTemplate.convertAndSend("/topic/analytics/user/" + userId, body);
        } catch (Exception ignored) {
            // avoid breaking profile save if WS payload fails
        }
        AnalyticsDtos.TalentRefreshNotice notice = new AnalyticsDtos.TalentRefreshNotice();
        notice.setChangedUserId(userId);
        messagingTemplate.convertAndSend("/topic/analytics/talent-refresh", notice);
    }
}
