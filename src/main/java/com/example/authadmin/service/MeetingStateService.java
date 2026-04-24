package com.example.authadmin.service;

import org.springframework.stereotype.Service;

/**
 * Holds global meeting state (single session, no room IDs).
 */
@Service
public class MeetingStateService {
	public enum MeetingMode {
		NORMAL,
		SCHEDULED
	}

	private volatile boolean active = false;
	private volatile MeetingMode mode = MeetingMode.NORMAL;
	private volatile Integer activeBookingId = null;
	private volatile Integer targetUserId = null;
	private volatile Integer activeSlotId = null;
	private volatile java.util.Set<Integer> allowedUserIds = java.util.Set.of();
	private volatile java.util.Set<Integer> activeBookingIds = java.util.Set.of();

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
		if (!active) {
			this.mode = MeetingMode.NORMAL;
			this.activeBookingId = null;
			this.targetUserId = null;
			this.activeSlotId = null;
			this.allowedUserIds = java.util.Set.of();
			this.activeBookingIds = java.util.Set.of();
		}
	}

	public void activateForBooking(Integer bookingId, Integer userId) {
		this.active = true;
		this.mode = MeetingMode.SCHEDULED;
		this.activeBookingId = bookingId;
		this.targetUserId = userId;
		this.activeSlotId = null;
		this.allowedUserIds = (userId == null) ? java.util.Set.of() : java.util.Set.of(userId);
		this.activeBookingIds = (bookingId == null) ? java.util.Set.of() : java.util.Set.of(bookingId);
	}

	public void activateNormal() {
		this.active = true;
		this.mode = MeetingMode.NORMAL;
		this.activeBookingId = null;
		this.targetUserId = null;
		this.activeSlotId = null;
		this.allowedUserIds = java.util.Set.of();
		this.activeBookingIds = java.util.Set.of();
	}

	public void activateForScheduledSlot(Integer slotId, java.util.Set<Integer> userIds, java.util.Set<Integer> bookingIds) {
		this.active = true;
		this.mode = MeetingMode.SCHEDULED;
		this.activeSlotId = slotId;
		this.allowedUserIds = userIds == null ? java.util.Set.of() : java.util.Set.copyOf(userIds);
		this.activeBookingIds = bookingIds == null ? java.util.Set.of() : java.util.Set.copyOf(bookingIds);
		this.targetUserId = this.allowedUserIds.isEmpty() ? null : this.allowedUserIds.iterator().next();
		this.activeBookingId = this.activeBookingIds.isEmpty() ? null : this.activeBookingIds.iterator().next();
	}

	public Integer getActiveBookingId() {
		return activeBookingId;
	}

	public Integer getTargetUserId() {
		return targetUserId;
	}

	public MeetingMode getMode() {
		return mode;
	}

	public Integer getActiveSlotId() {
		return activeSlotId;
	}

	public java.util.Set<Integer> getAllowedUserIds() {
		return allowedUserIds;
	}

	public java.util.Set<Integer> getActiveBookingIds() {
		return activeBookingIds;
	}
}
