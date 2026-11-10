package com.gitsummarizer.backend.github;

import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/github")
public class GitHubRepositoryController {

    private final GitHubApiClient gitHubApiClient;

    public GitHubRepositoryController(GitHubApiClient gitHubApiClient) {
        this.gitHubApiClient = gitHubApiClient;
    }

    @GetMapping("/repository")
    public GitHubRepositoryData getRepository(@RequestParam @NotBlank String url) {
        GitHubRepositoryReference reference = GitHubRepositoryReference.parse(url);
        return gitHubApiClient.fetchRepository(reference);
    }
}
