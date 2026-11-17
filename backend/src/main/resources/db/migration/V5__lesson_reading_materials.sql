-- Convert the catalogue's generated lesson summaries into structured reading
-- notes. Only known seed text and empty descriptions are replaced; authored
-- lesson descriptions are left intact. Lesson ids and progress rows are unchanged.
WITH materials AS (
  SELECT l.id AS lesson_id,
         c.title AS course_title,
         l.title AS lesson_title,
         cat.name AS category_name,
         COALESCE(NULLIF(c.learning_outcomes, ''), c.short_description) AS outcomes,
         CASE
           WHEN c.title LIKE 'C Programming%' THEN 'C makes data representation and memory ownership explicit. A function receives values or addresses, performs one focused operation, and returns a result that the caller must check. The lesson focus, “' || l.title || ',” should be practiced by tracing the value of each variable and identifying which function owns each change.'
           WHEN c.title LIKE 'C++%' THEN 'Modern C++ combines direct control over resources with abstractions such as classes, standard-library containers and algorithms. The lesson focus, “' || l.title || ',” is easier to reason about when object lifetime and ownership are made visible rather than handled through informal convention.'
           WHEN c.title LIKE 'Python%' THEN 'Python executes readable statements over values whose types can be inspected at runtime. The lesson focus, “' || l.title || ',” should be approached by checking the shape and meaning of each value before transforming it; this prevents a convenient expression from hiding a data-quality problem.'
           WHEN cat.name = 'Java Development' THEN 'Java programs are built from typed values and collaborating classes. The compiler checks many mistakes before the program runs, while the JVM executes the resulting bytecode. For “' || l.title || ',” follow the data from its input, through the responsible method or object, to the observable result.'
           WHEN cat.name = 'Web Development' THEN 'A web feature is a flow between a browser, its rendered interface and the data or service behind it. “' || l.title || '” belongs in that flow: identify what the user does, what state changes, and how the interface communicates success, loading and failure.'
           WHEN cat.name = 'Database' AND c.title LIKE 'MongoDB%' THEN 'Document databases store related fields together in flexible documents. “' || l.title || '” should be considered alongside the query patterns that read and update those documents; a convenient document shape is useful only when it supports correct, bounded operations.'
           WHEN cat.name = 'Database' THEN 'Relational data is represented as rows with declared relationships and constraints. “' || l.title || '” must preserve the meaning of each row and the rules between tables; joins, transactions and indexes are useful only when they match those rules and the questions the application asks.'
           WHEN cat.name = 'Cloud and DevOps' THEN 'A cloud service is operated as a repeatable system: configuration, artifact, deployment, health signal and recovery plan all matter. “' || l.title || '” is applied by making the change observable and reversible, then checking the service from the point of view of its users.'
           WHEN cat.name = 'AI and Machine Learning' THEN 'A machine-learning result depends on the task definition, representative data, a chosen method and an evaluation that reflects the real cost of mistakes. “' || l.title || '” should be assessed against a baseline and known limitations rather than treated as a model score in isolation.'
           WHEN cat.name = 'Data Science' THEN 'Data analysis turns a defined question and imperfect observations into a defensible conclusion. “' || l.title || '” is useful when the data grain, missing values, transformations and uncertainty are recorded so another analyst can reproduce the result.'
           WHEN cat.name = 'Design' THEN 'Interface design connects a user goal to a clear, accessible interaction. “' || l.title || '” should be evaluated in context: the user, task, information hierarchy, interaction states and accessibility needs all shape whether a visual choice works.'
           WHEN cat.name = 'Software Testing' THEN 'Software testing compares observed behavior with an explicit expectation. “' || l.title || '” becomes useful when a check has a clear precondition, action, expected result and evidence; that makes failures reproducible and helps the team choose the right next step.'
           WHEN cat.name = 'Professional Skills' THEN 'Professional practice depends on a clear outcome, an audience and a respectful exchange of information. “' || l.title || '” can be improved by making intent explicit, checking shared understanding and agreeing on a practical next action.'
           WHEN cat.name = 'Cybersecurity' THEN 'Defensive security starts with an asset, a plausible threat, a control and a way to notice when the control is failing. “' || l.title || '” should be applied proportionately, with least privilege, useful evidence and a recovery path in mind.'
           ELSE 'This lesson develops “' || l.title || '” within ' || c.title || '. Relate each new term to the course outcome it supports, then check the idea against a realistic task rather than memorizing a definition alone.'
         END AS explanation,
         CASE
           WHEN c.title LIKE 'C Programming%' THEN '**Function:** a named block that receives arguments and returns a value or status. **Pointer:** a value that identifies a memory location; dereferencing it is safe only while that location is valid. **Lesson connection:** ' || l.title || ' should make the flow of values and ownership visible.'
           WHEN c.title LIKE 'C++%' THEN '**Value semantics:** copying an object produces an independent value unless the type defines another behavior. **RAII:** a resource is acquired and released through an object lifetime. **Lesson connection:** ' || l.title || ' should leave ownership and cleanup clear.'
           WHEN c.title LIKE 'Python%' THEN '**Iterable:** a value that can supply items one at a time. **Transformation:** a deliberate change from an input representation to an output representation. **Lesson connection:** ' || l.title || ' should preserve the meaning of each row while making the transformation repeatable.'
           WHEN cat.name = 'Java Development' THEN '**Type:** a compile-time contract describing which values and operations are valid. **Object boundary:** the point where one class exposes behavior while protecting its internal state. **Lesson connection:** ' || l.title || ' should make responsibilities and invalid states easier to recognize.'
           WHEN cat.name = 'Web Development' THEN '**Component:** a reusable unit that renders part of an interface. **State:** the data that determines the current view. **Lesson connection:** ' || l.title || ' should keep user action, state change and rendered feedback aligned.'
           WHEN cat.name = 'Database' THEN '**Schema:** the declared shape and constraints of stored data. **Cardinality:** how many records on one side can relate to a record on the other. **Lesson connection:** ' || l.title || ' should preserve the intended grain of the data.'
           WHEN cat.name = 'Cloud and DevOps' THEN '**Artifact:** the versioned package being deployed. **Health check:** an observable signal that the service can perform useful work. **Lesson connection:** ' || l.title || ' should be repeatable and have a recovery path.'
           WHEN cat.name IN ('Data Science','AI and Machine Learning') THEN '**Feature:** an input used in an analysis or model. **Baseline:** a simple reference result used to judge whether a more complex method helps. **Lesson connection:** ' || l.title || ' must be evaluated against representative data and known uncertainty.'
           WHEN cat.name = 'Design' THEN '**Hierarchy:** the ordering that helps people notice and understand information. **Affordance:** a visible cue about how an element can be used. **Lesson connection:** ' || l.title || ' should support the user task across normal and error states.'
           WHEN cat.name = 'Software Testing' THEN '**Precondition:** the state required before a check runs. **Test oracle:** the rule used to decide whether the observed result is correct. **Lesson connection:** ' || l.title || ' needs both a reproducible action and a clear expected result.'
           WHEN cat.name = 'Professional Skills' THEN '**Audience:** the people whose knowledge or decision the communication must support. **Feedback:** specific information about an observed action and its effect. **Lesson connection:** ' || l.title || ' should help the audience understand a useful next step.'
           WHEN cat.name = 'Cybersecurity' THEN '**Asset:** something that needs protection. **Threat:** a plausible cause of harm to that asset. **Control:** a measure that reduces likelihood or impact. **Lesson connection:** ' || l.title || ' should pair prevention with detection and recovery.'
           ELSE '**Focus:** ' || l.title || ' is the practical idea being developed. **Evidence:** an observable result that supports the course outcome. Use both to explain why the chosen approach is appropriate.'
         END AS concepts,
         CASE
           WHEN cat.name = 'Java Development' THEN 'Check that the example compiles, that each method has one clear responsibility, and that the result follows from the stated input. A passing run is evidence for this example, not proof that every boundary case is handled.'
           WHEN cat.name = 'Web Development' THEN 'Check the rendered state after user input, after a slow response and after an error. The interface is correct only when the visible feedback remains consistent with the underlying state.'
           WHEN cat.name = 'Database' THEN 'Check a hand-calculated small dataset against the query or model, then test duplicates, missing relationships and boundary values. This catches plausible-looking but incorrect aggregates.'
           WHEN cat.name IN ('Data Science','AI and Machine Learning') THEN 'Check the result on data that was not used to tune the method, compare it with a simple baseline and inspect representative errors. A single aggregate score can hide important failures.'
           WHEN cat.name = 'Cloud and DevOps' THEN 'Check the deployed version, configuration and health signal independently. A successful build is not evidence that the running service is reachable or safe to operate.'
           WHEN cat.name = 'Design' THEN 'Check the task with a real user or an accessibility review. A visually polished screen is not evidence that a person can complete the task.'
           WHEN cat.name = 'Software Testing' THEN 'Keep the input, expected result and observed result together. That evidence allows another person to repeat the check and distinguish a product defect from a setup problem.'
           WHEN cat.name = 'Professional Skills' THEN 'Check understanding by asking the recipient to restate the decision or next action. Clear wording is useful only if it supports the intended shared understanding.'
           WHEN cat.name = 'Cybersecurity' THEN 'Check that the control is enabled, the relevant event is observable, and an authorized responder knows how to contain and recover. A policy alone does not demonstrate protection.'
           ELSE 'Record the input, action and observed result so another learner can repeat the example and assess the conclusion.'
         END AS evidence_text,
         CASE
           WHEN c.title LIKE 'C Programming%' THEN 'A sensor-reading program receives a raw integer, checks that it is inside the device’s accepted range, and passes the validated value to a focused function. This makes “' || l.title || '” concrete: trace the value, the function boundary and the result the caller receives.'
           WHEN c.title LIKE 'C++%' THEN 'An inventory tool loads a group of product records, applies “' || l.title || '” to update one item, and releases temporary resources when the operation ends. A small, well-owned change is easier to test than a global mutable state change.'
           WHEN c.title LIKE 'Python%' OR cat.name IN ('Data Science','AI and Machine Learning') THEN 'A team receives a weekly sales file with inconsistent dates and missing values. Applying “' || l.title || '” means checking those cases before calculating a trend, then reporting both the result and the limitations of the source data.'
           WHEN cat.name = 'Java Development' THEN 'An order service receives a request, applies a rule related to “' || l.title || ',” and returns a result that can be checked independently. Separating input validation, the rule and the response makes the behavior easier to maintain and test.'
           WHEN cat.name = 'Web Development' THEN 'In a course portal, a learner opens a lesson, changes a filter or submits a form. Applying “' || l.title || '” means keeping the visible state consistent with the action and giving the learner a useful response if the request is delayed or fails.'
           WHEN cat.name = 'Database' THEN 'A reporting screen summarizes customer orders. Applying “' || l.title || '” means first deciding what one result row represents, then checking duplicates, missing relationships and the query plan before trusting the totals.'
           WHEN cat.name = 'Cloud and DevOps' THEN 'A team deploys a new service version to staging. Applying “' || l.title || '” means recording the exact artifact and configuration, checking a health signal after deployment, and having a tested rollback if the service degrades.'
           WHEN cat.name = 'Design' THEN 'A learner tests a course sign-up flow on a phone. Applying “' || l.title || '” means observing whether the next action is obvious, labels are understandable, and errors can be corrected without losing entered information.'
           WHEN cat.name = 'Software Testing' THEN 'A registration flow accepts valid details and rejects invalid ones. Applying “' || l.title || '” means documenting the starting state, input, expected message and actual result so a developer can reproduce a defect.'
           WHEN cat.name = 'Professional Skills' THEN 'A project update must help a teammate decide what to do next. Applying “' || l.title || '” means stating the situation, the evidence, the decision needed and the owner of the next action in language suited to that audience.'
           WHEN cat.name = 'Cybersecurity' THEN 'A sign-in service records an unusual burst of failed attempts. Applying “' || l.title || '” means confirming the signal, limiting the exposed account or endpoint, preserving relevant logs and following the organization’s recovery process.'
           ELSE 'Use a realistic task from ' || c.title || ' and apply “' || l.title || '” to one small, observable decision. Record the input, the action and the result so the example can be reviewed.'
         END AS example_text,
         CASE
           WHEN cat.name = 'Java Development' THEN E'1. State the behavior the lesson should produce.\n2. Identify the Java value, object or service responsible for it.\n3. Implement the smallest readable change and keep input validation at the boundary.\n4. Run the example or test, inspect the result, and revise the design if the behavior is difficult to explain.'
           WHEN cat.name = 'Web Development' THEN E'1. Write down the user action and the expected screen state.\n2. Identify the component, route or request that owns the change.\n3. Implement the normal, loading and error states.\n4. Test keyboard use, a narrow screen and a failed or delayed request.'
           WHEN cat.name = 'Database' THEN E'1. Define the question and the grain of one result row.\n2. Identify the tables, documents or constraints involved.\n3. Run the operation on representative and boundary data.\n4. Check duplicates, nulls, transaction behavior and performance before accepting the result.'
           WHEN cat.name IN ('Data Science','AI and Machine Learning') THEN E'1. State the question, target and unit of analysis.\n2. Inspect data types, missing values, class balance and possible leakage.\n3. Apply the transformation or method to a small, traceable sample.\n4. Compare with a baseline, interpret errors and record uncertainty and limitations.'
           WHEN cat.name = 'Cloud and DevOps' THEN E'1. Define the service outcome and the failure signal to watch.\n2. Make the artifact and configuration repeatable.\n3. Deploy to a limited environment and check health from outside the process.\n4. Record the rollback path and confirm that secrets and permissions are scoped appropriately.'
           WHEN cat.name = 'Design' THEN E'1. Name the user and the task they need to finish.\n2. Order information and actions by importance.\n3. Prototype the normal, empty and error states.\n4. Test with a person or assistive technology and revise based on observed friction.'
           WHEN cat.name = 'Software Testing' THEN E'1. Choose a risk and write a test condition.\n2. Set the preconditions and input.\n3. State the expected result before running the check.\n4. Compare actual behavior, capture evidence and report a reproducible defect when they differ.'
           WHEN cat.name = 'Professional Skills' THEN E'1. Identify the audience and the decision or understanding needed.\n2. Put the key point first and support it with specific evidence.\n3. Invite questions and confirm shared understanding.\n4. Close with an owner, next action and time to follow up.'
           WHEN cat.name = 'Cybersecurity' THEN E'1. Identify the asset and the threat scenario.\n2. Reduce exposure with least privilege and a proportionate control.\n3. Monitor a signal that shows whether the control is working.\n4. Preserve evidence and practice containment and recovery without exposing sensitive data.'
           ELSE E'1. State the lesson goal.\n2. Connect the idea to a course outcome.\n3. Apply it to a small example and check the result.\n4. Note one limitation and one follow-up question.'
         END AS steps,
         CASE
           WHEN c.title LIKE 'C Programming%' THEN E'```c\n#include <stdio.h>\n\nint main(void) {\n    int minutes = 30;\n    printf("Study block: %d minutes\\n", minutes);\n    return 0;\n}\n```\n\nExpected output: `Study block: 30 minutes`.'
           WHEN c.title LIKE 'C++%' THEN E'```cpp\n#include <iostream>\n#include <vector>\n\nint main() {\n    std::vector<int> checks{1, 2, 3};\n    std::cout << "Checks: " << checks.size() << "\\n";\n}\n```\n\nExpected output: `Checks: 3`.'
           WHEN c.title LIKE '%JUnit%' THEN E'```java\nimport static org.junit.jupiter.api.Assertions.assertEquals;\nimport org.junit.jupiter.api.Test;\n\nclass LessonTest {\n    @Test void countsThreeCheckpoints() {\n        assertEquals(3, java.util.List.of("input", "rule", "result").size());\n    }\n}\n```\n\nExpected result: the test passes. A failing assertion points to a mismatch between actual and expected behavior.'
           WHEN l.title ILIKE '%generic%' THEN E'```java\nimport java.util.List;\n\npublic class ReadingExample {\n    static <T> T first(List<T> values) {\n        return values.get(0);\n    }\n    public static void main(String[] args) {\n        System.out.println(first(List.of("lesson", "practice")));\n    }\n}\n```\n\nExpected output: `lesson`. The type parameter lets the method work with different element types without discarding compile-time type checking.'
           WHEN l.title ILIKE '%stream%' THEN E'```java\nimport java.util.List;\n\npublic class ReadingExample {\n    public static void main(String[] args) {\n        List<Integer> scores = List.of(55, 82, 91);\n        long passed = scores.stream().filter(score -> score >= 70).count();\n        System.out.println(passed);\n    }\n}\n```\n\nExpected output: `2`. The filter expresses the rule and count summarizes the matching values.'
           WHEN l.title ILIKE '%syntax and types%' THEN E'```java\npublic class ReadingExample {\n    public static void main(String[] args) {\n        int minutes = 30;\n        boolean ready = minutes > 0;\n        System.out.println(ready);\n    }\n}\n```\n\nExpected output: `true`. The declared types make the intended values explicit.'
           WHEN l.title ILIKE '%object-oriented%' OR l.title ILIKE '%encapsulation%' THEN E'```java\nclass Counter {\n    private int value;\n    void increment() { value++; }\n    int value() { return value; }\n}\n\npublic class ReadingExample {\n    public static void main(String[] args) {\n        Counter counter = new Counter();\n        counter.increment();\n        System.out.println(counter.value());\n    }\n}\n```\n\nExpected output: `1`. The object owns its state and exposes a small behavior-focused interface.'
           WHEN l.title ILIKE '%collections%' OR l.title ILIKE '%data structures%' OR l.title ILIKE '%search and sorting%' THEN E'```java\nimport java.util.ArrayList;\nimport java.util.Collections;\nimport java.util.List;\n\npublic class ReadingExample {\n    public static void main(String[] args) {\n        List<Integer> values = new ArrayList<>(List.of(3, 1, 2));\n        Collections.sort(values);\n        System.out.println(values);\n    }\n}\n```\n\nExpected output: `[1, 2, 3]`. A collection stores related values; sorting changes their order according to a defined comparison.'
           WHEN cat.name = 'Java Development' OR c.title LIKE '%Selenium%' THEN E'```java\nimport java.util.List;\n\npublic class ReadingExample {\n    public static void main(String[] args) {\n        List<String> checks = List.of("input", "rule", "result");\n        System.out.println("Checks ready: " + checks.size());\n    }\n}\n```\n\nExpected output: `Checks ready: 3`. The list contains three explicit checkpoints; use the lesson topic to decide what each checkpoint should verify.'
           WHEN cat.name = 'Web Development' THEN E'```javascript\nconst lessons = [\n  { title: "Intro", visible: true },\n  { title: "Practice", visible: false },\n  { title: "Review", visible: true },\n];\nconst visibleCount = lessons.filter((lesson) => lesson.visible).length;\nconsole.log(visibleCount);\n```\n\nExpected output: `2`. Filtering derives the display from data instead of maintaining a separate counter.'
           WHEN c.title LIKE '%MongoDB%' THEN E'```javascript\ndb.orders.find(\n  { status: "paid" },\n  { _id: 0, status: 1, total: 1 }\n);\n```\n\nExample result: `{ status: "paid", total: 42 }`. The filter selects matching documents and the projection limits returned fields.'
           WHEN cat.name = 'Database' THEN E'```sql\nWITH orders(status) AS (\n  VALUES (''paid''), (''paid''), (''pending'')\n)\nSELECT status, COUNT(*) AS order_count\nFROM orders\nGROUP BY status\nORDER BY status;\n```\n\nExpected rows: `paid | 2`, `pending | 1`. Grouping makes the unit of each result explicit.'
           WHEN cat.name IN ('Data Science','AI and Machine Learning') THEN E'```python\nvalues = [12, 15, 18]\nmean = sum(values) / len(values)\nprint(f"Mean: {mean:.1f}")\n```\n\nExpected output: `Mean: 15.0`. Before using a real dataset, check that it is non-empty and that missing values are handled intentionally.'
           WHEN c.title LIKE '%GitHub Actions%' THEN E'```yaml\nname: verify\non: [push, pull_request]\njobs:\n  checks:\n    runs-on: ubuntu-latest\n    steps:\n      - uses: actions/checkout@v4\n      - run: ./mvnw test\n```\n\nExpected behavior: a push or pull request starts the checks job; the workflow is green only when the command exits successfully.'
           WHEN c.title LIKE '%Postman%' THEN E'```http\nGET /api/courses/42\nAccept: application/json\n\nHTTP/1.1 200 OK\nContent-Type: application/json\n```\n\nExpected result: a successful response with JSON for course 42. Also test an unknown id and verify that the error status is intentional.'
           WHEN cat.name = 'Cloud and DevOps' THEN E'```sh\nprintf "%s\\n" "artifact: academy-api" "health: ready"\n```\n\nExpected output:\n```text\nartifact: academy-api\nhealth: ready\n```\nThe check is useful only when it runs against the deployed service and its real health signal.'
           ELSE NULL
         END AS code_example,
         COALESCE(NULLIF(c.learning_outcomes, ''), c.short_description) AS outcome_text,
         CASE
           WHEN cat.name = 'Java Development' THEN 'Review the contract, trace the Java data flow, run the example, and check the result against the lesson outcome.'
           WHEN cat.name = 'Web Development' THEN 'Review the user action, follow the state change through the interface, and verify normal and failure feedback.'
           WHEN cat.name = 'Database' THEN 'Review the data grain and constraints, check the operation on a small dataset, and verify the result and cost.'
           WHEN cat.name IN ('Data Science','AI and Machine Learning') THEN 'Review the question and data assumptions, compare the method with a baseline, and describe uncertainty and error.'
           WHEN cat.name = 'Cloud and DevOps' THEN 'Review the artifact and configuration, verify the deployed health signal, and confirm the recovery path.'
           WHEN cat.name = 'Design' THEN 'Review the user task, test the hierarchy and interaction, and revise what observation shows is unclear.'
           WHEN cat.name = 'Software Testing' THEN 'Review the precondition and expected result, repeat the check, and retain evidence for any mismatch.'
           WHEN cat.name = 'Professional Skills' THEN 'Review the audience and intended decision, check shared understanding, and agree on the next action.'
           WHEN cat.name = 'Cybersecurity' THEN 'Review the asset and threat, confirm the control and monitoring signal, and know the containment and recovery steps.'
           ELSE 'Review the lesson goal, repeat the example, and explain how the observed result supports the course outcome.'
         END AS revision_text
  FROM lessons l
  JOIN courses c ON c.id = l.course_id
  JOIN categories cat ON cat.id = c.category_id
)
UPDATE lessons target
SET description = '## Introduction' || E'\n\n' ||
  'This reading is part of **' || materials.course_title || '** and focuses on **' || materials.lesson_title || '**. It connects the lesson topic to the course outcome: ' || replace(materials.outcomes, '|', '; ') || '.' || E'\n\n' ||
  '## Detailed topic explanation' || E'\n\n' || materials.explanation || E'\n\n' ||
  '## Important concepts and definitions' || E'\n\n' ||
  materials.concepts || E'\n\n' ||
  '## Real-world example' || E'\n\n' || materials.example_text || E'\n\n' ||
  '## Step-by-step application' || E'\n\n' || materials.steps || E'\n5. Check your result against the course outcome: ' || replace(materials.outcomes, '|', '; ') || '.' || E'\n\n' ||
  COALESCE('## Example' || E'\n\n' || materials.code_example || E'\n\n', '') ||
  '## Key takeaways' || E'\n\n' ||
  '- Apply **' || materials.lesson_title || '** to a concrete task in **' || materials.course_title || '**.' || E'\n' ||
  '- ' || materials.evidence_text || ' In **' || materials.course_title || '**, use **' || materials.lesson_title || '** to check this outcome: ' || replace(materials.outcome_text, '|', '; ') || '.' || E'\n' ||
  '- Keep the course outcome in view: ' || replace(materials.outcome_text, '|', '; ') || '.' || E'\n\n' ||
  '## Revision summary' || E'\n\n' ||
  materials.revision_text || ' For **' || materials.course_title || '**, explain how **' || materials.lesson_title || '** supports: ' || replace(materials.outcome_text, '|', '; ') || '.'
FROM materials
WHERE target.id = materials.lesson_id
  AND (
    target.description IS NULL
    OR target.description LIKE 'Study % through worked examples, guided practice and a short application to %.'
    OR target.description LIKE 'Guided study and practice: % as part of %.'
    OR target.description IN (
      'How controllers, services and repositories fit together.',
      'Bean validation and centralised exception handling.',
      'Entities, repositories and transactions.',
      'Authentication, authorisation and token handling.',
      'Building blocks of a React UI.',
      'Hooks for local state and side effects.',
      'Client-side navigation with React Router.',
      'Tables, keys and constraints.',
      'Combining and summarising data.',
      'Reading EXPLAIN output.'
    )
  );
