package com.example.authadmin.repository;

import com.example.authadmin.entity.InterviewBooking;
import com.example.authadmin.entity.InterviewSlot;
import com.example.authadmin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewBookingRepository extends JpaRepository<InterviewBooking, Integer> {
	long countBySlotAndStatus(InterviewSlot slot, String status);

	boolean existsBySlotAndUserAndStatus(InterviewSlot slot, User user, String status);

	List<InterviewBooking> findByUser(User user);

	List<InterviewBooking> findBySlot(InterviewSlot slot);
	List<InterviewBooking> findBySlotAndStatus(InterviewSlot slot, String status);

	List<InterviewBooking> findBySlotIn(List<InterviewSlot> slots);
}

