package com.northlight.academy.service;
import com.northlight.academy.dto.Dtos.*;
import com.northlight.academy.entity.*;
import com.northlight.academy.exception.ApiExceptions.*;
import com.northlight.academy.mapper.Mapper;
import com.northlight.academy.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.util.*;
/** Lesson management shared by ADMIN (any course) and INSTRUCTOR (assigned courses only). */
@Service
public class LessonService {
  private final LessonRepository lessons; private final CourseRepository courses; private final UserRepository users;
  public LessonService(LessonRepository lessons, CourseRepository courses, UserRepository users){ this.lessons=lessons; this.courses=courses; this.users=users; }

  private Course authorize(Long actorId, Long courseId){
    User actor = users.findById(actorId).orElseThrow(() -> new ForbiddenException("Access denied"));
    Course c = courses.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found"));
    if (actor.hasRole("ADMIN")) return c;
    if (actor.hasRole("INSTRUCTOR") && c.getInstructors().stream().anyMatch(i -> i.getId().equals(actorId))) return c;
    throw new ForbiddenException("You are not assigned to this course");
  }
  private Lesson lessonFor(Long actorId, Long lessonId){
    Lesson l = lessons.findById(lessonId).orElseThrow(() -> new NotFoundException("Lesson not found"));
    authorize(actorId, l.getCourse().getId()); return l;
  }
  private static String checkUrl(String url){
    if (url == null || url.isBlank()) return null;
    try { URI u = URI.create(url.trim()); if (u.getScheme() == null || !(u.getScheme().equals("http") || u.getScheme().equals("https")) || u.getHost() == null) throw new IllegalArgumentException(); return url.trim(); }
    catch (IllegalArgumentException e){ throw new BadRequestException("Video URL must be a valid http(s) URL"); }
  }
  private void fill(Lesson l, LessonRequest r){
    l.setTitle(r.title().trim()); l.setDescription(r.description()); l.setVideoUrl(checkUrl(r.videoUrl()));
    l.setDurationMinutes(r.durationMinutes()); l.setPreview(r.preview()); l.setRequired(r.required() == null || r.required());
  }
  @Transactional(readOnly = true)
  public List<LessonDto> list(Long actorId, Long courseId){ authorize(actorId, courseId); return lessons.findByCourseIdOrderByPositionAsc(courseId).stream().map(Mapper::lesson).toList(); }
  @Transactional
  public LessonDto create(Long actorId, Long courseId, LessonRequest r){
    Course c = authorize(actorId, courseId);
    Lesson l = new Lesson(); l.setCourse(c); fill(l, r); l.setPosition(lessons.maxPosition(courseId) + 1);
    return Mapper.lesson(lessons.save(l));
  }
  @Transactional
  public LessonDto update(Long actorId, Long lessonId, LessonRequest r){ Lesson l = lessonFor(actorId, lessonId); fill(l, r); return Mapper.lesson(lessons.save(l)); }
  @Transactional
  public void delete(Long actorId, Long lessonId){
    Lesson l = lessonFor(actorId, lessonId); Long cid = l.getCourse().getId();
    lessons.delete(l); lessons.flush();
    int pos = 1; for (Lesson x : lessons.findByCourseIdOrderByPositionAsc(cid)) { x.setPosition(pos++); lessons.save(x); }
    Course c = courses.findById(cid).orElseThrow();
    if (c.isPublished() && lessons.countByCourseId(cid) == 0) { c.setPublished(false); courses.save(c); }
  }
  @Transactional
  public List<LessonDto> reorder(Long actorId, Long courseId, List<Long> ids){
    authorize(actorId, courseId);
    List<Lesson> current = lessons.findByCourseIdOrderByPositionAsc(courseId);
    Set<Long> have = new HashSet<>(); current.forEach(x -> have.add(x.getId()));
    if (ids.size() != have.size() || !have.equals(new HashSet<>(ids))) throw new BadRequestException("Reorder list must contain every lesson of the course exactly once");
    Map<Long, Lesson> byId = new HashMap<>(); current.forEach(x -> byId.put(x.getId(), x));
    int pos = 1; for (Long id : ids) { byId.get(id).setPosition(pos++); }
    lessons.saveAll(byId.values());
    return lessons.findByCourseIdOrderByPositionAsc(courseId).stream().map(Mapper::lesson).toList();
  }
}
