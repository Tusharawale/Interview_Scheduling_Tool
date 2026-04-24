package com.example.authadmin.controller;

import com.example.authadmin.dto.ProfileDtos;
import com.example.authadmin.dto.ProfileRequestDtos;
import com.example.authadmin.service.AnalyticsStompPublisher;
import com.example.authadmin.service.FileStorageService;
import com.example.authadmin.service.ProfileCrudService;
import com.example.authadmin.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users/{userId}")
public class ProfileCrudController {

    private final UserProfileService userProfileService;
    private final ProfileCrudService profileCrudService;
    private final FileStorageService fileStorageService;
    private final AnalyticsStompPublisher analyticsStompPublisher;

    public ProfileCrudController(UserProfileService userProfileService,
                                ProfileCrudService profileCrudService,
                                FileStorageService fileStorageService,
                                AnalyticsStompPublisher analyticsStompPublisher) {
        this.userProfileService = userProfileService;
        this.profileCrudService = profileCrudService;
        this.fileStorageService = fileStorageService;
        this.analyticsStompPublisher = analyticsStompPublisher;
    }

    private void notifyAnalytics(Integer userId) {
        analyticsStompPublisher.publishAfterProfileChange(userId);
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileDtos.ProfileResponse> getProfile(@PathVariable("userId") Integer userId) {
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<Object> updateProfile(@PathVariable("userId") Integer userId,
                                                @RequestBody ProfileRequestDtos.SaveProfileRequest req) {
        profileCrudService.saveProfileDetails(userId, req);
        notifyAnalytics(userId);
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }

    @PostMapping("/profile/photo")
    public ResponseEntity<Object> uploadProfilePhoto(@PathVariable("userId") Integer userId,
                                                     @RequestParam("file") MultipartFile file) {
        String path = fileStorageService.store(file, "profile/" + userId);
        profileCrudService.updateProfilePhoto(userId, path);
        notifyAnalytics(userId);
        return ResponseEntity.ok(java.util.Map.of("path", path, "url", "/api/files/" + path));
    }

    @PostMapping("/education")
    public ResponseEntity<Object> saveEducation(@PathVariable("userId") Integer userId,
                                                @RequestParam(value = "file", required = false) MultipartFile file,
                                                @RequestParam(value = "id", required = false) Integer id,
                                                @RequestParam(value = "collegeName", required = false) String collegeName,
                                                @RequestParam(value = "branch", required = false) String branch,
                                                @RequestParam(value = "educationLevel", required = false) String educationLevel,
                                                @RequestParam(value = "semester", required = false) Integer semester,
                                                @RequestParam(value = "startYear", required = false) String startYear,
                                                @RequestParam(value = "endYear", required = false) String endYear,
                                                @RequestParam(value = "totalMarks", required = false) Integer totalMarks,
                                                @RequestParam(value = "marksObtained", required = false) Integer marksObtained,
                                                @RequestParam(value = "cgpa", required = false) String cgpa) {
        String docPath = file != null && !file.isEmpty() ? fileStorageService.store(file, "education/" + userId) : null;
        ProfileRequestDtos.SaveEducationRequest req = new ProfileRequestDtos.SaveEducationRequest();
        req.setId(id);
        req.setCollegeName(collegeName);
        req.setBranch(branch);
        req.setEducationLevel(educationLevel);
        req.setSemester(semester);
        req.setStartYear(startYear);
        req.setEndYear(endYear);
        req.setTotalMarks(totalMarks);
        req.setMarksObtained(marksObtained);
        req.setCgpa(cgpa);
        profileCrudService.saveEducation(userId, req, docPath);
        notifyAnalytics(userId);
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }

    @DeleteMapping("/education/{id}")
    public ResponseEntity<Object> deleteEducation(@PathVariable("userId") Integer userId, @PathVariable("id") Integer id) {
        profileCrudService.deleteEducation(id, userId);
        notifyAnalytics(userId);
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }

    @PostMapping("/experience")
    public ResponseEntity<Object> saveExperience(@PathVariable("userId") Integer userId,
                                                 @RequestParam(value = "file", required = false) MultipartFile file,
                                                 @RequestParam(value = "id", required = false) Integer id,
                                                 @RequestParam(value = "companyName", required = false) String companyName,
                                                 @RequestParam(value = "jobRole", required = false) String jobRole,
                                                 @RequestParam(value = "description", required = false) String description,
                                                 @RequestParam(value = "startDate", required = false) String startDate,
                                                 @RequestParam(value = "endDate", required = false) String endDate) {
        String docPath = file != null && !file.isEmpty() ? fileStorageService.store(file, "experience/" + userId) : null;
        ProfileRequestDtos.SaveExperienceRequest req = new ProfileRequestDtos.SaveExperienceRequest();
        req.setId(id);
        req.setCompanyName(companyName);
        req.setJobRole(jobRole);
        req.setDescription(description);
        req.setStartDate(startDate);
        req.setEndDate(endDate);
        profileCrudService.saveExperience(userId, req, docPath);
        notifyAnalytics(userId);
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }

    @DeleteMapping("/experience/{id}")
    public ResponseEntity<Object> deleteExperience(@PathVariable("userId") Integer userId, @PathVariable("id") Integer id) {
        profileCrudService.deleteExperience(id, userId);
        notifyAnalytics(userId);
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }

    @PostMapping("/programming-languages")
    public ResponseEntity<Object> saveProgrammingLanguage(@PathVariable("userId") Integer userId,
                                                          @RequestParam(value = "file", required = false) MultipartFile file,
                                                          @RequestParam(value = "id", required = false) Integer id,
                                                          @RequestParam(value = "languageName", required = false) String languageName,
                                                          @RequestParam(value = "proficiencyLevel", required = false) String proficiencyLevel,
                                                          @RequestParam(value = "certificateCompany", required = false) String certificateCompany) {
        String certPath = file != null && !file.isEmpty() ? fileStorageService.store(file, "certs/" + userId) : null;
        ProfileRequestDtos.SaveProgrammingLanguageRequest req = new ProfileRequestDtos.SaveProgrammingLanguageRequest();
        req.setId(id);
        req.setLanguageName(languageName);
        req.setProficiencyLevel(proficiencyLevel);
        req.setCertificateCompany(certificateCompany);
        profileCrudService.saveProgrammingLanguage(userId, req, certPath);
        notifyAnalytics(userId);
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }

    @DeleteMapping("/programming-languages/{id}")
    public ResponseEntity<Object> deleteProgrammingLanguage(@PathVariable("userId") Integer userId, @PathVariable("id") Integer id) {
        profileCrudService.deleteProgrammingLanguage(id, userId);
        notifyAnalytics(userId);
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }

    @PostMapping("/certificates")
    public ResponseEntity<Object> saveCertificate(@PathVariable("userId") Integer userId,
                                                  @RequestParam(value = "file", required = false) MultipartFile file,
                                                  @RequestParam(value = "id", required = false) Integer id,
                                                  @RequestParam(value = "certificateName", required = false) String certificateName,
                                                  @RequestParam(value = "issuer", required = false) String issuer,
                                                  @RequestParam(value = "issueDate", required = false) String issueDate) {
        String filePath = file != null && !file.isEmpty() ? fileStorageService.store(file, "certificates/" + userId) : null;
        ProfileRequestDtos.SaveCertificateRequest req = new ProfileRequestDtos.SaveCertificateRequest();
        req.setId(id);
        req.setCertificateName(certificateName);
        req.setIssuer(issuer);
        req.setIssueDate(issueDate);
        profileCrudService.saveCertificate(userId, req, filePath);
        notifyAnalytics(userId);
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }

    @DeleteMapping("/certificates/{id}")
    public ResponseEntity<Object> deleteCertificate(@PathVariable("userId") Integer userId, @PathVariable("id") Integer id) {
        profileCrudService.deleteCertificate(id, userId);
        notifyAnalytics(userId);
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }

    @PostMapping("/documents")
    public ResponseEntity<Object> saveDocument(@PathVariable("userId") Integer userId,
                                               @RequestParam("file") MultipartFile file,
                                               @RequestParam(value = "id", required = false) Integer id,
                                               @RequestParam(value = "documentName", required = false) String documentName) {
        String filePath = fileStorageService.store(file, "documents/" + userId);
        ProfileRequestDtos.SaveDocumentRequest req = new ProfileRequestDtos.SaveDocumentRequest();
        req.setId(id);
        req.setDocumentName(documentName != null ? documentName : file.getOriginalFilename());
        profileCrudService.saveDocument(userId, req, filePath);
        notifyAnalytics(userId);
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Object> deleteDocument(@PathVariable("userId") Integer userId, @PathVariable("id") Integer id) {
        profileCrudService.deleteDocument(id, userId);
        notifyAnalytics(userId);
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }

    @PostMapping("/skills")
    public ResponseEntity<Object> saveSkill(@PathVariable("userId") Integer userId,
                                            @RequestParam(value = "id", required = false) Integer id,
                                            @RequestParam(value = "skillName", required = false) String skillName,
                                            @RequestParam(value = "skillLevel", required = false) String skillLevel) {
        ProfileRequestDtos.SaveSkillRequest req = new ProfileRequestDtos.SaveSkillRequest();
        req.setId(id);
        req.setSkillName(skillName);
        req.setSkillLevel(skillLevel);
        profileCrudService.saveSkill(userId, req);
        notifyAnalytics(userId);
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<Object> deleteSkill(@PathVariable("userId") Integer userId, @PathVariable("id") Integer id) {
        profileCrudService.deleteSkill(id, userId);
        notifyAnalytics(userId);
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }
}
