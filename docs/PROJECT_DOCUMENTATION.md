# Project Documentation — Project4

## Project Overview

### Problem

Campus and recruitment workflows need a single place where candidates maintain rich profiles, book interview slots, join live technical discussions, and where administrators schedule interviews, observe participation, and compare candidates using consistent scoring. Manual coordination via email and spreadsheets does not scale and makes fair comparison difficult.

### Solution

**Project4** is a monolithic **Spring Boot** web application that provides:

- User registration with **email verification**, bcrypt-backed login, and a detailed **candidate profile** (education, experience, skills, certificates, documents, photo).
- **Interview scheduling**: admins define slots; users book and cancel; calendar views for admins.
- **Live meetings**: **WebRTC** mesh signaling over **STOMP WebSocket**, in-meeting chat and presence, optional **speech-to-text** via an external **Whisper-compatible HTTP** service.
- **Interview analytics**: transcript-based **communication scoring** (rate, filler words, derived clarity/confidence-style metrics), **configurable ranking weights**, **live coding** challenges with optional **Judge0** execution, and **PDF final reports** with charts.

### Target users

- **Candidates / students**: build profile, book interviews, join meetings, complete coding tasks, view analytics.
- **Administrators / recruiters**: manage users, slots, meetings, talent analytics, email outreach, ranking and reports.

---

## Technical Stack

| Layer | Technology | Why it fits |
|-------|------------|-------------|
| Backend | **Spring Boot 3.3**, **Java 17** | One process for REST, WebSocket (STOMP), JPA, mail, validation; strong ecosystem and hiring demand. |
| Security (crypto) | **BCrypt** (via `spring-security-crypto`) | Password hashing without custom crypto. |
| API | **Spring Web**, **Jakarta Validation** | Declarative validation on DTOs (`@Valid`). |
| Real-time | **Spring WebSocket** + **STOMP** broker | Browser-friendly SockJS endpoint `/ws`, topic broadcast for meeting events. |
| Persistence | **Spring Data JPA**, **MySQL** | Relational model for users, bookings, sessions, rankings; portable SQL via `schema.sql` / migrations. |
| Mail | **Spring Mail** | Verification and meeting notifications. |
| PDF / charts | **XChart**, **OpenPDF** / **iText** | Server-side chart images embedded in PDF reports. |
| Frontend | **Static HTML + JavaScript** under `src/main/resources/static` | No separate Node build; fast to deploy behind Spring; pages for user and admin. |
| External | **Whisper HTTP** (configurable), **Judge0** (optional) | Pluggable STT and code execution without embedding heavy runtimes in the JVM. |

**Comparison notes**

- **Monolith vs microservices**: A single deployable JAR matches thesis scope and reduces operational overhead; services are separated by packages (`controller` / `service` / `repository`) for clarity.
- **MySQL vs embedded H2**: MySQL matches production-style deployment; schema is initialized via SQL scripts.

---

## Open Source Usage

| Dependency / tool | Role in this project |
|-------------------|----------------------|
| Spring Boot starters (web, websocket, security, data-jpa, mail, validation) | HTTP API, STOMP, filter chain (permit-all), JPA, SMTP, validation |
| Lombok | Reduces boilerplate in entities (where used) |
| MySQL Connector/J | Database driver |
| jjwt | Present in POM; **not wired to REST auth** in current Java code |
| XChart | Category/XY charts for PDF reports |
| OpenPDF / iText | PDF document generation for final reports |
| GitHub Actions (`ci-cd.yml`) | `mvn compile`, `test`, `package` on push/PR |

---

## Features

### User features

- Register, verify email, login; inactive or unverified users cannot authenticate.
- CRUD profile sections with file uploads (stored under `app.upload-dir`).
- Per-user **profile analytics** API consumed by `profile-analytics.js` (charts and KPIs).
- List slots, book/cancel interviews; join WebRTC meeting when active and allowed.
- Upload audio chunks for STT (per booking); communication scoring and coding submission APIs.

