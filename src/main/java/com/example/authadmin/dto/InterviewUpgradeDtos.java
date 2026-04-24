package com.example.authadmin.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InterviewUpgradeDtos {

    public static class CommunicationScoreRequest {
        private Integer bookingId;
        private String transcriptText;
        private Integer speakingDurationSeconds;
        private Double technicalScore;
        private Double behavioralScore;

        public Integer getBookingId() { return bookingId; }
        public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
        public String getTranscriptText() { return transcriptText; }
        public void setTranscriptText(String transcriptText) { this.transcriptText = transcriptText; }
        public Integer getSpeakingDurationSeconds() { return speakingDurationSeconds; }
        public void setSpeakingDurationSeconds(Integer speakingDurationSeconds) { this.speakingDurationSeconds = speakingDurationSeconds; }
        public Double getTechnicalScore() { return technicalScore; }
        public void setTechnicalScore(Double technicalScore) { this.technicalScore = technicalScore; }
        public Double getBehavioralScore() { return behavioralScore; }
        public void setBehavioralScore(Double behavioralScore) { this.behavioralScore = behavioralScore; }
    }

    public static class CommunicationScoreResponse {
        private Long sessionId;
        private Integer bookingId;
        private Integer userId;
        private int wordsCount;
        private int fillerWordsCount;
        private BigDecimal speakingSpeedWpm;
        private BigDecimal clarityScore;
        private BigDecimal confidenceScore;
        private BigDecimal communicationScore;
        private BigDecimal technicalScore;
        private BigDecimal behavioralScore;
        private BigDecimal finalScore;

        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        public Integer getBookingId() { return bookingId; }
        public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public int getWordsCount() { return wordsCount; }
        public void setWordsCount(int wordsCount) { this.wordsCount = wordsCount; }
        public int getFillerWordsCount() { return fillerWordsCount; }
        public void setFillerWordsCount(int fillerWordsCount) { this.fillerWordsCount = fillerWordsCount; }
        public BigDecimal getSpeakingSpeedWpm() { return speakingSpeedWpm; }
        public void setSpeakingSpeedWpm(BigDecimal speakingSpeedWpm) { this.speakingSpeedWpm = speakingSpeedWpm; }
        public BigDecimal getClarityScore() { return clarityScore; }
        public void setClarityScore(BigDecimal clarityScore) { this.clarityScore = clarityScore; }
        public BigDecimal getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }
        public BigDecimal getCommunicationScore() { return communicationScore; }
        public void setCommunicationScore(BigDecimal communicationScore) { this.communicationScore = communicationScore; }
        public BigDecimal getTechnicalScore() { return technicalScore; }
        public void setTechnicalScore(BigDecimal technicalScore) { this.technicalScore = technicalScore; }
        public BigDecimal getBehavioralScore() { return behavioralScore; }
        public void setBehavioralScore(BigDecimal behavioralScore) { this.behavioralScore = behavioralScore; }
        public BigDecimal getFinalScore() { return finalScore; }
        public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }
    }

    public static class RankingWeightRequest {
        private Double communicationWeight;
        private Double technicalWeight;
        private Double behavioralWeight;
        private Double profileWeight;

        public Double getCommunicationWeight() { return communicationWeight; }
        public void setCommunicationWeight(Double communicationWeight) { this.communicationWeight = communicationWeight; }
        public Double getTechnicalWeight() { return technicalWeight; }
        public void setTechnicalWeight(Double technicalWeight) { this.technicalWeight = technicalWeight; }
        public Double getBehavioralWeight() { return behavioralWeight; }
        public void setBehavioralWeight(Double behavioralWeight) { this.behavioralWeight = behavioralWeight; }
        public Double getProfileWeight() { return profileWeight; }
        public void setProfileWeight(Double profileWeight) { this.profileWeight = profileWeight; }
    }

    public static class RankingWeightResponse {
        private BigDecimal communicationWeight;
        private BigDecimal technicalWeight;
        private BigDecimal behavioralWeight;
        private BigDecimal profileWeight;

        public BigDecimal getCommunicationWeight() { return communicationWeight; }
        public void setCommunicationWeight(BigDecimal communicationWeight) { this.communicationWeight = communicationWeight; }
        public BigDecimal getTechnicalWeight() { return technicalWeight; }
        public void setTechnicalWeight(BigDecimal technicalWeight) { this.technicalWeight = technicalWeight; }
        public BigDecimal getBehavioralWeight() { return behavioralWeight; }
        public void setBehavioralWeight(BigDecimal behavioralWeight) { this.behavioralWeight = behavioralWeight; }
        public BigDecimal getProfileWeight() { return profileWeight; }
        public void setProfileWeight(BigDecimal profileWeight) { this.profileWeight = profileWeight; }
    }

    public static class CandidateRankingRow {
        private Integer userId;
        private String username;
        private String displayName;
        private BigDecimal communicationScore;
        private BigDecimal technicalScore;
        private BigDecimal behavioralScore;
        private BigDecimal profileScore;
        private BigDecimal finalScore;

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public BigDecimal getCommunicationScore() { return communicationScore; }
        public void setCommunicationScore(BigDecimal communicationScore) { this.communicationScore = communicationScore; }
        public BigDecimal getTechnicalScore() { return technicalScore; }
        public void setTechnicalScore(BigDecimal technicalScore) { this.technicalScore = technicalScore; }
        public BigDecimal getBehavioralScore() { return behavioralScore; }
        public void setBehavioralScore(BigDecimal behavioralScore) { this.behavioralScore = behavioralScore; }
        public BigDecimal getProfileScore() { return profileScore; }
        public void setProfileScore(BigDecimal profileScore) { this.profileScore = profileScore; }
        public BigDecimal getFinalScore() { return finalScore; }
        public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }
    }

    public static class CodingChallengeRequest {
        private String title;
        private String description;
        private String language;
        private String starterCode;
        private String expectedOutput;
        private Integer timeLimitSeconds;
        private Integer memoryLimitMb;
        private Boolean active;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getStarterCode() { return starterCode; }
        public void setStarterCode(String starterCode) { this.starterCode = starterCode; }
        public String getExpectedOutput() { return expectedOutput; }
        public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
        public Integer getTimeLimitSeconds() { return timeLimitSeconds; }
        public void setTimeLimitSeconds(Integer timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }
        public Integer getMemoryLimitMb() { return memoryLimitMb; }
        public void setMemoryLimitMb(Integer memoryLimitMb) { this.memoryLimitMb = memoryLimitMb; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }

    public static class CodingChallengeResponse {
        private Long id;
        private String title;
        private String description;
        private String language;
        private String starterCode;
        private Integer timeLimitSeconds;
        private Integer memoryLimitMb;
        private boolean active;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getStarterCode() { return starterCode; }
        public void setStarterCode(String starterCode) { this.starterCode = starterCode; }
        public Integer getTimeLimitSeconds() { return timeLimitSeconds; }
        public void setTimeLimitSeconds(Integer timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }
        public Integer getMemoryLimitMb() { return memoryLimitMb; }
        public void setMemoryLimitMb(Integer memoryLimitMb) { this.memoryLimitMb = memoryLimitMb; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    public static class CodingSubmissionRequest {
        private Integer bookingId;
        private Long challengeId;
        private Integer userId;
        private String language;
        private String sourceCode;
        private String stdin;

        public Integer getBookingId() { return bookingId; }
        public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
        public Long getChallengeId() { return challengeId; }
        public void setChallengeId(Long challengeId) { this.challengeId = challengeId; }
        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getSourceCode() { return sourceCode; }
        public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
        public String getStdin() { return stdin; }
        public void setStdin(String stdin) { this.stdin = stdin; }
    }

    public static class CodingSubmissionResponse {
        private Long id;
        private Integer bookingId;
        private Long challengeId;
        private Integer userId;
        private String status;
        private Integer executionTimeMs;
        private BigDecimal score;
        private String stdoutText;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Integer getBookingId() { return bookingId; }
        public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
        public Long getChallengeId() { return challengeId; }
        public void setChallengeId(Long challengeId) { this.challengeId = challengeId; }
        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Integer getExecutionTimeMs() { return executionTimeMs; }
        public void setExecutionTimeMs(Integer executionTimeMs) { this.executionTimeMs = executionTimeMs; }
        public BigDecimal getScore() { return score; }
        public void setScore(BigDecimal score) { this.score = score; }
        public String getStdoutText() { return stdoutText; }
        public void setStdoutText(String stdoutText) { this.stdoutText = stdoutText; }
    }

    public static class RankingResponse {
        private RankingWeightResponse weights;
        private List<CandidateRankingRow> candidates = new ArrayList<>();

        public RankingWeightResponse getWeights() { return weights; }
        public void setWeights(RankingWeightResponse weights) { this.weights = weights; }
        public List<CandidateRankingRow> getCandidates() { return candidates; }
        public void setCandidates(List<CandidateRankingRow> candidates) { this.candidates = candidates; }
    }

    public static class NameValue {
        private String name;
        private double value;

        public NameValue() {}
        public NameValue(String name, double value) {
            this.name = name;
            this.value = value;
        }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
    }

    public static class CommunicationBehavioralPoint {
        private Integer userId;
        private String username;
        private String displayName;
        private Double communication;
        private Double behavioral;
        private Double finalScore;

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public Double getCommunication() { return communication; }
        public void setCommunication(Double communication) { this.communication = communication; }
        public Double getBehavioral() { return behavioral; }
        public void setBehavioral(Double behavioral) { this.behavioral = behavioral; }
        public Double getFinalScore() { return finalScore; }
        public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }
    }

    public static class FinalReportResponse {
        private String generatedAt;
        private long totalUsers;
        private long totalInterviewSessions;
        private long totalCodingSubmissions;
        private double averageCommunication;
        private double averageTechnical;
        private double averageBehavioral;
        private double averageFinal;
        private int acceptedSubmissions;
        private List<CandidateRankingRow> topCandidates = new ArrayList<>();
        private List<NameValue> codingStatusDistribution = new ArrayList<>();
        private List<NameValue> finalScoreBands = new ArrayList<>();
        private List<CommunicationBehavioralPoint> communicationBehavioralScatter = new ArrayList<>();

        public String getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        public long getTotalInterviewSessions() { return totalInterviewSessions; }
        public void setTotalInterviewSessions(long totalInterviewSessions) { this.totalInterviewSessions = totalInterviewSessions; }
        public long getTotalCodingSubmissions() { return totalCodingSubmissions; }
        public void setTotalCodingSubmissions(long totalCodingSubmissions) { this.totalCodingSubmissions = totalCodingSubmissions; }
        public double getAverageCommunication() { return averageCommunication; }
        public void setAverageCommunication(double averageCommunication) { this.averageCommunication = averageCommunication; }
        public double getAverageTechnical() { return averageTechnical; }
        public void setAverageTechnical(double averageTechnical) { this.averageTechnical = averageTechnical; }
        public double getAverageBehavioral() { return averageBehavioral; }
        public void setAverageBehavioral(double averageBehavioral) { this.averageBehavioral = averageBehavioral; }
        public double getAverageFinal() { return averageFinal; }
        public void setAverageFinal(double averageFinal) { this.averageFinal = averageFinal; }
        public int getAcceptedSubmissions() { return acceptedSubmissions; }
        public void setAcceptedSubmissions(int acceptedSubmissions) { this.acceptedSubmissions = acceptedSubmissions; }
        public List<CandidateRankingRow> getTopCandidates() { return topCandidates; }
        public void setTopCandidates(List<CandidateRankingRow> topCandidates) { this.topCandidates = topCandidates; }
        public List<NameValue> getCodingStatusDistribution() { return codingStatusDistribution; }
        public void setCodingStatusDistribution(List<NameValue> codingStatusDistribution) { this.codingStatusDistribution = codingStatusDistribution; }
        public List<NameValue> getFinalScoreBands() { return finalScoreBands; }
        public void setFinalScoreBands(List<NameValue> finalScoreBands) { this.finalScoreBands = finalScoreBands; }
        public List<CommunicationBehavioralPoint> getCommunicationBehavioralScatter() { return communicationBehavioralScatter; }
        public void setCommunicationBehavioralScatter(List<CommunicationBehavioralPoint> communicationBehavioralScatter) { this.communicationBehavioralScatter = communicationBehavioralScatter; }
    }

    public static class PublishReportResponse {
        private Long reportId;
        private String reportJsonPath;
        private String reportPdfPath;
        private int emailedUsers;
        private String generatedAt;

        public Long getReportId() { return reportId; }
        public void setReportId(Long reportId) { this.reportId = reportId; }
        public String getReportJsonPath() { return reportJsonPath; }
        public void setReportJsonPath(String reportJsonPath) { this.reportJsonPath = reportJsonPath; }
        public String getReportPdfPath() { return reportPdfPath; }
        public void setReportPdfPath(String reportPdfPath) { this.reportPdfPath = reportPdfPath; }
        public int getEmailedUsers() { return emailedUsers; }
        public void setEmailedUsers(int emailedUsers) { this.emailedUsers = emailedUsers; }
        public String getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
    }

    public static class UserPositionResponse {
        private Integer userId;
        private String username;
        private Integer position;
        private Integer totalCandidates;
        private Double finalScore;
        private String generatedAt;

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public Integer getPosition() { return position; }
        public void setPosition(Integer position) { this.position = position; }
        public Integer getTotalCandidates() { return totalCandidates; }
        public void setTotalCandidates(Integer totalCandidates) { this.totalCandidates = totalCandidates; }
        public Double getFinalScore() { return finalScore; }
        public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }
        public String getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
    }
}

