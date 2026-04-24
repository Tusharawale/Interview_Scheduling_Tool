package com.example.authadmin.repository;

import com.example.authadmin.entity.CodingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CodingSubmissionRepository extends JpaRepository<CodingSubmission, Long> {
    List<CodingSubmission> findByUser_IdOrderBySubmittedAtDesc(Integer userId);
    Optional<CodingSubmission> findTopByBooking_IdAndUser_IdOrderBySubmittedAtDesc(Integer bookingId, Integer userId);
}

