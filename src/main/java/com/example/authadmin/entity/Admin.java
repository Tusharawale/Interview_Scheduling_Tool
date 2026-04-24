package com.example.authadmin.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "admin")
public class Admin {
	@Id
	@Column(name = "admin_id", length = 50)
	private String adminId;

	@Column(nullable = false, length = 255)
	private String password;

	public String getAdminId() {
		return adminId;
	}

	public void setAdminId(String adminId) {
		this.adminId = adminId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
