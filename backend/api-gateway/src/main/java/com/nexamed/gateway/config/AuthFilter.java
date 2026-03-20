package com.nexamed.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Validates JWT and injects user info as headers for downstream services.
 *
 * Headers injected:
 *   X-User-Id    → userId claim (UUID)   ← was wrongly using subject (email)
 *   X-User-Role  → role claim
 *   X-User-Email → email claim (subject)
 */
@Component
@Slf4j
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("No Authorization header — path: {}", path);
                return sendError(exchange, HttpStatus.UNAUTHORIZED, "Missing Authorization header");
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = parseClaims(token);

                // ── FIX: X-User-Id must be the UUID, not the email ──────
                // subject = email (used as username in Spring Security)
                // userId claim = actual UUID from auth-service User.id
                String userId = claims.get("userId", String.class);
                String email  = claims.getSubject();           // email
                String role   = claims.get("role", String.class);

                // Fallback: if userId claim missing (old tokens), use subject
                if (userId == null || userId.isBlank()) {
                    userId = email;
                }

                ServerHttpRequest mutated = request.mutate()
                        .header("X-User-Id",    userId)   // UUID
                        .header("X-User-Email", email)    // email
                        .header("X-User-Role",  role)
                        .build();

                return chain.filter(exchange.mutate().request(mutated).build());

            } catch (ExpiredJwtException e) {
                log.warn("Expired token — path: {}", path);
                return sendError(exchange, HttpStatus.UNAUTHORIZED, "Token has expired");
            } catch (MalformedJwtException | SignatureException e) {
                log.warn("Invalid token — path: {}", path);
                return sendError(exchange, HttpStatus.UNAUTHORIZED, "Invalid token");
            } catch (Exception e) {
                log.error("Auth error — path: {}, error: {}", path, e.getMessage());
                return sendError(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Auth processing error");
            }
        };
    }

    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Mono<Void> sendError(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"error\":\"" + message + "\",\"status\":" + status.value() + "}";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    public static class Config {}
}