package com.northlight.academy.security;
import com.northlight.academy.repository.UserRepository;
import io.jsonwebtoken.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwt; private final UserRepository users;
  public JwtAuthFilter(JwtService jwt, UserRepository users){ this.jwt = jwt; this.users = users; }
  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
    String h = req.getHeader("Authorization");
    if (h != null && h.startsWith("Bearer ")) {
      try {
        Claims c = jwt.parse(h.substring(7));
        users.findById(Long.valueOf(c.getSubject())).filter(u -> u.isActive()).ifPresent(u -> {
          var auth = new UsernamePasswordAuthenticationToken(new AuthUser(u.getId(), u.getEmail()), null,
            u.getRoles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName())).toList());
          SecurityContextHolder.getContext().setAuthentication(auth);
        });
      } catch (JwtException | IllegalArgumentException ignored) { /* invalid or expired: stay unauthenticated -> 401 */ }
    }
    chain.doFilter(req, res);
  }
}
