package com.example.authadmin.service;

import com.example.authadmin.dto.AnalyticsDtos;
import com.example.authadmin.entity.*;
import com.example.authadmin.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProfileAnalyticsService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserEducationRepository userEducationRepository;
    private final UserExperienceRepository userExperienceRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserProgrammingLanguageRepository plRepository;
    private final UserCertificateRepository certificateRepository;
    private final UserDocumentRepository documentRepository;
    private final GeocodeCacheRepository geocodeCacheRepository;
    private final InterviewBookingRepository interviewBookingRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProfileAnalyticsService(UserRepository userRepository,
                                   UserProfileRepository userProfileRepository,
                                   UserEducationRepository userEducationRepository,
                                   UserExperienceRepository userExperienceRepository,
                                   UserSkillRepository userSkillRepository,
                                   UserProgrammingLanguageRepository plRepository,
                                   UserCertificateRepository certificateRepository,
                                   UserDocumentRepository documentRepository,
                                   GeocodeCacheRepository geocodeCacheRepository,
                                   InterviewBookingRepository interviewBookingRepository,
                                   InterviewSessionRepository interviewSessionRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userEducationRepository = userEducationRepository;
        this.userExperienceRepository = userExperienceRepository;
        this.userSkillRepository = userSkillRepository;
        this.plRepository = plRepository;
        this.certificateRepository = certificateRepository;
        this.documentRepository = documentRepository;
        this.geocodeCacheRepository = geocodeCacheRepository;
        this.interviewBookingRepository = interviewBookingRepository;
        this.interviewSessionRepository = interviewSessionRepository;
    }

    public AnalyticsDtos.UserAnalyticsResponse buildUserAnalytics(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        List<UserEducation> education = userEducationRepository.findByUserId(userId);
        List<UserExperience> experience = userExperienceRepository.findByUserId(userId);
        List<UserSkill> skills = userSkillRepository.findByUserId(userId);
        List<UserProgrammingLanguage> pls = plRepository.findByUserId(userId);
        List<UserCertificate> certs = certificateRepository.findByUserId(userId);
        List<UserDocument> docs = documentRepository.findByUserId(userId);

        AnalyticsDtos.UserAnalyticsResponse r = new AnalyticsDtos.UserAnalyticsResponse();
        r.setUserId(userId);
        if (user != null) {
            r.setUsername(user.getUsername());
        }
        r.setDisplayName(buildDisplayName(profile));
        if (profile != null) {
            r.setCurrentCourse(profile.getCurrentCourse());
        }

        r.setEducationCount(education.size());
        r.setExperienceCount(experience.size());
        r.setSkillCount(skills.size());
        r.setProgrammingLanguageCount(pls.size());
        r.setCertificateCount(certs.size());
        r.setDocumentCount(docs.size());

        r.setProfileCompleteness(computeCompleteness(profile, education, experience, skills, pls, certs, docs));
        r.setAverageCgpa(averageCgpa(education));
        r.setEducationTrend(buildEducationTrend(education));
        r.setEducationTrendsByLevel(buildEducationTrendsByLevel(education));
        r.setSkillLevelDistribution(skillLevelBuckets(skills));
        r.setProgrammingLanguages(pls.stream()
            .map(p -> new AnalyticsDtos.NamedValue(
                Optional.ofNullable(p.getLanguageName()).orElse("Unknown"), 1.0))
            .collect(Collectors.toList()));

        List<AnalyticsDtos.NamedValue> sections = new ArrayList<>();
        sections.add(new AnalyticsDtos.NamedValue("Education", education.size()));
        sections.add(new AnalyticsDtos.NamedValue("Experience", experience.size()));
        sections.add(new AnalyticsDtos.NamedValue("Skills", skills.size()));
        sections.add(new AnalyticsDtos.NamedValue("Languages", pls.size()));
        sections.add(new AnalyticsDtos.NamedValue("Certificates", certs.size()));
        sections.add(new AnalyticsDtos.NamedValue("Documents", docs.size()));
        r.setSectionCounts(sections);

        r.setInsights(buildInsights(r));
        applyInterviewScores(r, userId);

        Map<Integer, double[]> cohortVectors = new LinkedHashMap<>();
        for (User u : userRepository.findAll()) {
            cohortVectors.put(u.getId(), rawRadarVector(u.getId()));
        }
        double[] cohortMean = meanRadar(cohortVectors.values());
        double[] userVec = rawRadarVector(userId);
        r.setRadarCompare(buildRadarCompare(userVec, cohortMean));
        r.setPercentileCompare(buildPercentileCompare(userVec, cohortVectors.values()));

        return r;
    }

    public AnalyticsDtos.TalentPoolResponse buildTalentPool() {
        AnalyticsDtos.TalentPoolResponse pool = new AnalyticsDtos.TalentPoolResponse();
        Map<Integer, double[]> vectors = new LinkedHashMap<>();
        for (User u : userRepository.findAll()) {
            AnalyticsDtos.TalentUserSummary row = new AnalyticsDtos.TalentUserSummary();
            row.setUserId(u.getId());
            row.setUsername(u.getUsername());
            UserProfile p = userProfileRepository.findByUserId(u.getId()).orElse(null);
            row.setDisplayName(buildDisplayName(p));
            List<UserEducation> edu = userEducationRepository.findByUserId(u.getId());
            List<UserExperience> exp = userExperienceRepository.findByUserId(u.getId());
            List<UserSkill> sk = userSkillRepository.findByUserId(u.getId());
            List<UserProgrammingLanguage> pl = plRepository.findByUserId(u.getId());
            List<UserCertificate> ce = certificateRepository.findByUserId(u.getId());
            List<UserDocument> doc = documentRepository.findByUserId(u.getId());
            row.setEducationCount(edu.size());
            row.setExperienceCount(exp.size());
            row.setCertificateCount(ce.size());
            row.setTotalSkillItems(sk.size() + pl.size());
            row.setAverageCgpa(averageCgpa(edu));
            row.setProfileCompleteness(computeCompleteness(p, edu, exp, sk, pl, ce, doc));
            applyInterviewScores(row, u.getId());
            pool.getUsers().add(row);
            vectors.put(u.getId(), rawRadarVector(u.getId()));
        }
        pool.getUsers().sort(Comparator.comparingInt(AnalyticsDtos.TalentUserSummary::getProfileCompleteness).reversed());
        AnalyticsDtos.RadarCompare cohort = new AnalyticsDtos.RadarCompare();
        cohort.setLabels(Arrays.asList("Profile", "Academic", "Skills", "Experience", "Credentials"));
        double[] mean = meanRadar(vectors.values());
        for (double v : mean) {
            cohort.getCohortScores().add(round2(v));
        }
        for (double v : mean) {
            cohort.getUserScores().add(round2(v));
        }
        pool.setCohortRadarAverages(cohort);
        return pool;
    }

    public AnalyticsDtos.LocationAnalyticsResponse buildLocationAnalytics() {
        AnalyticsDtos.LocationAnalyticsResponse out = new AnalyticsDtos.LocationAnalyticsResponse();
        List<UserProfile> profiles = userProfileRepository.findAll();
        Map<Integer, String> usernames = userRepository.findAll().stream()
            .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));

        class MutablePoint {
            String country;
            String state;
            String city;
            double lat;
            double lng;
            List<Integer> userIds = new ArrayList<>();
            List<String> usernames = new ArrayList<>();
        }

        Map<String, Integer> countryCounts = new HashMap<>();
        Map<String, Integer> stateCounts = new HashMap<>();
        Map<String, Integer> cityCounts = new HashMap<>();
        Map<String, MutablePoint> points = new LinkedHashMap<>();
        int locatedUsers = 0;

        for (UserProfile p : profiles) {
            if (p == null || !hasText(p.getCountry()) || !hasText(p.getState()) || !hasText(p.getCity())) continue;
            String country = p.getCountry().trim();
            String state = p.getState().trim();
            String city = p.getCity().trim();

            countryCounts.merge(country, 1, Integer::sum);
            stateCounts.merge(state, 1, Integer::sum);
            cityCounts.merge(city, 1, Integer::sum);

            Optional<GeocodeCache> geo = geocodeFor(country, state, city);
            if (geo.isEmpty()) continue;

            GeocodeCache g = geo.get();
            String key = g.getLat() + "," + g.getLng() + "|" + country + "|" + state + "|" + city;
            MutablePoint pt = points.computeIfAbsent(key, k -> {
                MutablePoint m = new MutablePoint();
                m.country = country;
                m.state = state;
                m.city = city;
                m.lat = g.getLat();
                m.lng = g.getLng();
                return m;
            });
            locatedUsers++;
            if (p.getUserId() != null) {
                pt.userIds.add(p.getUserId());
                String un = usernames.get(p.getUserId());
                if (hasText(un)) pt.usernames.add(un);
            }
        }

        out.setTotalLocatedUsers(locatedUsers);
        out.setCountries(sortedLocationCounts(countryCounts));
        out.setStates(sortedLocationCounts(stateCounts));
        out.setCities(sortedLocationCounts(cityCounts));
        out.setPoints(points.values().stream().map(m -> {
            AnalyticsDtos.LocationPoint p = new AnalyticsDtos.LocationPoint();
            p.setCountry(m.country);
            p.setState(m.state);
            p.setCity(m.city);
            p.setLat(m.lat);
            p.setLng(m.lng);
            p.setUserIds(m.userIds);
            p.setUsernames(m.usernames);
            p.setCount(m.userIds.size());
            return p;
        }).collect(Collectors.toList()));
        return out;
    }

    private List<AnalyticsDtos.LocationCount> sortedLocationCounts(Map<String, Integer> counts) {
        return counts.entrySet().stream()
            .map(e -> new AnalyticsDtos.LocationCount(e.getKey(), e.getValue()))
            .sorted((a, b) -> {
                if (a.getCount() != b.getCount()) return Integer.compare(b.getCount(), a.getCount());
                return String.valueOf(a.getName()).compareToIgnoreCase(String.valueOf(b.getName()));
            })
            .collect(Collectors.toList());
    }

    private Optional<GeocodeCache> geocodeFor(String country, String state, String city) {
        String key = (country + "|" + state + "|" + city).trim().toLowerCase();
        Optional<GeocodeCache> cached = geocodeCacheRepository.findByQueryKey(key);
        if (cached.isPresent()) return cached;

        try {
            String q = URLEncoder.encode(city + ", " + state + ", " + country, StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q=" + q;
            HttpURLConnection conn = (HttpURLConnection) new java.net.URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "InternPro/1.0 (admin-location-analytics)");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(5000);

            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) return Optional.empty();

            try (InputStream is = conn.getInputStream()) {
                JsonNode arr = objectMapper.readTree(is);
                if (arr == null || !arr.isArray() || arr.isEmpty()) return Optional.empty();
                JsonNode row = arr.get(0);
                if (row == null || row.get("lat") == null || row.get("lon") == null) return Optional.empty();
                GeocodeCache g = new GeocodeCache();
                g.setQueryKey(key);
                g.setCountry(country);
                g.setState(state);
                g.setCity(city);
                g.setLat(row.get("lat").asDouble());
                g.setLng(row.get("lon").asDouble());
                g.setResolvedAt(LocalDateTime.now());
                return Optional.of(geocodeCacheRepository.save(g));
            }
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static AnalyticsDtos.RadarCompare buildRadarCompare(double[] user, double[] cohortMean) {
        AnalyticsDtos.RadarCompare rc = new AnalyticsDtos.RadarCompare();
        rc.setLabels(Arrays.asList("Profile", "Academic", "Skills", "Experience", "Credentials"));
        for (int i = 0; i < 5; i++) {
            rc.getUserScores().add(round2(user[i]));
            rc.getCohortScores().add(round2(cohortMean[i]));
        }
        return rc;
    }

    private static AnalyticsDtos.RadarCompare buildPercentileCompare(double[] user, Collection<double[]> cohortRows) {
        AnalyticsDtos.RadarCompare rc = new AnalyticsDtos.RadarCompare();
        rc.setLabels(Arrays.asList("Profile", "Academic", "Skills", "Experience", "Credentials"));
        for (int i = 0; i < 5; i++) {
            double p = percentile(user[i], cohortRows, i);
            rc.getUserScores().add(round2(p));
            rc.getCohortScores().add(50.0); // median baseline
        }
        return rc;
    }

    private static double percentile(double value, Collection<double[]> rows, int idx) {
        if (rows == null || rows.isEmpty()) return 0;
        int n = 0;
        int less = 0;
        int equal = 0;
        for (double[] r : rows) {
            if (r == null || r.length <= idx) continue;
            n++;
            if (r[idx] < value) less++;
            else if (r[idx] == value) equal++;
        }
        if (n == 0) return 0;
        // mid-rank percentile
        return 100.0 * (less + 0.5 * equal) / n;
    }

    private double[] rawRadarVector(Integer userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        List<UserEducation> education = userEducationRepository.findByUserId(userId);
        List<UserExperience> experience = userExperienceRepository.findByUserId(userId);
        List<UserSkill> skills = userSkillRepository.findByUserId(userId);
        List<UserProgrammingLanguage> pls = plRepository.findByUserId(userId);
        List<UserCertificate> certs = certificateRepository.findByUserId(userId);
        List<UserDocument> docs = documentRepository.findByUserId(userId);

        int completeness = computeCompleteness(profile, education, experience, skills, pls, certs, docs);
        Double avgCgpa = averageCgpa(education);
        double academic = avgCgpa == null ? 0 : Math.min(100, avgCgpa * 10.0);
        double skillBreadth = Math.min(100, (skills.size() + pls.size()) * 12.5);
        double expScore = Math.min(100, experience.size() * 25.0);
        double credScore = Math.min(100, (certs.size() + docs.size()) * 15.0);

        return new double[] { completeness, academic, skillBreadth, expScore, credScore };
    }

    private static double[] meanRadar(Collection<double[]> rows) {
        double[] sum = new double[5];
        int n = 0;
        for (double[] r : rows) {
            if (r == null || r.length < 5) continue;
            for (int i = 0; i < 5; i++) sum[i] += r[i];
            n++;
        }
        if (n == 0) return new double[5];
        for (int i = 0; i < 5; i++) sum[i] /= n;
        return sum;
    }

    private static double round2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static String buildDisplayName(UserProfile p) {
        if (p == null) return "";
        String s = String.join(" ",
            Optional.ofNullable(p.getFirstName()).orElse(""),
            Optional.ofNullable(p.getMiddleName()).orElse(""),
            Optional.ofNullable(p.getLastName()).orElse("")).trim();
        return s.isEmpty() ? "" : s;
    }

    private static int computeCompleteness(UserProfile p, List<UserEducation> education,
                                          List<UserExperience> experience, List<UserSkill> skills,
                                          List<UserProgrammingLanguage> pls,
                                          List<UserCertificate> certs, List<UserDocument> docs) {
        int score = 0;
        if (p != null) {
            if (hasText(p.getFirstName()) && hasText(p.getLastName())) score += 12;
            if (hasText(p.getContactNumber())) score += 10;
            if (hasText(p.getGender()) && p.getDob() != null) score += 8;
            if (hasText(p.getCity()) && hasText(p.getCountry())) score += 10;
            if (hasText(p.getLinkedinUrl())) score += 8;
            if (hasText(p.getGithubUrl())) score += 8;
            if (hasText(p.getProfileImage())) score += 8;
            if (hasText(p.getCurrentCourse())) score += 8;
        }
        if (!education.isEmpty()) score += 10;
        if (!experience.isEmpty()) score += 8;
        if (!skills.isEmpty() || !pls.isEmpty()) score += 10;
        if (!certs.isEmpty()) score += 5;
        if (!docs.isEmpty()) score += 5;
        return Math.min(100, score);
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private static Double averageCgpa(List<UserEducation> education) {
        List<BigDecimal> list = education.stream()
            .map(UserEducation::getCgpa)
            .filter(Objects::nonNull)
            .toList();
        if (list.isEmpty()) return null;
        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(list.size()), 2, RoundingMode.HALF_UP).doubleValue();
    }

    private static double educationLevelWeight(String educationLevel) {
        if (educationLevel == null) return 1.0;
        String s = educationLevel.trim().toLowerCase();
        String n = s.replaceAll("[^a-z0-9]", "");
        if (n.contains("mtech")) return 1.2;
        if (n.contains("btech")) return 1.1;
        if (n.contains("diploma")) return 0.9;
        return 1.0;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private static List<AnalyticsDtos.EducationChartPoint> buildEducationTrend(List<UserEducation> education) {
        return education.stream()
            .filter(e -> e.getSemester() != null || e.getCgpa() != null || pctFromMarks(e) != null)
            .sorted((a, b) -> {
                // Prefer chronological ordering when startYear exists (past education),
                // otherwise fallback to course importance (M.Tech > B.Tech > Diploma),
                // then semester number.
                Integer aStart = a.getStartYear() != null ? a.getStartYear().getValue() : null;
                Integer bStart = b.getStartYear() != null ? b.getStartYear().getValue() : null;
                if (aStart != null && bStart != null && !aStart.equals(bStart)) {
                    return aStart.compareTo(bStart);
                } else if (aStart != null && bStart == null) {
                    return -1;
                } else if (aStart == null && bStart != null) {
                    return 1;
                }

                double wA = educationLevelWeight(a.getEducationLevel());
                double wB = educationLevelWeight(b.getEducationLevel());
                if (wA != wB) return Double.compare(wB, wA);

                int aSem = Optional.ofNullable(a.getSemester()).orElse(999);
                int bSem = Optional.ofNullable(b.getSemester()).orElse(999);
                return Integer.compare(aSem, bSem);
            })
            .map(e -> {
                AnalyticsDtos.EducationChartPoint pt = new AnalyticsDtos.EducationChartPoint();
                pt.setSemester(e.getSemester());

                double w = educationLevelWeight(e.getEducationLevel());
                if (e.getCgpa() != null) {
                    double weighted = e.getCgpa().doubleValue() * w;
                    pt.setCgpa(clamp(weighted, 0, 10));
                }
                Double pct = pctFromMarks(e);
                if (pct != null) {
                    pt.setPercentage(clamp(pct * w, 0, 100));
                } else {
                    pt.setPercentage(null);
                }

                String sem = e.getSemester() != null ? "Sem " + e.getSemester() : "Row";
                String lvl = (e.getEducationLevel() != null && !e.getEducationLevel().isBlank()) ? e.getEducationLevel() : null;
                pt.setLabel(lvl != null ? lvl + " " + sem : sem);
                return pt;
            })
            .collect(Collectors.toList());
    }

    private static List<AnalyticsDtos.EducationTrendSeries> buildEducationTrendsByLevel(List<UserEducation> education) {
        if (education == null || education.isEmpty()) return List.of();

        Map<String, List<UserEducation>> grouped = new LinkedHashMap<>();
        for (UserEducation e : education) {
            if (e == null) continue;
            if (e.getSemester() == null && e.getCgpa() == null && pctFromMarks(e) == null) continue;
            String lvl = Optional.ofNullable(e.getEducationLevel()).orElse("").trim();
            String key = normalizeEduLevel(lvl);
            if (key.isEmpty()) key = "other";
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
        }

        List<Map.Entry<String, List<UserEducation>>> entries = new ArrayList<>(grouped.entrySet());
        entries.sort((a, b) -> {
            double wA = educationLevelWeight(a.getValue().stream().findFirst().map(UserEducation::getEducationLevel).orElse(null));
            double wB = educationLevelWeight(b.getValue().stream().findFirst().map(UserEducation::getEducationLevel).orElse(null));
            if (wA != wB) return Double.compare(wB, wA);
            return a.getKey().compareToIgnoreCase(b.getKey());
        });

        List<AnalyticsDtos.EducationTrendSeries> out = new ArrayList<>();
        for (Map.Entry<String, List<UserEducation>> en : entries) {
            List<UserEducation> rows = en.getValue();
            if (rows == null || rows.isEmpty()) continue;

            // Keep unweighted points per level (so charts reflect real CGPA/% per degree)
            List<AnalyticsDtos.EducationChartPoint> pts = rows.stream()
                .sorted((a, b) -> {
                    Integer aStart = a.getStartYear() != null ? a.getStartYear().getValue() : null;
                    Integer bStart = b.getStartYear() != null ? b.getStartYear().getValue() : null;
                    if (aStart != null && bStart != null && !aStart.equals(bStart)) return aStart.compareTo(bStart);
                    if (aStart != null && bStart == null) return -1;
                    if (aStart == null && bStart != null) return 1;
                    int aSem = Optional.ofNullable(a.getSemester()).orElse(999);
                    int bSem = Optional.ofNullable(b.getSemester()).orElse(999);
                    return Integer.compare(aSem, bSem);
                })
                .map(e -> {
                    AnalyticsDtos.EducationChartPoint pt = new AnalyticsDtos.EducationChartPoint();
                    pt.setSemester(e.getSemester());
                    if (e.getCgpa() != null) pt.setCgpa(clamp(e.getCgpa().doubleValue(), 0, 10));
                    Double pct = pctFromMarks(e);
                    pt.setPercentage(pct != null ? clamp(pct, 0, 100) : null);
                    String sem = e.getSemester() != null ? "Sem " + e.getSemester() : "Row";
                    pt.setLabel(sem);
                    return pt;
                })
                .collect(Collectors.toList());

            if (pts.isEmpty()) continue;
            AnalyticsDtos.EducationTrendSeries s = new AnalyticsDtos.EducationTrendSeries();
            String anyLvl = rows.stream().map(UserEducation::getEducationLevel).filter(ProfileAnalyticsService::hasText).findFirst().orElse("");
            s.setEducationLevel(prettyEduLevel(anyLvl));
            s.setPoints(pts);
            out.add(s);
        }
        return out;
    }

    private static String normalizeEduLevel(String educationLevel) {
        if (educationLevel == null) return "";
        return educationLevel.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private static String prettyEduLevel(String educationLevel) {
        String n = normalizeEduLevel(educationLevel);
        if (n.contains("mtech")) return "M.Tech";
        if (n.contains("btech")) return "B.Tech";
        if (n.contains("diploma")) return "Diploma";
        String s = educationLevel == null ? "" : educationLevel.trim();
        return s.isEmpty() ? "Education" : s;
    }

    private static Double pctFromMarks(UserEducation e) {
        if (e.getTotalMarks() == null || e.getTotalMarks() <= 0 || e.getMarksObtained() == null) return null;
        return 100.0 * e.getMarksObtained() / e.getTotalMarks();
    }

    private static List<AnalyticsDtos.NamedValue> skillLevelBuckets(List<UserSkill> skills) {
        Map<String, Long> counts = skills.stream()
            .map(s -> Optional.ofNullable(s.getSkillLevel()).filter(x -> !x.isBlank()).orElse("Unspecified"))
            .collect(Collectors.groupingBy(x -> x, Collectors.counting()));
        return counts.entrySet().stream()
            .map(e -> new AnalyticsDtos.NamedValue(e.getKey(), e.getValue().doubleValue()))
            .sorted(Comparator.comparing(AnalyticsDtos.NamedValue::getName))
            .collect(Collectors.toList());
    }

    private static List<String> buildInsights(AnalyticsDtos.UserAnalyticsResponse r) {
        List<String> out = new ArrayList<>();
        if (r.getProfileCompleteness() >= 85) {
            out.add("Strong profile completeness — you are ahead of most applicants.");
        } else if (r.getProfileCompleteness() < 40) {
            out.add("Add education, experience, and links to raise your profile score.");
        }
        if (r.getAverageCgpa() != null) {
            if (r.getAverageCgpa() >= 8.0) {
                out.add("Academic average (CGPA) is strong — highlight key semesters in interviews.");
            } else if (r.getAverageCgpa() < 6.0) {
                out.add("Consider adding certifications or projects to complement academic record.");
            }
        } else if (r.getEducationCount() > 0) {
            out.add("Add CGPA or marks to education rows to unlock academic trend charts.");
        }
        if (r.getProgrammingLanguageCount() == 0 && r.getSkillCount() > 0) {
            out.add("Listing programming languages improves technical visibility to recruiters.");
        }
        if (r.getExperienceCount() == 0 && r.getEducationCount() > 0) {
            out.add("Internships or projects under Experience increase comparison strength.");
        }
        if (r.getCertificateCount() + r.getDocumentCount() == 0) {
            out.add("Upload certificates or documents to verify achievements.");
        }
        if (out.isEmpty()) {
            out.add("Keep your profile updated after each semester or project.");
        }
        return out;
    }

    private void applyInterviewScores(AnalyticsDtos.UserAnalyticsResponse r, Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;
        List<InterviewBooking> bookings = interviewBookingRepository.findByUser(user);
        List<InterviewSession> sessions = bookings == null ? List.of() : bookings.stream()
            .map(b -> interviewSessionRepository.findByBooking(b).orElse(null))
            .filter(Objects::nonNull)
            .toList();
        r.setCommunicationScore(avgSessionScore(sessions, "communication"));
        r.setTechnicalScore(avgSessionScore(sessions, "technical"));
        r.setBehavioralScore(avgSessionScore(sessions, "behavioral"));
        r.setInterviewFinalScore(avgSessionScore(sessions, "final"));
    }

    private void applyInterviewScores(AnalyticsDtos.TalentUserSummary r, Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;
        List<InterviewBooking> bookings = interviewBookingRepository.findByUser(user);
        List<InterviewSession> sessions = bookings.stream()
            .map(b -> interviewSessionRepository.findByBooking(b).orElse(null))
            .filter(Objects::nonNull)
            .toList();
        r.setCommunicationScore(avgSessionScore(sessions, "communication"));
        r.setTechnicalScore(avgSessionScore(sessions, "technical"));
        r.setBehavioralScore(avgSessionScore(sessions, "behavioral"));
        r.setInterviewFinalScore(avgSessionScore(sessions, "final"));
    }

    private static Double avgSessionScore(List<InterviewSession> sessions, String type) {
        List<BigDecimal> values = sessions.stream().map(s -> {
            return switch (type) {
                case "communication" -> s.getCommunicationScore();
                case "technical" -> s.getTechnicalScore();
                case "behavioral" -> s.getBehavioralScore();
                default -> s.getFinalScore();
            };
        }).filter(Objects::nonNull).toList();
        if (values.isEmpty()) return null;
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP).doubleValue();
    }
}
