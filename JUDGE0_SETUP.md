# Judge0 Local Setup (Recommended)

This project already supports Judge0 in:

- `app.coding.judge0.url`
- `app.coding.judge0.api-key`

If not configured, the app uses fallback evaluator.  
Judge0 gives real execution/runtime-based results.

## Why Judge0 is important

- Runs candidate code in isolated sandbox
- Supports real compile/run verdicts (`Accepted`, `Runtime Error`, etc.)
- Gives actual execution time and output
- More credible technical scoring than text-based heuristics

## 1) Run Judge0 CE with Docker (quickest)

Use Docker Desktop, then in PowerShell:

```powershell
docker run -d --name judge0-ce -p 2358:2358 judge0/judge0:latest
```

Health check:

```powershell
curl http://localhost:2358/languages
```

You should get JSON list of languages.

## 2) Configure this Spring Boot app

Set these in `src/main/resources/application.properties`:

```properties
app.coding.judge0.url=http://localhost:2358
app.coding.judge0.api-key=
```

If using a hosted Judge0 provider, set the URL/API key accordingly.

## 3) Restart your Spring Boot app

After property changes, restart backend so config reloads.

## 4) Verify from Admin UI

Open:

- `admin-settings.html`

Click:

- `Check Judge0 Status`

Expected:

- `Connected: ...` message

## 5) Test coding submission API

Example payload:

```json
{
  "bookingId": 1,
  "challengeId": 1,
  "userId": 1,
  "language": "java",
  "sourceCode": "public class Main { public static void main(String[] args){ System.out.println(\"Hello\"); } }",
  "stdin": ""
}
```

Endpoint:

```http
POST /api/interview-upgrade/coding/submit
Content-Type: application/json
```

## Common issues

- **Judge0 not reachable**: check Docker is running and port `2358` is open.
- **Connection refused**: verify URL exactly matches Judge0 host/port.
- **No coding score update**: ensure `bookingId`, `challengeId`, `userId` exist.
- **Still fallback mode**: check `Check Judge0 Status` output in admin settings.

