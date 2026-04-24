package com.example.authadmin.controller;

import com.example.authadmin.dto.InterviewDtos;
import com.example.authadmin.entity.InterviewBooking;
import com.example.authadmin.entity.InterviewSlot;
import com.example.authadmin.service.InterviewService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

	private final InterviewService interviewService;

	public InterviewController(InterviewService interviewService) {
		this.interviewService = interviewService;
	}

	@GetMapping("/slots")
	public List<InterviewDtos.SlotResponse> listSlots() {
		return interviewService.listSlots().stream()
			.map(this::toSlotResponse)
			.collect(Collectors.toList());
	}

	@GetMapping("/bookings/user/{userId}")
	public List<InterviewDtos.BookingResponse> bookingsForUser(@PathVariable("userId") Integer userId) {
		return interviewService.listBookingsForUser(userId).stream()
			.map(this::toBookingResponse)
			.collect(Collectors.toList());
	}

	@PostMapping("/slots/{slotId}/book")
	public InterviewDtos.BookingResponse book(@PathVariable("slotId") Integer slotId,
	                                          @RequestBody InterviewDtos.BookRequest request) {
		InterviewBooking booking = interviewService.bookSlot(request.getUserId(), slotId);
		return toBookingResponse(booking);
	}

	@PostMapping("/bookings/{bookingId}/cancel")
	public void cancel(@PathVariable("bookingId") Integer bookingId,
	                   @RequestBody InterviewDtos.BookRequest request) {
		interviewService.cancelBooking(bookingId, request.getUserId());
	}

	private InterviewDtos.SlotResponse toSlotResponse(InterviewSlot slot) {
		InterviewDtos.SlotResponse resp = new InterviewDtos.SlotResponse();
		resp.setId(slot.getId());
		resp.setTitle(slot.getTitle());
		resp.setDescription(slot.getDescription());
		resp.setScheduledAt(slot.getScheduledAt().toString());
		resp.setDurationMinutes(slot.getDurationMinutes());
		resp.setCapacity(slot.getCapacity());
		// bookedCount is filled on admin side where needed
		return resp;
	}

	private InterviewDtos.BookingResponse toBookingResponse(InterviewBooking booking) {
		InterviewDtos.BookingResponse resp = new InterviewDtos.BookingResponse();
		resp.setId(booking.getId());
		resp.setSlotId(booking.getSlot().getId());
		resp.setSlotTitle(booking.getSlot().getTitle());
		resp.setScheduledAt(booking.getSlot().getScheduledAt().toString());
		resp.setUserId(booking.getUser().getId());
		resp.setUsername(booking.getUser().getUsername());
		resp.setEmail(booking.getUser().getEmail());
		resp.setStatus(booking.getStatus());
		return resp;
	}
}

