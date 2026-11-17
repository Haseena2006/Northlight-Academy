# Northlight Academy – Online Learning Management System

Full-stack LMS: **React + Vite** frontend, **Spring Boot 3 / Java 17** backend, **PostgreSQL + Flyway**, JWT access tokens with rotating HttpOnly refresh cookies, and role-based access for **Student / Instructor / Admin**.

> ## Verification status (read first)
> Neon PostgreSQL has successfully applied Flyway migrations V1 through V5. The lessons table has 184 lessons; all 184 contain non-empty Markdown reading content. Lessons are delivered as text-based readings, with lesson completion and course progress tracked by the platform; this is not a video-based learning workflow. These database facts are verified for the current Neon database. Each new developer still needs to configure their own environment and run the application locally. Deployment and production readiness have not been established here. No testimonials or course ratings are shown because there is no verified testimonial or rating data model.

## Prerequisites
JDK 17+, Maven 3.9+, Node 18+ (20/22 fine), PostgreSQL 14+.

## 1. Database
Configure `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` with your Neon PostgreSQL connection details. The JDBC URL must include `sslmode=require`. The backend has no localhost database fallback: it fails at startup if Neon connection settings are missing. Flyway is enabled and applies pending migrations on backend startup. The current Neon database has V1 through V5 applied; this does not mean a new developer's database has the same migration history or data.

## 2. Configuration
```bash
cp .env.example .env     # then edit values (never commit .env)
```
Required: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` (32+ chars; the app refuses to start without them). Set `ADMIN_EMAIL` / `ADMIN_PASSWORD` to bootstrap the first administrator (created only if no admin exists; password needs 10+ chars with upper, lower, digit). There are no default credentials in source.

## 3. Run the backend
```bash
cd backend
./run.sh                 # loads ../.env and runs mvn spring-boot:run   -> http://localhost:8080
```
Windows / IntelliJ: open the `backend` folder as a Maven project, set the environment variables from `.env` in *Run → Edit Configurations → Environment variables*, run `AcademyApplication`.

## 4. Run the frontend
```bash
cd frontend
npm install
npm run dev              # http://localhost:5173 (Vite proxies /api and /uploads to :8080)
```
VS Code: open the repo root, use two terminals (backend, frontend). Set `FRONTEND_ORIGIN` to the frontend URL; for production behind HTTPS set `COOKIE_SECURE=true`.

## 5. Tests
```bash
cd backend && mvn test        # JUnit 5 + Mockito unit tests, MockMvc integration tests on in-memory H2 (PostgreSQL mode)
cd frontend && npm test       # Vitest + Testing Library
```
Integration tests use H2, not PostgreSQL; separately confirm the connected Neon API and Flyway history when running against the configured database.

## First-run verification checklist
**Already verified for the current Neon database:** V1–V5 are applied; 184 lessons have non-empty Markdown reading content (0 are empty).

**Steps for a new developer:**
1. Configure the required environment variables and start the backend and frontend using the instructions above. Confirm the backend starts against the intended Neon database.
2. Run `mvn test` from `backend`, `npm test` from `frontend`, and `npm run build` from `frontend` to check the local checkout.
3. Postman: import `postman/Northlight-Academy.postman_collection.json`, set `baseUrl`, `adminEmail`, `adminPassword`; run Auth → Admin → Student → Instructor folders in order.
4. In `psql`, inspect schema without displaying password hashes: `\d enrollments` should show the unique `(student_id, course_id)` constraint. Do not print or share credentials or password hashes.
5. In the browser, register, enroll, read lessons, mark lessons complete, check course progress, and download a certificate; open `/verify/<code>` to verify it.

## Sample workflows
**Admin:** sign in with the bootstrap account → *Instructors → Add instructor* → *Courses → Add course* (assign instructor, upload thumbnail) → *Lessons* (add ≥1) → *Publish*.
**Instructor:** sign in → *My courses* → open a course → add/edit/reorder lessons, view *Students* progress. Courses not assigned to you return 403.
**Student:** register → *Courses* → open course → *Enroll* → read lessons, select *Mark as complete*, and track course progress → after the last required lesson a certificate is issued → *Certificates → Download PDF*.

## Security notes
- BCrypt (strength 12); passwords/tokens never logged or returned.
- Access JWT (15 min default) kept in browser memory only. Refresh token: random 256-bit, only its SHA-256 stored, delivered as `HttpOnly; SameSite=Strict; Path=/api/auth` cookie (`Secure` via `COOKIE_SECURE`). Rotated on every refresh; reuse of a rotated token revokes the whole family. "Keep me signed in" off ⇒ session cookie + 1-day server expiry.
- CSRF: API is stateless/bearer-based (CSRF protection disabled deliberately); the two cookie-authenticated endpoints (`/auth/refresh`, `/auth/logout`) require `X-Requested-With: NorthlightWeb` plus SameSite=Strict. CORS allows only `FRONTEND_ORIGIN`.
- Limitation: two browser tabs refreshing at the same instant with the same cookie can trigger reuse detection and sign the user out (no grace window).
- Roles enforced in `SecurityConfig` and by ownership checks in services (student data, instructor course assignment). Public registration always yields STUDENT.
- Uploads: JPEG/PNG/WebP only (magic-byte check), ≤2 MB, random filenames, stored under `STORAGE_DIR` outside the source tree.
- Password reset: single-use, 30-minute tokens (hashed). **No email provider is configured**, so links are not sent. For development set `DEV_LOG_RESET_LINKS=true` to print the link to the server log. Wire a mail sender into `AuthService.forgotPassword` for production.
- Deleting a course with enrollments is refused (409); unpublish instead, so certificates are never orphaned.

See `docs/` for architecture, schema and API reference.
