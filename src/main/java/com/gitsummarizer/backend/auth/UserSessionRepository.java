package com.gitsummarizer.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    @Query("select s from UserSession s join fetch s.user where s.tokenHash = :tokenHash and s.expiresAt > :now")
    Optional<UserSession> findActiveSession(@Param("tokenHash") String tokenHash, @Param("now") Instant now);

    void deleteByTokenHash(String tokenHash);
    void deleteAllByUser_Id(Long userId);
}
