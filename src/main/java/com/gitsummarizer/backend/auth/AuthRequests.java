package com.gitsummarizer.backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthRequests {

    private AuthRequests() {
    }

    public record Register(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email @Size(max = 320) String email,
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record Login(
            @NotBlank @Email @Size(max = 320) String email,
            @NotBlank @Size(max = 72) String password) {
    }

    public record ForgotPassword(@NotBlank @Email @Size(max = 320) String email) {
    }

    public record ResetPassword(
            @NotBlank String token,
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record UserResponse(String id, String name, String email) {
    }

    public record AuthResponse(String token, UserResponse user) {
    }
}
