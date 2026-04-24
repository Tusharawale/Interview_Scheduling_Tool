package com.example.authadmin.controller;

import com.example.authadmin.dto.AdminDtos;
import com.example.authadmin.dto.AnalyticsDtos;
import com.example.authadmin.dto.UserDtos;
import com.example.authadmin.entity.AdminEmailLog;
import com.example.authadmin.entity.User;
import com.example.authadmin.repository.AdminEmailLogRepository;
import com.example.authadmin.service.AdminService;
import com.example.authadmin.service.EmailService;
import com.example.authadmin.service.FileStorageService;
import com.example.authadmin.service.MeetingAdminTokenService;
import com.example.authadmin.service.ProfileAnalyticsService;
import com.example.authadmin.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
	private final AdminService adminService;
	private final UserService userService;
	private final MeetingAdminTokenService meetingAdminTokenService;
	private final ProfileAnalyticsService profileAnalyticsService;
    private final EmailService emailService;
    private final FileStorageService fileStorageService;
    private final AdminEmailLogRepository adminEmailLogRepository;

	public AdminController(AdminService adminService, UserService userService,
	                      MeetingAdminTokenService meetingAdminTokenService,
	                      ProfileAnalyticsService profileAnalyticsService,
                          EmailService emailService,
                          FileStorageService fileStorageService,
                          AdminEmailLogRepository adminEmailLogRepository) {
		this.adminService = adminService;
		this.userService = userService;
		this.meetingAdminTokenService = meetingAdminTokenService;
		this.profileAnalyticsService = profileAnalyticsService;
        this.emailService = emailService;
        this.fileStorageService = fileStorageService;
        this.adminEmailLogRepository = adminEmailLogRepository;
	}

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody AdminDtos.LoginRequest request) {
		boolean ok = adminService.authenticate(request.getAdminId(), request.getPassword());
		if (!ok) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid admin credentials");
		}
		AdminDtos.LoginResponse body = new AdminDtos.LoginResponse();
		body.setStatus("OK");
		body.setMeetingAdminToken(meetingAdminTokenService.issueToken());
		return ResponseEntity.ok((Object) body);
	}

    @GetMapping("/talent/analytics")
    public ResponseEntity<AnalyticsDtos.TalentPoolResponse> getTalentAnalytics() {
        return ResponseEntity.ok(profileAnalyticsService.buildTalentPool());
    }

    @GetMapping("/talent/locations")
    public ResponseEntity<AnalyticsDtos.LocationAnalyticsResponse> getTalentLocations() {
        return ResponseEntity.ok(profileAnalyticsService.buildLocationAnalytics());
    }

    @GetMapping("/users")
    public ResponseEntity<Object> getAllUsers() {
        List<UserDtos.AdminUserResponse> users = userService.findAll().stream()
            .map(AdminController::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok((Object) users);
    }

    @PostMapping("/users/{id}/activate")
    public ResponseEntity<Object> activate(@PathVariable("id") Integer id) {
        return ResponseEntity.ok((Object) toResponse(userService.setActive(id, true)));
    }

    @PostMapping("/users/{id}/deactivate")
    public ResponseEntity<Object> deactivate(@PathVariable("id") Integer id) {
        return ResponseEntity.ok((Object) toResponse(userService.setActive(id, false)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Object> delete(@PathVariable("id") Integer id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/email/send", consumes = {"multipart/form-data"})
    public ResponseEntity<Object> sendEmail(@RequestParam("toEmail") String toEmail,
                                            @RequestParam("subject") String subject,
                                            @RequestParam("messageBody") String messageBody,
                                            @RequestParam(value = "file", required = false) MultipartFile file) {
        String attachmentPath = null;
        File attachment = null;
        if (file != null && !file.isEmpty()) {
            attachmentPath = fileStorageService.store(file, "email");
            if (attachmentPath != null) {
                attachment = fileStorageService.getUploadRoot().resolve(attachmentPath).toFile();
            }
        }

        emailService.sendAdminEmailWithAttachment(toEmail, subject, messageBody, attachment);

        AdminEmailLog log = new AdminEmailLog();
        log.setRecipientEmail(toEmail);
        log.setSubject(subject);
        log.setMessageBody(messageBody);
        log.setAttachmentPath(attachmentPath);
        log.setCreatedAt(LocalDateTime.now());
        log = adminEmailLogRepository.save(log);

        Map<String, Object> body = new HashMap<>();
        body.put("id", log.getId());
        body.put("recipientEmail", log.getRecipientEmail());
        body.put("subject", log.getSubject());
        body.put("attachmentPath", log.getAttachmentPath());
        return ResponseEntity.ok(body);
    }

    private static UserDtos.AdminUserResponse toResponse(User u) {
        UserDtos.AdminUserResponse r = new UserDtos.AdminUserResponse();
        r.setId(u.getId());
        r.setUsername(u.getUsername());
        r.setEmail(u.getEmail());
        r.setVerified(u.isVerified());
        r.setActive(u.isActive());
        return r;
    }
}
