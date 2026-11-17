package com.northlight.academy.service;
import com.northlight.academy.dto.Dtos.*;
import com.northlight.academy.entity.*;
import com.northlight.academy.exception.ApiExceptions.*;
import com.northlight.academy.mapper.Mapper;
import com.northlight.academy.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
@Service
public class CourseService {
  private final CourseRepository courses; private final CategoryRepository categories; private final LessonRepository lessons;
  private final UserRepository users; private final EnrollmentRepository enrollments; private final FileStorageService files;
  public CourseService(CourseRepository courses, CategoryRepository categories, LessonRepository lessons, UserRepository users, EnrollmentRepository enrollments, FileStorageService files){
    this.courses=courses; this.categories=categories; this.lessons=lessons; this.users=users; this.enrollments=enrollments; this.files=files;
  }
  public CourseSummary summary(Course c){ return Mapper.course(c, lessons.countByCourseId(c.getId())); }

  @Transactional(readOnly = true)
  public PageDto<CourseSummary> search(String q, Long categoryId, String difficulty, Integer minDuration, Integer maxDuration, String sort, int page, int size, boolean publishedOnly){
    if (minDuration != null && minDuration < 0 || maxDuration != null && maxDuration < 0 || minDuration != null && maxDuration != null && minDuration > maxDuration) throw new BadRequestException("Invalid course duration range");
    Difficulty d = null;
    if (difficulty != null && !difficulty.isBlank()) { try { d = Difficulty.valueOf(difficulty.toUpperCase()); } catch (IllegalArgumentException e) { throw new BadRequestException("Unknown difficulty"); } }
    final Difficulty dd = d;
    Specification<Course> spec = (root, query, cb) -> {
      List<Predicate> ps = new ArrayList<>();
      if (publishedOnly) ps.add(cb.isTrue(root.get("published")));
      if (q != null && !q.isBlank()) {
        String p = "%" + q.trim().toLowerCase() + "%";
        var category = root.join("category", jakarta.persistence.criteria.JoinType.LEFT);
        var instructor = root.join("instructors", jakarta.persistence.criteria.JoinType.LEFT);
        query.distinct(true);
        ps.add(cb.or(
          cb.like(cb.lower(root.get("title")), p),
          cb.like(cb.lower(root.get("shortDescription")), p),
          cb.like(cb.lower(root.get("description")), p),
          cb.like(cb.lower(root.get("learningOutcomes")), p),
          cb.like(cb.lower(category.get("name")), p),
          cb.like(cb.lower(instructor.get("fullName")), p)
        ));
      }
      if (categoryId != null) ps.add(cb.equal(root.get("category").get("id"), categoryId));
      if (dd != null) ps.add(cb.equal(root.get("difficulty"), dd));
      if (minDuration != null) ps.add(cb.greaterThanOrEqualTo(root.get("durationMinutes"), minDuration));
      if (maxDuration != null) ps.add(cb.lessThanOrEqualTo(root.get("durationMinutes"), maxDuration));
      return cb.and(ps.toArray(new Predicate[0]));
    };
    Sort s = switch (sort == null ? "newest" : sort) {
      case "title" -> Sort.by("title").ascending();
      case "title-desc" -> Sort.by("title").descending();
      case "duration" -> Sort.by("durationMinutes").ascending();
      case "oldest" -> Sort.by("createdAt").ascending();
      default -> Sort.by("createdAt").descending().and(Sort.by("id").descending());
    };
    Page<Course> p = courses.findAll(spec, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), s));
    return PageDto.of(p, this::summary);
  }

  @Transactional(readOnly = true)
  public CourseDetail detail(Long id, boolean publishedOnly){
    Course c = courses.findById(id).filter(x -> !publishedOnly || x.isPublished()).orElseThrow(() -> new NotFoundException("Course not found"));
    List<LessonOutline> outline = lessons.findByCourseIdOrderByPositionAsc(id).stream()
      .map(l -> new LessonOutline(l.getId(), l.getTitle(), publishedOnly && !l.isPreview() ? null : l.getDescription(), l.getDurationMinutes(), l.getPosition(), l.isPreview(), l.isPreview() ? l.getVideoUrl() : null)).toList();
    return new CourseDetail(summary(c), c.getDescription(), Mapper.outcomes(c.getLearningOutcomes()), Mapper.outcomes(c.getPrerequisites()), outline);
  }

  @Transactional
  public CourseDetail create(CourseRequest r){
    Course c = new Course(); apply(c, r); c.setPublished(false);
    return detail(courses.save(c).getId(), false);
  }
  @Transactional
  public CourseDetail update(Long id, CourseRequest r){
    Course c = courses.findById(id).orElseThrow(() -> new NotFoundException("Course not found"));
    apply(c, r); courses.save(c); return detail(id, false);
  }
  private void apply(Course c, CourseRequest r){
    c.setTitle(r.title().trim()); c.setShortDescription(r.shortDescription().trim()); c.setDescription(r.description().trim());
    c.setCategory(categories.findById(r.categoryId()).orElseThrow(() -> new BadRequestException("Category does not exist")));
    c.setDifficulty(r.difficulty()); c.setDurationMinutes(r.durationMinutes()); c.setLearningOutcomes(Mapper.joinOutcomes(r.learningOutcomes()));
    c.setPrerequisites(Mapper.joinOutcomes(r.prerequisites()));
    c.getInstructors().clear();
    if (r.instructorIds() != null) for (Long id : new LinkedHashSet<>(r.instructorIds())) {
      User u = users.findById(id).orElseThrow(() -> new BadRequestException("Instructor " + id + " does not exist"));
      if (!u.hasRole("INSTRUCTOR")) throw new BadRequestException("User " + id + " is not an instructor");
      c.getInstructors().add(u);
    }
  }
  @Transactional
  public void delete(Long id){
    Course c = courses.findById(id).orElseThrow(() -> new NotFoundException("Course not found"));
    if (enrollments.countByCourseId(id) > 0) throw new ConflictException("This course has enrollments and cannot be deleted. Unpublish it instead.");
    files.delete(c.getThumbnailPath()); courses.delete(c);
  }
  @Transactional
  public CourseSummary setPublished(Long id, boolean published){
    Course c = courses.findById(id).orElseThrow(() -> new NotFoundException("Course not found"));
    if (published && lessons.countByCourseId(id) == 0) throw new BadRequestException("Add at least one lesson before publishing");
    c.setPublished(published); return summary(courses.save(c));
  }
  @Transactional
  public CourseSummary uploadThumbnail(Long id, MultipartFile f){
    Course c = courses.findById(id).orElseThrow(() -> new NotFoundException("Course not found"));
    String name = files.storeImage(f); files.delete(c.getThumbnailPath()); c.setThumbnailPath(name);
    return summary(courses.save(c));
  }

  // categories
  @Transactional(readOnly = true)
  public List<CategoryDto> categories(){ return categories.findAll(Sort.by("name")).stream().map(c -> new CategoryDto(c.getId(), c.getName(), c.getDescription(), courses.countByCategoryId(c.getId()))).toList(); }
  @Transactional
  public CategoryDto saveCategory(Long id, CategoryRequest r){
    Category c = id == null ? new Category() : categories.findById(id).orElseThrow(() -> new NotFoundException("Category not found"));
    String name = r.name().trim();
    if ((id == null || !c.getName().equalsIgnoreCase(name)) && categories.existsByNameIgnoreCase(name)) throw new ConflictException("A category with this name already exists");
    c.setName(name); c.setDescription(r.description()); c = categories.save(c);
    return new CategoryDto(c.getId(), c.getName(), c.getDescription(), courses.countByCategoryId(c.getId()));
  }
  @Transactional
  public void deleteCategory(Long id){
    if (!categories.existsById(id)) throw new NotFoundException("Category not found");
    if (courses.countByCategoryId(id) > 0) throw new ConflictException("Category is used by courses and cannot be deleted");
    categories.deleteById(id);
  }
  @Transactional(readOnly = true)
  public PublicStats stats(){
    return new PublicStats(courses.countByPublishedTrue(), users.countByRole("STUDENT"), enrollments.count(), enrollments.countByStatus(EnrollmentStatus.COMPLETED));
  }
  @Transactional(readOnly = true)
  public List<CourseSummary> featured(int n){ return courses.findByPublishedTrueOrderByCreatedAtDesc(PageRequest.of(0, n)).stream().map(this::summary).toList(); }
}
