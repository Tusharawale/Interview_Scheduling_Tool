### Interview Scheduling Tool


<img width="353" height="353" alt="image" src="https://github.com/user-attachments/assets/3f4a29e1-cad3-4c42-a9d8-fdde93363a26" />


![Project Banner](https://img.shields.io/badge/Interview%20Scheduling%20Tool-blueviolet?style=for-the-badge&logo=appveyor)

## 📌 Overview

Recruitment processes are often fragmented across multiple tools—forms, spreadsheets, emails, and meeting platforms—leading to inefficiencies, inconsistent evaluations, and delays.

**Interview Scheduling Tool** eliminates this fragmentation by providing a **single, integrated system** that manages the complete recruitment lifecycle:

- Candidate onboarding
- Interview scheduling
- Real-time interview execution
- Evaluation and analytics

---

## 🎯 Key Features

### 👨‍🎓 Candidate Module

- Secure registration with email verification
- Authentication & login system
- Profile management:
  - Education
  - Experience
  - Skills
  - Certifications
  - Document uploads
- Interview slot booking

### 🛠️ Admin Module

- User lifecycle management
- Interview scheduling & coordination
- Booking supervision
- Candidate tracking dashboard
- Analytics & report generation

---

## 🎥 Real-Time Interview System

- **WebRTC-based video/audio communication**
- **WebSocket + STOMP** for:
  - Signaling
  - Chat
  - Typing indicators
  - Presence tracking

### 🧠 AI-Powered Insights

- Speech-to-text transcription (Whisper-compatible service)
- Communication analytics:
  - Speaking rate
  - Filler word frequency
- Evidence-based evaluation system

---

## 💻 Technical Assessment

- Coding challenge support
- Integration with **Judge0 API** (when available)
- Fallback deterministic code evaluation engine

---

## 📊 Smart Evaluation System

Candidate ranking based on configurable weights:

- Communication skills
- Technical performance
- Behavioral analysis
- Profile strength

---

## 📄 Reporting

- Automated PDF report generation
- Optional email notifications
- Interview transcripts storage

---

## 🏗️ Architecture

This project follows a **Modular Monolithic Architecture**, ensuring:

- Simplicity in deployment
- High maintainability
- Scalability for future enhancements

---

## ⚙️ Tech Stack

### 🔙 Backend

- Java 17
- Spring Boot 3.3
- Spring Data JPA
- WebSocket (STOMP)
- REST APIs

### 🗄️ Database

- MySQL

### 🌐 Frontend

- HTML5
- CSS3
- JavaScript (Vanilla)

### 🔗 Real-Time & Media

- WebRTC
- WebSocket

### 🧠 AI & Processing

- Whisper (Speech-to-Text)
- Custom analytics engine

### 🧪 Code Evaluation

- Judge0 API (optional)
- Internal evaluation engine

---

## 📦 Deployment Model

- Single artifact (JAR)
- Backend serves frontend (no separate deployment)
- Ideal for:
  - Academic institutions
  - Small-scale enterprise setups

---


## 🔮 Future Enhancements

- 🔐 JWT-based authentication & authorization
- 🛡️ Object-level access control
- ⚡ Asynchronous processing (RabbitMQ / Kafka)
- ☁️ Migration to cloud object storage (AWS S3 / GCP)
- 📈 Advanced monitoring & logging (Prometheus, Grafana)
- 📊 Enhanced analytics dashboard
- 🤖 AI-based candidate recommendation system

---

## 📷 Screenshots

> Add your images inside an `images/` folder in your repo

### 4.1 Layered architecture (diagram)

```mermaid
flowchart TB
  subgraph Client["Browser client"]
    P[Static HTML/CSS/JS]
    LS[(localStorage / sessionStorage)]
  end

  subgraph Spring["Spring Boot monolith"]
    MVC[Spring MVC + Security]
    C[REST controllers]
    S[Services]
    R[JPA repositories]
    FS[FileStorageService]
    WS[WebSocket STOMP]
  end

  DB[(MySQL)]
  DISK[(Local upload folder)]
  P --> MVC
  LS -.->|user id in API calls| C
  MVC --> C
  C --> S
  S --> R
  R --> DB
  S --> FS
  FS --> DISK
  C --> WS
```

### 4.2 User journey (high level)

```mermaid
flowchart LR
  A[Register] --> B[Verify email]
  B --> C[Login]
  C --> D[Dashboard user.html]
  D --> E[Edit Profile form2.html]
  D --> F[Book interview]
  D --> G[Meeting call + WS]
  E --> H[(MySQL profile + files)]
  F --> I[(interview_bookings)]
```

### 4.3 Admin journey (high level)

```mermaid
flowchart LR
  A[Admin login] --> B[admin.html]
  B --> C[Manage users]
  B --> D[Manage interview slots]
  B --> E[View user profile modal]
  C --> F[(users flags / delete)]
  D --> G[(interview_slots / bookings)]
  E --> H[GET /api/users/id/profile]
```

### 4.4 Profile save and file storage (sequence)

```mermaid
sequenceDiagram
  participant U as User browser
  participant API as ProfileCrudController
  participant PCS as ProfileCrudService
  participant FS as FileStorageService
  participant DB as MySQL
  participant D as Local disk

  U->>API: PUT /api/users/{id}/profile JSON
  API->>PCS: saveProfileDetails
  PCS->>DB: upsert user_profile
  DB-->>PCS: ok
  PCS-->>API: ok
  API->>DB: read aggregate via UserProfileService
  API-->>U: ProfileResponse JSON

  U->>API: POST .../documents multipart
  API->>FS: store(file, documents/userId)
  FS->>D: write file
  D-->>FS: relative path
  FS-->>API: storage key
  API->>PCS: saveDocument metadata
  PCS->>DB: insert user_documents
  API-->>U: ProfileResponse

  U->>API: GET /api/files/{key}
  API->>FS: loadAsResource(key)
  FS->>D: read stream
  FS-->>U: bytes + Content-Type
```

### 4.5 Main modules (dependency view)

```mermaid
graph LR
  UC[UserController]
  AC[AdminController]
  IC[InterviewController]
  AIC[AdminInterviewController]
  PC[ProfileCrudController]
  FC[FileController]
  MC[MeetingController]
  MWC[MeetingWsController]

  US[UserService]
  AS[AdminService]
  IS[InterviewService]
  PCS[ProfileCrudService]
  UPS[UserProfileService]
  FS[FileStorageService]
  ES[EmailService]

  UC --> US
  AC --> AS
  IC --> IS
  AIC --> IS
  PC --> PCS
  PC --> UPS
  PC --> FS
  FC --> FS
  MC --> ES
  PC --> ES
```

### 4.6 Feature areas (illustrative)

The following chart is **not** a runtime metric; it groups major capabilities for onboarding and planning (e.g. charts/analytics later).

```mermaid
pie showData
    title Capabilities by functional area (illustrative)
    "User auth & account" : 1
    "Profile & files" : 1
    "Interviews" : 1
    "Administration" : 1
    "Meetings & realtime" : 1
```

---

## 📂 Project Structure

```
Interview-Scheduling-Tool/
|---main
    +---java
    |   \---com
    |       \---example
    |           \---authadmin
    |               |   Application.java
    |               |
    |               +---config
    |               |       DataLoader.java
    |               |       GlobalExceptionHandler.java
    |               |       SecurityConfig.java
    |               |       WebSocketConfig.java
    |               |       WebSocketEvents.java
    |               |
    |               +---controller
    |               |       AdminController.java
    |               |       AdminInterviewController.java
    |               |       FileController.java
    |               |       HomeController.java
    |               |       InterviewController.java
    |               |       MeetingController.java
    |               |       MeetingWsController.java
    |               |       ProfileCrudController.java
    |               |       UserController.java
    |               |       ProfileAnalyticsController.java
    |               |       InterviewUpgradeController.java
    |               |       MeetingSttController.java
    |               |
    |               +---dto
    |               |       AdminDtos.java
    |               |       InterviewDtos.java
    |               |       MeetingDtos.java
    |               |       ProfileDtos.java
    |               |       ProfileRequestDtos.java
    |               |       UserDtos.java
    |               |       AnalyticsDtos.java
    |               |       InterviewUpgradeDtos.java
    |               |
    |               +---entity
    |               |       Admin.java
    |               |       InterviewBooking.java
    |               |       InterviewSlot.java
    |               |       User.java
    |               |       UserCertificate.java
    |               |       UserDocument.java
    |               |       UserEducation.java
    |               |       UserExperience.java
    |               |       UserProfile.java
    |               |       UserProgrammingLanguage.java
    |               |       UserSkill.java
    |               |       MeetingChatMessage.java
    |               |       GeocodeCache.java
    |               |       AdminEmailLog.java
    |               |       InterviewSession.java
    |               |       RankingWeight.java
    |               |       CodingChallenge.java
    |               |       CodingSubmission.java
    |               |       FinalReport.java
    |               |
    |               +---repository
    |               |       AdminRepository.java
    |               |       InterviewBookingRepository.java
    |               |       InterviewSlotRepository.java
    |               |       UserCertificateRepository.java
    |               |       UserDocumentRepository.java
    |               |       UserEducationRepository.java
    |               |       UserExperienceRepository.java
    |               |       UserProfileRepository.java
    |               |       UserProgrammingLanguageRepository.java
    |               |       UserRepository.java
    |               |       UserSkillRepository.java
    |               |       MeetingChatRepository.java
    |               |       GeocodeCacheRepository.java
    |               |       AdminEmailLogRepository.java
    |               |       InterviewSessionRepository.java
    |               |       RankingWeightRepository.java
    |               |       CodingChallengeRepository.java
    |               |       CodingSubmissionRepository.java
    |               |       FinalReportRepository.java
    |               |
    |               \---service
    |                       AdminService.java
    |                       EmailService.java
    |                       FileStorageService.java
    |                       InterviewService.java
    |                       MeetingPresenceService.java
    |                       MeetingService.java
    |                       MeetingStateService.java
    |                       ProfileCrudService.java
    |                       UserProfileService.java
    |                       UserService.java
    |                       MeetingAdminTokenService.java
    |                       MeetingChatService.java
    |                       MeetingChatRateLimiter.java
    |                       MeetingRtcConfigService.java
    |                       ProfileAnalyticsService.java
    |                       AnalyticsStompPublisher.java
    |                       InterviewUpgradeService.java
    |                       MeetingSttService.java
    |
    \---resources
        |   application-mysql.properties
        |   application.properties
        |   data.sql
        |   schema-migration.sql
        |   schema.sql
        |
        \---static
            |   login.html
            |   register.html
            |
            +---common
            |       meeting.js
            |       profile-analytics.js
            |
            +---user
            |   |   user.html
            |   |   user-profile.html
            |   |   user-profile-edit.html
            |   |   user-live-coding.html
            |   |   user-Meeting Call.html
            |   |   user-appointments.html
            |   |
            |   +---css
            |   |       User-Chat-Metting.css
            |   |       user-dashboard.css
            |   |       User-metting.css
            |   |       user-profile-edit.css
            |   |
            |   +---js
            |   |       User-Chat-Metting.js
            |   |       user-dashboard.js
            |   |       User-metting.js
            |   |       user-profile-edit.js
            |   |
            |   \---profile-form
            |           college.js
            |           education.js
            |           experience.js
            |           form.css
            |           form2.html
            |           imageUpload.js
            |           programmingLanguages.js
            |           README.txt
            |           semesterFields.js
            |           skills.js
            |
            \---admin
                |   admin.html
                |   admin-appointments.html
                |   admin-fix-appointment.html
                |   admin-settings.html
                |   admin-meeting.html
                |   admin-talent.html
                |   admin-login.html
                |
                +---css
                |       admin-dashboard.css
                |       Admin-metting.css
                |       Chat-Metting.css
                |
                \---js
                        admin-dashboard.js
                        admin-metting.js
                        Chat-Metting.js
                        admin-fix-appointment.js
|    │── pom.xml
│── README.md
```

---

##  How to Run

```bash
# Clone the repository
git clone https://github.com/Tusharawale/interview-scheduling-tool.git

# Navigate to project
cd interview-scheduling-tool

# Run the application
mvn spring-boot:run
```

---

##  Why This Project Stands Out


---

##  Contribution



---

##  License


---

##  Author

**Tushar Awale**  
B.Tech (Computer Technology)

---


