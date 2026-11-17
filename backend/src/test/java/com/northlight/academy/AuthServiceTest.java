package com.northlight.academy;
import com.northlight.academy.dto.Dtos.*;
import com.northlight.academy.entity.*;
import com.northlight.academy.exception.ApiExceptions.*;
import com.northlight.academy.repository.*;
import com.northlight.academy.security.JwtService;
import com.northlight.academy.service.AuthService;
import com.northlight.academy.util.Hashing;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
class AuthServiceTest {
  UserRepository users = mock(UserRepository.class); RoleRepository roles = mock(RoleRepository.class); RefreshTokenRepository tokens = mock(RefreshTokenRepository.class);
  PasswordResetTokenRepository resets = mock(PasswordResetTokenRepository.class);
  BCryptPasswordEncoder enc = new BCryptPasswordEncoder(4);
  JwtService jwt = new JwtService("unit-test-secret-unit-test-secret-0123456789", 15);
  AuthService svc;
  @BeforeEach void setUp(){
    svc = new AuthService(users, roles, tokens, resets, enc, jwt, 14, "http://localhost:5173", false);
    Role student = new Role(); student.setName("STUDENT");
    when(roles.findByName("STUDENT")).thenReturn(Optional.of(student));
    when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
    when(tokens.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
  }
  @Test void registerHashesPasswordAndAssignsStudentRole(){
    var dto = svc.register(new RegisterRequest("Ada Lovelace", "Ada@Example.com", "Secret123"));
    ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class); verify(users).save(cap.capture());
    User saved = cap.getValue();
    assertNotEquals("Secret123", saved.getPasswordHash());
    assertTrue(enc.matches("Secret123", saved.getPasswordHash()));
    assertEquals("ada@example.com", saved.getEmail());
    assertEquals(java.util.List.of("STUDENT"), dto.roles());
  }
  @Test void registerRejectsDuplicateEmail(){
    when(users.existsByEmail("ada@example.com")).thenReturn(true);
    assertThrows(ConflictException.class, () -> svc.register(new RegisterRequest("Ada", "ada@example.com", "Secret123")));
    verify(users, never()).save(any());
  }
  User user(boolean active){ User u = new User(); u.setEmail("a@b.com"); u.setFullName("A"); u.setActive(active); u.setPasswordHash(enc.encode("Secret123")); return u; }
  @Test void loginWithWrongPasswordIsUnauthorized(){
    when(users.findByEmail("a@b.com")).thenReturn(Optional.of(user(true)));
    assertThrows(UnauthorizedException.class, () -> svc.login(new LoginRequest("a@b.com", "Wrong1234", false)));
  }
  @Test void loginForUnknownUserIsUnauthorized(){
    when(users.findByEmail("x@y.com")).thenReturn(Optional.empty());
    assertThrows(UnauthorizedException.class, () -> svc.login(new LoginRequest("x@y.com", "Secret123", false)));
  }
  @Test void inactiveAccountCannotLogin(){
    when(users.findByEmail("a@b.com")).thenReturn(Optional.of(user(false)));
    assertThrows(ForbiddenException.class, () -> svc.login(new LoginRequest("a@b.com", "Secret123", false)));
  }
  @Test void validLoginIssuesAccessAndRefreshTokens(){
    when(users.findByEmail("a@b.com")).thenReturn(Optional.of(user(true)));
    var issued = svc.login(new LoginRequest("a@b.com", "Secret123", true));
    assertNotNull(issued.body().accessToken()); assertNotNull(issued.refreshToken()); assertTrue(issued.maxAgeSeconds() > 0);
    ArgumentCaptor<RefreshToken> cap = ArgumentCaptor.forClass(RefreshToken.class); verify(tokens).save(cap.capture());
    assertEquals(Hashing.sha256(issued.refreshToken()), cap.getValue().getTokenHash());   // only the hash is stored
  }
  @Test void revokedRefreshTokenRevokesFamily(){
    RefreshToken t = new RefreshToken(); t.setRevoked(true); t.setFamilyId("fam"); t.setExpiresAt(Instant.now().plusSeconds(60));
    when(tokens.findByTokenHash(Hashing.sha256("raw"))).thenReturn(Optional.of(t));
    assertThrows(UnauthorizedException.class, () -> svc.refresh("raw"));
    verify(tokens).revokeFamily("fam");
  }
  @Test void jwtRoundTrip(){
    User u = user(true); var r = new Role(); r.setName("STUDENT"); u.getRoles().add(r);
    String token = jwt.createAccessToken(u);
    assertEquals("a@b.com", jwt.parse(token).get("email"));
    assertThrows(io.jsonwebtoken.JwtException.class, () -> jwt.parse(token + "x"));
  }
}
