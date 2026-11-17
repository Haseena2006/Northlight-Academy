package com.northlight.academy.security;
import com.northlight.academy.entity.User;
import com.northlight.academy.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
@Service
public class JwtService {
  private final SecretKey key; private final long accessMinutes;
  public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.access-minutes}") long accessMinutes){
    byte[] b = secret.getBytes(StandardCharsets.UTF_8);
    if (b.length < 32) throw new IllegalStateException("JWT_SECRET must be at least 32 characters");
    this.key = Keys.hmacShaKeyFor(b); this.accessMinutes = accessMinutes;
  }
  public String createAccessToken(User u){
    Instant now = Instant.now();
    return Jwts.builder().subject(String.valueOf(u.getId())).claim("email", u.getEmail())
      .claim("roles", u.getRoles().stream().map(Role::getName).toList())
      .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(accessMinutes * 60))).signWith(key).compact();
  }
  public Claims parse(String token){ return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); }
}
