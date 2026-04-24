# API Documentation — Project4 (Interview & Talent Platform)

Base URL (development): `http://localhost:8080` (configurable via `server.port`).

This document reflects the Spring Boot controllers under `com.example.authadmin.controller`. **Spring Security is configured with `permitAll()`**; REST endpoints do not validate JWT or server sessions. The SPA stores the logged-in user JSON client-side; APIs trust `userId` path/query parameters as sent by the client.

---

## Authentication

### User identity (client-side)

- **Registration**: `POST /api/users/register` → verification email.
- **Verification**: `GET /api/users/verify?token=...` → redirect to `/login.html`.
- **Login**: `POST /api/users/login` → JSON body with `UserResponse` (`id`, `username`, `email`, `verified`).
- The frontend (`login.html`, dashboards) stores this object in **`sessionStorage`** / **`localStorage`** under key `user`. Subsequent requests pass **`userId`** in URLs or bodies; the server does not re-check the password on each call.

### Admin

- **Login**: `POST /api/admin/login` with `adminId` and `password`.
- Response: `{ "status": "OK", "meetingAdminToken": "<opaque-token>" }`.
- Admin HTML pages set **`localStorage.admin`** to mark an “admin session”; **`meetingAdminToken`** is required for privileged **meeting lifecycle** HTTP calls.

### Meeting admin token (HTTP header)

| Header | When |
|--------|------|
| `X-Meeting-Admin-Token: <token>` | Required for `POST /meeting/start` and `POST /meeting/end` |

Invalid or missing token → **403** with body like `{ "error": "invalid_admin_token" }`.

### JWT library (`jjwt`)

The project lists `jjwt` in `pom.xml`, but **no REST controller uses JWT** for authentication. Treat JWT as an unused dependency unless you add a filter chain later.

---

## User APIs

### `UserController` — `/api/users`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/users/register` | Create account; sends verification email. |
| `GET` | `/api/users/verify?token=` | Verify email token; redirects to login. |
| `POST` | `/api/users/login` | Authenticate verified, active user. |
| `GET` | `/api/users/{id}` | Fetch user by id. |
| `PUT` | `/api/users/{id}/email` | Update email. |

**Register — request JSON**

```json
{
  "username": "jane_doe",
  "email": "jane@example.com",
  "password": "secret12"
}
```

**Login — request JSON**

```json
{
  "email": "jane@example.com",
  "password": "secret12"
}
```

**Login — success response (200)**

```json
{
  "id": 1,
  "username": "jane_doe",
  "email": "jane@example.com",
  "verified": true
}
```

**Login — failure (401)**  
Plain text: `Invalid credentials or email not verified`

**Update email — request JSON**

```json
{
  "email": "new@example.com"
}
```

---

### `InterviewController` — `/api/interviews`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/interviews/slots` | List interview slots. |
| `GET` | `/api/interviews/bookings/user/{userId}` | Bookings for a user. |
| `POST` | `/api/interviews/slots/{slotId}/book` | Book a slot. |
| `POST` | `/api/interviews/bookings/{bookingId}/cancel` | Cancel booking. |

**Book — request JSON**

```json
{
  "userId": 1
}
```

**Slot response (example fields)**  
`id`, `title`, `description`, `scheduledAt`, `durationMinutes`, `capacity`, `bookedCount` (may be 0 on user list).

**Booking response (example fields)**  
`id`, `slotId`, `slotTitle`, `scheduledAt`, `userId`, `username`, `email`, `status`.

Dates in ISO-style strings as produced by the server (e.g. `2026-02-26T14:30`).

---

### `ProfileCrudController` — `/api/users/{userId}`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/profile` | Full profile aggregate. |
| `PUT` | `/profile` | JSON body: profile fields (`SaveProfileRequest`). |
| `POST` | `/profile/photo` | Multipart `file`. |
| `POST` | `/education` | Multipart form fields (see controller). |
| `DELETE` | `/education/{id}` | |
| `POST` | `/experience` | Multipart form fields. |
| `DELETE` | `/experience/{id}` | |
| `POST` | `/programming-languages` | Multipart form fields. |
| `DELETE` | `/programming-languages/{id}` | |
| `POST` | `/certificates` | Multipart form fields. |
| `DELETE` | `/certificates/{id}` | |
| `POST` | `/documents` | Multipart `file` + optional `documentName`. |
| `DELETE` | `/documents/{id}` | |
| `POST` | `/skills` | Form params `skillName`, `skillLevel`, optional `id`. |
| `DELETE` | `/skills/{id}` | |

