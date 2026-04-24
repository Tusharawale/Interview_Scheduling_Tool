package com.example.authadmin.controller;

import com.example.authadmin.dto.AnalyticsDtos;
import com.example.authadmin.service.ProfileAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/profile")
public class ProfileAnalyticsController {

    private final ProfileAnalyticsService profileAnalyticsService;

    public ProfileAnalyticsController(ProfileAnalyticsService profileAnalyticsService) {
        this.profileAnalyticsService = profileAnalyticsService;
    }

    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsDtos.UserAnalyticsResponse> getAnalytics(@PathVariable("userId") Integer userId) {
        return ResponseEntity.ok(profileAnalyticsService.buildUserAnalytics(userId));
    }
}
