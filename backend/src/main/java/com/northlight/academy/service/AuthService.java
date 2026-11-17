package com.northlight.academy.service;
import com.northlight.academy.dto.Dtos.*;
import com.northlight.academy.entity.*;
import com.northlight.academy.exception.ApiExceptions.*;
import com.northlight.academy.mapper.Mapper;
import com.northlight.academy.repository.*;
import com.northlight.academy.security.JwtService;
import com.northlight.academy.util.Hashing;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
@Service
public class AuthService {
  private static final Logger log = LoggerFactory.getLogger(AuthService.class);
  public record Issued(AuthResponse body, String refreshToken, boolean remember, long maxAgeSeconds) {}
  private final UserRepository users; private final RoleRepository roles; private final RefreshTokenRepository tokens;
  private final PasswordResetTokenRepository resets; private final PasswordEncoder encoder; private final JwtService jwt;
  private final long refreshDays; private final String frontendOrigin; private final boolean logResetLinks;
  private final String dummyHash;
  public AuthService(UserRepository users, RoleRepository roles, RefreshTokenRepository tokens, PasswordResetTokenRepository resets, PasswordEncoder encoder, JwtService jwt,
                     @Value("${app.jwt.refresh-days}") long refreshDays, @Value("${app.cors.frontend-origin}") String frontendOrigin, @Value("${app.password-reset.log-links:false}") boolean logResetLinks){
    this.users=users; this.roles=roles; this.tokens=tokens; this.resets=resets; this.encoder=encoder; this.jwt=jwt;
    this.refreshDays=refreshDays; this.frontendOrigin=frontendOrigin; this.logResetLinks=logResetLinks;
    this.dummyHash = encoder.encode("dummy-password-for-timing");
  }

  @Transactional
  public UserDto register(RegisterRequest r){
    String email = r.email().trim().toLowerCase();
    if (users.existsByEmail(email)) throw new ConflictException("An account with this email already exists");
    User u = new User();
    u.setFullName(r.fullName().trim()); u.setEmail(email); u.setPasswordHash(encoder.encode(r.password()));
    u.getRoles().add(roles.findByName("STUDENT").orElseThrow());   // public registration is ALWAYS a student
    return Mapper.user(users.save(u));
  }

  @Transactional
  public Issued login(LoginRequest r){
    User u = users.findByEmail(r.email().trim().toLowerCase()).orElse(null);
    boolean ok = encoder.matches(r.password(), u == null ? dummyHash : u.getPasswordHash());
    if (u == null || !ok) throw new UnauthorizedException("Invalid email or password");
    if (!u.isActive()) throw new ForbiddenException("This account has been deactivated. Please contact an administrator.");
    return issue(u, UUID.randomUUID().toString(), r.rememberMe());
  }

  private Issued issue(User u, String family, boolean remember){
    String raw = Hashing.randomToken();
    RefreshToken t = new RefreshToken();
    t.setUser(u); t.setTokenHash(Hashing.sha256(raw)); t.setFamilyId(family); t.setRememberMe(remember);
    t.setExpiresAt(Instant.now().plus(remember ? refreshDays : 1, ChronoUnit.DAYS));
    tokens.save(t);
    return new Issued(new AuthResponse(jwt.createAccessToken(u), Mapper.user(u)), raw, remember, remember ? refreshDays * 86400 : -1);
  }

  /** Rotation: every refresh revokes the presented token and issues a new one in the same family. Reuse of a revoked token revokes the whole family. */
  @Transactional(noRollbackFor = {UnauthorizedException.class, ForbiddenException.class})
  public Issued refresh(String raw){
    if (raw == null || raw.isBlank()) throw new UnauthorizedException("Missing refresh token");
    RefreshToken t = tokens.findByTokenHash(Hashing.sha256(raw)).orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
    if (t.isRevoked()) { tokens.revokeFamily(t.getFamilyId()); throw new UnauthorizedException("Refresh token reuse detected; please sign in again"); }
    if (t.getExpiresAt().isBefore(Instant.now())) { t.setRevoked(true); tokens.save(t); throw new UnauthorizedException("Session expired"); }
    User u = t.getUser();
    if (!u.isActive()) { tokens.revokeFamily(t.getFamilyId()); throw new ForbiddenException("This account has been deactivated"); }
    t.setRevoked(true); tokens.save(t);
    return issue(u, t.getFamilyId(), t.isRememberMe());
  }

  @Transactional
  public void logout(String raw){
    if (raw == null || raw.isBlank()) return;
    tokens.findByTokenHash(Hashing.sha256(raw)).ifPresent(t -> tokens.revokeFamily(t.getFamilyId()));
  }

  @Transactional(readOnly = true)
  public UserDto me(Long id){ return Mapper.user(users.findById(id).orElseThrow(() -> new NotFoundException("User not found"))); }

  @Transactional
  public UserDto updateProfile(Long id, ProfileRequest r){
    User u = users.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    u.setFullName(r.fullName().trim()); return Mapper.user(users.save(u));
  }

  @Transactional
  public void changePassword(Long id, ChangePasswordRequest r){
    User u = users.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    if (!encoder.matches(r.currentPassword(), u.getPasswordHash())) throw new BadRequestException("Current password is incorrect");
    u.setPasswordHash(encoder.encode(r.newPassword())); users.save(u);
    tokens.revokeAllForUser(u.getId());   // last statement: it clears the persistence context
  }

  @Transactional
  public void forgotPassword(String emailRaw){
    users.findByEmail(emailRaw.trim().toLowerCase()).filter(User::isActive).ifPresent(u -> {
      String raw = Hashing.randomToken();
      PasswordResetToken t = new PasswordResetToken();
      t.setUser(u); t.setTokenHash(Hashing.sha256(raw)); t.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
      resets.save(t);
      // No email provider is configured. In development only, the link can be written to the server log.
      if (logResetLinks) log.warn("DEV ONLY - password reset link: {}/reset-password?token={}", frontendOrigin, raw);
    });
  }

  @Transactional
  public void resetPassword(ResetRequest r){
    PasswordResetToken t = resets.findByTokenHash(Hashing.sha256(r.token())).orElseThrow(() -> new BadRequestException("Reset link is invalid or has expired"));
    if (t.isUsed() || t.getExpiresAt().isBefore(Instant.now())) throw new BadRequestException("Reset link is invalid or has expired");
    User u = t.getUser();
    u.setPasswordHash(encoder.encode(r.newPassword())); users.save(u);
    t.setUsed(true); resets.save(t);
    tokens.revokeAllForUser(u.getId());
  }
}
