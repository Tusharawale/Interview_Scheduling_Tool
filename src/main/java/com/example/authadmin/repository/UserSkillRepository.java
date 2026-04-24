package com.example.authadmin.repository;

import com.example.authadmin.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSkillRepository extends JpaRepository<UserSkill, Integer> {
    List<UserSkill> findByUserId(Integer userId);
    void deleteByUserId(Integer userId);
}
