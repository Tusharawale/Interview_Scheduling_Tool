package com.example.authadmin.service;

import com.example.authadmin.dto.ProfileRequestDtos;
import com.example.authadmin.entity.*;
import com.example.authadmin.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class ProfileCrudService {

    private final UserProfileRepository userProfileRepository;
    private final UserEducationRepository userEducationRepository;
    private final UserExperienceRepository userExperienceRepository;
    private final UserProgrammingLanguageRepository plRepository;
    private final UserCertificateRepository certRepository;
    private final UserDocumentRepository docRepository;
    private final UserSkillRepository skillRepository;
    private final FileStorageService fileStorageService;

    public ProfileCrudService(UserProfileRepository userProfileRepository,
                             UserEducationRepository userEducationRepository,
                             UserExperienceRepository userExperienceRepository,
                             UserProgrammingLanguageRepository plRepository,
                             UserCertificateRepository certRepository,
                             UserDocumentRepository docRepository,
                             UserSkillRepository skillRepository,
                             FileStorageService fileStorageService) {
        this.userProfileRepository = userProfileRepository;
        this.userEducationRepository = userEducationRepository;
        this.userExperienceRepository = userExperienceRepository;
        this.plRepository = plRepository;
        this.certRepository = certRepository;
        this.docRepository = docRepository;
        this.skillRepository = skillRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Full profile field update from PUT /profile (replaces scalar fields; leaves photo unchanged unless null overwrites).
     */
    @Transactional
    public void saveProfileDetails(Integer userId, ProfileRequestDtos.SaveProfileRequest req) {
        UserProfile p = userProfileRepository.findByUserId(userId).orElse(new UserProfile());
        p.setUserId(userId);
        p.setFirstName(req.getFirstName());
        p.setMiddleName(req.getMiddleName());
        p.setLastName(req.getLastName());
        p.setContactNumber(req.getContactNumber());
        p.setGender(req.getGender());
        p.setDob(req.getDob());
        p.setCountry(req.getCountry());
        p.setState(req.getState());
        p.setCity(req.getCity());
        p.setLinkedinUrl(req.getLinkedinUrl());
        p.setGithubUrl(req.getGithubUrl());
        p.setCurrentCourse(req.getCurrentCourse());
        p.setCurrentCollegeCode(req.getCurrentCollegeCode());
        p.setCurrentBranch(req.getCurrentBranch());
        userProfileRepository.save(p);
    }

    @Transactional
    public void updateProfilePhoto(Integer userId, String profileImagePath) {
        if (profileImagePath == null) return;
        UserProfile p = userProfileRepository.findByUserId(userId).orElse(new UserProfile());
        p.setUserId(userId);
        String previous = p.getProfileImage();
        if (previous != null && !previous.equals(profileImagePath)) {
            fileStorageService.delete(previous);
        }
        p.setProfileImage(profileImagePath);
        userProfileRepository.save(p);
    }

    @Transactional
    public UserEducation saveEducation(Integer userId, ProfileRequestDtos.SaveEducationRequest req, String documentPath) {
        UserEducation e = req.getId() != null ? userEducationRepository.findById(req.getId())
            .filter(x -> x.getUserId().equals(userId))
            .orElse(new UserEducation()) : new UserEducation();
        e.setUserId(userId);
        if (req.getCollegeName() != null) e.setCollegeName(req.getCollegeName());
        if (req.getBranch() != null) e.setBranch(req.getBranch());
        if (req.getEducationLevel() != null) e.setEducationLevel(req.getEducationLevel());
        if (req.getSemester() != null) e.setSemester(req.getSemester());
        if (req.getStartYear() != null) e.setStartYear(parseYear(req.getStartYear()));
        if (req.getEndYear() != null) e.setEndYear(parseYear(req.getEndYear()));
        if (req.getTotalMarks() != null) e.setTotalMarks(req.getTotalMarks());
        if (req.getMarksObtained() != null) e.setMarksObtained(req.getMarksObtained());
        if (req.getCgpa() != null) try { e.setCgpa(new BigDecimal(req.getCgpa())); } catch (Exception ex) {}
        if (documentPath != null) e.setDocumentPath(documentPath);
        return userEducationRepository.save(e);
    }

    @Transactional
    public UserExperience saveExperience(Integer userId, ProfileRequestDtos.SaveExperienceRequest req, String documentPath) {
        UserExperience e = req.getId() != null ? userExperienceRepository.findById(req.getId())
            .filter(x -> x.getUserId().equals(userId))
            .orElse(new UserExperience()) : new UserExperience();
        e.setUserId(userId);
        if (req.getCompanyName() != null) e.setCompanyName(req.getCompanyName());
        if (req.getJobRole() != null) e.setJobRole(req.getJobRole());
        if (req.getDescription() != null) e.setDescription(req.getDescription());
        if (req.getStartDate() != null) e.setStartDate(parseDate(req.getStartDate()));
        if (req.getEndDate() != null) e.setEndDate(parseDate(req.getEndDate()));
        if (documentPath != null) e.setDocumentPath(documentPath);
        return userExperienceRepository.save(e);
    }

    @Transactional
    public UserProgrammingLanguage saveProgrammingLanguage(Integer userId, ProfileRequestDtos.SaveProgrammingLanguageRequest req, String certificatePath) {
        UserProgrammingLanguage pl = req.getId() != null ? plRepository.findById(req.getId())
            .filter(x -> x.getUserId().equals(userId))
            .orElse(new UserProgrammingLanguage()) : new UserProgrammingLanguage();
        pl.setUserId(userId);
        if (req.getLanguageName() != null) pl.setLanguageName(req.getLanguageName());
        if (req.getProficiencyLevel() != null) pl.setProficiencyLevel(req.getProficiencyLevel());
        if (req.getCertificateCompany() != null) pl.setCertificateCompany(req.getCertificateCompany());
        if (certificatePath != null) pl.setCertificateFile(certificatePath);
        return plRepository.save(pl);
    }

    @Transactional
    public UserCertificate saveCertificate(Integer userId, ProfileRequestDtos.SaveCertificateRequest req, String filePath) {
        UserCertificate c = req.getId() != null ? certRepository.findById(req.getId())
            .filter(x -> x.getUserId().equals(userId))
            .orElse(new UserCertificate()) : new UserCertificate();
        c.setUserId(userId);
        if (req.getCertificateName() != null) c.setCertificateName(req.getCertificateName());
        if (req.getIssuer() != null) c.setIssuer(req.getIssuer());
        if (req.getIssueDate() != null) c.setIssueDate(parseDate(req.getIssueDate()));
        if (filePath != null) c.setCertificateFile(filePath);
        return certRepository.save(c);
    }

    @Transactional
    public UserDocument saveDocument(Integer userId, ProfileRequestDtos.SaveDocumentRequest req, String filePath) {
        UserDocument d = req.getId() != null ? docRepository.findById(req.getId())
            .filter(x -> x.getUserId().equals(userId))
            .orElse(new UserDocument()) : new UserDocument();
        d.setUserId(userId);
        if (req.getDocumentName() != null) d.setDocumentName(req.getDocumentName());
        if (filePath != null) d.setFilePath(filePath);
        return docRepository.save(d);
    }

    @Transactional
    public UserSkill saveSkill(Integer userId, ProfileRequestDtos.SaveSkillRequest req) {
        UserSkill s = req.getId() != null ? skillRepository.findById(req.getId())
            .filter(x -> x.getUserId().equals(userId))
            .orElse(new UserSkill()) : new UserSkill();
        s.setUserId(userId);
        if (req.getSkillName() != null) s.setSkillName(req.getSkillName());
        if (req.getSkillLevel() != null) s.setSkillLevel(req.getSkillLevel());
        return skillRepository.save(s);
    }

    @Transactional
    public void deleteEducation(Integer id, Integer userId) {
        userEducationRepository.findById(id).ifPresent(e -> {
            if (e.getUserId().equals(userId)) {
                if (e.getDocumentPath() != null) fileStorageService.delete(e.getDocumentPath());
                userEducationRepository.delete(e);
            }
        });
    }

    @Transactional
    public void deleteExperience(Integer id, Integer userId) {
        userExperienceRepository.findById(id).ifPresent(e -> {
            if (e.getUserId().equals(userId)) {
                if (e.getDocumentPath() != null) fileStorageService.delete(e.getDocumentPath());
                userExperienceRepository.delete(e);
            }
        });
    }

    @Transactional
    public void deleteProgrammingLanguage(Integer id, Integer userId) {
        plRepository.findById(id).ifPresent(pl -> {
            if (pl.getUserId().equals(userId)) {
                if (pl.getCertificateFile() != null) fileStorageService.delete(pl.getCertificateFile());
                plRepository.delete(pl);
            }
        });
    }

    @Transactional
    public void deleteCertificate(Integer id, Integer userId) {
        certRepository.findById(id).ifPresent(c -> {
            if (c.getUserId().equals(userId)) {
                if (c.getCertificateFile() != null) fileStorageService.delete(c.getCertificateFile());
                certRepository.delete(c);
            }
        });
    }

    @Transactional
    public void deleteDocument(Integer id, Integer userId) {
        docRepository.findById(id).ifPresent(d -> {
            if (d.getUserId().equals(userId)) {
                if (d.getFilePath() != null) fileStorageService.delete(d.getFilePath());
                docRepository.delete(d);
            }
        });
    }

    @Transactional
    public void deleteSkill(Integer id, Integer userId) {
        skillRepository.findById(id).ifPresent(s -> {
            if (s.getUserId().equals(userId)) {
                skillRepository.delete(s);
            }
        });
    }

    private Year parseYear(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return Year.parse(s.trim()); } catch (Exception e) { return null; }
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDate.parse(s.trim()); // supports yyyy-MM-dd from HTML date inputs
        } catch (Exception ignored) {
        }
        try {
            DateTimeFormatter dmy = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return LocalDate.parse(s.trim(), dmy);
        } catch (Exception ignored) {
        }
        return null;
    }
}
