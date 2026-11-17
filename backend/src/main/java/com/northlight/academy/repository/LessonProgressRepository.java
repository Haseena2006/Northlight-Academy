package com.northlight.academy.repository;
import com.northlight.academy.entity.LessonProgress;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
  boolean existsByEnrollmentIdAndLessonId(Long enrollmentId, Long lessonId);
  int countByEnrollmentId(Long enrollmentId);
  List<LessonProgress> findByEnrollmentId(Long enrollmentId);
  List<LessonProgress> findTop5ByEnrollmentStudentIdOrderByCompletedAtDesc(Long studentId);
  @Query("select count(p) from LessonProgress p where p.enrollment.id = :e and p.lesson.required = true")
  int countRequiredDone(@Param("e") Long enrollmentId);
}
