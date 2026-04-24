package com.example.authadmin.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class MeetingDtos {

	public static class CreateMeetingResponse {
		private String roomId;
		public String getRoomId() { return roomId; }
		public void setRoomId(String roomId) { this.roomId = roomId; }
	}

	public static class StatusResponse {
		private boolean active;
		private String mode;
		private Integer bookingId;
		private Integer targetUserId;
		private Integer slotId;
		private java.util.List<Integer> allowedUserIds;
		private java.util.List<Integer> bookingIds;
		public boolean isActive() { return active; }
		public void setActive(boolean active) { this.active = active; }
		public String getMode() { return mode; }
		public void setMode(String mode) { this.mode = mode; }
		public Integer getBookingId() { return bookingId; }
		public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
		public Integer getTargetUserId() { return targetUserId; }
		public void setTargetUserId(Integer targetUserId) { this.targetUserId = targetUserId; }
		public Integer getSlotId() { return slotId; }
		public void setSlotId(Integer slotId) { this.slotId = slotId; }
		public java.util.List<Integer> getAllowedUserIds() { return allowedUserIds; }
		public void setAllowedUserIds(java.util.List<Integer> allowedUserIds) { this.allowedUserIds = allowedUserIds; }
		public java.util.List<Integer> getBookingIds() { return bookingIds; }
		public void setBookingIds(java.util.List<Integer> bookingIds) { this.bookingIds = bookingIds; }
	}

	public static class TranscriptSegment {
		private String speaker;
		private String text;
		private Double confidence;
		private Double startSec;
		private Double endSec;

		public String getSpeaker() { return speaker; }
		public void setSpeaker(String speaker) { this.speaker = speaker; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
		public Double getConfidence() { return confidence; }
		public void setConfidence(Double confidence) { this.confidence = confidence; }
		public Double getStartSec() { return startSec; }
		public void setStartSec(Double startSec) { this.startSec = startSec; }
		public Double getEndSec() { return endSec; }
		public void setEndSec(Double endSec) { this.endSec = endSec; }
	}

	public static class WsEnvelope {
		private String type;
		private String sender;
		private String senderName;
		private Integer senderId;
		private String kind;
		private String toClientId;
		private String fromClientId;
		private Object payload;
		private Instant ts;

		public String getType() { return type; }
		public void setType(String type) { this.type = type; }
		public String getSender() { return sender; }
		public void setSender(String sender) { this.sender = sender; }
		public String getSenderName() { return senderName; }
		public void setSenderName(String senderName) { this.senderName = senderName; }
		public Integer getSenderId() { return senderId; }
		public void setSenderId(Integer senderId) { this.senderId = senderId; }
		public String getKind() { return kind; }
		public void setKind(String kind) { this.kind = kind; }
		public String getToClientId() { return toClientId; }
		public void setToClientId(String toClientId) { this.toClientId = toClientId; }
		public String getFromClientId() { return fromClientId; }
		public void setFromClientId(String fromClientId) { this.fromClientId = fromClientId; }
		public Object getPayload() { return payload; }
		public void setPayload(Object payload) { this.payload = payload; }
		public Instant getTs() { return ts; }
		public void setTs(Instant ts) { this.ts = ts; }
	}

	public static class ChatPayload {
		private String text;
		private String fileName;
		private String fileUrl;
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
		public String getFileName() { return fileName; }
		public void setFileName(String fileName) { this.fileName = fileName; }
		public String getFileUrl() { return fileUrl; }
		public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
	}

	public static class PresencePayload {
		private String clientId;
		private String role;
		private String name;
		private Integer userId;

		public String getClientId() { return clientId; }
		public void setClientId(String clientId) { this.clientId = clientId; }
		public String getRole() { return role; }
		public void setRole(String role) { this.role = role; }
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public Integer getUserId() { return userId; }
		public void setUserId(Integer userId) { this.userId = userId; }
	}

	public static class ChatHistoryItem {
		private String senderRole;
		private String senderName;
		private Integer senderUserId;
		private String fromClientId;
		private String text;
		private String fileName;
		private String fileUrl;
		private Instant ts;

		public String getSenderRole() { return senderRole; }
		public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
		public String getSenderName() { return senderName; }
		public void setSenderName(String senderName) { this.senderName = senderName; }
		public Integer getSenderUserId() { return senderUserId; }
		public void setSenderUserId(Integer senderUserId) { this.senderUserId = senderUserId; }
		public String getFromClientId() { return fromClientId; }
		public void setFromClientId(String fromClientId) { this.fromClientId = fromClientId; }
		public String getText() { return text; }
		public void setText(String text) { this.text = text; }
		public String getFileName() { return fileName; }
		public void setFileName(String fileName) { this.fileName = fileName; }
		public String getFileUrl() { return fileUrl; }
		public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
		public Instant getTs() { return ts; }
		public void setTs(Instant ts) { this.ts = ts; }
	}

	public static class TypingPayload {
		private String fromClientId;
		private String senderName;
		private String role;
		private boolean typing;

		public String getFromClientId() { return fromClientId; }
		public void setFromClientId(String fromClientId) { this.fromClientId = fromClientId; }
		public String getSenderName() { return senderName; }
		public void setSenderName(String senderName) { this.senderName = senderName; }
		public String getRole() { return role; }
		public void setRole(String role) { this.role = role; }
		public boolean isTyping() { return typing; }
		public void setTyping(boolean typing) { this.typing = typing; }
	}

	public static class MeetingControlCommand {
		private String actionType; // mute_mic, unmute_mic, camera_off, camera_on, speaker_off, speaker_on
		private String mode; // request, force
		private String targetClientId;
		private Integer targetUserId;
		private String adminClientId;
		private Integer adminUserId;
		private String reason;
		private Instant ts;

		public String getActionType() { return actionType; }
		public void setActionType(String actionType) { this.actionType = actionType; }
		public String getMode() { return mode; }
		public void setMode(String mode) { this.mode = mode; }
		public String getTargetClientId() { return targetClientId; }
		public void setTargetClientId(String targetClientId) { this.targetClientId = targetClientId; }
		public Integer getTargetUserId() { return targetUserId; }
		public void setTargetUserId(Integer targetUserId) { this.targetUserId = targetUserId; }
		public String getAdminClientId() { return adminClientId; }
		public void setAdminClientId(String adminClientId) { this.adminClientId = adminClientId; }
		public Integer getAdminUserId() { return adminUserId; }
		public void setAdminUserId(Integer adminUserId) { this.adminUserId = adminUserId; }
		public String getReason() { return reason; }
		public void setReason(String reason) { this.reason = reason; }
		public Instant getTs() { return ts; }
		public void setTs(Instant ts) { this.ts = ts; }
	}

	public static class MeetingControlAck {
		private String actionType;
		private String mode;
		private String targetClientId;
		private Integer targetUserId;
		private String adminClientId;
		private Integer adminUserId;
		private boolean accepted;
		private boolean applied;
		private String message;
		private Instant ts;

		public String getActionType() { return actionType; }
		public void setActionType(String actionType) { this.actionType = actionType; }
		public String getMode() { return mode; }
		public void setMode(String mode) { this.mode = mode; }
		public String getTargetClientId() { return targetClientId; }
		public void setTargetClientId(String targetClientId) { this.targetClientId = targetClientId; }
		public Integer getTargetUserId() { return targetUserId; }
		public void setTargetUserId(Integer targetUserId) { this.targetUserId = targetUserId; }
		public String getAdminClientId() { return adminClientId; }
		public void setAdminClientId(String adminClientId) { this.adminClientId = adminClientId; }
		public Integer getAdminUserId() { return adminUserId; }
		public void setAdminUserId(Integer adminUserId) { this.adminUserId = adminUserId; }
		public boolean isAccepted() { return accepted; }
		public void setAccepted(boolean accepted) { this.accepted = accepted; }
		public boolean isApplied() { return applied; }
		public void setApplied(boolean applied) { this.applied = applied; }
		public String getMessage() { return message; }
		public void setMessage(String message) { this.message = message; }
		public Instant getTs() { return ts; }
		public void setTs(Instant ts) { this.ts = ts; }
	}

	/** Browser RTCPeerConfiguration.iceServers */
	public static class RtcConfigResponse {
		private List<Map<String, Object>> iceServers;
		private String mode;

		public List<Map<String, Object>> getIceServers() { return iceServers; }
		public void setIceServers(List<Map<String, Object>> iceServers) { this.iceServers = iceServers; }
		public String getMode() { return mode; }
		public void setMode(String mode) { this.mode = mode; }
	}
}
