package com.example.authadmin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.Year;

@Entity
@Table(name = "user_education")
public class UserEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "college_name", length = 200)
    private String collegeName;

    @Column(length = 100)
    private String branch;

    @Column(name = "education_level", length = 100)
    private String educationLevel;

    private Integer semester;

    @Column(name = "start_year")
    private Year startYear;

    @Column(name = "end_year")
    private Year endYear;

    @Column(name = "total_marks")
    private Integer totalMarks;

    @Column(name = "marks_obtained")
    private Integer marksObtained;

    @Column(name = "cgpa", precision = 4, scale = 2)
    private java.math.BigDecimal cgpa;

    @Column(name = "document_path", length = 500)
    private String documentPath;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }
    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }
    public Year getStartYear() { return startYear; }
    public void setStartYear(Year startYear) { this.startYear = startYear; }
    public Year getEndYear() { return endYear; }
    public void setEndYear(Year endYear) { this.endYear = endYear; }
    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
    public Integer getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Integer marksObtained) { this.marksObtained = marksObtained; }
    public java.math.BigDecimal getCgpa() { return cgpa; }
    public void setCgpa(java.math.BigDecimal cgpa) { this.cgpa = cgpa; }
    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
