-- Demo catalogue so a fresh install is not empty. Video URLs are intentionally NULL:
-- add real URLs from the admin/instructor lesson editor. Remove these rows if unwanted.
INSERT INTO courses(title, short_description, description, category_id, difficulty, duration_minutes, learning_outcomes, published)
VALUES
 ('Spring Boot REST APIs in Practice', 'Design, secure and test production-style REST services.',
  'A hands-on course covering layered architecture, validation, persistence with JPA, security and testing for Spring Boot services.',
  (SELECT id FROM categories WHERE name='Java'), 'INTERMEDIATE', 240,
  'Design clean layered REST APIs|Persist data with Spring Data JPA|Secure endpoints with JWT|Write integration tests', TRUE),
 ('React Fundamentals', 'Build maintainable single-page applications with React.',
  'Learn components, state, effects, routing and data fetching, and structure a real application.',
  (SELECT id FROM categories WHERE name='Web Development'), 'BEGINNER', 180,
  'Compose UIs from components|Manage state and side effects|Use React Router|Integrate with REST APIs', TRUE),
 ('PostgreSQL Design and Queries', 'Model relational data and write efficient SQL.',
  'Normalisation, constraints, indexing, joins, transactions and query plans using PostgreSQL.',
  (SELECT id FROM categories WHERE name='Databases'), 'INTERMEDIATE', 210,
  'Model data with keys and constraints|Write joins and aggregates|Index for performance|Reason about transactions', TRUE);

INSERT INTO lessons(course_id, title, description, duration_minutes, position, preview)
SELECT c.id, t.title, t.descr, t.dur, t.pos, t.prev FROM courses c JOIN (
  SELECT 'Spring Boot REST APIs in Practice' ct, 'Project structure and layering' title, 'How controllers, services and repositories fit together.' descr, 20 dur, 1 pos, TRUE prev UNION ALL
  SELECT 'Spring Boot REST APIs in Practice', 'Validation and error handling', 'Bean validation and centralised exception handling.', 25, 2, FALSE UNION ALL
  SELECT 'Spring Boot REST APIs in Practice', 'Persistence with JPA', 'Entities, repositories and transactions.', 35, 3, FALSE UNION ALL
  SELECT 'Spring Boot REST APIs in Practice', 'Securing the API', 'Authentication, authorisation and token handling.', 40, 4, FALSE UNION ALL
  SELECT 'React Fundamentals', 'Components and props', 'Building blocks of a React UI.', 20, 1, TRUE UNION ALL
  SELECT 'React Fundamentals', 'State and effects', 'Hooks for local state and side effects.', 30, 2, FALSE UNION ALL
  SELECT 'React Fundamentals', 'Routing', 'Client-side navigation with React Router.', 25, 3, FALSE UNION ALL
  SELECT 'PostgreSQL Design and Queries', 'Relational modelling', 'Tables, keys and constraints.', 30, 1, TRUE UNION ALL
  SELECT 'PostgreSQL Design and Queries', 'Joins and aggregates', 'Combining and summarising data.', 35, 2, FALSE UNION ALL
  SELECT 'PostgreSQL Design and Queries', 'Indexes and query plans', 'Reading EXPLAIN output.', 40, 3, FALSE
) t ON t.ct = c.title;
