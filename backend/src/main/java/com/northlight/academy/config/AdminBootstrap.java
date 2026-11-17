package com.northlight.academy.config;
import com.northlight.academy.entity.User;
import com.northlight.academy.repository.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.northlight.academy.dto.Dtos;
/** Creates the first ADMIN from ADMIN_EMAIL / ADMIN_PASSWORD, only if no admin exists. No defaults in source. */
@Component
public class AdminBootstrap implements ApplicationRunner {
  private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
  private final UserRepository users; private final RoleRepository roles; private final PasswordEncoder enc;
  private final String email, password;
  public AdminBootstrap(UserRepository users, RoleRepository roles, PasswordEncoder enc, @Value("${app.admin.email:}") String email, @Value("${app.admin.password:}") String password){
    this.users = users; this.roles = roles; this.enc = enc; this.email = email; this.password = password;
  }
  @Override @Transactional public void run(ApplicationArguments args){
    if (users.adminExists()) return;
    if (email.isBlank() || password.isBlank()) { log.warn("No ADMIN exists and ADMIN_EMAIL/ADMIN_PASSWORD are not set: no administrator was created."); return; }
    if (!password.matches(Dtos.PWD) || password.length() < 10) { log.error("ADMIN_PASSWORD does not meet the password policy (10+ chars, upper, lower, digit). Admin not created."); return; }
    User u = new User(); u.setFullName("Platform Administrator"); u.setEmail(email.trim().toLowerCase()); u.setPasswordHash(enc.encode(password));
    u.getRoles().add(roles.findByName("ADMIN").orElseThrow());
    users.save(u); log.info("Initial administrator account created for the configured ADMIN_EMAIL.");
  }
}
