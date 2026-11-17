-- Additional sample catalogue records. These courses are educational samples, not instructor endorsements.
UPDATE categories SET name='Java Development' WHERE name='Java';
UPDATE categories SET name='Database' WHERE name='Databases';
UPDATE categories SET name='Cloud and DevOps' WHERE name='Cloud & DevOps';
UPDATE categories SET name='Data Science' WHERE name='Data Science & AI';
INSERT INTO categories(name,description) SELECT 'Java Development','Java frameworks, backend services and the JVM' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Java Development');
INSERT INTO categories(name,description) SELECT 'Web Development','Modern frontend and full-stack application development' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Web Development');
INSERT INTO categories(name,description) SELECT 'AI and Machine Learning','Artificial intelligence and machine learning applications' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='AI and Machine Learning');
INSERT INTO categories(name,description) SELECT 'Data Science','Statistics, data tools and visualization' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Data Science');
INSERT INTO categories(name,description) SELECT 'Design','User experience and interface design' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Design');
INSERT INTO categories(name,description) SELECT 'Software Testing','Manual and automated software quality' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Software Testing');
INSERT INTO categories(name,description) SELECT 'Database','Relational and document data systems' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Database');
INSERT INTO categories(name,description) SELECT 'Cloud and DevOps','Cloud platforms and delivery practices' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Cloud and DevOps');
INSERT INTO categories(name,description) SELECT 'Professional Skills','Communication and workplace methods' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Professional Skills');
INSERT INTO categories(name,description) SELECT 'Cybersecurity','Cybersecurity principles, threat awareness and defensive practice' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Cybersecurity');

-- V3 used a temporary Programming category. Fold its courses into the closest
-- categories in the required catalogue before removing that legacy category.
UPDATE categories SET description='Java development and core software engineering foundations' WHERE name='Java Development';
UPDATE courses SET category_id=(SELECT id FROM categories WHERE name='Java Development')
WHERE category_id=(SELECT id FROM categories WHERE name='Programming')
  AND title NOT IN ('Python for Beginners to Advanced','Software Testing and Automation');
UPDATE courses SET category_id=(SELECT id FROM categories WHERE name='Data Science')
WHERE title='Python for Beginners to Advanced'
  AND category_id=(SELECT id FROM categories WHERE name='Programming');
UPDATE courses SET category_id=(SELECT id FROM categories WHERE name='Software Testing')
WHERE title='Software Testing and Automation'
  AND category_id=(SELECT id FROM categories WHERE name='Programming');
UPDATE courses SET category_id=(SELECT id FROM categories WHERE name='Cybersecurity')
WHERE title='Cybersecurity Fundamentals';
DELETE FROM categories legacy
WHERE legacy.name='Programming'
  AND NOT EXISTS (SELECT 1 FROM courses c WHERE c.category_id=legacy.id);

