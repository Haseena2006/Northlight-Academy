package com.northlight.academy.config;
import com.northlight.academy.repository.UserRepository;
import com.northlight.academy.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.List;
@Configuration @EnableWebSecurity
public class SecurityConfig {
  @Bean PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(12); }

  @Bean SecurityFilterChain chain(HttpSecurity http, JwtService jwt, UserRepository users) throws Exception {
    http
      // CSRF: the API is stateless and authenticated by an Authorization header, which browsers do not attach
      // cross-site. The only cookie-authenticated endpoints (/api/auth/refresh, /logout) are protected by
      // SameSite=Strict plus a mandatory custom header (X-Requested-With) checked in AuthController.
      .csrf(AbstractHttpConfigurer::disable)
      .cors(Customizer.withDefaults())
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable).logout(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(a -> a
        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh", "/api/auth/logout", "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/courses", "/api/courses/*", "/api/categories", "/api/public/stats", "/api/certificates/*/verify", "/uploads/**").permitAll()
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers("/api/instructor/**").hasRole("INSTRUCTOR")
        .requestMatchers("/api/student/**").hasRole("STUDENT")
        .requestMatchers("/api/**").authenticated()
        .anyRequest().denyAll())
      .exceptionHandling(e -> e
        .authenticationEntryPoint((req, res, ex) -> json(res, 401, "Authentication required"))
        .accessDeniedHandler((req, res, ex) -> json(res, 403, "Access denied")))
      .addFilterBefore(new JwtAuthFilter(jwt, users), UsernamePasswordAuthenticationFilter.class)
      .headers(h -> h.frameOptions(f -> f.deny()).contentTypeOptions(Customizer.withDefaults()));
    return http.build();
  }
  private static void json(jakarta.servlet.http.HttpServletResponse res, int status, String msg) throws java.io.IOException {
    res.setStatus(status); res.setContentType("application/json");
    res.getWriter().write("{\"status\":" + status + ",\"message\":\"" + msg + "\"}");
  }
  @Bean CorsConfigurationSource cors(@Value("${app.cors.frontend-origin}") String origin){
    CorsConfiguration c = new CorsConfiguration();
    c.setAllowedOrigins(List.of(origin));
    c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    c.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));
    c.setAllowCredentials(true); c.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource(); s.registerCorsConfiguration("/**", c); return s;
  }
}
