package com.example.authadmin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ranking_weights")
public class RankingWeight {
    @Id
    private Integer id;

    @Column(name = "communication_weight", precision = 5, scale = 2, nullable = false)
    private BigDecimal communicationWeight;

    @Column(name = "technical_weight", precision = 5, scale = 2, nullable = false)
    private BigDecimal technicalWeight;

    @Column(name = "behavioral_weight", precision = 5, scale = 2, nullable = false)
    private BigDecimal behavioralWeight;

    @Column(name = "profile_weight", precision = 5, scale = 2, nullable = false)
    private BigDecimal profileWeight;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public BigDecimal getCommunicationWeight() { return communicationWeight; }
    public void setCommunicationWeight(BigDecimal communicationWeight) { this.communicationWeight = communicationWeight; }
    public BigDecimal getTechnicalWeight() { return technicalWeight; }
    public void setTechnicalWeight(BigDecimal technicalWeight) { this.technicalWeight = technicalWeight; }
    public BigDecimal getBehavioralWeight() { return behavioralWeight; }
    public void setBehavioralWeight(BigDecimal behavioralWeight) { this.behavioralWeight = behavioralWeight; }
    public BigDecimal getProfileWeight() { return profileWeight; }
    public void setProfileWeight(BigDecimal profileWeight) { this.profileWeight = profileWeight; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

