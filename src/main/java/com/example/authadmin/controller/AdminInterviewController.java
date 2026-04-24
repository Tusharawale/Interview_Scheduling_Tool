package com.example.authadmin.controller;

import com.example.authadmin.dto.InterviewDtos;
import com.example.authadmin.entity.InterviewBooking;
import com.example.authadmin.entity.InterviewSlot;
import com.example.authadmin.service.InterviewService;
import com.example.authadmin.repository.InterviewBookingRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/interviews")
public class AdminInterviewController {

	private final InterviewService interviewService;
	private final InterviewBookingRepository bookingRepository;

	public AdminInterviewController(InterviewService interviewService,
	                                InterviewBookingRepository bookingRepository) {
		this.interviewService = interviewService;
		this.bookingRepository = bookingRepository;
	}

	@GetMapping("/slots")
	public List<InterviewDtos.SlotResponse> listSlots() {
		return interviewService.listSlots().stream()
			.map(slot -> {
				InterviewDtos.SlotResponse resp = new InterviewDtos.SlotResponse();
				resp.setId(slot.getId());
				resp.setTitle(slot.getTitle());
				resp.setDescription(slot.getDescription());
				resp.setScheduledAt(slot.getScheduledAt().toString());
				resp.setDurationMinutes(slot.getDurationMinutes());
				resp.setCapacity(slot.getCapacity());
				long booked = bookingRepository.countBySlotAndStatus(slot, "BOOKED");
				resp.setBookedCount(booked);
				return resp;
			})
			.collect(Collectors.toList());
	}

	@PostMapping("/slots")
	public InterviewDtos.SlotResponse createSlot(@RequestBody InterviewDtos.CreateSlotRequest request) {
		InterviewSlot slot = interviewService.createSlot(request);
		InterviewDtos.SlotResponse resp = new InterviewDtos.SlotResponse();
		resp.setId(slot.getId());
		resp.setTitle(slot.getTitle());
		resp.setDescription(slot.getDescription());
		resp.setScheduledAt(slot.getScheduledAt().toString());
		resp.setDurationMinutes(slot.getDurationMinutes());
		resp.setCapacity(slot.getCapacity());
		resp.setBookedCount(0);
		return resp;
	}

	@DeleteMapping("/slots/{id}")
	public void deleteSlot(@PathVariable("id") Integer id) {
		interviewService.deleteSlot(id);
	}

	@GetMapping("/bookings")
	public List<InterviewDtos.BookingResponse> listBookings() {
		List<InterviewBooking> bookings = interviewService.listAllBookings();
		return bookings.stream().map(booking -> {
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
		}).collect(Collectors.toList());
	}

	@GetMapping("/calendar/summary")
	public List<InterviewDtos.CalendarDaySummary> getCalendarSummary(@RequestParam("year") int year,
	                                                                 @RequestParam("month") int month) {
		YearMonth ym = YearMonth.of(year, month);
		List<InterviewSlot> slots = interviewService.listSlotsForMonth(year, month);
		List<InterviewBooking> bookings = interviewService.listBookingsForSlots(slots);

		Map<LocalDate, InterviewDtos.CalendarDaySummary> map = new HashMap<>();
		for (LocalDate d = ym.atDay(1); !d.isAfter(ym.atEndOfMonth()); d = d.plusDays(1)) {
			InterviewDtos.CalendarDaySummary row = new InterviewDtos.CalendarDaySummary();
			row.setDate(d.toString());
			row.setSlotCount(0);
			row.setBookedCount(0);
			map.put(d, row);
		}

		for (InterviewSlot s : slots) {
			LocalDate d = s.getScheduledAt().toLocalDate();
			InterviewDtos.CalendarDaySummary row = map.get(d);
			if (row != null) row.setSlotCount(row.getSlotCount() + 1);
		}
		for (InterviewBooking b : bookings) {
			if (!"BOOKED".equalsIgnoreCase(b.getStatus())) continue;
			LocalDate d = b.getSlot().getScheduledAt().toLocalDate();
			InterviewDtos.CalendarDaySummary row = map.get(d);
			if (row != null) row.setBookedCount(row.getBookedCount() + 1);
		}

		return map.values().stream()
			.sorted((a, b) -> a.getDate().compareTo(b.getDate()))
			.collect(Collectors.toList());
	}

	@GetMapping("/calendar/date/{date}")
	public List<InterviewDtos.BookingResponse> getBookingsForDate(@PathVariable("date") String date) {
		LocalDate d = LocalDate.parse(date);
		List<InterviewSlot> slots = interviewService.listSlotsForDate(d);
		List<InterviewBooking> bookings = interviewService.listBookingsForSlots(slots);
		return bookings.stream().map(booking -> {
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
		}).collect(Collectors.toList());
	}
}

