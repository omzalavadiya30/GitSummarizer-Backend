package com.gitsummarizer.backend.ai;

import com.gitsummarizer.backend.auth.ApiAuthenticationFilter;
import com.gitsummarizer.backend.github.GitHubApiClient;
import com.gitsummarizer.backend.github.GitHubRepositoryData;
import com.gitsummarizer.backend.github.GitHubRepositoryReference;
import com.gitsummarizer.backend.summary.SavedRepositorySummary;
import com.gitsummarizer.backend.summary.SavedRepositorySummaryRepository;
import com.gitsummarizer.backend.summary.SummaryResult;
import com.gitsummarizer.backend.user.AppUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/ai")
public class RepositorySummaryController {

    private final GitHubApiClient gitHubApiClient;
    private final RepositorySummaryService summaryService;
    private final SavedRepositorySummaryRepository historyRepository;
    private final ObjectMapper objectMapper;

    public RepositorySummaryController(
            GitHubApiClient gitHubApiClient,
            RepositorySummaryService summaryService,
            SavedRepositorySummaryRepository historyRepository,
            ObjectMapper objectMapper) {
        this.gitHubApiClient = gitHubApiClient;
        this.summaryService = summaryService;
        this.historyRepository = historyRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/summarize")
    public SummaryResult summarize(
            @Valid @RequestBody RepositorySummaryRequest request,
            @RequestAttribute(ApiAuthenticationFilter.USER_ATTRIBUTE) AppUser user) {
        GitHubRepositoryReference reference = GitHubRepositoryReference.parse(request.url());
        GitHubRepositoryData repository = gitHubApiClient.fetchRepository(reference);
        RepositorySummaryService.SummaryWithProvider generated = summaryService.summarize(repository);

        try {
            historyRepository.save(new SavedRepositorySummary(
                    user,
                    repository.repositoryUrl(),
                    objectMapper.writeValueAsString(generated.summary()),
                    objectMapper.writeValueAsString(repository)));
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "The summary was created but could not be saved to history.", exception);
        }
        return new SummaryResult(repository, generated.summary(), generated.provider());
    }
}
