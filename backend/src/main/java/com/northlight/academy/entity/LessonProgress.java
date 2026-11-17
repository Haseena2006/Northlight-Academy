package com.northlight.academy.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="lesson_progress", uniqueConstraints=@UniqueConstraint(name="uq_progress_enrollment_lesson", columnNames={"enrollment_id","lesson_id"}))
public class LessonProgress {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(fetch=FetchType.EAGER, optional=false) @JoinColumn(name="enrollment_id") private Enrollment enrollment;
  @ManyToOne(fetch=FetchType.EAGER, optional=false) @JoinColumn(name="lesson_id") private Lesson lesson;
  @Column(name="completed_at", nullable=false) private Instant completedAt = Instant.now();
  public Long getId(){return id;}
  public Enrollment getEnrollment(){return enrollment;} public void setEnrollment(Enrollment v){enrollment=v;}
  public Lesson getLesson(){return lesson;} public void setLesson(Lesson v){lesson=v;}
  public Instant getCompletedAt(){return completedAt;}
}
