package com.example.authadmin.repository;

import com.example.authadmin.entity.InterviewSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface InterviewSlotRepository extends JpaRepository<InterviewSlot, Integer> {
	List<InterviewSlot> findAllByOrderByScheduledAtAsc();
	List<InterviewSlot> findByScheduledAtBetweenOrderByScheduledAtAsc(LocalDateTime start, LocalDateTime end);
}

