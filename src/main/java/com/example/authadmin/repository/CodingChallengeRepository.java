package com.example.authadmin.repository;

import com.example.authadmin.entity.CodingChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodingChallengeRepository extends JpaRepository<CodingChallenge, Long> {
    List<CodingChallenge> findByActiveTrueOrderByIdDesc();
}

