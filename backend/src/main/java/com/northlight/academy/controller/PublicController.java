package com.northlight.academy.controller;
import com.northlight.academy.dto.Dtos.*;
import com.northlight.academy.service.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api")
public class PublicController {
  private final CourseService courses; private final CertificateService certs;
  public PublicController(CourseService courses, CertificateService certs){ this.courses = courses; this.certs = certs; }
  @GetMapping("/courses") public PageDto<CourseSummary> list(@RequestParam(required=false) String q, @RequestParam(required=false) Long categoryId, @RequestParam(required=false) String difficulty,
      @RequestParam(required=false) Integer minDuration, @RequestParam(required=false) Integer maxDuration,
      @RequestParam(required=false) String sort, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="9") int size){
    return courses.search(q, categoryId, difficulty, minDuration, maxDuration, sort, page, size, true);
  }
  @GetMapping("/courses/{id}") public CourseDetail detail(@PathVariable Long id){ return courses.detail(id, true); }
  @GetMapping("/categories") public List<CategoryDto> categories(){ return courses.categories(); }
  @GetMapping("/public/stats") public PublicStats stats(){ return courses.stats(); }
  @GetMapping("/certificates/{code}/verify") public VerifyDto verify(@PathVariable String code){ return certs.verify(code); }
}