### Admin features

- Admin login; **`meetingAdminToken`** for starting/stopping meetings from the server.
- User list, activate/deactivate/delete; talent and location analytics.
- Create/delete interview slots; view bookings; calendar summary and per-date lists.
- Send email with optional attachment (logged in `AdminEmailLog`).
- Control global meeting modes (normal vs scheduled slot/booking gating).

### Smart / automation features

- **STT pipeline**: `MeetingSttService` sends audio chunks to Whisper HTTP, deduplicates by `clientId` + `chunkSeq`, aggregates segments; finalize builds transcript for downstream scoring.
- **Communication scoring**: token statistics, filler detection, WPM, merged with optional technical/behavioral inputs into session scores (`InterviewUpgradeService`).
- **Ranking**: weighted combination of communication, technical, behavioral, profile dimensions.
- **Coding**: challenges in DB; submissions judged via Judge0 when configured, else fallback logic.
- **Reports**: publish final PDF; optional email distribution.

---

## System Architecture

### Component breakdown

```text
┌─────────────────────────────────────────────────────────────────┐
│                     Browser (static HTML/JS)                     │
│  user/*.html  admin/*.html  common/*.js                          │
└───────────────┬───────────────────────────────┬──────────────────┘
                │ HTTPS                         │
                ▼                               ▼
┌───────────────────────────┐     ┌─────────────────────────────┐
│ Spring MVC REST controllers│     │ SockJS + STOMP (/ws)        │
│ /api/*, /meeting/*         │     │ @MessageMapping handlers    │
└───────────────┬───────────┘     └──────────────┬──────────────┘
                │                                 │
                ▼                                 ▼
┌───────────────────────────────────────────────────────────────┐
│ Services: User, Interview, Profile, Meeting*, InterviewUpgrade│
│ FileStorage, Email, Analytics, STT (Whisper client), Judge0    │
└───────────────────────────────┬───────────────────────────────┘
                │                 │                 │
                ▼                 ▼                 ▼
         ┌──────────┐    ┌──────────────┐   ┌─────────────┐
         │  MySQL   │    │ Local uploads │   │ HTTP out    │
         │  (JPA)   │    │ (profile/…)   │   │ Whisper,    │
         └──────────┘    └──────────────┘   │ Judge0      │
                                            └─────────────┘
```

### User architecture (text diagram)

```text
User → Static UI (login.html, user/*.html)
     → REST: /api/users/*, /api/interviews/*, /api/users/{id}/*, /api/interview-upgrade/*, /api/meeting/{bookingId}/stt/*
     → GET /api/files/* (uploaded assets)
     → SockJS /ws → STOMP publish /app/meeting/* → subscribe /topic/meeting/*
     → WebRTC peer connections (ICE from /meeting/rtc-config)
```

### Admin architecture (text diagram)

```text
Admin → admin-login.html → POST /api/admin/login → stores meetingAdminToken + localStorage.admin
      → REST: /api/admin/*, /api/admin/interviews/*
      → POST /meeting/start|end with X-Meeting-Admin-Token
      → Same WebSocket endpoint as users; admin role required for /app/meeting/control
```

---

## Bot / automation (interview analysis pipeline)

There is **no conversational chatbot**. Automation is **algorithmic + external services**:

1. **Input**: Audio chunks from browser, optional manual technical/behavioral scores.
2. **Processing**: Whisper transcription → text metrics → `InterviewSession` updates → ranking weights applied → optional Judge0 for code.
3. **Output**: Aggregated scores, leaderboard, PDF report, optional emails.

See [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) for endpoint details.

---

## File structure and responsibilities

### Backend (`src/main/java/com/example/authadmin/`)

