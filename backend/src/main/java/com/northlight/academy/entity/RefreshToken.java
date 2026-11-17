package com.northlight.academy.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="refresh_tokens")
public class RefreshToken {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(fetch=FetchType.EAGER, optional=false) @JoinColumn(name="user_id") private User user;
  @Column(name="token_hash", nullable=false, unique=true) private String tokenHash;
  @Column(name="family_id", nullable=false) private String familyId;
  @Column(name="remember_me", nullable=false) private boolean rememberMe;
  @Column(nullable=false) private boolean revoked;
  @Column(name="expires_at", nullable=false) private Instant expiresAt;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
  public Long getId(){return id;}
  public User getUser(){return user;} public void setUser(User v){user=v;}
  public String getTokenHash(){return tokenHash;} public void setTokenHash(String v){tokenHash=v;}
  public String getFamilyId(){return familyId;} public void setFamilyId(String v){familyId=v;}
  public boolean isRememberMe(){return rememberMe;} public void setRememberMe(boolean v){rememberMe=v;}
  public boolean isRevoked(){return revoked;} public void setRevoked(boolean v){revoked=v;}
  public Instant getExpiresAt(){return expiresAt;} public void setExpiresAt(Instant v){expiresAt=v;}
}
