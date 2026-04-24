package com.example.authadmin.controller;

import com.example.authadmin.dto.MeetingDtos;
import com.example.authadmin.entity.InterviewBooking;
import com.example.authadmin.entity.InterviewSlot;
import com.example.authadmin.entity.User;
import com.example.authadmin.repository.InterviewBookingRepository;
import com.example.authadmin.repository.InterviewSlotRepository;
import com.example.authadmin.repository.UserRepository;
import com.example.authadmin.service.EmailService;
import com.example.authadmin.service.MeetingAdminTokenService;
import com.example.authadmin.service.MeetingChatService;
import com.example.authadmin.service.MeetingRtcConfigService;
import com.example.authadmin.service.MeetingService;
import com.example.authadmin.service.MeetingStateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/meeting")
public class MeetingController {

	private static final String ADMIN_TOKEN_HEADER = "X-Meeting-Admin-Token";

	private final MeetingStateService meetingStateService;
	private final MeetingService meetingService;
	private final EmailService emailService;
	private final UserRepository userRepository;
	private final MeetingAdminTokenService meetingAdminTokenService;
	private final MeetingRtcConfigService meetingRtcConfigService;
	private final MeetingChatService meetingChatService;
	private final InterviewBookingRepository interviewBookingRepository;
	private final InterviewSlotRepository interviewSlotRepository;

	public MeetingController(MeetingStateService meetingStateService,
	                         MeetingService meetingService,
	                         EmailService emailService,
	                         UserRepository userRepository,
	                         InterviewBookingRepository interviewBookingRepository,
	                         InterviewSlotRepository interviewSlotRepository,
	                         MeetingAdminTokenService meetingAdminTokenService,
	                         MeetingRtcConfigService meetingRtcConfigService,
	                         MeetingChatService meetingChatService) {
		this.meetingStateService = meetingStateService;
		this.meetingService = meetingService;
		this.emailService = emailService;
		this.userRepository = userRepository;
		this.interviewBookingRepository = interviewBookingRepository;
		this.interviewSlotRepository = interviewSlotRepository;
		this.meetingAdminTokenService = meetingAdminTokenService;
		this.meetingRtcConfigService = meetingRtcConfigService;
		this.meetingChatService = meetingChatService;
	}

	/**
	 * ICE servers for WebRTC (STUN + optional TURN). Public so clients can connect through restrictive NATs.
	 */
	@GetMapping("/rtc-config")
	public MeetingDtos.RtcConfigResponse rtcConfig() {
		return meetingRtcConfigService.build();
	}

	@GetMapping("/chat/history")
	public List<MeetingDtos.ChatHistoryItem> chatHistory(@RequestParam(name = "limit", defaultValue = "80") int limit) {
		return meetingChatService.recent(limit);
	}

