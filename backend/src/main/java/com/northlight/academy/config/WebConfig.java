package com.northlight.academy.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import java.nio.file.*;
@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final Path root;
  public WebConfig(@Value("${app.storage.dir}") String dir){ this.root = Paths.get(dir).toAbsolutePath().normalize(); }
  @Override public void addResourceHandlers(ResourceHandlerRegistry r){
    r.addResourceHandler("/uploads/**").addResourceLocations(root.toUri().toString().endsWith("/") ? root.toUri().toString() : root.toUri() + "/")
     .setCacheControl(org.springframework.http.CacheControl.maxAge(java.time.Duration.ofDays(7)));
  }
}
