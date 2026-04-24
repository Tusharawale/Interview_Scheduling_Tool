package com.example.authadmin.repository;

import com.example.authadmin.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
	Optional<User> findByEmail(String email);
	Optional<User> findByVerificationToken(String token);

	Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);
	Page<User> findByVerified(boolean verified, Pageable pageable);
	Page<User> findByEmailContainingIgnoreCaseAndVerified(String email, boolean verified, Pageable pageable);
}
