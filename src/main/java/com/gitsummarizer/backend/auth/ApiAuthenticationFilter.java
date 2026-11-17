package com.gitsummarizer.backend.auth;

import tools.jackson.databind.ObjectMapper;
import com.gitsummarizer.backend.user.AppUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
@Order(10)
public class ApiAuthenticationFilter extends OncePerRequestFilter {

    public static final String USER_ATTRIBUTE = "authenticatedUser";
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public ApiAuthenticationFilter(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/")
                || path.equals("/api/health")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/forgot-password")
                || path.equals("/api/auth/reset-password")
                || request.getMethod().equalsIgnoreCase("OPTIONS");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        String token = authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)
                ? authorization.substring(7).trim()
                : null;
        AppUser user = authService.authenticate(token);
        if (user == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "code", "INVALID_CREDENTIALS",
                    "message", "Sign in to continue.",
                    "timestamp", Instant.now().toString()));
            return;
        }
        request.setAttribute(USER_ATTRIBUTE, user);
        filterChain.doFilter(request, response);
    }
}
