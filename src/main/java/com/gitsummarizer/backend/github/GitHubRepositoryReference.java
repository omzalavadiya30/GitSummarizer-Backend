package com.gitsummarizer.backend.github;

import java.net.URI;
import java.util.regex.Pattern;

public record GitHubRepositoryReference(String owner, String repository) {

    private static final Pattern NAME =
            Pattern.compile("[A-Za-z0-9](?:[A-Za-z0-9._-]{0,98}[A-Za-z0-9])?");

    public static GitHubRepositoryReference parse(String input) {
        if (input == null || input.isBlank()) {
            throw new InvalidGitHubRepositoryUrlException(
                    "GitHub repository URL is required.");
        }

        final URI uri;
        try {
            uri = URI.create(input.trim());
        } catch (IllegalArgumentException exception) {
            throw new InvalidGitHubRepositoryUrlException(
                    "Enter a valid GitHub repository URL.");
        }

        String scheme = uri.getScheme();
        String host = uri.getHost();

        if (scheme == null
                || !(scheme.equalsIgnoreCase("https")
                || scheme.equalsIgnoreCase("http"))
                || host == null
                || !(host.equalsIgnoreCase("github.com")
                || host.equalsIgnoreCase("www.github.com"))
                || uri.getUserInfo() != null
                || uri.getPort() != -1
                || uri.getQuery() != null
                || uri.getFragment() != null) {
            throw new InvalidGitHubRepositoryUrlException(
                    "URL must point to a repository on github.com.");
        }

        String path = uri.getPath();
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String[] parts = path.split("/");
        if (parts.length != 3 || !parts[0].isEmpty()) {
            throw new InvalidGitHubRepositoryUrlException(
                    "Use https://github.com/owner/repository.");
        }

        String owner = parts[1];
        String repository = parts[2];

        if (repository.endsWith(".git")) {
            repository = repository.substring(0, repository.length() - 4);
        }

        if (!NAME.matcher(owner).matches()
                || !NAME.matcher(repository).matches()) {
            throw new InvalidGitHubRepositoryUrlException(
                    "The owner or repository name is invalid.");
        }

        return new GitHubRepositoryReference(owner, repository);
    }
}