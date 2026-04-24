package com.example.authadmin.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_sessions")
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private InterviewBooking booking;

    @Column(name = "communication_score", precision = 5, scale = 2)
    private BigDecimal communicationScore;

    @Column(name = "technical_score", precision = 5, scale = 2)
    private BigDecimal technicalScore;

    @Column(name = "behavioral_score", precision = 5, scale = 2)
    private BigDecimal behavioralScore;

    @Column(name = "final_score", precision = 5, scale = 2)
    private BigDecimal finalScore;

    @Lob
    @Column(name = "transcript_text")
    private String transcriptText;

    @Column(name = "speaking_duration_seconds")
    private Integer speakingDurationSeconds;

    @Column(name = "words_count")
    private Integer wordsCount;

    @Column(name = "filler_words_count")
    private Integer fillerWordsCount;

    @Column(name = "speaking_speed_wpm", precision = 7, scale = 2)
    private BigDecimal speakingSpeedWpm;

    @Column(name = "clarity_score", precision = 5, scale = 2)
    private BigDecimal clarityScore;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public InterviewBooking getBooking() { return booking; }
    public void setBooking(InterviewBooking booking) { this.booking = booking; }
    public BigDecimal getCommunicationScore() { return communicationScore; }
    public void setCommunicationScore(BigDecimal communicationScore) { this.communicationScore = communicationScore; }
    public BigDecimal getTechnicalScore() { return technicalScore; }
    public void setTechnicalScore(BigDecimal technicalScore) { this.technicalScore = technicalScore; }
    public BigDecimal getBehavioralScore() { return behavioralScore; }
    public void setBehavioralScore(BigDecimal behavioralScore) { this.behavioralScore = behavioralScore; }
    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }
    public String getTranscriptText() { return transcriptText; }
    public void setTranscriptText(String transcriptText) { this.transcriptText = transcriptText; }
    public Integer getSpeakingDurationSeconds() { return speakingDurationSeconds; }
    public void setSpeakingDurationSeconds(Integer speakingDurationSeconds) { this.speakingDurationSeconds = speakingDurationSeconds; }
    public Integer getWordsCount() { return wordsCount; }
    public void setWordsCount(Integer wordsCount) { this.wordsCount = wordsCount; }
    public Integer getFillerWordsCount() { return fillerWordsCount; }
    public void setFillerWordsCount(Integer fillerWordsCount) { this.fillerWordsCount = fillerWordsCount; }
    public BigDecimal getSpeakingSpeedWpm() { return speakingSpeedWpm; }
    public void setSpeakingSpeedWpm(BigDecimal speakingSpeedWpm) { this.speakingSpeedWpm = speakingSpeedWpm; }
    public BigDecimal getClarityScore() { return clarityScore; }
    public void setClarityScore(BigDecimal clarityScore) { this.clarityScore = clarityScore; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

