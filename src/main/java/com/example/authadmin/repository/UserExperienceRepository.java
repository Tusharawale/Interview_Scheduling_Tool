package com.example.authadmin.repository;

import com.example.authadmin.entity.UserExperience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserExperienceRepository extends JpaRepository<UserExperience, Integer> {
    List<UserExperience> findByUserId(Integer userId);
    void deleteByUserId(Integer userId);
}
