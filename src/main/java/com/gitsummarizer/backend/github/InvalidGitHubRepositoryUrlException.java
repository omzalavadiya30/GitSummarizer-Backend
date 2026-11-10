package com.gitsummarizer.backend.github;

public class InvalidGitHubRepositoryUrlException extends RuntimeException {

    public InvalidGitHubRepositoryUrlException(String message) {
        super(message);
    }
}