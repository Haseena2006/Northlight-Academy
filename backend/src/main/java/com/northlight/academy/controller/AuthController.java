package com.northlight.academy.controller;
import com.northlight.academy.dto.Dtos.*;
import com.northlight.academy.exception.ApiExceptions.*;
import com.northlight.academy.security.AuthUser;
import com.northlight.academy.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
@RestController @RequestMapping("/api/auth")
public class AuthController {
  static final String COOKIE = "nl_refresh";
  private final AuthService auth; private final boolean secure;
  public AuthController(AuthService auth, @Value("${app.cookie.secure}") boolean secure){ this.auth = auth; this.secure = secure; }

  private static void requireHeader(HttpServletRequest r){
    if (!"NorthlightWeb".equals(r.getHeader("X-Requested-With"))) throw new ForbiddenException("Missing required request header");
  }
  private ResponseCookie cookie(String value, long maxAgeSeconds){
    ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(COOKIE, value).httpOnly(true).secure(secure).sameSite("Strict").path("/api/auth");
    if (maxAgeSeconds >= 0) b.maxAge(Duration.ofSeconds(maxAgeSeconds));   // negative => session cookie
    return b.build();
  }
  private ResponseEntity<AuthResponse> respond(AuthService.Issued i){
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie(i.refreshToken(), i.maxAgeSeconds()).toString()).header(HttpHeaders.CACHE_CONTROL, "no-store").body(i.body());
  }
  @PostMapping("/register") public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest r){ return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(r)); }
  @PostMapping("/login") public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest r){ return respond(auth.login(r)); }
  @PostMapping("/refresh") public ResponseEntity<AuthResponse> refresh(HttpServletRequest req, @CookieValue(name = COOKIE, required = false) String token){
    requireHeader(req);
    return respond(auth.refresh(token));
  }
  @PostMapping("/logout") public ResponseEntity<MessageDto> logout(HttpServletRequest req, @CookieValue(name = COOKIE, required = false) String token){
    requireHeader(req); auth.logout(token);
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie("", 0).toString()).body(new MessageDto("Signed out"));
  }
  @GetMapping("/me") public UserDto me(@AuthenticationPrincipal AuthUser me){ return auth.me(me.id()); }
  @PutMapping("/me") public UserDto update(@AuthenticationPrincipal AuthUser me, @Valid @RequestBody ProfileRequest r){ return auth.updateProfile(me.id(), r); }
  @PostMapping("/change-password") public ResponseEntity<MessageDto> change(@AuthenticationPrincipal AuthUser me, @Valid @RequestBody ChangePasswordRequest r){
    auth.changePassword(me.id(), r);
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie("", 0).toString()).body(new MessageDto("Password changed. Please sign in again."));
  }
  @PostMapping("/forgot-password") public MessageDto forgot(@Valid @RequestBody ForgotRequest r){
    auth.forgotPassword(r.email());
    return new MessageDto("If an account exists for that email, a reset link has been issued.");
  }
  @PostMapping("/reset-password") public MessageDto reset(@Valid @RequestBody ResetRequest r){ auth.resetPassword(r); return new MessageDto("Password updated. You can now sign in."); }
}
