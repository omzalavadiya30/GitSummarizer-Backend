package com.gitsummarizer.backend.github;

import java.time.Instant;
import java.util.List;

public record GitHubRepositoryData(
        String owner,
        String name,
        String fullName,
        String description,
        String repositoryUrl,
        String defaultBranch,
        String primaryLanguage,
        long stars,
        long forks,
        String readme,
        List<Commit> recentCommits) {

    public record Commit(String sha, String message, String author, Instant committedAt, String url) {
    }
}
