package com.example.authadmin.service;

import com.example.authadmin.dto.InterviewDtos;
import com.example.authadmin.entity.InterviewBooking;
import com.example.authadmin.entity.InterviewSlot;
import com.example.authadmin.entity.User;
import com.example.authadmin.repository.InterviewBookingRepository;
import com.example.authadmin.repository.InterviewSlotRepository;
import com.example.authadmin.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InterviewService {

	private final InterviewSlotRepository slotRepository;
	private final InterviewBookingRepository bookingRepository;
	private final UserRepository userRepository;
	private final EmailService emailService;

	public InterviewService(InterviewSlotRepository slotRepository,
	                        InterviewBookingRepository bookingRepository,
	                        UserRepository userRepository,
	                        EmailService emailService) {
		this.slotRepository = slotRepository;
		this.bookingRepository = bookingRepository;
		this.userRepository = userRepository;
		this.emailService = emailService;
	}

	public List<InterviewSlot> listSlots() {
		return slotRepository.findAllByOrderByScheduledAtAsc();
	}

	public InterviewSlot createSlot(InterviewDtos.CreateSlotRequest request) {
		InterviewSlot slot = new InterviewSlot();
		slot.setTitle(request.getTitle());
		slot.setDescription(request.getDescription());
		slot.setScheduledAt(LocalDateTime.parse(request.getScheduledAt()));
		slot.setDurationMinutes(request.getDurationMinutes());
		slot.setCapacity(request.getCapacity());
		return slotRepository.save(slot);
	}

	public void deleteSlot(Integer id) {
		InterviewSlot slot = slotRepository.findById(id)
			.orElseThrow(() -> new IllegalStateException("Slot not found"));
		bookingRepository.findBySlot(slot).forEach(bookingRepository::delete);
		slotRepository.delete(slot);
	}

	public List<InterviewBooking> listAllBookings() {
		return bookingRepository.findAll();
	}

	public List<InterviewBooking> listBookingsForUser(Integer userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalStateException("User not found"));
		return bookingRepository.findByUser(user);
	}

	public List<InterviewSlot> listSlotsForDate(LocalDate date) {
		LocalDateTime from = date.atStartOfDay();
		LocalDateTime to = date.plusDays(1).atStartOfDay();
		return slotRepository.findByScheduledAtBetweenOrderByScheduledAtAsc(from, to);
	}

	public List<InterviewSlot> listSlotsForMonth(int year, int month) {
		LocalDate fromDate = LocalDate.of(year, month, 1);
		LocalDate toDate = fromDate.plusMonths(1);
		return slotRepository.findByScheduledAtBetweenOrderByScheduledAtAsc(fromDate.atStartOfDay(), toDate.atStartOfDay());
	}

	public List<InterviewBooking> listBookingsForSlots(List<InterviewSlot> slots) {
		if (slots == null || slots.isEmpty()) return List.of();
		return bookingRepository.findBySlotIn(slots);
	}

	public InterviewBooking bookSlot(Integer userId, Integer slotId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalStateException("User not found"));
		if (!user.isVerified()) {
			throw new IllegalStateException("Please verify your email before booking an interview");
		}
		if (!user.isActive()) {
			throw new IllegalStateException("User account is inactive");
		}

		InterviewSlot slot = slotRepository.findById(slotId)
			.orElseThrow(() -> new IllegalStateException("Slot not found"));

		if (bookingRepository.existsBySlotAndUserAndStatus(slot, user, "BOOKED")) {
			throw new IllegalStateException("You have already booked this slot");
		}

		long bookedCount = bookingRepository.countBySlotAndStatus(slot, "BOOKED");
		if (bookedCount >= slot.getCapacity()) {
			throw new IllegalStateException("This slot is already full");
		}

		InterviewBooking booking = new InterviewBooking();
		booking.setSlot(slot);
		booking.setUser(user);
		booking.setStatus("BOOKED");
		InterviewBooking saved = bookingRepository.save(booking);

		emailService.sendInterviewBookedEmail(user.getEmail(), user.getUsername(), slot);
		return saved;
	}

	public void cancelBooking(Integer bookingId, Integer userId) {
		InterviewBooking booking = bookingRepository.findById(bookingId)
			.orElseThrow(() -> new IllegalStateException("Booking not found"));
		if (!booking.getUser().getId().equals(userId)) {
			throw new IllegalStateException("You can only cancel your own bookings");
		}
		if (!"BOOKED".equals(booking.getStatus())) {
			return;
		}
		booking.setStatus("CANCELLED");
		bookingRepository.save(booking);

		emailService.sendInterviewCancelledEmail(
			booking.getUser().getEmail(),
			booking.getUser().getUsername(),
			booking.getSlot()
		);
	}
}

