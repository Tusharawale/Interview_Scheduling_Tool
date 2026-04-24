package com.example.authadmin.repository;

import com.example.authadmin.entity.UserEducation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserEducationRepository extends JpaRepository<UserEducation, Integer> {
    List<UserEducation> findByUserId(Integer userId);
    void deleteByUserId(Integer userId);
}
