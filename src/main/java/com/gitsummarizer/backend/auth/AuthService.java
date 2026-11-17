package com.gitsummarizer.backend.auth;

import com.gitsummarizer.backend.user.AppUser;
import com.gitsummarizer.backend.user.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

import static com.gitsummarizer.backend.auth.AuthRequests.AuthResponse;
import static com.gitsummarizer.backend.auth.AuthRequests.UserResponse;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordResetTokenRepository resetRepository;
    private final PasswordHasher passwordHasher;
    private final PasswordResetDelivery resetDelivery;
    private final SecureRandom secureRandom = new SecureRandom();
    private final int sessionDays;
    private final int resetMinutes;

    public AuthService(
            AppUserRepository userRepository,
            UserSessionRepository sessionRepository,
            PasswordResetTokenRepository resetRepository,
            PasswordHasher passwordHasher,
            PasswordResetDelivery resetDelivery,
            @Value("${auth.session.days:7}") int sessionDays,
            @Value("${auth.reset.minutes:30}") int resetMinutes) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.resetRepository = resetRepository;
        this.passwordHasher = passwordHasher;
        this.resetDelivery = resetDelivery;
        this.sessionDays = sessionDays;
        this.resetMinutes = resetMinutes;
    }

    @Transactional
    public AuthResponse register(AuthRequests.Register request) {
        String email = normalizeEmail(request.email());
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new AuthException(HttpStatus.CONFLICT, "EMAIL_TAKEN", "An account with this email already exists.");
        }
        validatePassword(request.password());
        AppUser user = userRepository.save(new AppUser(
                email,
                request.name().trim(),
                passwordHasher.hash(request.password())));
        return createSession(user);
    }

    @Transactional
    public AuthResponse login(AuthRequests.Login request) {
        AppUser user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email())).orElse(null);
        if (user == null || !passwordHasher.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        return createSession(user);
    }

    @Transactional(readOnly = true)
    public AppUser authenticate(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) return null;
        return sessionRepository.findActiveSession(hashToken(accessToken), Instant.now())
                .map(UserSession::getUser)
                .orElse(null);
    }

    @Transactional
    public void logout(String accessToken) {
        if (accessToken != null && !accessToken.isBlank()) {
            sessionRepository.deleteByTokenHash(hashToken(accessToken));
        }
    }

    @Transactional
    public String requestPasswordReset(String rawEmail) {
        AppUser user = userRepository.findByEmailIgnoreCase(normalizeEmail(rawEmail)).orElse(null);
        if (user == null || user.getPasswordHash() == null) return null;

        resetRepository.deleteAllByUser_Id(user.getId());
        String token = newAccessToken();
        resetRepository.save(new PasswordResetToken(
                user,
                hashToken(token),
                Instant.now().plus(resetMinutes, ChronoUnit.MINUTES)));
        return resetDelivery.deliver(user, token);
    }

    @Transactional
    public void resetPassword(String token, String password) {
        PasswordResetToken reset = resetRepository.findActiveToken(hashToken(token), Instant.now())
                .orElseThrow(() -> new AuthException(
                        HttpStatus.BAD_REQUEST,
                        "INVALID_TOKEN",
                        "This password reset link is invalid or has expired."));
        validatePassword(password);
        AppUser user = reset.getUser();
        user.setPasswordHash(passwordHasher.hash(password));
        userRepository.save(user);
        resetRepository.deleteByTokenHash(hashToken(token));
        sessionRepository.deleteAllByUser_Id(user.getId());
    }

    public static UserResponse toResponse(AppUser user) {
        String name = user.getDisplayName();
        if (name == null || name.isBlank()) name = user.getEmail().split("@", 2)[0];
        return new UserResponse(String.valueOf(user.getId()), name, user.getEmail());
    }

    public static String hashToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("Secure token hashing is unavailable.", exception);
        }
    }

    private AuthResponse createSession(AppUser user) {
        String token = newAccessToken();
        sessionRepository.save(new UserSession(
                user,
                hashToken(token),
                Instant.now().plus(sessionDays, ChronoUnit.DAYS)));
        return new AuthResponse(token, toResponse(user));
    }

    private String newAccessToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 72
                || !password.matches("(?s).*\\p{L}.*") || !password.matches("(?s).*\\d.*")) {
            throw new AuthException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_REQUEST",
                    "Password must contain at least 8 characters, including a letter and a number.");
        }
    }

    private static AuthException invalidCredentials() {
        return new AuthException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Incorrect email or password.");
    }
}
