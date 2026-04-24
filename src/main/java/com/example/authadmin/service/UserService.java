package com.example.authadmin.service;

import com.example.authadmin.dto.UserDtos;
import com.example.authadmin.entity.User;
import com.example.authadmin.repository.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final InterviewBookingRepository bookingRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserCertificateRepository userCertificateRepository;
    private final UserDocumentRepository userDocumentRepository;
    private final UserEducationRepository userEducationRepository;
    private final UserExperienceRepository userExperienceRepository;
    private final UserProgrammingLanguageRepository userProgrammingLanguageRepository;
    private final UserSkillRepository userSkillRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, InterviewBookingRepository bookingRepository,
                       UserProfileRepository userProfileRepository,
                       UserCertificateRepository userCertificateRepository,
                       UserDocumentRepository userDocumentRepository,
                       UserEducationRepository userEducationRepository,
                       UserExperienceRepository userExperienceRepository,
                       UserProgrammingLanguageRepository userProgrammingLanguageRepository,
                       UserSkillRepository userSkillRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.userProfileRepository = userProfileRepository;
        this.userCertificateRepository = userCertificateRepository;
        this.userDocumentRepository = userDocumentRepository;
        this.userEducationRepository = userEducationRepository;
        this.userExperienceRepository = userExperienceRepository;
        this.userProgrammingLanguageRepository = userProgrammingLanguageRepository;
        this.userSkillRepository = userSkillRepository;
        this.emailService = emailService;
    }

    public User register(UserDtos.RegisterRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalStateException("Email already registered");
        });
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setVerified(false);
        User saved = userRepository.save(user);
        emailService.sendVerificationEmail(saved.getEmail(), saved.getUsername(), token);
        return saved;
    }

    public boolean verify(String token) {
        Optional<User> optionalUser = userRepository.findByVerificationToken(token);
        if (optionalUser.isEmpty()) {
            return false;
        }
        User user = optionalUser.get();
        user.setVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
        return true;
    }

    public Optional<User> authenticate(String email, String rawPassword) {
        Optional<User> optional = userRepository.findByEmail(email);
        if (optional.isEmpty()) return Optional.empty();
        User user = optional.get();
        if (!user.isVerified()) return Optional.empty();
        if (!user.isActive()) return Optional.empty();
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) return Optional.empty();
        return Optional.of(user);
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User updateEmail(Integer userId, String email) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("User not found"));
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw new IllegalStateException("Email is required");
        }
        userRepository.findByEmail(normalizedEmail).ifPresent(existing -> {
            if (!existing.getId().equals(userId)) {
                throw new IllegalStateException("Email already registered");
            }
        });
        user.setEmail(normalizedEmail);
        return userRepository.save(user);
    }

    public User setActive(Integer userId, boolean active) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("User not found"));
        user.setActive(active);
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("User not found"));
        bookingRepository.findByUser(user).forEach(bookingRepository::delete);
        userProfileRepository.deleteByUserId(userId);
        userCertificateRepository.deleteByUserId(userId);
        userDocumentRepository.deleteByUserId(userId);
        userEducationRepository.deleteByUserId(userId);
        userExperienceRepository.deleteByUserId(userId);
        userProgrammingLanguageRepository.deleteByUserId(userId);
        userSkillRepository.deleteByUserId(userId);
        userRepository.delete(user);
    }
}
