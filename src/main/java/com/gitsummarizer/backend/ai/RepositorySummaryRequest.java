package com.gitsummarizer.backend.ai;

import jakarta.validation.constraints.NotBlank;

public record RepositorySummaryRequest(
        @NotBlank String url
) {
}