package com.northlight.academy;
import com.northlight.academy.entity.*;
import com.northlight.academy.repository.*;
import com.fasterxml.jackson.databind.*;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class ApiIntegrationTest {
  @Autowired MockMvc mvc; @Autowired ObjectMapper om; @Autowired UserRepository users; @Autowired RoleRepository roles; @Autowired PasswordEncoder enc;
  @Autowired CourseRepository courses; @Autowired CategoryRepository categories; @Autowired LessonRepository lessons;
  static final String PW = "Secret123";
  static final String H = "X-Requested-With", HV = "NorthlightWeb";

  String email(){ return "u" + UUID.randomUUID().toString().substring(0, 8) + "@test.com"; }
  void register(String email) throws Exception {
    mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(java.util.Map.of("fullName","Test User","email",email,"password",PW)))).andExpect(status().isCreated());
  }
  MvcResult login(String email, String pw) throws Exception {
    return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(java.util.Map.of("email",email,"password",pw,"rememberMe",true)))).andReturn();
  }
  String token(String email) throws Exception { MvcResult r = login(email, PW); assertEquals(200, r.getResponse().getStatus()); return om.readTree(r.getResponse().getContentAsString()).get("accessToken").asText(); }
  String bearer(String t){ return "Bearer " + t; }

  @Test void sampleCatalogueIsDatabaseBackedWithLessonsAndCoverUrls() throws Exception {
    JsonNode page = om.readTree(mvc.perform(get("/api/courses").param("page", "0").param("size", "50"))
      .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    assertTrue(page.get("totalElements").asLong() >= 61);
    JsonNode python = null;
    for (JsonNode course : page.get("content")) if (course.get("title").asText().equals("Python for Beginners to Advanced")) python = course;
    assertNotNull(python);
    assertTrue(python.get("thumbnailUrl").asText().startsWith("/images/courses/"));
    JsonNode detail = om.readTree(mvc.perform(get("/api/courses/" + python.get("id").asLong()))
      .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    assertEquals(3, detail.get("lessons").size());
    assertFalse(detail.get("lessons").get(0).get("description").asText().isBlank());
    assertTrue(detail.get("lessons").get(1).get("description").isNull()); // keep full lesson readings behind enrollment
    assertTrue(detail.get("learningOutcomes").size() > 0);
    assertTrue(detail.get("prerequisites").size() > 0);

    String studentEmail = email(); register(studentEmail); String studentToken = token(studentEmail);
    mvc.perform(post("/api/student/enrollments/" + python.get("id").asLong()).header("Authorization", bearer(studentToken)))
      .andExpect(status().isCreated());
    JsonNode learning = om.readTree(mvc.perform(get("/api/student/courses/" + python.get("id").asLong() + "/lessons")
      .header("Authorization", bearer(studentToken))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    assertTrue(learning.get("lessons").get(0).get("description").asText().contains("## Detailed topic explanation"));
  }

  @Test void publicCatalogueFiltersCoursesByDuration() throws Exception {
    JsonNode page = om.readTree(mvc.perform(get("/api/courses").param("minDuration", "300").param("maxDuration", "360").param("size", "50"))
      .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    assertTrue(page.get("totalElements").asLong() > 0);
    for (JsonNode course : page.get("content")) {
      assertTrue(course.get("durationMinutes").asInt() >= 300);
      assertTrue(course.get("durationMinutes").asInt() <= 360);
    }
  }

  @Test void catalogueSearchFindsCourseTopicsAndSupportsNameSorting() throws Exception {
    for (String term : java.util.List.of("Java", "Spring", "Python", "React", "SQL", "AI")) {
      JsonNode found = om.readTree(mvc.perform(get("/api/courses").param("q", term).param("size", "50"))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
      assertTrue(found.get("totalElements").asLong() > 0, "Expected search results for " + term);
    }
    JsonNode sorted = om.readTree(mvc.perform(get("/api/courses").param("sort", "title-desc").param("size", "50"))
      .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    assertTrue(sorted.get("content").get(0).get("title").asText().compareTo(sorted.get("content").get(1).get("title").asText()) > 0);
  }
  User makeUser(String role) {
    User u = new User(); u.setFullName(role + " User"); u.setEmail(email()); u.setPasswordHash(enc.encode(PW)); u.getRoles().add(roles.findByName(role).orElseThrow()); return users.save(u);
  }
  Course makeCourse(boolean published, int lessonCount, User... instructors){
    Category cat = categories.findAll().get(0);
    Course c = new Course(); c.setTitle("Test course " + UUID.randomUUID()); c.setShortDescription("s"); c.setDescription("d"); c.setCategory(cat); c.setDifficulty(Difficulty.BEGINNER); c.setDurationMinutes(30); c.setPublished(published);
    for (User i : instructors) c.getInstructors().add(i);
    c = courses.save(c);
    for (int i = 1; i <= lessonCount; i++) { Lesson l = new Lesson(); l.setCourse(c); l.setTitle("L" + i); l.setPosition(i); lessons.save(l); }
    return c;
  }

  @Test void registrationCreatesStudentAndNeverExposesHash() throws Exception {
    String e = email();
    String body = om.writeValueAsString(java.util.Map.of("fullName","Eve","email",e,"password",PW,"role","ADMIN"));
    String res = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    assertFalse(res.toLowerCase().contains("password"));
    JsonNode roles = om.readTree(res).get("roles");
    assertEquals(1, roles.size()); assertEquals("STUDENT", roles.get(0).asText());
    assertTrue(users.findByEmail(e).orElseThrow().getPasswordHash().startsWith("$2"));   // BCrypt
  }
  @Test void duplicateAndWeakPasswordRejected() throws Exception {
    String e = email(); register(e);
    mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(java.util.Map.of("fullName","X","email",e,"password",PW)))).andExpect(status().isConflict());
    mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(java.util.Map.of("fullName","X","email",email(),"password","weak")))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.password").exists());
  }
  @Test void loginValidAndInvalid() throws Exception {
    String e = email(); register(e);
    MvcResult ok = login(e, PW);
    assertEquals(200, ok.getResponse().getStatus());
    String setCookie = ok.getResponse().getHeader("Set-Cookie");
    assertTrue(setCookie.contains("HttpOnly") && setCookie.contains("SameSite=Strict"));
    assertEquals(401, login(e, "Wrong1234").getResponse().getStatus());
  }
  @Test void refreshRotatesAndReuseRevokesFamily() throws Exception {
    String e = email(); register(e);
    Cookie c1 = login(e, PW).getResponse().getCookie("nl_refresh");
    MvcResult r2 = mvc.perform(post("/api/auth/refresh").cookie(c1).header(H, HV)).andExpect(status().isOk()).andReturn();
    Cookie c2 = r2.getResponse().getCookie("nl_refresh"); assertNotEquals(c1.getValue(), c2.getValue());
    mvc.perform(post("/api/auth/refresh").cookie(c1).header(H, HV)).andExpect(status().isUnauthorized());          // reuse of rotated token
    mvc.perform(post("/api/auth/refresh").cookie(c2).header(H, HV)).andExpect(status().isUnauthorized());          // family revoked
    mvc.perform(post("/api/auth/refresh").cookie(c2)).andExpect(status().isForbidden());                            // CSRF header required
  }
  @Test void logoutRevokesRefreshToken() throws Exception {
    String e = email(); register(e);
    Cookie c = login(e, PW).getResponse().getCookie("nl_refresh");
    mvc.perform(post("/api/auth/logout").cookie(c).header(H, HV)).andExpect(status().isOk());
    mvc.perform(post("/api/auth/refresh").cookie(c).header(H, HV)).andExpect(status().isUnauthorized());
  }
  @Test void roleAuthorization() throws Exception {
    mvc.perform(get("/api/student/dashboard")).andExpect(status().isUnauthorized());
    String e = email(); register(e); String t = token(e);
    mvc.perform(get("/api/student/dashboard").header("Authorization", bearer(t))).andExpect(status().isOk());
    mvc.perform(get("/api/admin/dashboard").header("Authorization", bearer(t))).andExpect(status().isForbidden());
    mvc.perform(get("/api/instructor/dashboard").header("Authorization", bearer(t))).andExpect(status().isForbidden());
    mvc.perform(get("/api/student/dashboard").header("Authorization", bearer(t + "x"))).andExpect(status().isUnauthorized());
  }
  @Test void adminCourseCrudSearchAndDeleteGuard() throws Exception {
    User admin = makeUser("ADMIN"); String t = token(admin.getEmail());
    Long catId = categories.findAll().get(0).getId();
    String title = "Zebra course " + UUID.randomUUID();
    String body = om.writeValueAsString(java.util.Map.of("title",title,"shortDescription","short","description","long","categoryId",catId,"difficulty","BEGINNER","durationMinutes",60,"learningOutcomes",java.util.List.of("A","B"),"instructorIds",java.util.List.of()));
    String created = mvc.perform(post("/api/admin/courses").header("Authorization", bearer(t)).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    long id = om.readTree(created).get("summary").get("id").asLong();
    // cannot publish without lessons
    mvc.perform(patch("/api/admin/courses/" + id + "/status").header("Authorization", bearer(t)).contentType(MediaType.APPLICATION_JSON).content("{\"published\":true}")).andExpect(status().isBadRequest());
    mvc.perform(post("/api/admin/courses/" + id + "/lessons").header("Authorization", bearer(t)).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Intro\",\"durationMinutes\":5,\"preview\":true}")).andExpect(status().isCreated());
    mvc.perform(patch("/api/admin/courses/" + id + "/status").header("Authorization", bearer(t)).contentType(MediaType.APPLICATION_JSON).content("{\"published\":true}")).andExpect(status().isOk());
    mvc.perform(get("/api/courses").param("q", "zebra course")).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].title").value(title));
    // invalid input
    mvc.perform(post("/api/admin/courses").header("Authorization", bearer(t)).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\"}")).andExpect(status().isBadRequest());
    // update then delete
    String upd = body.replace(title, title + " v2");
    mvc.perform(put("/api/admin/courses/" + id).header("Authorization", bearer(t)).contentType(MediaType.APPLICATION_JSON).content(upd)).andExpect(status().isOk());
    // enrolled courses cannot be deleted
    String se = email(); register(se); String st = token(se);
    mvc.perform(post("/api/student/enrollments/" + id).header("Authorization", bearer(st))).andExpect(status().isCreated());
    mvc.perform(delete("/api/admin/courses/" + id).header("Authorization", bearer(t))).andExpect(status().isConflict());
    Course free = makeCourse(false, 1);
    mvc.perform(delete("/api/admin/courses/" + free.getId()).header("Authorization", bearer(t))).andExpect(status().isNoContent());
  }
  @Test void enrollmentProgressCertificateAndOwnership() throws Exception {
    Course c = makeCourse(true, 2);
    String e1 = email(), e2 = email(); register(e1); register(e2); String t1 = token(e1), t2 = token(e2);
    mvc.perform(post("/api/student/enrollments/" + c.getId()).header("Authorization", bearer(t1))).andExpect(status().isCreated());
    mvc.perform(post("/api/student/enrollments/" + c.getId()).header("Authorization", bearer(t1))).andExpect(status().isConflict());     // duplicate prevented
    var ls = lessons.findByCourseIdOrderByPositionAsc(c.getId());
    String p1 = om.writeValueAsString(java.util.Map.of("courseId", c.getId(), "lessonId", ls.get(0).getId()));
    // not enrolled -> forbidden
    mvc.perform(post("/api/student/progress").header("Authorization", bearer(t2)).contentType(MediaType.APPLICATION_JSON).content(p1)).andExpect(status().isForbidden());
    mvc.perform(get("/api/student/courses/" + c.getId() + "/lessons").header("Authorization", bearer(t2))).andExpect(status().isForbidden());
    mvc.perform(post("/api/student/progress").header("Authorization", bearer(t1)).contentType(MediaType.APPLICATION_JSON).content(p1)).andExpect(status().isOk()).andExpect(jsonPath("$.progress").value(50)).andExpect(jsonPath("$.status").value("ACTIVE"));
    mvc.perform(post("/api/student/progress").header("Authorization", bearer(t1)).contentType(MediaType.APPLICATION_JSON).content(p1)).andExpect(status().isOk()).andExpect(jsonPath("$.completedLessons").value(1));  // idempotent
    mvc.perform(get("/api/student/certificates").header("Authorization", bearer(t1))).andExpect(jsonPath("$.length()").value(0));       // not eligible yet
    String p2 = om.writeValueAsString(java.util.Map.of("courseId", c.getId(), "lessonId", ls.get(1).getId()));
    String done = mvc.perform(post("/api/student/progress").header("Authorization", bearer(t1)).contentType(MediaType.APPLICATION_JSON).content(p2)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED")).andReturn().getResponse().getContentAsString();
    String code = om.readTree(done).get("certificateCode").asText();
    byte[] pdf = mvc.perform(get("/api/student/certificates/" + code + "/download").header("Authorization", bearer(t1))).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
    assertEquals("%PDF", new String(pdf, 0, 4));
    mvc.perform(get("/api/student/certificates/" + code + "/download").header("Authorization", bearer(t2))).andExpect(status().isNotFound());   // other student
    mvc.perform(get("/api/certificates/" + code + "/verify")).andExpect(status().isOk()).andExpect(jsonPath("$.valid").value(true));
    mvc.perform(get("/api/certificates/NLA-NOPE/verify")).andExpect(jsonPath("$.valid").value(false));
  }
  @Test void instructorOnlyManagesAssignedCourses() throws Exception {
    User i1 = makeUser("INSTRUCTOR"), i2 = makeUser("INSTRUCTOR");
    Course mine = makeCourse(false, 1, i1), other = makeCourse(false, 1, i2);
    String t = token(i1.getEmail());
    String lesson = "{\"title\":\"New\",\"durationMinutes\":3,\"preview\":false,\"videoUrl\":\"https://example.com/v.mp4\"}";
    mvc.perform(post("/api/instructor/courses/" + mine.getId() + "/lessons").header("Authorization", bearer(t)).contentType(MediaType.APPLICATION_JSON).content(lesson)).andExpect(status().isCreated());
    mvc.perform(post("/api/instructor/courses/" + other.getId() + "/lessons").header("Authorization", bearer(t)).contentType(MediaType.APPLICATION_JSON).content(lesson)).andExpect(status().isForbidden());
    mvc.perform(get("/api/instructor/courses/" + other.getId() + "/students").header("Authorization", bearer(t))).andExpect(status().isForbidden());
    Long foreignLesson = lessons.findByCourseIdOrderByPositionAsc(other.getId()).get(0).getId();
    mvc.perform(delete("/api/instructor/lessons/" + foreignLesson).header("Authorization", bearer(t))).andExpect(status().isForbidden());
    mvc.perform(post("/api/instructor/courses/" + mine.getId() + "/lessons").header("Authorization", bearer(t)).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Bad\",\"videoUrl\":\"javascript:alert(1)\"}")).andExpect(status().isBadRequest());
  }
}
