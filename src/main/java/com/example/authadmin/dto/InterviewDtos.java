package com.example.authadmin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class InterviewDtos {

	public static class CreateSlotRequest {
		@NotBlank
		private String title;

		private String description;

		/**
		 * ISO-8601 date time string, e.g. 2026-02-26T14:30
		 */
		@NotBlank
		private String scheduledAt;

		@Min(15)
		private int durationMinutes;

		@Min(1)
		private int capacity = 1;

		public String getTitle() { return title; }
		public void setTitle(String title) { this.title = title; }
		public String getDescription() { return description; }
		public void setDescription(String description) { this.description = description; }
		public String getScheduledAt() { return scheduledAt; }
		public void setScheduledAt(String scheduledAt) { this.scheduledAt = scheduledAt; }
		public int getDurationMinutes() { return durationMinutes; }
		public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
		public int getCapacity() { return capacity; }
		public void setCapacity(int capacity) { this.capacity = capacity; }
	}

	public static class SlotResponse {
		private Integer id;
		private String title;
		private String description;
		private String scheduledAt;
		private int durationMinutes;
		private int capacity;
		private long bookedCount;

		public Integer getId() { return id; }
		public void setId(Integer id) { this.id = id; }
		public String getTitle() { return title; }
		public void setTitle(String title) { this.title = title; }
		public String getDescription() { return description; }
		public void setDescription(String description) { this.description = description; }
		public String getScheduledAt() { return scheduledAt; }
		public void setScheduledAt(String scheduledAt) { this.scheduledAt = scheduledAt; }
		public int getDurationMinutes() { return durationMinutes; }
		public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
		public int getCapacity() { return capacity; }
		public void setCapacity(int capacity) { this.capacity = capacity; }
		public long getBookedCount() { return bookedCount; }
		public void setBookedCount(long bookedCount) { this.bookedCount = bookedCount; }
	}

	public static class BookRequest {
		@NotNull
		private Integer userId;

		public Integer getUserId() { return userId; }
		public void setUserId(Integer userId) { this.userId = userId; }
	}

	public static class BookingResponse {
		private Integer id;
		private Integer slotId;
		private String slotTitle;
		private String scheduledAt;
		private Integer userId;
		private String username;
		private String email;
		private String status;

		public Integer getId() { return id; }
		public void setId(Integer id) { this.id = id; }
		public Integer getSlotId() { return slotId; }
		public void setSlotId(Integer slotId) { this.slotId = slotId; }
		public String getSlotTitle() { return slotTitle; }
		public void setSlotTitle(String slotTitle) { this.slotTitle = slotTitle; }
		public String getScheduledAt() { return scheduledAt; }
		public void setScheduledAt(String scheduledAt) { this.scheduledAt = scheduledAt; }
		public Integer getUserId() { return userId; }
		public void setUserId(Integer userId) { this.userId = userId; }
		public String getUsername() { return username; }
		public void setUsername(String username) { this.username = username; }
		public String getEmail() { return email; }
		public void setEmail(String email) { this.email = email; }
		public String getStatus() { return status; }
		public void setStatus(String status) { this.status = status; }
	}

	public static class CalendarDaySummary {
		private String date;
		private int slotCount;
		private int bookedCount;

		public String getDate() { return date; }
		public void setDate(String date) { this.date = date; }
		public int getSlotCount() { return slotCount; }
		public void setSlotCount(int slotCount) { this.slotCount = slotCount; }
		public int getBookedCount() { return bookedCount; }
		public void setBookedCount(int bookedCount) { this.bookedCount = bookedCount; }
	}
}