UPDATE courses SET category_id=(SELECT id FROM categories WHERE name='AI and Machine Learning') WHERE title IN ('Machine Learning Fundamentals','Artificial Intelligence Fundamentals','Deep Learning');
UPDATE courses SET category_id=(SELECT id FROM categories WHERE name='Data Science') WHERE title IN ('Python for Data Science','Data Analytics with Python','Power BI Professional Course','Excel for Data Analysis');
UPDATE courses SET category_id=(SELECT id FROM categories WHERE name='Design') WHERE title='UI/UX Design';
UPDATE courses SET category_id=(SELECT id FROM categories WHERE name='Software Testing') WHERE title='Software Testing and Automation';
INSERT INTO courses(title,short_description,description,category_id,difficulty,duration_minutes,thumbnail_path,learning_outcomes,prerequisites,published)
SELECT s.title,s.summary,'Sample educational content for independent study. ' || s.description,c.id,s.level,CAST(s.duration AS INTEGER),'seed:' || s.cover,s.outcomes,s.prerequisites,TRUE
FROM (VALUES
 ('Advanced Java Programming', 'Java Development', 'INTERMEDIATE', '360', 'Deepen Java skills with generics, streams, concurrency and JVM behavior.', 'Move beyond introductory Java by applying generics, stream pipelines and concurrency primitives to small maintainable programs.', 'Generics and type-safe APIs|Streams and functional composition|Concurrency and JVM fundamentals', 'Comfort writing Java classes and using collections.', 'java.svg'),
 ('Angular Application Development', 'Web Development', 'INTERMEDIATE', '360', 'Build structured Angular interfaces with components, routing and services.', 'Create a modular client application, connect it to HTTP services and organize state and navigation with Angular patterns.', 'Components and dependency injection|Routing and forms|HTTP services and application structure', 'Familiarity with HTML, CSS and JavaScript.', 'design.svg'),
 ('REST API Design and Development', 'Java Development', 'INTERMEDIATE', '300', 'Design predictable REST endpoints with validation and clear resource models.', 'Practice resource naming, status codes, pagination, validation and error responses while shaping an API contract for a sample service.', 'Resource-oriented endpoint design|Validation and error handling|Pagination and API contracts', 'Basic HTTP and JSON familiarity.', 'java.svg'),
 ('Spring Framework Essentials', 'Java Development', 'INTERMEDIATE', '300', 'Understand Spring configuration, dependency injection and application context.', 'Explore bean lifecycles, configuration and dependency injection through a small service application before introducing web and persistence layers.', 'Dependency injection and beans|Configuration and profiles|Application context', 'Comfort with Java classes and interfaces.', 'java.svg'),
 ('Spring Security in Practice', 'Java Development', 'ADVANCED', '360', 'Apply authentication and role-based access to Spring applications.', 'Configure request authorization, authentication providers and secure defaults, then verify behavior with focused security tests.', 'Authentication and authorization|Request security configuration|Security testing', 'Spring Boot and HTTP fundamentals.', 'security.svg'),
 ('Spring Data JPA', 'Java Development', 'INTERMEDIATE', '300', 'Persist domain models using repositories, queries and transactions.', 'Map entities to relational data, build repository operations and reason about transaction boundaries and fetch behavior.', 'Entity mapping and repositories|Query methods and pagination|Transactions and fetch strategies', 'Java and basic SQL familiarity.', 'database.svg'),
 ('Hibernate ORM', 'Java Development', 'INTERMEDIATE', '300', 'Map object models to relational tables and understand ORM behavior.', 'Work through entity relationships, persistence context behavior, lazy loading and query tuning in a relational sample application.', 'Entity relationships|Persistence context and transactions|Fetch plans and query tuning', 'Java and relational database basics.', 'database.svg'),
 ('Microservices Architecture', 'Java Development', 'ADVANCED', '420', 'Explore service boundaries, resilience and communication patterns.', 'Evaluate how independently deployed services communicate, handle failure and observe cross-service workflows using architecture exercises.', 'Service boundaries and contracts|Resilience and communication|Observability and deployment', 'Experience building a web application or service.', 'cloud.svg'),
 ('Maven Build Tools', 'Java Development', 'BEGINNER', '180', 'Manage Java builds, dependencies and repeatable project commands.', 'Read and shape a Maven project, understand dependency scopes and plugins, and create predictable packaging and verification steps.', 'Project structure and lifecycle|Dependencies and plugins|Repeatable builds', 'Basic Java project familiarity.', 'devops.svg'),
 ('JUnit 5 Testing', 'Java Development', 'INTERMEDIATE', '240', 'Write focused Java tests with JUnit 5 and useful test fixtures.', 'Build readable unit and parameterized tests, isolate collaborators and organize test coverage around application behavior.', 'Assertions and test lifecycle|Parameterized tests and fixtures|Test boundaries and coverage', 'Basic Java programming.', 'testing.svg'),
 ('MongoDB Fundamentals', 'Database', 'BEGINNER', '240', 'Model document data and query MongoDB collections.', 'Compare document and relational modeling choices, then practice CRUD operations, indexes and aggregation pipelines on example data.', 'Documents and collection design|Queries and indexes|Aggregation pipelines', 'Basic data modeling concepts.', 'database.svg'),
 ('Statistics for Data Science', 'Data Science', 'BEGINNER', '300', 'Use descriptive statistics and probability to interpret datasets.', 'Build intuition for distributions, sampling and uncertainty, then select summary measures that fit the data and question.', 'Distributions and summary statistics|Sampling and probability|Uncertainty and interpretation', 'Comfort with arithmetic and reading charts.', 'analytics.svg'),
 ('Pandas for Data Analysis', 'Data Science', 'BEGINNER', '240', 'Clean, join and summarize tabular data using Pandas.', 'Work with DataFrames, handle missing values, combine datasets and produce repeatable summaries for a small analysis workflow.', 'DataFrames and data cleaning|Joins and group summaries|Reproducible analysis steps', 'Basic Python familiarity.', 'analytics.svg'),
 ('NumPy Essentials', 'Data Science', 'INTERMEDIATE', '180', 'Use arrays and vectorized operations for numerical work.', 'Understand ndarray shapes, broadcasting and common numerical operations while comparing vectorized approaches with loops.', 'Array shapes and dtypes|Broadcasting and vectorization|Numerical workflows', 'Basic Python syntax.', 'analytics.svg'),
 ('Data Visualization Principles', 'Data Science', 'BEGINNER', '240', 'Choose clear charts and communicate what the data can support.', 'Practice chart selection, visual hierarchy and annotation while avoiding misleading encodings in data stories.', 'Chart selection and encodings|Visual hierarchy and annotation|Honest analytical storytelling', 'Basic spreadsheet or data familiarity.', 'analytics.svg'),
 ('Tableau Data Visualization', 'Data Science', 'INTERMEDIATE', '300', 'Create interactive Tableau views from shaped data.', 'Connect and prepare data, build calculated fields and assemble dashboards that guide viewers through a clear analytical question.', 'Connect and prepare data|Calculated fields and views|Dashboards and interaction', 'Basic data analysis concepts.', 'design.svg'),
 ('Microsoft Azure Fundamentals', 'Cloud and DevOps', 'BEGINNER', '300', 'Learn cloud infrastructure, identity and core Azure service concepts.', 'Connect cloud concepts to Azure compute, storage, networking and identity, using scenarios to reason about security and cost.', 'Cloud concepts and shared responsibility|Core Azure services|Identity, governance and cost', 'No cloud experience required.', 'cloud.svg'),
 ('Jenkins Pipeline Automation', 'Cloud and DevOps', 'INTERMEDIATE', '300', 'Automate build and test workflows with Jenkins pipelines.', 'Model pipeline stages, manage credentials safely and use automated feedback to make delivery workflows more repeatable.', 'Pipeline stages and Jenkinsfiles|Credentials and agents|Build feedback and artifacts', 'Git and basic command-line familiarity.', 'devops.svg'),
 ('Figma Interface Design', 'Design', 'BEGINNER', '240', 'Create organized interface mockups and reusable components in Figma.', 'Practice frames, layout systems, component variants and prototyping to communicate a focused user flow.', 'Frames and layout systems|Components and variants|Prototypes and handoff', 'No prior design tool experience required.', 'design.svg'),
 ('Selenium WebDriver Automation', 'Software Testing', 'INTERMEDIATE', '360', 'Automate browser checks with stable Selenium WebDriver patterns.', 'Build browser tests around user journeys, locate elements reliably and manage waits and test data to reduce brittle automation.', 'WebDriver basics and locators|Waits and resilient journeys|Test data and reporting', 'Basic programming and HTML familiarity.', 'testing.svg'),
 ('Manual Software Testing', 'Software Testing', 'BEGINNER', '210', 'Plan exploratory and scripted checks for product behavior.', 'Translate requirements into risk-based test ideas, report defects clearly and run focused regression checks across a feature.', 'Test conditions and coverage|Exploratory testing and defects|Regression and release checks', 'No prior testing experience required.', 'testing.svg'),
 ('Agile and Scrum Foundations', 'Professional Skills', 'BEGINNER', '180', 'Understand Scrum roles, events and practical team planning.', 'Use a small product scenario to practice backlog refinement, planning, review and retrospective habits with attention to collaboration.', 'Scrum accountabilities and events|Backlog and estimation|Review and team improvement', 'No prior Agile experience required.', 'communication.svg'),
 ('Natural Language Processing', 'AI and Machine Learning', 'INTERMEDIATE', '360', 'Explore text preparation, representation and language model tasks.', 'Work through tokenization, text features and task framing, then evaluate language system output for usefulness and limitations.', 'Text cleaning and tokenization|Representations and task design|Evaluation and limitations', 'Python and introductory machine learning familiarity.', 'ai.svg'),
 ('Generative AI Applications', 'AI and Machine Learning', 'INTERMEDIATE', '300', 'Design useful generative AI workflows with evaluation and safeguards.', 'Examine common model capabilities, structure application workflows and assess quality, privacy and reliability before deployment.', 'Generative model capabilities|Application workflows and grounding|Evaluation, privacy and safety', 'Basic software and AI familiarity.', 'ai.svg'),
 ('Prompt Engineering', 'AI and Machine Learning', 'BEGINNER', '150', 'Write, refine and evaluate prompts for common language tasks.', 'Practice clear instructions, context, examples and output constraints, then test prompt quality against a small rubric and representative cases.', 'Task framing and instruction design|Context and output constraints|Prompt evaluation and iteration', 'No prior AI experience required.', 'ai.svg'),
 ('React Projects and Architecture', 'Web Development', 'INTERMEDIATE', '360', 'Organize larger React interfaces around reusable feature boundaries.', 'Build an application flow with state ownership, data loading, route structure and accessible components while keeping modules easy to change.', 'Component and feature boundaries|State and data-loading patterns|Routing and accessibility', 'React fundamentals and JavaScript.', 'design.svg'),
 ('Spring Batch Processing', 'Java Development', 'ADVANCED', '300', 'Process larger datasets with restartable Spring Batch jobs.', 'Model jobs and steps, validate chunk-oriented processing and reason about retries, checkpoints and operational visibility.', 'Jobs, steps and readers|Chunk processing and restartability|Retries and operations', 'Spring Boot and relational data familiarity.', 'java.svg'),
 ('Linux Command Line', 'Cloud and DevOps', 'BEGINNER', '180', 'Navigate files, processes and permissions from a Linux shell.', 'Practice core commands, pipes, file permissions and process inspection using safe, repeatable terminal exercises.', 'Files and shell navigation|Pipes and text tools|Permissions and processes', 'No prior Linux experience required.', 'devops.svg'),
 ('API Testing with Postman', 'Software Testing', 'BEGINNER', '180', 'Create repeatable API checks using requests, variables and collections.', 'Build request collections, validate status and response data, and organize environment variables for repeatable API verification.', 'Requests and response assertions|Variables and environments|Collections and repeatable runs', 'Basic HTTP and JSON familiarity.', 'testing.svg'),
 ('GitHub Actions CI Workflows', 'Cloud and DevOps', 'INTERMEDIATE', '240', 'Automate repository checks with GitHub Actions workflows.', 'Define events, jobs and workflow steps, manage secrets appropriately and use status checks to support code review and release routines.', 'Workflow events and jobs|Actions, secrets and artifacts|Checks and release automation', 'Git and basic CI concepts.', 'devops.svg')
) AS s(title,category_name,level,duration,summary,description,outcomes,prerequisites,cover)
JOIN categories c ON c.name=s.category_name
WHERE NOT EXISTS (SELECT 1 FROM courses old WHERE lower(old.title)=lower(s.title));
INSERT INTO lessons(course_id,title,description,duration_minutes,position,preview,required)
SELECT c.id,x.lesson,'Guided study and practice: ' || x.lesson || ' as part of ' || c.title || '.',GREATEST(1,c.duration_minutes/3),x.position,x.position=1,TRUE
FROM courses c JOIN (VALUES
 ('Advanced Java Programming','Generics and type-safe APIs',1),
 ('Advanced Java Programming','Streams and functional composition',2),
 ('Advanced Java Programming','Concurrency and JVM fundamentals',3),
 ('Angular Application Development','Components and dependency injection',1),
 ('Angular Application Development','Routing and forms',2),
 ('Angular Application Development','HTTP services and application structure',3),
 ('REST API Design and Development','Resource-oriented endpoint design',1),
 ('REST API Design and Development','Validation and error handling',2),
 ('REST API Design and Development','Pagination and API contracts',3),
 ('Spring Framework Essentials','Dependency injection and beans',1),
 ('Spring Framework Essentials','Configuration and profiles',2),
 ('Spring Framework Essentials','Application context',3),
 ('Spring Security in Practice','Authentication and authorization',1),
 ('Spring Security in Practice','Request security configuration',2),
 ('Spring Security in Practice','Security testing',3),
 ('Spring Data JPA','Entity mapping and repositories',1),
 ('Spring Data JPA','Query methods and pagination',2),
 ('Spring Data JPA','Transactions and fetch strategies',3),
 ('Hibernate ORM','Entity relationships',1),
 ('Hibernate ORM','Persistence context and transactions',2),
 ('Hibernate ORM','Fetch plans and query tuning',3),
 ('Microservices Architecture','Service boundaries and contracts',1),
 ('Microservices Architecture','Resilience and communication',2),
 ('Microservices Architecture','Observability and deployment',3),
 ('Maven Build Tools','Project structure and lifecycle',1),
 ('Maven Build Tools','Dependencies and plugins',2),
 ('Maven Build Tools','Repeatable builds',3),
 ('JUnit 5 Testing','Assertions and test lifecycle',1),
 ('JUnit 5 Testing','Parameterized tests and fixtures',2),
 ('JUnit 5 Testing','Test boundaries and coverage',3),
 ('MongoDB Fundamentals','Documents and collection design',1),
 ('MongoDB Fundamentals','Queries and indexes',2),
 ('MongoDB Fundamentals','Aggregation pipelines',3),
 ('Statistics for Data Science','Distributions and summary statistics',1),
 ('Statistics for Data Science','Sampling and probability',2),
 ('Statistics for Data Science','Uncertainty and interpretation',3),
 ('Pandas for Data Analysis','DataFrames and data cleaning',1),
 ('Pandas for Data Analysis','Joins and group summaries',2),
 ('Pandas for Data Analysis','Reproducible analysis steps',3),
 ('NumPy Essentials','Array shapes and dtypes',1),
 ('NumPy Essentials','Broadcasting and vectorization',2),
 ('NumPy Essentials','Numerical workflows',3),
 ('Data Visualization Principles','Chart selection and encodings',1),
 ('Data Visualization Principles','Visual hierarchy and annotation',2),
 ('Data Visualization Principles','Honest analytical storytelling',3),
 ('Tableau Data Visualization','Connect and prepare data',1),
 ('Tableau Data Visualization','Calculated fields and views',2),
 ('Tableau Data Visualization','Dashboards and interaction',3),
 ('Microsoft Azure Fundamentals','Cloud concepts and shared responsibility',1),
 ('Microsoft Azure Fundamentals','Core Azure services',2),
 ('Microsoft Azure Fundamentals','Identity, governance and cost',3),
 ('Jenkins Pipeline Automation','Pipeline stages and Jenkinsfiles',1),
 ('Jenkins Pipeline Automation','Credentials and agents',2),
 ('Jenkins Pipeline Automation','Build feedback and artifacts',3),
 ('Figma Interface Design','Frames and layout systems',1),
 ('Figma Interface Design','Components and variants',2),
 ('Figma Interface Design','Prototypes and handoff',3),
 ('Selenium WebDriver Automation','WebDriver basics and locators',1),
 ('Selenium WebDriver Automation','Waits and resilient journeys',2),
 ('Selenium WebDriver Automation','Test data and reporting',3),
 ('Manual Software Testing','Test conditions and coverage',1),
 ('Manual Software Testing','Exploratory testing and defects',2),
 ('Manual Software Testing','Regression and release checks',3),
 ('Agile and Scrum Foundations','Scrum accountabilities and events',1),
 ('Agile and Scrum Foundations','Backlog and estimation',2),
 ('Agile and Scrum Foundations','Review and team improvement',3),
 ('Natural Language Processing','Text cleaning and tokenization',1),
 ('Natural Language Processing','Representations and task design',2),
 ('Natural Language Processing','Evaluation and limitations',3),
 ('Generative AI Applications','Generative model capabilities',1),
 ('Generative AI Applications','Application workflows and grounding',2),
 ('Generative AI Applications','Evaluation, privacy and safety',3),
 ('Prompt Engineering','Task framing and instruction design',1),
 ('Prompt Engineering','Context and output constraints',2),
 ('Prompt Engineering','Prompt evaluation and iteration',3),
 ('React Projects and Architecture','Component and feature boundaries',1),
 ('React Projects and Architecture','State and data-loading patterns',2),
 ('React Projects and Architecture','Routing and accessibility',3),
 ('Spring Batch Processing','Jobs, steps and readers',1),
 ('Spring Batch Processing','Chunk processing and restartability',2),
 ('Spring Batch Processing','Retries and operations',3),
 ('Linux Command Line','Files and shell navigation',1),
 ('Linux Command Line','Pipes and text tools',2),
 ('Linux Command Line','Permissions and processes',3),
 ('API Testing with Postman','Requests and response assertions',1),
 ('API Testing with Postman','Variables and environments',2),
 ('API Testing with Postman','Collections and repeatable runs',3),
 ('GitHub Actions CI Workflows','Workflow events and jobs',1),
 ('GitHub Actions CI Workflows','Actions, secrets and artifacts',2),
 ('GitHub Actions CI Workflows','Checks and release automation',3)
) AS x(course_title,lesson,position) ON x.course_title=c.title
WHERE NOT EXISTS (SELECT 1 FROM lessons l WHERE l.course_id=c.id);
