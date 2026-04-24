package com.example.authadmin.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminDtos {
	public static class LoginRequest {
		@NotBlank
		private String adminId;
		@NotBlank
		private String password;

		public String getAdminId() { return adminId; }
		public void setAdminId(String adminId) { this.adminId = adminId; }
		public String getPassword() { return password; }
		public void setPassword(String password) { this.password = password; }
	}

	public static class LoginResponse {
		private String status;
		private String meetingAdminToken;

		public String getStatus() { return status; }
		public void setStatus(String status) { this.status = status; }
		public String getMeetingAdminToken() { return meetingAdminToken; }
		public void setMeetingAdminToken(String meetingAdminToken) { this.meetingAdminToken = meetingAdminToken; }
	}
}
