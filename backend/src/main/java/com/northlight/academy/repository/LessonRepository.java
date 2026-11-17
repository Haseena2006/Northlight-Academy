package com.northlight.academy.repository;
import com.northlight.academy.entity.Lesson;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface LessonRepository extends JpaRepository<Lesson, Long> {
  List<Lesson> findByCourseIdOrderByPositionAsc(Long courseId);
  int countByCourseId(Long courseId);
  int countByCourseIdAndRequiredTrue(Long courseId);
  @Query("select coalesce(max(l.position),0) from Lesson l where l.course.id = :c")
  int maxPosition(@Param("c") Long courseId);
}
