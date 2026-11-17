package com.northlight.academy.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="certificates")
public class Certificate {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="certificate_code", nullable=false, unique=true) private String certificateCode;
  @OneToOne(fetch=FetchType.EAGER, optional=false) @JoinColumn(name="enrollment_id", unique=true) private Enrollment enrollment;
  @Column(name="issued_at", nullable=false) private Instant issuedAt = Instant.now();
  public Long getId(){return id;}
  public String getCertificateCode(){return certificateCode;} public void setCertificateCode(String v){certificateCode=v;}
  public Enrollment getEnrollment(){return enrollment;} public void setEnrollment(Enrollment v){enrollment=v;}
  public Instant getIssuedAt(){return issuedAt;}
}
