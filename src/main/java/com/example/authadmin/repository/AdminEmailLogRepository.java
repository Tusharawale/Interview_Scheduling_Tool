package com.example.authadmin.repository;

import com.example.authadmin.entity.AdminEmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminEmailLogRepository extends JpaRepository<AdminEmailLog, Integer> {
}
