package com.gitsummarizer.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @Query("select t from PasswordResetToken t join fetch t.user where t.tokenHash = :tokenHash and t.expiresAt > :now")
    Optional<PasswordResetToken> findActiveToken(@Param("tokenHash") String tokenHash, @Param("now") Instant now);

    void deleteAllByUser_Id(Long userId);
    void deleteByTokenHash(String tokenHash);
}
