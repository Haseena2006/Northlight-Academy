package com.northlight.academy.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
@Entity @Table(name="users")
public class User {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="full_name", nullable=false) private String fullName;
  @Column(nullable=false, unique=true) private String email;
  @Column(name="password_hash", nullable=false) private String passwordHash;
  private boolean active = true;
  @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt = Instant.now();
  @Column(name="updated_at", nullable=false) private Instant updatedAt = Instant.now();
  @ManyToMany(fetch=FetchType.EAGER)
  @JoinTable(name="user_roles", joinColumns=@JoinColumn(name="user_id"), inverseJoinColumns=@JoinColumn(name="role_id"))
  private Set<Role> roles = new HashSet<>();
  @PreUpdate void touch(){ updatedAt = Instant.now(); }
  public boolean hasRole(String r){ return roles.stream().anyMatch(x->x.getName().equals(r)); }
  public Long getId(){return id;}
  public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
  public String getEmail(){return email;} public void setEmail(String v){email=v;}
  public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String v){passwordHash=v;}
  public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
  public Instant getCreatedAt(){return createdAt;}
  public Set<Role> getRoles(){return roles;}
}
