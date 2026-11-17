package com.northlight.academy.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="lessons")
public class Lesson {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(fetch=FetchType.EAGER, optional=false) @JoinColumn(name="course_id") private Course course;
  @Column(nullable=false) private String title;
  @Column(columnDefinition="TEXT") private String description;
  @Column(name="video_url") private String videoUrl;
  @Column(name="duration_minutes", nullable=false) private int durationMinutes;
  @Column(nullable=false) private int position;
  private boolean preview;
  private boolean required = true;
  @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt = Instant.now();
  @Column(name="updated_at", nullable=false) private Instant updatedAt = Instant.now();
  @PreUpdate void touch(){ updatedAt = Instant.now(); }
  public Long getId(){return id;}
  public Course getCourse(){return course;} public void setCourse(Course v){course=v;}
  public String getTitle(){return title;} public void setTitle(String v){title=v;}
  public String getDescription(){return description;} public void setDescription(String v){description=v;}
  public String getVideoUrl(){return videoUrl;} public void setVideoUrl(String v){videoUrl=v;}
  public int getDurationMinutes(){return durationMinutes;} public void setDurationMinutes(int v){durationMinutes=v;}
  public int getPosition(){return position;} public void setPosition(int v){position=v;}
  public boolean isPreview(){return preview;} public void setPreview(boolean v){preview=v;}
  public boolean isRequired(){return required;} public void setRequired(boolean v){required=v;}
}
