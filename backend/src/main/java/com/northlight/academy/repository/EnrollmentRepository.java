package com.northlight.academy.repository;
import com.northlight.academy.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
  Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
  boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
  List<Enrollment> findByStudentIdOrderByEnrolledAtDesc(Long studentId);
  List<Enrollment> findByCourseId(Long courseId);
  long countByCourseId(Long courseId);
  long countByCourseIdAndStatus(Long courseId, EnrollmentStatus s);
  long countByStatus(EnrollmentStatus s);
  long countByStudentId(Long studentId);
  long countByStudentIdAndStatus(Long studentId, EnrollmentStatus s);
  @Query("select count(distinct e.student.id) from Enrollment e where e.course.id in :ids")
  long countDistinctStudentsInCourses(@Param("ids") Collection<Long> ids);
  @Query("select e from Enrollment e where (:courseId is null or e.course.id = :courseId)")
  Page<Enrollment> findAllFiltered(@Param("courseId") Long courseId, Pageable p);
}
