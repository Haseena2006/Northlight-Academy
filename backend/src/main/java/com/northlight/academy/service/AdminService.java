package com.northlight.academy.service;
import com.northlight.academy.dto.Dtos.*;
import com.northlight.academy.entity.*;
import com.northlight.academy.exception.ApiExceptions.*;
import com.northlight.academy.mapper.Mapper;
import com.northlight.academy.repository.*;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
public class AdminService {
  private final UserRepository users; private final RoleRepository roles; private final CourseRepository courses; private final EnrollmentRepository enrollments;
  private final LessonRepository lessons; private final LessonProgressRepository progress; private final RefreshTokenRepository tokens; private final PasswordEncoder encoder;
  public AdminService(UserRepository users, RoleRepository roles, CourseRepository courses, EnrollmentRepository enrollments, LessonRepository lessons, LessonProgressRepository progress, RefreshTokenRepository tokens, PasswordEncoder encoder){
    this.users=users; this.roles=roles; this.courses=courses; this.enrollments=enrollments; this.lessons=lessons; this.progress=progress; this.tokens=tokens; this.encoder=encoder;
  }
  static CourseReport report(Course c, long enrolled, long completed){ return new CourseReport(c.getId(), c.getTitle(), enrolled, completed, StudentService.percent(completed, enrolled)); }

  @Transactional(readOnly = true)
  public AdminDashboard dashboard(){
    List<CourseReport> reports = courses.findAll(Sort.by("title")).stream()
      .map(c -> report(c, enrollments.countByCourseId(c.getId()), enrollments.countByCourseIdAndStatus(c.getId(), EnrollmentStatus.COMPLETED))).toList();
    return new AdminDashboard(users.countByRole("STUDENT"), users.countByRole("INSTRUCTOR"), courses.count(), courses.countByPublishedTrue(), enrollments.count(), enrollments.countByStatus(EnrollmentStatus.COMPLETED), reports);
  }
  private UserRow row(User u){
    List<CourseRef> assigned = u.hasRole("INSTRUCTOR") ? courses.findByInstructorsIdOrderByTitleAsc(u.getId()).stream().map(c -> new CourseRef(c.getId(), c.getTitle())).toList() : List.of();
    return new UserRow(u.getId(), u.getFullName(), u.getEmail(), u.getCreatedAt(), u.isActive(), enrollments.countByStudentId(u.getId()), enrollments.countByStudentIdAndStatus(u.getId(), EnrollmentStatus.COMPLETED), assigned);
  }
  @Transactional(readOnly = true)
  public PageDto<UserRow> users(String role, String q, Boolean active, int page, int size){
    String like = "%" + (q == null ? "" : q.trim().toLowerCase()) + "%";
    Pageable p = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by("createdAt").descending());
    Page<User> res = active == null ? users.searchByRole(role, like, p) : users.searchByRoleAndActive(role, active, like, p);
    return PageDto.of(res, this::row);
  }
  @Transactional
  public UserRow setActive(Long id, boolean active){
    User u = users.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    if (u.hasRole("ADMIN")) throw new ForbiddenException("Administrator accounts cannot be changed here");
    u.setActive(active); users.save(u);
    UserRow r = row(u);
    if (!active) tokens.revokeAllForUser(id);
    return r;
  }
  @Transactional
  public UserRow createInstructor(InstructorCreateRequest r){
    String email = r.email().trim().toLowerCase();
    if (users.existsByEmail(email)) throw new ConflictException("An account with this email already exists");
    User u = new User(); u.setFullName(r.fullName().trim()); u.setEmail(email); u.setPasswordHash(encoder.encode(r.password()));
    u.getRoles().add(roles.findByName("INSTRUCTOR").orElseThrow());
    return row(users.save(u));
  }
  @Transactional(readOnly = true)
  public UserRow instructor(Long id){
    User u = users.findById(id).filter(x -> x.hasRole("INSTRUCTOR")).orElseThrow(() -> new NotFoundException("Instructor not found")); return row(u);
  }
  @Transactional
  public UserRow assign(Long instructorId, List<Long> courseIds){
    User u = users.findById(instructorId).filter(x -> x.hasRole("INSTRUCTOR")).orElseThrow(() -> new NotFoundException("Instructor not found"));
    Set<Long> want = courseIds == null ? Set.of() : new HashSet<>(courseIds);
    for (Course c : courses.findAll()) {
      boolean has = c.getInstructors().stream().anyMatch(i -> i.getId().equals(instructorId));
      if (want.contains(c.getId()) && !has) { c.getInstructors().add(u); courses.save(c); }
      else if (!want.contains(c.getId()) && has) { c.getInstructors().removeIf(i -> i.getId().equals(instructorId)); courses.save(c); }
    }
    want.forEach(id -> { if (!courses.existsById(id)) throw new BadRequestException("Course " + id + " does not exist"); });
    return row(u);
  }
  @Transactional(readOnly = true)
  public PageDto<EnrollmentRow> enrollments(Long courseId, int page, int size){
    Pageable p = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by("enrolledAt").descending());
    return PageDto.of(enrollments.findAllFiltered(courseId, p), this::enrollmentRow);
  }
  EnrollmentRow enrollmentRow(Enrollment e){
    int total = lessons.countByCourseId(e.getCourse().getId());
    int pct = e.getStatus() == EnrollmentStatus.COMPLETED ? 100 : StudentService.percent(progress.countByEnrollmentId(e.getId()), total);
    return new EnrollmentRow(e.getId(), e.getStudent().getId(), e.getStudent().getFullName(), e.getStudent().getEmail(), e.getCourse().getId(), e.getCourse().getTitle(), e.getStatus().name(), pct, e.getEnrolledAt());
  }
}
