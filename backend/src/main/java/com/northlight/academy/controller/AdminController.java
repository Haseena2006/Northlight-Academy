package com.northlight.academy.controller;
import com.northlight.academy.dto.Dtos.*;
import com.northlight.academy.security.AuthUser;
import com.northlight.academy.service.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
@RestController @RequestMapping("/api/admin")
public class AdminController {
  private final AdminService admin; private final CourseService courses; private final LessonService lessons;
  public AdminController(AdminService admin, CourseService courses, LessonService lessons){ this.admin = admin; this.courses = courses; this.lessons = lessons; }
  @GetMapping("/dashboard") public AdminDashboard dashboard(){ return admin.dashboard(); }
  // courses
  @GetMapping("/courses") public PageDto<CourseSummary> courses(@RequestParam(required=false) String q, @RequestParam(required=false) Long categoryId, @RequestParam(required=false) String difficulty,
      @RequestParam(required=false) Integer minDuration, @RequestParam(required=false) Integer maxDuration,
      @RequestParam(required=false) String sort, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size){
    return courses.search(q, categoryId, difficulty, minDuration, maxDuration, sort, page, size, false);
  }
  @GetMapping("/courses/{id}") public CourseDetail course(@PathVariable Long id){ return courses.detail(id, false); }
  @PostMapping("/courses") public ResponseEntity<CourseDetail> create(@Valid @RequestBody CourseRequest r){ return ResponseEntity.status(HttpStatus.CREATED).body(courses.create(r)); }
  @PutMapping("/courses/{id}") public CourseDetail update(@PathVariable Long id, @Valid @RequestBody CourseRequest r){ return courses.update(id, r); }
  @DeleteMapping("/courses/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){ courses.delete(id); return ResponseEntity.noContent().build(); }
  @PatchMapping("/courses/{id}/status") public CourseSummary status(@PathVariable Long id, @RequestBody StatusRequest r){ return courses.setPublished(id, r.published()); }
  @PostMapping(value = "/courses/{id}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) public CourseSummary thumbnail(@PathVariable Long id, @RequestPart("file") MultipartFile file){ return courses.uploadThumbnail(id, file); }
  // categories
  @PostMapping("/categories") public ResponseEntity<CategoryDto> addCategory(@Valid @RequestBody CategoryRequest r){ return ResponseEntity.status(HttpStatus.CREATED).body(courses.saveCategory(null, r)); }
  @PutMapping("/categories/{id}") public CategoryDto editCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest r){ return courses.saveCategory(id, r); }
  @DeleteMapping("/categories/{id}") public ResponseEntity<Void> deleteCategory(@PathVariable Long id){ courses.deleteCategory(id); return ResponseEntity.noContent().build(); }
  // lessons
  @GetMapping("/courses/{id}/lessons") public List<LessonDto> lessons(@AuthenticationPrincipal AuthUser me, @PathVariable Long id){ return lessons.list(me.id(), id); }
  @PostMapping("/courses/{id}/lessons") public ResponseEntity<LessonDto> addLesson(@AuthenticationPrincipal AuthUser me, @PathVariable Long id, @Valid @RequestBody LessonRequest r){ return ResponseEntity.status(HttpStatus.CREATED).body(lessons.create(me.id(), id, r)); }
  @PutMapping("/courses/{id}/lessons/reorder") public List<LessonDto> reorder(@AuthenticationPrincipal AuthUser me, @PathVariable Long id, @Valid @RequestBody ReorderRequest r){ return lessons.reorder(me.id(), id, r.lessonIds()); }
  @PutMapping("/lessons/{id}") public LessonDto editLesson(@AuthenticationPrincipal AuthUser me, @PathVariable Long id, @Valid @RequestBody LessonRequest r){ return lessons.update(me.id(), id, r); }
  @DeleteMapping("/lessons/{id}") public ResponseEntity<Void> deleteLesson(@AuthenticationPrincipal AuthUser me, @PathVariable Long id){ lessons.delete(me.id(), id); return ResponseEntity.noContent().build(); }
  // users
  @GetMapping("/students") public PageDto<UserRow> students(@RequestParam(required=false) String q, @RequestParam(required=false) Boolean active, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size){ return admin.users("STUDENT", q, active, page, size); }
  @GetMapping("/instructors") public PageDto<UserRow> instructors(@RequestParam(required=false) String q, @RequestParam(required=false) Boolean active, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size){ return admin.users("INSTRUCTOR", q, active, page, size); }
  @GetMapping("/instructors/{id}") public UserRow instructor(@PathVariable Long id){ return admin.instructor(id); }
  @PostMapping("/instructors") public ResponseEntity<UserRow> createInstructor(@Valid @RequestBody InstructorCreateRequest r){ return ResponseEntity.status(HttpStatus.CREATED).body(admin.createInstructor(r)); }
  @PutMapping("/instructors/{id}/courses") public UserRow assign(@PathVariable Long id, @RequestBody AssignRequest r){ return admin.assign(id, r.courseIds()); }
  @PatchMapping("/users/{id}/status") public UserRow active(@PathVariable Long id, @RequestBody ActiveRequest r){ return admin.setActive(id, r.active()); }
  @GetMapping("/enrollments") public PageDto<EnrollmentRow> enrollments(@RequestParam(required=false) Long courseId, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size){ return admin.enrollments(courseId, page, size); }
}
