package com.northlight.academy.service;
import com.northlight.academy.dto.Dtos.*;
import com.northlight.academy.entity.*;
import com.northlight.academy.exception.ApiExceptions.*;
import com.northlight.academy.mapper.Mapper;
import com.northlight.academy.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
@Service
public class StudentService {
  private final EnrollmentRepository enrollments; private final CourseRepository courses; private final UserRepository users; private final LessonRepository lessons;
  private final LessonProgressRepository progress; private final CertificateRepository certs; private final CertificateService certService; private final CourseService courseService;
  public StudentService(EnrollmentRepository enrollments, CourseRepository courses, UserRepository users, LessonRepository lessons, LessonProgressRepository progress, CertificateRepository certs, CertificateService certService, CourseService courseService){
    this.enrollments=enrollments; this.courses=courses; this.users=users; this.lessons=lessons; this.progress=progress; this.certs=certs; this.certService=certService; this.courseService=courseService;
  }
  static int percent(long done, long total){ return total <= 0 ? 0 : (int) Math.min(100, Math.round(done * 100.0 / total)); }

  @Transactional
  public EnrollmentDto enroll(Long studentId, Long courseId){
    Course c = courses.findById(courseId).filter(Course::isPublished).orElseThrow(() -> new NotFoundException("Course not found or not available"));
    if (enrollments.existsByStudentIdAndCourseId(studentId, courseId)) throw new ConflictException("You are already enrolled in this course");
    Enrollment e = new Enrollment(); e.setStudent(users.getReferenceById(studentId)); e.setCourse(c);
    return toDto(enrollments.save(e));
  }
  EnrollmentDto toDto(Enrollment e){
    int total = lessons.countByCourseId(e.getCourse().getId()); int done = progress.countByEnrollmentId(e.getId());
    int pct = e.getStatus() == EnrollmentStatus.COMPLETED ? 100 : percent(done, total);
    String code = certs.findByEnrollmentId(e.getId()).map(Certificate::getCertificateCode).orElse(null);
    return new EnrollmentDto(e.getId(), courseService.summary(e.getCourse()), e.getStatus().name(), pct, done, total, e.getLastLessonId(), e.getEnrolledAt(), e.getCompletedAt(), code);
  }
  @Transactional(readOnly = true)
  public List<EnrollmentDto> myEnrollments(Long studentId){ return enrollments.findByStudentIdOrderByEnrolledAtDesc(studentId).stream().map(this::toDto).toList(); }

  private Enrollment owned(Long studentId, Long courseId){
    return enrollments.findByStudentIdAndCourseId(studentId, courseId).orElseThrow(() -> new ForbiddenException("You are not enrolled in this course"));
  }
  @Transactional(readOnly = true)
  public LearningDto learning(Long studentId, Long courseId){
    Enrollment e = owned(studentId, courseId);
    Set<Long> done = new HashSet<>(); progress.findByEnrollmentId(e.getId()).forEach(p -> done.add(p.getLesson().getId()));
    List<LearningLesson> ls = lessons.findByCourseIdOrderByPositionAsc(courseId).stream()
      .map(l -> new LearningLesson(l.getId(), l.getTitle(), l.getDescription(), l.getVideoUrl(), l.getDurationMinutes(), l.getPosition(), done.contains(l.getId()))).toList();
    EnrollmentDto d = toDto(e);
    return new LearningDto(courseId, e.getCourse().getTitle(), d.progress(), d.status(), e.getLastLessonId(), ls);
  }
  @Transactional(readOnly = true)
  public ProgressDto progress(Long studentId, Long courseId){ return progressDto(owned(studentId, courseId)); }
  private ProgressDto progressDto(Enrollment e){
    EnrollmentDto d = toDto(e);
    return new ProgressDto(e.getCourse().getId(), d.completedLessons(), d.totalLessons(), d.progress(), d.status(), d.certificateCode());
  }
  /** Marks a lesson complete. Requires enrollment owned by the caller; idempotent; completes the course and issues a certificate when all required lessons are done. */
  @Transactional
  public ProgressDto complete(Long studentId, ProgressRequest r){
    Enrollment e = owned(studentId, r.courseId());
    Lesson l = lessons.findById(r.lessonId()).filter(x -> x.getCourse().getId().equals(r.courseId())).orElseThrow(() -> new NotFoundException("Lesson not found in this course"));
    if (!progress.existsByEnrollmentIdAndLessonId(e.getId(), l.getId())) {
      LessonProgress p = new LessonProgress(); p.setEnrollment(e); p.setLesson(l); progress.saveAndFlush(p);
    }
    e.setLastLessonId(l.getId());
    int required = lessons.countByCourseIdAndRequiredTrue(r.courseId());
    if (e.getStatus() != EnrollmentStatus.COMPLETED && required > 0 && progress.countRequiredDone(e.getId()) >= required) {
      e.setStatus(EnrollmentStatus.COMPLETED); e.setCompletedAt(Instant.now()); enrollments.save(e); certService.issue(e);
    } else enrollments.save(e);
    return progressDto(e);
  }
  @Transactional
  public void lastAccessed(Long studentId, Long courseId, Long lessonId){
    Enrollment e = owned(studentId, courseId);
    lessons.findById(lessonId).filter(x -> x.getCourse().getId().equals(courseId)).orElseThrow(() -> new NotFoundException("Lesson not found in this course"));
    e.setLastLessonId(lessonId); enrollments.save(e);
  }
  @Transactional(readOnly = true)
  public StudentDashboard dashboard(Long studentId){
    List<EnrollmentDto> all = myEnrollments(studentId);
    long completed = all.stream().filter(x -> x.status().equals("COMPLETED")).count();
    int overall = all.isEmpty() ? 0 : (int) Math.round(all.stream().mapToInt(EnrollmentDto::progress).average().orElse(0));
    List<ActivityDto> recent = progress.findTop5ByEnrollmentStudentIdOrderByCompletedAtDesc(studentId).stream()
      .map(p -> new ActivityDto(p.getLesson().getTitle(), p.getEnrollment().getCourse().getTitle(), p.getCompletedAt())).toList();
    List<EnrollmentDto> cont = all.stream().filter(x -> x.status().equals("ACTIVE")).limit(3).toList();
    return new StudentDashboard(all.size(), all.size() - completed, completed, overall, recent, cont);
  }
}
