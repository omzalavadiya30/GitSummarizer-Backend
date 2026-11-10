package com.gitsummarizer.backend.github;

//import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class GitHubApiClient {

    private static final String API_VERSION = "2022-11-28";
    private final RestClient restClient;

    public GitHubApiClient(
            @Value("${github.api.base-url:https://api.github.com}") String baseUrl,
            @Value("${github.api.token:}") String token) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", API_VERSION)
                .defaultHeaders(headers -> {
                    if (token != null && !token.isBlank()) {
                        headers.setBearerAuth(token.trim());
                    }
                })
                .build();
    }

    public GitHubRepositoryData fetchRepository(GitHubRepositoryReference reference) {
        String basePath = "/repos/{owner}/{repo}";
        try {
            JsonNode repository = restClient.get()
                    .uri(basePath, reference.owner(), reference.repository())
                    .retrieve()
                    .body(JsonNode.class);
            if (repository == null) {
                throw upstreamFailure("GitHub returned an empty repository response.");
            }

            String readme = fetchReadme(reference);
            List<GitHubRepositoryData.Commit> commits = fetchCommits(reference);
            return new GitHubRepositoryData(
                    text(repository, "owner", "login"),
                    text(repository, "name"),
                    text(repository, "full_name"),
                    nullableText(repository, "description"),
                    text(repository, "html_url"),
                    text(repository, "default_branch"),
                    nullableText(repository, "language"),
                    number(repository, "stargazers_count"),
                    number(repository, "forks_count"),
                    readme,
                    commits);
        } catch (RestClientResponseException exception) {
            throw translate(exception);
        }
    }

    private String fetchReadme(GitHubRepositoryReference reference) {
        try {
            String content = restClient.get()
                    .uri("/repos/{owner}/{repo}/readme", reference.owner(), reference.repository())
                    .header(HttpHeaders.ACCEPT, "application/vnd.github.raw+json")
                    .retrieve()
                    .body(String.class);
            return content == null ? "" : content;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                return ""; // A repository can be valid without a README.
            }
            throw translate(exception);
        }
    }

    private List<GitHubRepositoryData.Commit> fetchCommits(GitHubRepositoryReference reference) {
        JsonNode response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/repos/{owner}/{repo}/commits")
                        .queryParam("per_page", 10)
                        .build(reference.owner(), reference.repository()))
                .retrieve()
                .body(JsonNode.class);
        List<GitHubRepositoryData.Commit> commits = new ArrayList<>();
        if (response == null || !response.isArray()) {
            return commits;
        }
        for (JsonNode item : response) {
            String sha = text(item, "sha");
            JsonNode commit = item.path("commit");
            String message = text(commit, "message");
            String author = nullableText(item, "author", "login");
            String date = nullableText(commit, "author", "date");
            Instant committedAt = parseInstant(date);
            commits.add(new GitHubRepositoryData.Commit(sha, message, author, committedAt, text(item, "html_url")));
        }
        return commits;
    }

    private GitHubIntegrationException translate(RestClientResponseException exception) {
        int status = exception.getStatusCode().value();
        HttpHeaders headers = exception.getResponseHeaders();
        if (status == 401) {
            return new GitHubIntegrationException(HttpStatus.BAD_GATEWAY, "GITHUB_AUTHENTICATION_FAILED",
                    "GitHub rejected the configured access token.");
        }
        if (status == 404) {
            return new GitHubIntegrationException(HttpStatus.NOT_FOUND, "GITHUB_REPOSITORY_NOT_FOUND",
                    "Repository was not found or is private. Configure GITHUB_TOKEN to access private repositories.");
        }
        if (status == 403 || status == 429) {
            String retryAfter = headers == null ? null : headers.getFirst("Retry-After");
            String remaining = headers == null ? null : headers.getFirst("X-RateLimit-Remaining");
            boolean rateLimited = status == 429
                    || "0".equals(remaining)
                    || retryAfter != null;
            if (rateLimited) {
                String message = "GitHub API rate limit exceeded.";
                if (retryAfter != null) {
                    message += " Retry after " + retryAfter + " seconds.";
                }
                return new GitHubIntegrationException(HttpStatus.TOO_MANY_REQUESTS, "GITHUB_RATE_LIMITED", message);
            }
            return new GitHubIntegrationException(HttpStatus.FORBIDDEN, "GITHUB_ACCESS_DENIED",
                    "GitHub denied access to this repository.");
        }
        return upstreamFailure("GitHub request failed with status " + status + ".");
    }

    private static GitHubIntegrationException upstreamFailure(String message) {
        return new GitHubIntegrationException(HttpStatus.BAD_GATEWAY, "GITHUB_UPSTREAM_ERROR", message);
    }

    private static String text(JsonNode node, String... path) {
        JsonNode value = node;
        for (String part : path) {
            value = value.path(part);
        }
        return value.isMissingNode() || value.isNull() ? "" : value.asText("");
    }

    private static String nullableText(JsonNode node, String... path) {
        JsonNode value = node;
        for (String part : path) {
            value = value.path(part);
        }
        return value.isMissingNode() || value.isNull() || value.asText().isBlank() ? null : value.asText();
    }

    private static long number(JsonNode node, String field) {
        return node.path(field).asLong(0);
    }

    private static Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}
