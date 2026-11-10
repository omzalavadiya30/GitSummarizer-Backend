package com.gitsummarizer.backend.ai;

import org.springframework.http.HttpStatus;

public class AiIntegrationException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public AiIntegrationException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public AiIntegrationException(
            HttpStatus status,
            String code,
            String message,
            Throwable cause) {
        super(message, cause);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
