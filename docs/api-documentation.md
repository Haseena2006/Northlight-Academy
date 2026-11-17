# API reference
Errors: `{ "status":400, "message":"Validation failed", "errors":{"field":"msg"}, "timestamp":"…" }`. Protected calls need `Authorization: Bearer <accessToken>`. Pages: `{content,page,size,totalElements,totalPages}`.

## Auth (`/api/auth`)
| Method & path | Notes |
|---|---|
| POST /register | `{fullName,email,password}` → 201 UserDto (always STUDENT); 409 duplicate; 400 policy |
| POST /login | `{email,password,rememberMe}` → `{accessToken,user}` + `nl_refresh` cookie; 401 bad credentials; 403 inactive |
| POST /refresh | needs cookie + header `X-Requested-With: NorthlightWeb`; rotates; 401 invalid/reused |
| POST /logout | same header; revokes token family, clears cookie |
| GET/PUT /me | current user / update `fullName` |
| POST /change-password | `{currentPassword,newPassword}`; revokes all sessions |
| POST /forgot-password, /reset-password | generic response; single-use 30-min token |

## Public
`GET /api/courses?q&categoryId&difficulty&sort(newest|oldest|title|duration)&page&size` · `GET /api/courses/{id}` (video URLs only on preview lessons) · `GET /api/categories` · `GET /api/public/stats` · `GET /api/certificates/{code}/verify`

## Student (`ROLE_STUDENT`)
`GET /api/student/dashboard` · `GET /enrollments` · `POST /enrollments/{courseId}` (201; 409 duplicate) · `GET /courses/{courseId}/lessons` (403 if not enrolled) · `POST /progress {courseId,lessonId}` · `POST /progress/last-accessed` · `GET /progress/{courseId}` · `GET /certificates` · `GET /certificates/{code}/download` (PDF; 404 if not yours)

## Instructor (`ROLE_INSTRUCTOR`, assigned courses only → else 403)
`GET /api/instructor/dashboard` · `GET /courses` · `GET|POST /courses/{id}/lessons` · `PUT /courses/{id}/lessons/reorder {lessonIds}` · `PUT|DELETE /lessons/{id}` · `GET /courses/{id}/students`

## Admin (`ROLE_ADMIN`)
`GET /api/admin/dashboard` · courses: `GET|POST /courses`, `GET|PUT|DELETE /courses/{id}`, `PATCH /courses/{id}/status {published}`, `POST /courses/{id}/thumbnail` (multipart `file`) · categories: `POST /categories`, `PUT|DELETE /categories/{id}` · lessons: same shapes as instructor under `/api/admin` · users: `GET /students?q&active&page&size`, `GET|POST /instructors`, `GET /instructors/{id}`, `PUT /instructors/{id}/courses {courseIds}`, `PATCH /users/{id}/status {active}` · `GET /enrollments?courseId&page&size`
