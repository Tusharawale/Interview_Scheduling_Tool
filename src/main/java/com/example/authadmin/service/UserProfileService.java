package com.example.authadmin.service;

import com.example.authadmin.dto.ProfileDtos;
import com.example.authadmin.entity.*;
import com.example.authadmin.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserCertificateRepository userCertificateRepository;
    private final UserDocumentRepository userDocumentRepository;
    private final UserEducationRepository userEducationRepository;
    private final UserExperienceRepository userExperienceRepository;
    private final UserProgrammingLanguageRepository userProgrammingLanguageRepository;
    private final UserSkillRepository userSkillRepository;

    public UserProfileService(UserProfileRepository userProfileRepository,
                              UserCertificateRepository userCertificateRepository,
                              UserDocumentRepository userDocumentRepository,
                              UserEducationRepository userEducationRepository,
                              UserExperienceRepository userExperienceRepository,
                              UserProgrammingLanguageRepository userProgrammingLanguageRepository,
                              UserSkillRepository userSkillRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userCertificateRepository = userCertificateRepository;
        this.userDocumentRepository = userDocumentRepository;
        this.userEducationRepository = userEducationRepository;
        this.userExperienceRepository = userExperienceRepository;
        this.userProgrammingLanguageRepository = userProgrammingLanguageRepository;
        this.userSkillRepository = userSkillRepository;
    }

    public ProfileDtos.ProfileResponse getProfileByUserId(Integer userId) {
        ProfileDtos.ProfileResponse response = new ProfileDtos.ProfileResponse();

        userProfileRepository.findByUserId(userId).ifPresent(p -> {
            ProfileDtos.UserProfileDto dto = new ProfileDtos.UserProfileDto();
            dto.setId(p.getId());
            dto.setUserId(p.getUserId());
            dto.setFirstName(p.getFirstName());
            dto.setMiddleName(p.getMiddleName());
            dto.setLastName(p.getLastName());
            dto.setContactNumber(p.getContactNumber());
            dto.setGender(p.getGender());
            dto.setDob(p.getDob());
            dto.setCountry(p.getCountry());
            dto.setState(p.getState());
            dto.setCity(p.getCity());
            dto.setLinkedinUrl(p.getLinkedinUrl());
            dto.setGithubUrl(p.getGithubUrl());
            dto.setProfileImage(p.getProfileImage());
            dto.setCurrentCourse(p.getCurrentCourse());
            dto.setCurrentCollegeCode(p.getCurrentCollegeCode());
            dto.setCurrentBranch(p.getCurrentBranch());
            response.setProfile(dto);
        });

        List<ProfileDtos.CertificateDto> certs = userCertificateRepository.findByUserId(userId).stream()
            .map(c -> {
                ProfileDtos.CertificateDto d = new ProfileDtos.CertificateDto();
                d.setId(c.getId());
                d.setCertificateName(c.getCertificateName());
                d.setIssuer(c.getIssuer());
                d.setIssueDate(c.getIssueDate());
                d.setCertificateFile(c.getCertificateFile());
                return d;
            })
            .collect(Collectors.toList());
        response.setCertificates(certs);

        List<ProfileDtos.DocumentDto> docDtos = userDocumentRepository.findByUserId(userId).stream()
            .map(d -> {
                ProfileDtos.DocumentDto dto = new ProfileDtos.DocumentDto();
                dto.setId(d.getId());
                dto.setDocumentName(d.getDocumentName());
                dto.setFilePath(d.getFilePath());
                dto.setUploadedAt(d.getUploadedAt());
                return dto;
            })
            .collect(Collectors.toList());
        response.setDocuments(docDtos);

        List<ProfileDtos.EducationDto> eduDtos = userEducationRepository.findByUserId(userId).stream()
            .map(e -> {
                ProfileDtos.EducationDto dto = new ProfileDtos.EducationDto();
                dto.setId(e.getId());
                dto.setCollegeName(e.getCollegeName());
                dto.setBranch(e.getBranch());
                dto.setEducationLevel(e.getEducationLevel());
                dto.setSemester(e.getSemester());
                dto.setStartYear(e.getStartYear() != null ? e.getStartYear().toString() : null);
                dto.setEndYear(e.getEndYear() != null ? e.getEndYear().toString() : null);
                dto.setTotalMarks(e.getTotalMarks());
                dto.setMarksObtained(e.getMarksObtained());
                dto.setCgpa(e.getCgpa() != null ? e.getCgpa().toString() : null);
                dto.setDocumentPath(e.getDocumentPath());
                return dto;
            })
            .collect(Collectors.toList());
        response.setEducation(eduDtos);

        List<ProfileDtos.ExperienceDto> expDtos = userExperienceRepository.findByUserId(userId).stream()
            .map(e -> {
                ProfileDtos.ExperienceDto dto = new ProfileDtos.ExperienceDto();
                dto.setId(e.getId());
                dto.setCompanyName(e.getCompanyName());
                dto.setJobRole(e.getJobRole());
                dto.setStartDate(e.getStartDate());
                dto.setEndDate(e.getEndDate());
                dto.setDescription(e.getDescription());
                dto.setDocumentPath(e.getDocumentPath());
                return dto;
            })
            .collect(Collectors.toList());
        response.setExperience(expDtos);

        List<ProfileDtos.SkillDto> skillDtos = userSkillRepository.findByUserId(userId).stream()
            .map(s -> {
                ProfileDtos.SkillDto dto = new ProfileDtos.SkillDto();
                dto.setId(s.getId());
                dto.setSkillName(s.getSkillName());
                dto.setSkillLevel(s.getSkillLevel());
                return dto;
            })
            .collect(Collectors.toList());
        response.setSkills(skillDtos);

        List<ProfileDtos.ProgrammingLanguageDto> plDtos = userProgrammingLanguageRepository.findByUserId(userId).stream()
            .map(pl -> {
                ProfileDtos.ProgrammingLanguageDto dto = new ProfileDtos.ProgrammingLanguageDto();
                dto.setId(pl.getId());
                dto.setLanguageName(pl.getLanguageName());
                dto.setProficiencyLevel(pl.getProficiencyLevel());
                dto.setCertificateCompany(pl.getCertificateCompany());
                dto.setCertificateFile(pl.getCertificateFile());
                return dto;
            })
            .collect(Collectors.toList());
        response.setProgrammingLanguages(plDtos);

        return response;
    }
}
