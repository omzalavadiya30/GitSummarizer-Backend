package com.gitsummarizer.backend.auth;

import com.gitsummarizer.backend.user.AppUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.gitsummarizer.backend.auth.AuthRequests.AuthResponse;
import static com.gitsummarizer.backend.auth.AuthRequests.UserResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequests.Register request) {
        return ResponseEntity.status(201).body(authService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequests.Login request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me(@RequestAttribute(ApiAuthenticationFilter.USER_ATTRIBUTE) AppUser user) {
        return AuthService.toResponse(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(bearerToken(request));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody AuthRequests.ForgotPassword request) {
        String developmentLink = authService.requestPasswordReset(request.email());
        HttpHeaders headers = new HttpHeaders();
        if (developmentLink != null) headers.set("X-Dev-Reset-Link", developmentLink);
        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody AuthRequests.ResetPassword request) {
        authService.resetPassword(request.token(), request.password());
        return ResponseEntity.noContent().build();
    }

    private static String bearerToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        return authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)
                ? authorization.substring(7).trim()
                : null;
    }
}
