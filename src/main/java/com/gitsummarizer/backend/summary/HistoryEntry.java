package com.gitsummarizer.backend.summary;

import com.gitsummarizer.backend.ai.RepositorySummary;

import java.time.Instant;

public record HistoryEntry(
        String id,
        String url,
        String fullName,
        String description,
        String language,
        long stars,
        RepositorySummary summary,
        boolean saved,
        Instant createdAt) {
}
