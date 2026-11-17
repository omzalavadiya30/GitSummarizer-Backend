package com.gitsummarizer.backend.auth;

import com.gitsummarizer.backend.user.AppUser;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "user_sessions", indexes = @Index(name = "idx_session_token_hash", columnList = "token_hash", unique = true))
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected UserSession() {
    }

    public UserSession(AppUser user, String tokenHash, Instant expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void setCreationTime() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public AppUser getUser() { return user; }
    public Instant getExpiresAt() { return expiresAt; }
}
