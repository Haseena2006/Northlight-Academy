package com.northlight.academy.dto;
import com.northlight.academy.entity.Difficulty;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;
public final class Dtos {
  private Dtos(){}
  public static final String PWD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,72}$";
  public static final String PWD_MSG = "Password must be 8-72 characters and include upper-case, lower-case and a digit";

  public record PageDto<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public static <S,T> PageDto<T> of(Page<S> p, Function<S,T> f){ return new PageDto<>(p.getContent().stream().map(f).toList(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages()); }
  }
  // auth
  public record RegisterRequest(@NotBlank @Size(max=120) String fullName, @NotBlank @Email @Size(max=255) String email, @NotBlank @Pattern(regexp=PWD, message=PWD_MSG) String password) {}
  public record LoginRequest(@NotBlank @Email String email, @NotBlank String password, boolean rememberMe) {}
  public record UserDto(Long id, String fullName, String email, List<String> roles, boolean active, Instant createdAt) {}
  public record AuthResponse(String accessToken, UserDto user) {}
  public record ForgotRequest(@NotBlank @Email String email) {}
  public record ResetRequest(@NotBlank String token, @NotBlank @Pattern(regexp=PWD, message=PWD_MSG) String newPassword) {}
  public record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank @Pattern(regexp=PWD, message=PWD_MSG) String newPassword) {}
  public record ProfileRequest(@NotBlank @Size(max=120) String fullName) {}
  public record MessageDto(String message) {}
  // catalogue
  public record InstructorBrief(Long id, String fullName) {}
  public record CourseSummary(Long id, String title, String shortDescription, Long categoryId, String categoryName, String difficulty, int durationMinutes, String thumbnailUrl, boolean published, List<InstructorBrief> instructors, int lessonCount) {}
  public record LessonDto(Long id, Long courseId, String title, String description, String videoUrl, int durationMinutes, int position, boolean preview, boolean required) {}
  public record LessonOutline(Long id, String title, String description, int durationMinutes, int position, boolean preview, String videoUrl) {}
  public record CourseDetail(CourseSummary summary, String description, List<String> learningOutcomes, List<String> prerequisites, List<LessonOutline> lessons) {}
  public record CourseRequest(@NotBlank @Size(max=200) String title, @NotBlank @Size(max=300) String shortDescription, @NotBlank String description, @NotNull Long categoryId, @NotNull Difficulty difficulty, @Min(1) int durationMinutes, List<String> learningOutcomes, List<Long> instructorIds, List<String> prerequisites) {}
  public record LessonRequest(@NotBlank @Size(max=200) String title, String description, @Size(max=1000) String videoUrl, @Min(0) int durationMinutes, boolean preview, Boolean required) {}
  public record ReorderRequest(@NotEmpty List<Long> lessonIds) {}
  public record StatusRequest(boolean published) {}
  public record ActiveRequest(boolean active) {}
  public record CategoryRequest(@NotBlank @Size(max=80) String name, @Size(max=300) String description) {}
  public record CategoryDto(Long id, String name, String description, long courseCount) {}
  public record PublicStats(long courses, long students, long enrollments, long completions) {}
  // student
  public record EnrollmentDto(Long id, CourseSummary course, String status, int progress, int completedLessons, int totalLessons, Long lastLessonId, Instant enrolledAt, Instant completedAt, String certificateCode) {}
  public record LearningLesson(Long id, String title, String description, String videoUrl, int durationMinutes, int position, boolean completed) {}
  public record LearningDto(Long courseId, String courseTitle, int progress, String status, Long lastLessonId, List<LearningLesson> lessons) {}
  public record ProgressRequest(@NotNull Long courseId, @NotNull Long lessonId) {}
  public record ProgressDto(Long courseId, int completedLessons, int totalLessons, int progress, String status, String certificateCode) {}
  public record ActivityDto(String lessonTitle, String courseTitle, Instant completedAt) {}
  public record StudentDashboard(long enrolled, long inProgress, long completed, int overallProgress, List<ActivityDto> recent, List<EnrollmentDto> continueLearning) {}
  public record CertificateDto(Long id, String certificateCode, String courseTitle, String studentName, Instant issuedAt) {}
  public record VerifyDto(boolean valid, String certificateCode, String studentName, String courseTitle, Instant issuedAt) {}
  // admin / instructor
  public record CourseRef(Long id, String title) {}
  public record CourseReport(Long courseId, String title, long enrolled, long completed, int completionRate) {}
  public record AdminDashboard(long students, long instructors, long courses, long publishedCourses, long enrollments, long completedEnrollments, List<CourseReport> courseReports) {}
  public record UserRow(Long id, String fullName, String email, Instant createdAt, boolean active, long enrolledCourses, long completedCourses, List<CourseRef> assignedCourses) {}
  public record EnrollmentRow(Long id, Long studentId, String studentName, String studentEmail, Long courseId, String courseTitle, String status, int progress, Instant enrolledAt) {}
  public record InstructorCreateRequest(@NotBlank @Size(max=120) String fullName, @NotBlank @Email String email, @NotBlank @Pattern(regexp=PWD, message=PWD_MSG) String password) {}
  public record AssignRequest(List<Long> courseIds) {}
  public record InstructorDashboard(long assignedCourses, long totalStudents, long completedEnrollments, List<CourseReport> courses) {}
}