	@PostMapping("/start")
	public ResponseEntity<Map<String, String>> start(@RequestHeader(ADMIN_TOKEN_HEADER) String adminToken,
	                                                 @RequestParam(name = "mode", required = false) String mode,
	                                                 @RequestParam(name = "slotId", required = false) Integer slotId,
	                                                 @RequestParam(name = "bookingId", required = false) Integer bookingId) {
		if (!meetingAdminTokenService.validate(adminToken)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "invalid_admin_token"));
		}
		String safeMode = (mode == null || mode.isBlank()) ? "NORMAL" : mode.trim().toUpperCase();
		if ("SCHEDULED".equals(safeMode)) {
			InterviewSlot slot = null;
			if (slotId != null) {
				slot = interviewSlotRepository.findById(slotId).orElse(null);
			} else if (bookingId != null) {
				InterviewBooking byBooking = interviewBookingRepository.findById(bookingId).orElse(null);
				slot = byBooking == null ? null : byBooking.getSlot();
			}
			if (slot == null) {
				return ResponseEntity.badRequest().body(Map.of("error", "slot_id_required_for_scheduled_mode"));
			}
			List<InterviewBooking> booked = interviewBookingRepository.findBySlotAndStatus(slot, "BOOKED");
			if (booked.isEmpty()) {
				return ResponseEntity.badRequest().body(Map.of("error", "no_booked_users_for_slot"));
			}
			java.util.Set<Integer> allowedUserIds = booked.stream()
				.map(b -> b.getUser() == null ? null : b.getUser().getId())
				.filter(java.util.Objects::nonNull)
				.collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
			java.util.Set<Integer> bookingIds = booked.stream()
				.map(InterviewBooking::getId)
				.filter(java.util.Objects::nonNull)
				.collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
			boolean wasActive = meetingStateService.isActive();
			meetingStateService.activateForScheduledSlot(slot.getId(), allowedUserIds, bookingIds);
			if (!wasActive) {
				for (InterviewBooking b : booked) {
					User u = b.getUser();
					if (u != null && u.isVerified() && u.isActive()) {
						emailService.sendMeetingStartedEmail(u.getEmail(), u.getUsername());
					}
				}
			}
			return ResponseEntity.ok(Map.of(
				"status", "ACTIVE",
				"mode", "SCHEDULED",
				"slotId", String.valueOf(slot.getId()),
				"eligibleUsers", String.valueOf(allowedUserIds.size()),
				"emailed", Boolean.toString(!wasActive)
			));
		}
		// NORMAL mode: global ad-hoc meeting, no slot/booking gating.
		if (bookingId == null) {
			boolean wasActive = meetingStateService.isActive();
			meetingStateService.activateNormal();
			if (!wasActive) {
				List<User> users = userRepository.findAll();
				for (User u : users) {
					if (u.isVerified() && u.isActive()) {
						emailService.sendMeetingStartedEmail(u.getEmail(), u.getUsername());
					}
				}
			}
			return ResponseEntity.ok(Map.of("status", "ACTIVE", "mode", "NORMAL", "emailed", Boolean.toString(!wasActive)));
		}
		// Backward compatibility path: bookingId-only start behaves as scheduled by booking's slot.
		InterviewBooking booking = interviewBookingRepository.findById(bookingId).orElse(null);
		if (booking == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "booking_not_found"));
		}
		if (!"BOOKED".equalsIgnoreCase(booking.getStatus())) {
			return ResponseEntity.badRequest().body(Map.of("error", "booking_not_booked"));
		}
		User target = booking.getUser();
		if (target == null) {
			return ResponseEntity.badRequest().body(Map.of("error", "booking_has_no_user"));
		}
		InterviewSlot slot = booking.getSlot();
		List<InterviewBooking> booked = interviewBookingRepository.findBySlotAndStatus(slot, "BOOKED");
		java.util.Set<Integer> allowedUserIds = booked.stream()
			.map(b -> b.getUser() == null ? null : b.getUser().getId())
			.filter(java.util.Objects::nonNull)
			.collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
		java.util.Set<Integer> bookingIds = booked.stream()
			.map(InterviewBooking::getId)
			.filter(java.util.Objects::nonNull)
			.collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
		boolean wasActive = meetingStateService.isActive();
		meetingStateService.activateForScheduledSlot(slot == null ? null : slot.getId(), allowedUserIds, bookingIds);
		if (!wasActive) {
			for (InterviewBooking b : booked) {
				User u = b.getUser();
				if (u != null && u.isVerified() && u.isActive()) {
					emailService.sendMeetingStartedEmail(u.getEmail(), u.getUsername());
				}
			}
		}
		return ResponseEntity.ok(Map.of(
			"status", "ACTIVE",
			"mode", "SCHEDULED",
			"slotId", slot == null ? "" : String.valueOf(slot.getId()),
			"emailed", Boolean.toString(!wasActive),
			"bookingId", String.valueOf(bookingId)
		));
	}

	@GetMapping("/status")
	public MeetingDtos.StatusResponse status() {
		MeetingDtos.StatusResponse resp = new MeetingDtos.StatusResponse();
		resp.setActive(meetingStateService.isActive());
		resp.setMode(meetingStateService.getMode() == null ? null : meetingStateService.getMode().name());
		resp.setSlotId(meetingStateService.getActiveSlotId());
		resp.setBookingId(meetingStateService.getActiveBookingId());
		resp.setTargetUserId(meetingStateService.getTargetUserId());
		resp.setAllowedUserIds(new java.util.ArrayList<>(meetingStateService.getAllowedUserIds()));
		resp.setBookingIds(new java.util.ArrayList<>(meetingStateService.getActiveBookingIds()));
		return resp;
	}

	@PostMapping("/end")
	public ResponseEntity<Map<String, String>> end(@RequestHeader(ADMIN_TOKEN_HEADER) String adminToken) {
		if (!meetingAdminTokenService.validate(adminToken)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "invalid_admin_token"));
		}
		meetingStateService.setActive(false);
		meetingChatService.clearAllMessages();
		return ResponseEntity.ok(Map.of("status", "INACTIVE"));
	}

	@PostMapping("/upload")
	public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws IOException {
		var stored = meetingService.storeFile("global", file);
		return Map.of(
			"fileId", stored.id(),
			"fileName", stored.fileName(),
			"fileUrl", "/meeting/files/" + stored.id()
		);
	}

	@GetMapping("/files/{fileId}")
	public ResponseEntity<Resource> download(@PathVariable("fileId") String fileId) {
		var stored = meetingService.getFile(fileId);
		Resource resource = meetingService.asResource(stored);
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + stored.fileName().replace("\"", "") + "\"")
			.contentType(stored.contentType())
			.body(resource);
	}
}
