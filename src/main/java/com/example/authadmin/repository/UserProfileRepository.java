package com.example.authadmin.repository;

import com.example.authadmin.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
    Optional<UserProfile> findByUserId(Integer userId);
    void deleteByUserId(Integer userId);
}
