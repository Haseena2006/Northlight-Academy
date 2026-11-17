package com.northlight.academy.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
@Entity @Table(name="courses")
public class Course {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false) private String title;
  @Column(name="short_description", nullable=false) private String shortDescription;
  @Column(nullable=false, columnDefinition="TEXT") private String description;
  @ManyToOne(fetch=FetchType.EAGER, optional=false) @JoinColumn(name="category_id") private Category category;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private Difficulty difficulty;
  @Column(name="duration_minutes", nullable=false) private int durationMinutes;
  @Column(name="thumbnail_path") private String thumbnailPath;
  @Column(name="learning_outcomes", columnDefinition="TEXT") private String learningOutcomes;
  @Column(columnDefinition="TEXT") private String prerequisites;
  private boolean published;
  @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt = Instant.now();
  @Column(name="updated_at", nullable=false) private Instant updatedAt = Instant.now();
  @ManyToMany(fetch=FetchType.EAGER)
  @JoinTable(name="course_instructors", joinColumns=@JoinColumn(name="course_id"), inverseJoinColumns=@JoinColumn(name="user_id"))
  private Set<User> instructors = new LinkedHashSet<>();
  @PreUpdate void touch(){ updatedAt = Instant.now(); }
  public Long getId(){return id;}
  public String getTitle(){return title;} public void setTitle(String v){title=v;}
  public String getShortDescription(){return shortDescription;} public void setShortDescription(String v){shortDescription=v;}
  public String getDescription(){return description;} public void setDescription(String v){description=v;}
  public Category getCategory(){return category;} public void setCategory(Category v){category=v;}
  public Difficulty getDifficulty(){return difficulty;} public void setDifficulty(Difficulty v){difficulty=v;}
  public int getDurationMinutes(){return durationMinutes;} public void setDurationMinutes(int v){durationMinutes=v;}
  public String getThumbnailPath(){return thumbnailPath;} public void setThumbnailPath(String v){thumbnailPath=v;}
  public String getLearningOutcomes(){return learningOutcomes;} public void setLearningOutcomes(String v){learningOutcomes=v;}
  public String getPrerequisites(){return prerequisites;} public void setPrerequisites(String v){prerequisites=v;}
  public boolean isPublished(){return published;} public void setPublished(boolean v){published=v;}
  public Instant getCreatedAt(){return createdAt;}
  public Set<User> getInstructors(){return instructors;}
}
