package com.northlight.academy.mapper;
import com.northlight.academy.dto.Dtos.*;
import com.northlight.academy.entity.*;
import java.util.*;
public final class Mapper {
  private Mapper(){}
  public static UserDto user(User u){
    return new UserDto(u.getId(), u.getFullName(), u.getEmail(), u.getRoles().stream().map(Role::getName).sorted().toList(), u.isActive(), u.getCreatedAt());
  }
  public static CourseSummary course(Course c, int lessonCount){
    return new CourseSummary(c.getId(), c.getTitle(), c.getShortDescription(), c.getCategory().getId(), c.getCategory().getName(), c.getDifficulty().name(), c.getDurationMinutes(),
      thumbnailUrl(c), c.isPublished(),
      c.getInstructors().stream().map(i -> new InstructorBrief(i.getId(), i.getFullName())).toList(), lessonCount);
  }
  private static String thumbnailUrl(Course c){
    String path = c.getThumbnailPath();
    if (path != null && !path.isBlank()) return path.startsWith("seed:") ? "/images/courses/" + path.substring(5) : "/uploads/" + path;
    String key = (c.getTitle() + " " + c.getCategory().getName()).toLowerCase(Locale.ROOT);
    String cover = key.contains("python") ? "python.svg"
      : key.contains("database") || key.contains("sql") || key.contains("mysql") || key.contains("postgres") || key.contains("hibernate") || key.contains("jpa") || key.contains("mongo") ? "database.svg"
      : key.contains("data") || key.contains("excel") || key.contains("power bi") || key.contains("tableau") || key.contains("statistic") ? "analytics.svg"
      : key.contains("artificial") || key.contains("machine learning") || key.contains("deep learning") || key.contains("ai and machine learning") ? "ai.svg"
      : key.contains("cloud") || key.contains("aws") || key.contains("azure") || key.contains("microservice") ? "cloud.svg"
      : key.contains("security") || key.contains("cyber") ? "security.svg"
      : key.contains("test") || key.contains("junit") || key.contains("selenium") ? "testing.svg"
      : key.contains("design") || key.contains("figma") || key.contains("react") || key.contains("angular") || key.contains("html") || key.contains("css") ? "design.svg"
      : key.contains("devops") || key.contains("docker") || key.contains("kubernetes") || key.contains("git") || key.contains("maven") || key.contains("jenkins") ? "devops.svg"
      : key.contains("communication") || key.contains("agile") || key.contains("professional") ? "communication.svg"
      : key.contains("java") || key.contains("spring") ? "java.svg" : "code.svg";
    return "/images/courses/" + cover;
  }
  public static LessonDto lesson(Lesson l){
    return new LessonDto(l.getId(), l.getCourse().getId(), l.getTitle(), l.getDescription(), l.getVideoUrl(), l.getDurationMinutes(), l.getPosition(), l.isPreview(), l.isRequired());
  }
  public static List<String> outcomes(String s){
    if (s == null || s.isBlank()) return List.of();
    return Arrays.stream(s.split("\\|")).map(String::trim).filter(x -> !x.isEmpty()).toList();
  }
  public static String joinOutcomes(List<String> l){
    if (l == null) return null;
    return String.join("|", l.stream().filter(Objects::nonNull).map(x -> x.replace("|", " ").trim()).filter(x -> !x.isEmpty()).toList());
  }
}
