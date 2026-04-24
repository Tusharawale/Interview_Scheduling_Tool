package com.example.authadmin.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "final_reports")
public class FinalReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "generated_at", insertable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Column(name = "total_users")
    private Integer totalUsers;

    @Column(name = "report_path", length = 500)
    private String reportPath;

    @Lob
    @Column(name = "report_json")
    private String reportJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public Integer getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Integer totalUsers) { this.totalUsers = totalUsers; }
    public String getReportPath() { return reportPath; }
    public void setReportPath(String reportPath) { this.reportPath = reportPath; }
    public String getReportJson() { return reportJson; }
    public void setReportJson(String reportJson) { this.reportJson = reportJson; }
}

