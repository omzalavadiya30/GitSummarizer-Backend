package com.gitsummarizer.backend.summary;

import com.gitsummarizer.backend.user.AppUser;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "saved_repository_summaries")
public class SavedRepositorySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "repository_url", nullable = false, columnDefinition = "text")
    private String repositoryUrl;

    @Column(name = "summary_text", nullable = false, columnDefinition = "text")
    private String summaryText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SavedRepositorySummary() {
    }

    public SavedRepositorySummary(AppUser user, String repositoryUrl, String summaryText) {
        this.user = user;
        this.repositoryUrl = repositoryUrl;
        this.summaryText = summaryText;
    }

    @PrePersist
    void setCreationTime() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}