package com.example.authadmin.dto;

import java.time.LocalDate;

public class ProfileRequestDtos {

    public static class SaveProfileRequest {
        private String firstName, middleName, lastName, contactNumber, gender;
        private LocalDate dob;
        private String country, state, city, linkedinUrl, githubUrl;
        /** btech | diploma | mtech — same values as edit form */
        private String currentCourse;
        private String currentCollegeCode;
        private String currentBranch;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getMiddleName() { return middleName; }
        public void setMiddleName(String middleName) { this.middleName = middleName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getContactNumber() { return contactNumber; }
        public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public LocalDate getDob() { return dob; }
        public void setDob(LocalDate dob) { this.dob = dob; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getLinkedinUrl() { return linkedinUrl; }
        public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
        public String getGithubUrl() { return githubUrl; }
        public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }
        public String getCurrentCourse() { return currentCourse; }
        public void setCurrentCourse(String currentCourse) { this.currentCourse = currentCourse; }
        public String getCurrentCollegeCode() { return currentCollegeCode; }
        public void setCurrentCollegeCode(String currentCollegeCode) { this.currentCollegeCode = currentCollegeCode; }
        public String getCurrentBranch() { return currentBranch; }
        public void setCurrentBranch(String currentBranch) { this.currentBranch = currentBranch; }
    }

    public static class SaveEducationRequest {
        private Integer id;
        private String collegeName, branch, educationLevel;
        private Integer semester;
        private String startYear, endYear;
        private Integer totalMarks, marksObtained;
        private String cgpa;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getCollegeName() { return collegeName; }
        public void setCollegeName(String collegeName) { this.collegeName = collegeName; }
        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }
        public String getEducationLevel() { return educationLevel; }
        public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }
        public Integer getSemester() { return semester; }
        public void setSemester(Integer semester) { this.semester = semester; }
        public String getStartYear() { return startYear; }
        public void setStartYear(String startYear) { this.startYear = startYear; }
        public String getEndYear() { return endYear; }
        public void setEndYear(String endYear) { this.endYear = endYear; }
        public Integer getTotalMarks() { return totalMarks; }
        public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
        public Integer getMarksObtained() { return marksObtained; }
        public void setMarksObtained(Integer marksObtained) { this.marksObtained = marksObtained; }
        public String getCgpa() { return cgpa; }
        public void setCgpa(String cgpa) { this.cgpa = cgpa; }
    }

    public static class SaveExperienceRequest {
        private Integer id;
        private String companyName, jobRole, description;
        private String startDate, endDate;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public String getJobRole() { return jobRole; }
        public void setJobRole(String jobRole) { this.jobRole = jobRole; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
    }

    public static class SaveProgrammingLanguageRequest {
        private Integer id;
        private String languageName, proficiencyLevel, certificateCompany;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getLanguageName() { return languageName; }
        public void setLanguageName(String languageName) { this.languageName = languageName; }
        public String getProficiencyLevel() { return proficiencyLevel; }
        public void setProficiencyLevel(String proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }
        public String getCertificateCompany() { return certificateCompany; }
        public void setCertificateCompany(String certificateCompany) { this.certificateCompany = certificateCompany; }
    }

    public static class SaveCertificateRequest {
        private Integer id;
        private String certificateName, issuer;
        private String issueDate;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getCertificateName() { return certificateName; }
        public void setCertificateName(String certificateName) { this.certificateName = certificateName; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public String getIssueDate() { return issueDate; }
        public void setIssueDate(String issueDate) { this.issueDate = issueDate; }
    }

    public static class SaveDocumentRequest {
        private Integer id;
        private String documentName;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getDocumentName() { return documentName; }
        public void setDocumentName(String documentName) { this.documentName = documentName; }
    }

    public static class SaveSkillRequest {
        private Integer id;
        private String skillName;
        private String skillLevel;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getSkillName() { return skillName; }
        public void setSkillName(String skillName) { this.skillName = skillName; }
        public String getSkillLevel() { return skillLevel; }
        public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }
    }
}
