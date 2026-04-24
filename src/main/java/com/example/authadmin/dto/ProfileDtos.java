package com.example.authadmin.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProfileDtos {

    public static class ProfileResponse {
        private UserProfileDto profile;
        private List<CertificateDto> certificates;
        private List<DocumentDto> documents;
        private List<EducationDto> education;
        private List<ExperienceDto> experience;
        private List<SkillDto> skills;
        private List<ProgrammingLanguageDto> programmingLanguages;

        public UserProfileDto getProfile() { return profile; }
        public void setProfile(UserProfileDto profile) { this.profile = profile; }
        public List<CertificateDto> getCertificates() { return certificates; }
        public void setCertificates(List<CertificateDto> certificates) { this.certificates = certificates; }
        public List<DocumentDto> getDocuments() { return documents; }
        public void setDocuments(List<DocumentDto> documents) { this.documents = documents; }
        public List<EducationDto> getEducation() { return education; }
        public void setEducation(List<EducationDto> education) { this.education = education; }
        public List<ExperienceDto> getExperience() { return experience; }
        public void setExperience(List<ExperienceDto> experience) { this.experience = experience; }
        public List<SkillDto> getSkills() { return skills; }
        public void setSkills(List<SkillDto> skills) { this.skills = skills; }
        public List<ProgrammingLanguageDto> getProgrammingLanguages() { return programmingLanguages; }
        public void setProgrammingLanguages(List<ProgrammingLanguageDto> programmingLanguages) { this.programmingLanguages = programmingLanguages; }
    }

    public static class UserProfileDto {
        private Integer id;
        private Integer userId;
        private String firstName;
        private String middleName;
        private String lastName;
        private String contactNumber;
        private String gender;
        private LocalDate dob;
        private String country;
        private String state;
        private String city;
        private String linkedinUrl;
        private String githubUrl;
        private String profileImage;
        private String currentCourse;
        private String currentCollegeCode;
        private String currentBranch;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
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
        public String getProfileImage() { return profileImage; }
        public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
        public String getCurrentCourse() { return currentCourse; }
        public void setCurrentCourse(String currentCourse) { this.currentCourse = currentCourse; }
        public String getCurrentCollegeCode() { return currentCollegeCode; }
        public void setCurrentCollegeCode(String currentCollegeCode) { this.currentCollegeCode = currentCollegeCode; }
        public String getCurrentBranch() { return currentBranch; }
        public void setCurrentBranch(String currentBranch) { this.currentBranch = currentBranch; }
    }

    public static class CertificateDto {
        private Integer id;
        private String certificateName;
        private String issuer;
        private LocalDate issueDate;
        private String certificateFile;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getCertificateName() { return certificateName; }
        public void setCertificateName(String certificateName) { this.certificateName = certificateName; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public LocalDate getIssueDate() { return issueDate; }
        public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
        public String getCertificateFile() { return certificateFile; }
        public void setCertificateFile(String certificateFile) { this.certificateFile = certificateFile; }
    }

    public static class DocumentDto {
        private Integer id;
        private String documentName;
        private String filePath;
        private LocalDateTime uploadedAt;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getDocumentName() { return documentName; }
        public void setDocumentName(String documentName) { this.documentName = documentName; }
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public LocalDateTime getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    }

    public static class EducationDto {
        private Integer id;
        private String collegeName;
        private String branch;
        private String educationLevel;
        private Integer semester;
        private String startYear;
        private String endYear;
        private Integer totalMarks;
        private Integer marksObtained;
        private String cgpa;
        private String documentPath;

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
        public String getDocumentPath() { return documentPath; }
        public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
    }

    public static class ExperienceDto {
        private Integer id;
        private String companyName;
        private String jobRole;
        private LocalDate startDate;
        private LocalDate endDate;
        private String description;
        private String documentPath;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public String getJobRole() { return jobRole; }
        public void setJobRole(String jobRole) { this.jobRole = jobRole; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDocumentPath() { return documentPath; }
        public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
    }

    public static class SkillDto {
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

    public static class ProgrammingLanguageDto {
        private Integer id;
        private String languageName;
        private String proficiencyLevel;
        private String certificateCompany;
        private String certificateFile;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getLanguageName() { return languageName; }
        public void setLanguageName(String languageName) { this.languageName = languageName; }
        public String getProficiencyLevel() { return proficiencyLevel; }
        public void setProficiencyLevel(String proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }
        public String getCertificateCompany() { return certificateCompany; }
        public void setCertificateCompany(String certificateCompany) { this.certificateCompany = certificateCompany; }
        public String getCertificateFile() { return certificateFile; }
        public void setCertificateFile(String certificateFile) { this.certificateFile = certificateFile; }
    }
}
