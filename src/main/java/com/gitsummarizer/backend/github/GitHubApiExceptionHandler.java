package com.gitsummarizer.backend.github;

import com.gitsummarizer.backend.ai.AiIntegrationException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

@RestControllerAdvice
public class GitHubApiExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GitHubApiExceptionHandler.class);

    @ExceptionHandler(InvalidGitHubRepositoryUrlException.class)
    public ResponseEntity<ApiError> handleInvalidUrl(
            InvalidGitHubRepositoryUrlException exception) {
        return ResponseEntity.badRequest().body(
                new ApiError(
                        "INVALID_GITHUB_URL",
                        exception.getMessage(),
                        Instant.now()
                )
        );
    }

    @ExceptionHandler(GitHubIntegrationException.class)
    public ResponseEntity<ApiError> handleGitHubFailure(
            GitHubIntegrationException exception) {
        return ResponseEntity.status(exception.getStatus()).body(
                new ApiError(
                        exception.getCode(),
                        exception.getMessage(),
                        Instant.now()
                )
        );
    }

    @ExceptionHandler(AiIntegrationException.class)
    public ResponseEntity<ApiError> handleAiFailure(
            AiIntegrationException exception) {
        if (exception.getStatus().is5xxServerError()) {
            log.error("AI request failed ({})", exception.getCode(), exception);
        }

        return ResponseEntity.status(exception.getStatus()).body(
                new ApiError(
                        exception.getCode(),
                        exception.getMessage(),
                        Instant.now()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleInvalidRequestBody(
            MethodArgumentNotValidException exception) {
        return badRequest();
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> handleMethodValidation(
            HandlerMethodValidationException exception) {
        return badRequest();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception) {
        return badRequest();
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleMissingResource(
            NoResourceFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiError(
                        "NOT_FOUND",
                        "The requested endpoint was not found.",
                        Instant.now()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedFailure(
            Exception exception) {
        log.error("Unexpected error while processing API request", exception);

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                new ApiError(
                        "UPSTREAM_ERROR",
                        "The request could not be completed.",
                        Instant.now()
                )
        );
    }

    private ResponseEntity<ApiError> badRequest() {
        return ResponseEntity.badRequest().body(
                new ApiError(
                        "INVALID_REQUEST",
                        "Request body must contain a nonblank GitHub repository URL.",
                        Instant.now()
                )
        );
    }

    public record ApiError(String code, String message, Instant timestamp) {
    }
}