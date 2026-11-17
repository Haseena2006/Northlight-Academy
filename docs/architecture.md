# Architecture
- **Frontend** (`frontend/`): React 18 + Vite, React Router, Axios. `services/api.js` holds the in-memory access token, attaches it, and transparently refreshes on 401 (single shared refresh promise). `context/AuthContext` restores the session from the refresh cookie on load. `routes/ProtectedRoute` is a UX guard only.
- **Backend** (`backend/`, package `com.northlight.academy`): `controller` (HTTP only) → `service` (business rules, `@Transactional`) → `repository` (Spring Data JPA). DTOs are Java records in `dto/Dtos`; entities are never returned. `exception/GlobalExceptionHandler` produces `{status,message,errors,timestamp}`.
- **Security**: stateless Spring Security chain; `JwtAuthFilter` validates the bearer token and reloads the user (inactive users are rejected immediately). URL rules: `/api/admin/**`→ADMIN, `/api/instructor/**`→INSTRUCTOR, `/api/student/**`→STUDENT. Ownership checks live in services (`StudentService.owned`, `LessonService.authorize`, `InstructorService.students`, `CertificateService.pdf`).
- **Data**: PostgreSQL schema managed only by Flyway (`ddl-auto=none`).
- **Progress**: percent = completed lessons / total lessons × 100; a course completes when all lessons flagged `required` are done, which issues a certificate (idempotent).
- **Lesson management** is one service (`LessonService`) used by both admin and instructor controllers.
