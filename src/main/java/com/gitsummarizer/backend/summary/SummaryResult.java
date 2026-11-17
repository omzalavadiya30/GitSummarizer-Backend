package com.gitsummarizer.backend.summary;

import com.gitsummarizer.backend.ai.RepositorySummary;
import com.gitsummarizer.backend.github.GitHubRepositoryData;

public record SummaryResult(
        GitHubRepositoryData repository,
        RepositorySummary summary,
        String provider) {
}
