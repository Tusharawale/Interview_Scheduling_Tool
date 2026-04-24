package com.example.authadmin.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coding_submissions")
public class CodingSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private InterviewBooking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private CodingChallenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 30)
    private String language;

    @Lob
    @Column(name = "source_code")
    private String sourceCode;

    @Lob
    @Column(name = "stdout_text")
    private String stdoutText;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "submitted_at", insertable = false, updatable = false)
    private LocalDateTime submittedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public InterviewBooking getBooking() { return booking; }
    public void setBooking(InterviewBooking booking) { this.booking = booking; }
    public CodingChallenge getChallenge() { return challenge; }
    public void setChallenge(CodingChallenge challenge) { this.challenge = challenge; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
    public String getStdoutText() { return stdoutText; }
    public void setStdoutText(String stdoutText) { this.stdoutText = stdoutText; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Integer executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
}

