package com.northlight.academy.repository;
import com.northlight.academy.entity.User;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
  @Query("select count(u) from User u join u.roles r where r.name = :role")
  long countByRole(@Param("role") String role);
  @Query("select u from User u join u.roles r where r.name = :role and (lower(u.fullName) like :q or lower(u.email) like :q)")
  Page<User> searchByRole(@Param("role") String role, @Param("q") String q, Pageable pageable);
  @Query("select u from User u join u.roles r where r.name = :role and u.active = :active and (lower(u.fullName) like :q or lower(u.email) like :q)")
  Page<User> searchByRoleAndActive(@Param("role") String role, @Param("active") boolean active, @Param("q") String q, Pageable pageable);
  @Query("select count(u) > 0 from User u join u.roles r where r.name = 'ADMIN'")
  boolean adminExists();
}
