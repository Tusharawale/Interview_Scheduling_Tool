package com.example.authadmin.repository;

import com.example.authadmin.entity.UserProgrammingLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserProgrammingLanguageRepository extends JpaRepository<UserProgrammingLanguage, Integer> {
    List<UserProgrammingLanguage> findByUserId(Integer userId);
    void deleteByUserId(Integer userId);
}
