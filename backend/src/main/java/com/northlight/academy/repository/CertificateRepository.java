package com.northlight.academy.repository;
import com.northlight.academy.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
  Optional<Certificate> findByCertificateCode(String code);
  Optional<Certificate> findByEnrollmentId(Long enrollmentId);
  List<Certificate> findByEnrollmentStudentIdOrderByIssuedAtDesc(Long studentId);
}
