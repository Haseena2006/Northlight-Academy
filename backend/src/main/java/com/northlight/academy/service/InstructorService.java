package com.northlight.academy.service;
import com.northlight.academy.dto.Dtos.*;
import com.northlight.academy.entity.*;
import com.northlight.academy.exception.ApiExceptions.*;
import com.northlight.academy.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
public class InstructorService {
  private final CourseRepository courses; private final EnrollmentRepository enrollments; private final CourseService courseService; private final AdminService admin;
  public InstructorService(CourseRepository courses, EnrollmentRepository enrollments, CourseService courseService, AdminService admin){ this.courses=courses; this.enrollments=enrollments; this.courseService=courseService; this.admin=admin; }
  @Transactional(readOnly = true)
  public List<CourseSummary> myCourses(Long id){ return courses.findByInstructorsIdOrderByTitleAsc(id).stream().map(courseService::summary).toList(); }
  @Transactional(readOnly = true)
  public InstructorDashboard dashboard(Long id){
    List<Course> mine = courses.findByInstructorsIdOrderByTitleAsc(id);
    List<CourseReport> reps = mine.stream().map(c -> AdminService.report(c, enrollments.countByCourseId(c.getId()), enrollments.countByCourseIdAndStatus(c.getId(), EnrollmentStatus.COMPLETED))).toList();
    long students = mine.isEmpty() ? 0 : enrollments.countDistinctStudentsInCourses(mine.stream().map(Course::getId).toList());
    return new InstructorDashboard(mine.size(), students, reps.stream().mapToLong(CourseReport::completed).sum(), reps);
  }
  @Transactional(readOnly = true)
  public List<EnrollmentRow> students(Long instructorId, Long courseId){
    Course c = courses.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found"));
    if (c.getInstructors().stream().noneMatch(i -> i.getId().equals(instructorId))) throw new ForbiddenException("You are not assigned to this course");
    return enrollments.findByCourseId(courseId).stream().map(admin::enrollmentRow).toList();
  }
}
