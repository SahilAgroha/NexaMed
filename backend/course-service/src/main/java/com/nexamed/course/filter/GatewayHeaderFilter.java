package com.nexamed.course.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class GatewayHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Public endpoints — no gateway header needed
        if (path.startsWith("/actuator/") || path.equals("/api/courses/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Read-only course listing is public (no auth needed)
        if (request.getMethod().equals("GET") &&
                (path.equals("/api/courses") || path.startsWith("/api/courses/search"))) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized — route through API Gateway\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}