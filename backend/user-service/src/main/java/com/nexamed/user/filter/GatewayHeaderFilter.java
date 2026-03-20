package com.nexamed.user.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Validates that requests arrive through the API Gateway.
 * The gateway injects X-User-Id on every authenticated request.
 * If this header is missing, the request bypassed the gateway — reject it.
 *
 * Exception: /internal/** endpoints are only callable from other services
 * inside Docker network (no gateway header needed there).
 */
@Component
@Slf4j
public class GatewayHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Allow internal + actuator calls without gateway headers
        if (path.startsWith("/api/users/internal/")
                || path.startsWith("/actuator/")
                || path.equals("/api/users/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        // All other requests must have come through the gateway
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            log.warn("Request to {} missing X-User-Id header — possible gateway bypass", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized — must route through API Gateway\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}