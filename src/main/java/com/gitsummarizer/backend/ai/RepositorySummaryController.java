package com.gitsummarizer.backend.ai;

import com.gitsummarizer.backend.github.GitHubApiClient;
import com.gitsummarizer.backend.github.GitHubRepositoryData;
import com.gitsummarizer.backend.github.GitHubRepositoryReference;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class RepositorySummaryController {

    private final GitHubApiClient gitHubApiClient;
    private final OpenAiSummaryService openAiSummaryService;

    public RepositorySummaryController(
            GitHubApiClient gitHubApiClient,
            OpenAiSummaryService openAiSummaryService) {
        this.gitHubApiClient = gitHubApiClient;
        this.openAiSummaryService = openAiSummaryService;
    }

    @PostMapping("/summarize")
    public RepositorySummary summarize(
            @Valid @RequestBody RepositorySummaryRequest request) {
        GitHubRepositoryReference reference =
                GitHubRepositoryReference.parse(request.url());

        GitHubRepositoryData repository =
                gitHubApiClient.fetchRepository(reference);

        return openAiSummaryService.summarize(repository);
    }
}