package com.example.authadmin.config;

import com.example.authadmin.entity.Admin;
import com.example.authadmin.repository.AdminRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile({"default","dev","mysql"})
public class DataLoader implements ApplicationRunner {
	private final AdminRepository adminRepository;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public DataLoader(AdminRepository adminRepository) {
		this.adminRepository = adminRepository;
	}

	@Override
	public void run(ApplicationArguments args) {
		adminRepository.findById("admin").orElseGet(() -> {
			Admin a = new Admin();
			a.setAdminId("admin");
			a.setPassword(passwordEncoder.encode("admin"));
			return adminRepository.save(a);
		});
	}
}