Successful mutations return **`200`** with updated `ProfileResponse` (same shape as `GET /profile`) unless noted. Photo upload returns:

```json
{
  "path": "profile/1/uuid-filename.jpg",
  "url": "/api/files/profile/1/uuid-filename.jpg"
}
```

---

### `ProfileAnalyticsController` — `/api/users/{userId}/profile`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/analytics` | `UserAnalyticsResponse`: scores, counts, charts data, etc. |

---

### `FileController` — `/api/files`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/files/{*path}` | Stream uploaded file; `path` is storage-relative (e.g. `profile/1/...`). |

Content-Type is probed from disk or inferred from extension.

---

## Admin APIs

### `AdminController` — `/api/admin`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/login` | Admin credentials → `meetingAdminToken`. |
| `GET` | `/talent/analytics` | Aggregated talent pool analytics. |
| `GET` | `/talent/locations` | Location analytics. |
| `GET` | `/users` | All users (`AdminUserResponse` list). |
| `POST` | `/users/{id}/activate` | Set user active. |
| `POST` | `/users/{id}/deactivate` | Set user inactive. |
| `DELETE` | `/users/{id}` | Delete user and related data (service rules apply). |
| `POST` | `/email/send` | `multipart/form-data`: `toEmail`, `subject`, `messageBody`, optional `file`. |

**Admin login — request**

```json
{
  "adminId": "admin",
  "password": "••••••••"
}
```

**Admin login — success**

```json
{
  "status": "OK",
  "meetingAdminToken": "<opaque-string>"
}
```

---

### `AdminInterviewController` — `/api/admin/interviews`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/slots` | Slots with `bookedCount`. |
| `POST` | `/slots` | Create slot (`CreateSlotRequest` JSON). |
| `DELETE` | `/slots/{id}` | Delete slot. |
| `GET` | `/bookings` | All bookings. |
| `GET` | `/calendar/summary?year=&month=` | Per-day slot/booked counts. |
| `GET` | `/calendar/date/{date}` | Bookings on `yyyy-MM-dd`. |

**Create slot — request**

```json
{
  "title": "Round 1",
  "description": "Technical",
  "scheduledAt": "2026-02-26T14:30",
  "durationMinutes": 60,
  "capacity": 5
}
```

---

## Interview upgrade & automation APIs

### `InterviewUpgradeController` — `/api/interview-upgrade`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/communication/score` | Compute communication metrics from transcript + optional scores. |
| `GET` | `/ranking/weights` | Current weight configuration. |
| `PUT` | `/ranking/weights` | Update weights. |
| `GET` | `/ranking` | Weighted candidate ranking. |
| `GET` | `/report/final` | Final report summary/metadata. |
| `POST` | `/report/final/publish?sendEmails=` | Publish PDF report; optional emails. |
| `GET` | `/report/final/user/{userId}` | User’s latest position/score snapshot. |
| `GET` | `/coding/challenges` | Active coding challenges. |
| `GET` | `/coding/challenges/all` | All challenges. |
| `GET` | `/coding/judge0/status` | Judge0 configuration/connectivity hint. |
| `POST` | `/coding/challenges` | Create/update challenge. |
| `POST` | `/coding/submit` | Submit code for judging (Judge0 or fallback). |

**Communication score — request**

```json
{
  "bookingId": 10,
  "transcriptText": "Hello um basically we implemented ...",
  "speakingDurationSeconds": 120,
  "technicalScore": 78.5,
  "behavioralScore": 82.0
}
```

**Communication score — response (illustrative)**

```json
{
  "sessionId": 100,
  "bookingId": 10,
  "userId": 1,
  "wordsCount": 180,
  "fillerWordsCount": 4,
  "speakingSpeedWpm": 90.0,
  "clarityScore": 85.0,
  "confidenceScore": 80.0,
  "communicationScore": 82.5,
  "technicalScore": 78.5,
  "behavioralScore": 82.0,
  "finalScore": 80.25
}
```

**Ranking weights — request**

```json
{
  "communicationWeight": 0.25,
  "technicalWeight": 0.35,
  "behavioralWeight": 0.2,
  "profileWeight": 0.2
}
```

**Coding submit — request**

```json
{
  "bookingId": 10,
  "challengeId": 1,
  "userId": 1,
  "language": "java",
  "sourceCode": "public class Main { ... }",
  "stdin": ""
}
```

Service may return **400**, **404**, **503** (e.g. Judge0 not configured) depending on operation.

---

## Meeting & STT APIs

