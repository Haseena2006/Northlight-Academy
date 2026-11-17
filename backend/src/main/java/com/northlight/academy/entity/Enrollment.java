package com.northlight.academy.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="enrollments", uniqueConstraints=@UniqueConstraint(name="uq_enrollment_student_course", columnNames={"student_id","course_id"}))
public class Enrollment {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(fetch=FetchType.EAGER, optional=false) @JoinColumn(name="student_id") private User student;
  @ManyToOne(fetch=FetchType.EAGER, optional=false) @JoinColumn(name="course_id") private Course course;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private EnrollmentStatus status = EnrollmentStatus.ACTIVE;
  @Column(name="enrolled_at", nullable=false) private Instant enrolledAt = Instant.now();
  @Column(name="completed_at") private Instant completedAt;
  @Column(name="last_lesson_id") private Long lastLessonId;
  public Long getId(){return id;}
  public User getStudent(){return student;} public void setStudent(User v){student=v;}
  public Course getCourse(){return course;} public void setCourse(Course v){course=v;}
  public EnrollmentStatus getStatus(){return status;} public void setStatus(EnrollmentStatus v){status=v;}
  public Instant getEnrolledAt(){return enrolledAt;}
  public Instant getCompletedAt(){return completedAt;} public void setCompletedAt(Instant v){completedAt=v;}
  public Long getLastLessonId(){return lastLessonId;} public void setLastLessonId(Long v){lastLessonId=v;}
}
