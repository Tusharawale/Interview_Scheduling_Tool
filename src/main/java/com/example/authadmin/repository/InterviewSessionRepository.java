package com.example.authadmin.repository;

import com.example.authadmin.entity.InterviewBooking;
import com.example.authadmin.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    Optional<InterviewSession> findByBooking(InterviewBooking booking);
}