| Path | Responsibility |
|------|----------------|
| `AuthAdminApplication.java` | Spring Boot entry point |
| `controller/*` | HTTP mapping; DTO in/out; thin delegation to services |
| `service/*` | Business rules: booking conflicts, scoring, STT orchestration, PDF, email |
| `repository/*` | Spring Data JPA interfaces |
| `entity/*` | JPA entities (User, InterviewSlot, InterviewBooking, InterviewSession, …) |
| `dto/*` | Request/response shapes |
| `config/SecurityConfig.java` | CSRF off, **all requests permitted** (no method-level auth) |
| `config/WebSocketConfig.java` | `/ws` + `/app` + `/topic` |
| `config/GlobalExceptionHandler.java` | Unified JSON errors |

**Notable services**

- `MeetingStateService` — global meeting active flag, mode, allowed user/booking sets.
- `MeetingRtcConfigService` — STUN/TURN-style ICE list from configuration.
- `MeetingSttService` — Whisper HTTP adapter, chunk deduplication.
- `InterviewUpgradeService` — scoring, ranking, coding, PDF publish.
- `ProfileAnalyticsService` — user and talent-pool analytics.
- `MeetingAdminTokenService` — opaque token for meeting start/end.

### Frontend (`src/main/resources/static/`)

| Area | Responsibility |
|------|----------------|
| `login.html` | User login; stores `user` in storage |
| `user/*.html`, `user/js/*.js` | Dashboard, profile edit, appointments, live coding, meeting join |
| `admin/*.html`, `admin/js/*.js` | Dashboard, talent, appointments, meeting control, settings |
| `common/meeting.js` | WebRTC + STOMP + meeting UX |
| `common/profile-analytics.js` | Charts for `/profile/analytics` |

### Database & config

- `resources/schema.sql`, `schema-migration.sql`, `data.sql` — initialized when `spring.sql.init.mode=always`.
- `application.properties` — use **environment variables** for secrets in production.

---

## Data flow (end-to-end)

1. User registers → email verification → login → `user` JSON in storage.
2. User completes profile → files on disk, rows in MySQL → analytics recalculated on read/publish.
3. Admin creates slot → user books → `InterviewBooking` row.
4. Admin starts meeting (`SCHEDULED` or `NORMAL`) → optional emails → `MeetingStateService` updates → clients poll `/meeting/status` and open WebRTC.
5. Audio → `POST .../stt/chunk` → Whisper → segments; finalize → transcript string fed to `POST .../communication/score`.
6. Coding → `POST .../coding/submit` → Judge0 or fallback → scores persisted.
7. Admin adjusts weights → `GET .../ranking` → publish report → PDF path + optional emails.

---

## Important learnings

### Technical

- **WebRTC in browsers** requires ICE (STUN; TURN for symmetric NAT); configuration is externalized (`app.meeting.ice.stun-urls`).
- **STOMP** cleanly separates signaling (`/topic/meeting/signal`) from chat and presence; rate limiting protects chat flooding (`MeetingChatRateLimiter`).
- **Server-side PDF** keeps report layout consistent; charts are rendered as images then embedded.
- **Permissive security** simplifies demos but is **not production-safe**; defense in depth needs authenticated APIs.

### Industry relevance

- Mirrors **ATS + video interview** patterns: scheduling, collaboration, structured evaluation.
- **Judge0** is a common way to sandbox code execution without maintaining compilers on the app server.

### Resume value

- Spring Boot full stack, WebSocket, JPA, third-party HTTP integration (STT, judge), PDF reporting, static SPA integration.

---

## Related documents

- [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) — endpoints, payloads, WebSocket destinations, errors.
- [ENGINEERING_THESIS.md](./ENGINEERING_THESIS.md) — thesis narrative (Markdown source).
- [ENGINEERING_THESIS.docx](./ENGINEERING_THESIS.docx) — thesis exported for Word (regenerate with Pandoc: `pandoc ENGINEERING_THESIS.md -o ENGINEERING_THESIS.docx`; optional `--reference-doc=your-template.docx` for institutional styles).
