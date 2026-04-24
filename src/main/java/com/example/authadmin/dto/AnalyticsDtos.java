package com.example.authadmin.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Chart-friendly aggregates derived from MySQL profile data (education, skills, etc.).
 */
public class AnalyticsDtos {

    public static class UserAnalyticsResponse {
        private Integer userId;
        private String username;
        private String displayName;
        private String currentCourse;
        private int profileCompleteness;
        private int educationCount;
        private int experienceCount;
        private int skillCount;
        private int programmingLanguageCount;
        private int certificateCount;
        private int documentCount;
        private Double averageCgpa;
        private List<EducationChartPoint> educationTrend = new ArrayList<>();
        private List<EducationTrendSeries> educationTrendsByLevel = new ArrayList<>();
        private List<NamedValue> skillLevelDistribution = new ArrayList<>();
        private List<NamedValue> programmingLanguages = new ArrayList<>();
        private List<NamedValue> sectionCounts = new ArrayList<>();
        private List<String> insights = new ArrayList<>();
        private RadarCompare radarCompare;
        private RadarCompare percentileCompare;
        private Double communicationScore;
        private Double technicalScore;
        private Double behavioralScore;
        private Double interviewFinalScore;

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public String getCurrentCourse() { return currentCourse; }
        public void setCurrentCourse(String currentCourse) { this.currentCourse = currentCourse; }
        public int getProfileCompleteness() { return profileCompleteness; }
        public void setProfileCompleteness(int profileCompleteness) { this.profileCompleteness = profileCompleteness; }
        public int getEducationCount() { return educationCount; }
        public void setEducationCount(int educationCount) { this.educationCount = educationCount; }
        public int getExperienceCount() { return experienceCount; }
        public void setExperienceCount(int experienceCount) { this.experienceCount = experienceCount; }
        public int getSkillCount() { return skillCount; }
        public void setSkillCount(int skillCount) { this.skillCount = skillCount; }
        public int getProgrammingLanguageCount() { return programmingLanguageCount; }
        public void setProgrammingLanguageCount(int programmingLanguageCount) { this.programmingLanguageCount = programmingLanguageCount; }
        public int getCertificateCount() { return certificateCount; }
        public void setCertificateCount(int certificateCount) { this.certificateCount = certificateCount; }
        public int getDocumentCount() { return documentCount; }
        public void setDocumentCount(int documentCount) { this.documentCount = documentCount; }
        public Double getAverageCgpa() { return averageCgpa; }
        public void setAverageCgpa(Double averageCgpa) { this.averageCgpa = averageCgpa; }
        public List<EducationChartPoint> getEducationTrend() { return educationTrend; }
        public void setEducationTrend(List<EducationChartPoint> educationTrend) { this.educationTrend = educationTrend; }
        public List<EducationTrendSeries> getEducationTrendsByLevel() { return educationTrendsByLevel; }
        public void setEducationTrendsByLevel(List<EducationTrendSeries> educationTrendsByLevel) { this.educationTrendsByLevel = educationTrendsByLevel; }
        public List<NamedValue> getSkillLevelDistribution() { return skillLevelDistribution; }
        public void setSkillLevelDistribution(List<NamedValue> skillLevelDistribution) { this.skillLevelDistribution = skillLevelDistribution; }
        public List<NamedValue> getProgrammingLanguages() { return programmingLanguages; }
        public void setProgrammingLanguages(List<NamedValue> programmingLanguages) { this.programmingLanguages = programmingLanguages; }
        public List<NamedValue> getSectionCounts() { return sectionCounts; }
        public void setSectionCounts(List<NamedValue> sectionCounts) { this.sectionCounts = sectionCounts; }
        public List<String> getInsights() { return insights; }
        public void setInsights(List<String> insights) { this.insights = insights; }
        public RadarCompare getRadarCompare() { return radarCompare; }
        public void setRadarCompare(RadarCompare radarCompare) { this.radarCompare = radarCompare; }
        public RadarCompare getPercentileCompare() { return percentileCompare; }
        public void setPercentileCompare(RadarCompare percentileCompare) { this.percentileCompare = percentileCompare; }
        public Double getCommunicationScore() { return communicationScore; }
        public void setCommunicationScore(Double communicationScore) { this.communicationScore = communicationScore; }
        public Double getTechnicalScore() { return technicalScore; }
        public void setTechnicalScore(Double technicalScore) { this.technicalScore = technicalScore; }
        public Double getBehavioralScore() { return behavioralScore; }
        public void setBehavioralScore(Double behavioralScore) { this.behavioralScore = behavioralScore; }
        public Double getInterviewFinalScore() { return interviewFinalScore; }
        public void setInterviewFinalScore(Double interviewFinalScore) { this.interviewFinalScore = interviewFinalScore; }
    }

    public static class EducationChartPoint {
        private Integer semester;
        private Double cgpa;
        private Double percentage;
        private String label;

        public Integer getSemester() { return semester; }
        public void setSemester(Integer semester) { this.semester = semester; }
        public Double getCgpa() { return cgpa; }
        public void setCgpa(Double cgpa) { this.cgpa = cgpa; }
        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    public static class EducationTrendSeries {
        private String educationLevel;
        private List<EducationChartPoint> points = new ArrayList<>();

        public String getEducationLevel() { return educationLevel; }
        public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }
        public List<EducationChartPoint> getPoints() { return points; }
        public void setPoints(List<EducationChartPoint> points) { this.points = points; }
    }

