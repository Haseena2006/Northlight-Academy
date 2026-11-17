package com.northlight.academy.repository;
import com.northlight.academy.entity.RefreshToken;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByTokenHash(String hash);
  @Modifying(clearAutomatically = true, flushAutomatically = true) @Query("update RefreshToken t set t.revoked = true where t.familyId = :f")
  void revokeFamily(@Param("f") String familyId);
  @Modifying(clearAutomatically = true, flushAutomatically = true) @Query("update RefreshToken t set t.revoked = true where t.user.id = :u")
  void revokeAllForUser(@Param("u") Long userId);
}
