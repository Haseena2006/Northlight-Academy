package com.northlight.academy.repository;
import com.northlight.academy.entity.Course;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import java.util.List;
public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
  List<Course> findByInstructorsIdOrderByTitleAsc(Long instructorId);
  long countByPublishedTrue();
  long countByCategoryId(Long categoryId);
  List<Course> findByPublishedTrueOrderByCreatedAtDesc(Pageable p);
}