    public static class NamedValue {
        private String name;
        private double value;

        public NamedValue() {}
        public NamedValue(String name, double value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
    }

    public static class RadarCompare {
        private List<String> labels = new ArrayList<>();
        private List<Double> userScores = new ArrayList<>();
        private List<Double> cohortScores = new ArrayList<>();

        public List<String> getLabels() { return labels; }
        public void setLabels(List<String> labels) { this.labels = labels; }
        public List<Double> getUserScores() { return userScores; }
        public void setUserScores(List<Double> userScores) { this.userScores = userScores; }
        public List<Double> getCohortScores() { return cohortScores; }
        public void setCohortScores(List<Double> cohortScores) { this.cohortScores = cohortScores; }
    }

    public static class TalentUserSummary {
        private Integer userId;
        private String username;
        private String displayName;
        private int profileCompleteness;
        private Double averageCgpa;
        private int totalSkillItems;
        private int experienceCount;
        private int educationCount;
        private int certificateCount;
        private Double communicationScore;
        private Double technicalScore;
        private Double behavioralScore;
        private Double interviewFinalScore;

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public int getProfileCompleteness() { return profileCompleteness; }
        public void setProfileCompleteness(int profileCompleteness) { this.profileCompleteness = profileCompleteness; }
        public Double getAverageCgpa() { return averageCgpa; }
        public void setAverageCgpa(Double averageCgpa) { this.averageCgpa = averageCgpa; }
        public int getTotalSkillItems() { return totalSkillItems; }
        public void setTotalSkillItems(int totalSkillItems) { this.totalSkillItems = totalSkillItems; }
        public int getExperienceCount() { return experienceCount; }
        public void setExperienceCount(int experienceCount) { this.experienceCount = experienceCount; }
        public int getEducationCount() { return educationCount; }
        public void setEducationCount(int educationCount) { this.educationCount = educationCount; }
        public int getCertificateCount() { return certificateCount; }
        public void setCertificateCount(int certificateCount) { this.certificateCount = certificateCount; }
        public Double getCommunicationScore() { return communicationScore; }
        public void setCommunicationScore(Double communicationScore) { this.communicationScore = communicationScore; }
        public Double getTechnicalScore() { return technicalScore; }
        public void setTechnicalScore(Double technicalScore) { this.technicalScore = technicalScore; }
        public Double getBehavioralScore() { return behavioralScore; }
        public void setBehavioralScore(Double behavioralScore) { this.behavioralScore = behavioralScore; }
        public Double getInterviewFinalScore() { return interviewFinalScore; }
        public void setInterviewFinalScore(Double interviewFinalScore) { this.interviewFinalScore = interviewFinalScore; }
    }

    public static class TalentPoolResponse {
        private List<TalentUserSummary> users = new ArrayList<>();
        private RadarCompare cohortRadarAverages;

        public List<TalentUserSummary> getUsers() { return users; }
        public void setUsers(List<TalentUserSummary> users) { this.users = users; }
        public RadarCompare getCohortRadarAverages() { return cohortRadarAverages; }
        public void setCohortRadarAverages(RadarCompare cohortRadarAverages) { this.cohortRadarAverages = cohortRadarAverages; }
    }

    public static class LocationPoint {
        private String country;
        private String state;
        private String city;
        private double lat;
        private double lng;
        private int count;
        private List<Integer> userIds = new ArrayList<>();
        private List<String> usernames = new ArrayList<>();

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
        public double getLng() { return lng; }
        public void setLng(double lng) { this.lng = lng; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public List<Integer> getUserIds() { return userIds; }
        public void setUserIds(List<Integer> userIds) { this.userIds = userIds; }
        public List<String> getUsernames() { return usernames; }
        public void setUsernames(List<String> usernames) { this.usernames = usernames; }
    }

    public static class LocationCount {
        private String name;
        private int count;

        public LocationCount() {}
        public LocationCount(String name, int count) {
            this.name = name;
            this.count = count;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    public static class LocationAnalyticsResponse {
        private int totalLocatedUsers;
        private List<LocationPoint> points = new ArrayList<>();
        private List<LocationCount> countries = new ArrayList<>();
        private List<LocationCount> states = new ArrayList<>();
        private List<LocationCount> cities = new ArrayList<>();

        public int getTotalLocatedUsers() { return totalLocatedUsers; }
        public void setTotalLocatedUsers(int totalLocatedUsers) { this.totalLocatedUsers = totalLocatedUsers; }
        public List<LocationPoint> getPoints() { return points; }
        public void setPoints(List<LocationPoint> points) { this.points = points; }
        public List<LocationCount> getCountries() { return countries; }
        public void setCountries(List<LocationCount> countries) { this.countries = countries; }
        public List<LocationCount> getStates() { return states; }
        public void setStates(List<LocationCount> states) { this.states = states; }
        public List<LocationCount> getCities() { return cities; }
        public void setCities(List<LocationCount> cities) { this.cities = cities; }
    }

    public static class TalentRefreshNotice {
        private String type = "talent-refresh";
        private Integer changedUserId;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Integer getChangedUserId() { return changedUserId; }
        public void setChangedUserId(Integer changedUserId) { this.changedUserId = changedUserId; }
    }
}
