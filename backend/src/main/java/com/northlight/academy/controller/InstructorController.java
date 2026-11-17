package com.northlight.academy.controller;
import com.northlight.academy.dto.Dtos.*;
import com.northlight.academy.security.AuthUser;
import com.northlight.academy.service.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/instructor")
public class InstructorController {
  private final InstructorService instructors; private final LessonService lessons;
  public InstructorController(InstructorService instructors, LessonService lessons){ this.instructors = instructors; this.lessons = lessons; }
  @GetMapping("/dashboard") public InstructorDashboard dashboard(@AuthenticationPrincipal AuthUser me){ return instructors.dashboard(me.id()); }
  @GetMapping("/courses") public List<CourseSummary> courses(@AuthenticationPrincipal AuthUser me){ return instructors.myCourses(me.id()); }
  @GetMapping("/courses/{courseId}/lessons") public List<LessonDto> lessons(@AuthenticationPrincipal AuthUser me, @PathVariable Long courseId){ return lessons.list(me.id(), courseId); }
  @PostMapping("/courses/{courseId}/lessons") public ResponseEntity<LessonDto> add(@AuthenticationPrincipal AuthUser me, @PathVariable Long courseId, @Valid @RequestBody LessonRequest r){ return ResponseEntity.status(HttpStatus.CREATED).body(lessons.create(me.id(), courseId, r)); }
  @PutMapping("/courses/{courseId}/lessons/reorder") public List<LessonDto> reorder(@AuthenticationPrincipal AuthUser me, @PathVariable Long courseId, @Valid @RequestBody ReorderRequest r){ return lessons.reorder(me.id(), courseId, r.lessonIds()); }
  @PutMapping("/lessons/{lessonId}") public LessonDto edit(@AuthenticationPrincipal AuthUser me, @PathVariable Long lessonId, @Valid @RequestBody LessonRequest r){ return lessons.update(me.id(), lessonId, r); }
  @DeleteMapping("/lessons/{lessonId}") public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthUser me, @PathVariable Long lessonId){ lessons.delete(me.id(), lessonId); return ResponseEntity.noContent().build(); }
  @GetMapping("/courses/{courseId}/students") public List<EnrollmentRow> students(@AuthenticationPrincipal AuthUser me, @PathVariable Long courseId){ return instructors.students(me.id(), courseId); }
}
