package com.example.authadmin.service;

import com.example.authadmin.entity.Admin;
import com.example.authadmin.repository.AdminRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {
    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public boolean authenticate(String adminId, String rawPassword) {
        Optional<Admin> optional = adminRepository.findById(adminId);
        if (optional.isEmpty()) {
            return false;
        }
        Admin admin = optional.get();
        return passwordEncoder.matches(rawPassword, admin.getPassword());
    }
}
