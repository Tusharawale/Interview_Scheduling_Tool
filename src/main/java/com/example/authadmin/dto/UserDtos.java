package com.example.authadmin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDtos {
	public static class RegisterRequest {
		@NotBlank
		private String username;
		@NotBlank @Email
		private String email;
		@NotBlank @Size(min = 6, max = 100)
		private String password;

		public String getUsername() { return username; }
		public void setUsername(String username) { this.username = username; }
		public String getEmail() { return email; }
		public void setEmail(String email) { this.email = email; }
		public String getPassword() { return password; }
		public void setPassword(String password) { this.password = password; }
	}

	public static class LoginRequest {
		@NotBlank @Email
		private String email;
		@NotBlank
		private String password;

		public String getEmail() { return email; }
		public void setEmail(String email) { this.email = email; }
		public String getPassword() { return password; }
		public void setPassword(String password) { this.password = password; }
	}

	public static class UpdateEmailRequest {
		@NotBlank @Email
		private String email;

		public String getEmail() { return email; }
		public void setEmail(String email) { this.email = email; }
	}

	public static class UserResponse {
		private Integer id;
		private String username;
		private String email;
		private boolean verified;

		public Integer getId() { return id; }
		public void setId(Integer id) { this.id = id; }
		public String getUsername() { return username; }
		public void setUsername(String username) { this.username = username; }
		public String getEmail() { return email; }
		public void setEmail(String email) { this.email = email; }
		public boolean isVerified() { return verified; }
		public void setVerified(boolean verified) { this.verified = verified; }
	}

	public static class AdminUserResponse {
		private Integer id;
		private String username;
		private String email;
		private boolean verified;
		private boolean active;

		public Integer getId() { return id; }
		public void setId(Integer id) { this.id = id; }
		public String getUsername() { return username; }
		public void setUsername(String username) { this.username = username; }
		public String getEmail() { return email; }
		public void setEmail(String email) { this.email = email; }
		public boolean isVerified() { return verified; }
		public void setVerified(boolean verified) { this.verified = verified; }
		public boolean isActive() { return active; }
		public void setActive(boolean active) { this.active = active; }
	}
}