### `MeetingController` — `/meeting`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/meeting/rtc-config` | Public | ICE servers for WebRTC. |
| `GET` | `/meeting/chat/history?limit=` | Public | Recent chat messages. |
| `POST` | `/meeting/start` | **Admin header** | Start meeting (`mode`, `slotId`, `bookingId` query params). |
| `GET` | `/meeting/status` | Public | Current meeting state. |
| `POST` | `/meeting/end` | **Admin header** | End meeting, clear chat. |
| `POST` | `/meeting/upload` | Public | Multipart `file` → `fileId`, `fileUrl`. |
| `GET` | `/meeting/files/{fileId}` | Public | Download shared file. |

**Start query parameters (examples)**

- `mode=NORMAL` — global ad-hoc meeting.
- `mode=SCHEDULED&slotId=5` or `bookingId=10` — gated to booked users for that slot.

**Status — response shape (`StatusResponse`)**  
`active`, `mode`, `slotId`, `bookingId`, `targetUserId`, `allowedUserIds`, `bookingIds`.

---

### `MeetingSttController` — `/api/meeting`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/{bookingId}/stt/chunk` | Multipart: `audio` file; optional `clientId`, `chunkSeq`. |
| `POST` | `/{bookingId}/stt/finalize` | Finalize transcript for booking. |

Chunk response may include transcription segments, duplicate acks, etc. If Whisper URL is not configured, the service returns **503** (`ResponseStatusException`).

---

## WebSocket (STOMP)

- **Endpoint**: `/ws` with **SockJS** (`WebSocketConfig`).
- **Broker**: simple broker with destination prefix `/topic`.
- **App prefix**: `/app` (client sends to `/app/...`).

### Client → server (`@MessageMapping`)

| Destination | Payload | Notes |
|-------------|---------|--------|
| `/app/meeting/signal` | `WsEnvelope` | WebRTC signaling relay. |
| `/app/meeting/chat` | `WsEnvelope` | Chat; rate-limited per session for text. |
| `/app/meeting/typing` | `TypingPayload` | Typing indicators. |
| `/app/meeting/end` | `WsEnvelope` | Broadcast end as system signal. |
| `/app/meeting/presence/join` | `PresencePayload` | Join presence list. |
| `/app/meeting/control` | `MeetingControlCommand` | **Admin role only** (sender must have role `admin`). |
| `/app/meeting/control/ack` | `MeetingControlAck` | Control acknowledgements. |

### Server → client (subscribe)

| Topic | Content |
|-------|---------|
| `/topic/meeting/signal` | `WsEnvelope` (signals + system end) |
| `/topic/meeting/chat` | `WsEnvelope` |
| `/topic/meeting/typing` | `TypingPayload` |
| `/topic/meeting/presence` | Full presence list (JSON array) |
| `/topic/meeting/control` | `MeetingControlCommand` |
| `/topic/meeting/control/ack` | `MeetingControlAck` |

**`WsEnvelope` (common fields)**  
`type`, `sender`, `senderName`, `senderId`, `kind`, `toClientId`, `fromClientId`, `payload`, `ts`.

---

## Static entry

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/` | Redirects to `/login.html`. |

---

## Error handling

### `GlobalExceptionHandler` JSON bodies

| HTTP | `error` / shape | Typical cause |
|------|-----------------|----------------|
| 400 | `{ "error": "validation_failed", "fields": { "email": "..." } }` | Bean validation |
| 400 | `{ "error": "bad_request", "message": "..." }` | Malformed JSON / date |
| 400 | `{ "error": "bind_failed" }` | Binding error |
| 409 | `{ "error": "conflict", "message": "..." }` | `IllegalStateException` |
| 409 | `{ "error": "data_integrity", "message": "..." }` | DB constraint |
| 404 | (empty) | `NoResourceFoundException` |
| 500 | `{ "error": "internal_error", "message": "..." }` | Unhandled exception |

### `ResponseStatusException` (services/controllers)

Many services throw `ResponseStatusException` with **400**, **404**, **503**, etc. Bodies are often a simple message string or problem detail from Spring — check runtime behavior for exact text.

### Meeting controller error maps

- **403** `{ "error": "invalid_admin_token" }`
- **400** `{ "error": "slot_id_required_for_scheduled_mode" }`, `{ "error": "no_booked_users_for_slot" }`, etc.

---

## Environment variables (deployment)

Use env vars for secrets; **do not commit passwords**. Examples:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `UPLOAD_DIR`
- `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`
- `JUDGE0_URL`, `JUDGE0_API_KEY`
- `app.stt.whisper.url` (Spring property; set via `application.properties` override or env-specific config)

---

*Generated from the Project4 codebase; verify against running application for exact edge-case responses.*
