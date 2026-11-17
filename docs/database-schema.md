# Database schema (Flyway `V1__init_schema.sql`)
| Table | Purpose / key constraints |
|---|---|
| roles | STUDENT, INSTRUCTOR, ADMIN (seeded) |
| users | unique lower-cased `email`, `password_hash`, `active` |
| user_roles | PK (user_id, role_id) |
| categories | unique `name` |
| courses | FK category; `difficulty`, `published`, `thumbnail_path`, `learning_outcomes` (`|`-separated) |
| course_instructors | PK (course_id, user_id): instructor assignment |
| lessons | FK course (cascade); `position`, `preview`, `required`, `video_url`; index (course_id, position) |
| enrollments | **UNIQUE (student_id, course_id)**; `status` ACTIVE/COMPLETED; `last_lesson_id` |
| lesson_progress | **UNIQUE (enrollment_id, lesson_id)**; FKs cascade |
| certificates | unique `certificate_code`, unique `enrollment_id` |
| refresh_tokens | `token_hash` unique, `family_id`, `revoked`, `remember_me`, `expires_at` |
| password_reset_tokens | `token_hash` unique, `used`, `expires_at` |

All tables carry created timestamps; mutable ones also `updated_at`. Enrollments reference courses without cascade, so a course with enrollments cannot be removed. `V2__demo_catalogue.sql` seeds 3 demo courses (no video URLs).
