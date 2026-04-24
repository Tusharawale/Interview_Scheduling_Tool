package com.example.authadmin.repository;

import com.example.authadmin.entity.FinalReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinalReportRepository extends JpaRepository<FinalReport, Long> {
    Optional<FinalReport> findTopByOrderByGeneratedAtDesc();
}

