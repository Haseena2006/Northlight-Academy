package com.northlight.academy.controller;
import com.northlight.academy.dto.Dtos.*;
import com.northlight.academy.security.AuthUser;
import com.northlight.academy.service.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/student")
public class StudentController {
  private final StudentService students; private final CertificateService certs;
  public StudentController(StudentService students, CertificateService certs){ this.students = students; this.certs = certs; }
  @GetMapping("/dashboard") public StudentDashboard dashboard(@AuthenticationPrincipal AuthUser me){ return students.dashboard(me.id()); }
  @GetMapping("/enrollments") public List<EnrollmentDto> enrollments(@AuthenticationPrincipal AuthUser me){ return students.myEnrollments(me.id()); }
  @PostMapping("/enrollments/{courseId}") public ResponseEntity<EnrollmentDto> enroll(@AuthenticationPrincipal AuthUser me, @PathVariable Long courseId){
    return ResponseEntity.status(HttpStatus.CREATED).body(students.enroll(me.id(), courseId));
  }
  @GetMapping("/courses/{courseId}/lessons") public LearningDto lessons(@AuthenticationPrincipal AuthUser me, @PathVariable Long courseId){ return students.learning(me.id(), courseId); }
  @PostMapping("/progress") public ProgressDto complete(@AuthenticationPrincipal AuthUser me, @Valid @RequestBody ProgressRequest r){ return students.complete(me.id(), r); }
  @PostMapping("/progress/last-accessed") public ResponseEntity<Void> last(@AuthenticationPrincipal AuthUser me, @Valid @RequestBody ProgressRequest r){ students.lastAccessed(me.id(), r.courseId(), r.lessonId()); return ResponseEntity.noContent().build(); }
  @GetMapping("/progress/{courseId}") public ProgressDto progress(@AuthenticationPrincipal AuthUser me, @PathVariable Long courseId){ return students.progress(me.id(), courseId); }
  @GetMapping("/certificates") public List<CertificateDto> certificates(@AuthenticationPrincipal AuthUser me){ return certs.forStudent(me.id()); }
  @GetMapping("/certificates/{code}/download") public ResponseEntity<byte[]> download(@AuthenticationPrincipal AuthUser me, @PathVariable String code){
    byte[] pdf = certs.pdf(me.id(), code);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
      .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("northlight-certificate-" + code + ".pdf").build().toString())
      .header(HttpHeaders.CACHE_CONTROL, "no-store").body(pdf);
  }
}
