package com.example.authadmin.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "meeting_chat_messages")
public class MeetingChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_role", length = 20)
    private String senderRole;

    @Column(name = "sender_name", length = 200)
    private String senderName;

    @Column(name = "sender_user_id")
    private Integer senderUserId;

    @Column(name = "from_client_id", length = 64)
    private String fromClientId;

    @Column(name = "message_text", columnDefinition = "TEXT")
    private String messageText;

    @Column(name = "file_name", length = 500)
    private String fileName;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public Integer getSenderUserId() { return senderUserId; }
    public void setSenderUserId(Integer senderUserId) { this.senderUserId = senderUserId; }
    public String getFromClientId() { return fromClientId; }
    public void setFromClientId(String fromClientId) { this.fromClientId = fromClientId; }
    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
