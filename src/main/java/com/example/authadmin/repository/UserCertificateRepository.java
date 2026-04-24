package com.example.authadmin.repository;

import com.example.authadmin.entity.UserCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCertificateRepository extends JpaRepository<UserCertificate, Integer> {
    List<UserCertificate> findByUserId(Integer userId);
    void deleteByUserId(Integer userId);
}
