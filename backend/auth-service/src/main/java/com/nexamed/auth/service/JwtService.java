package com.nexamed.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private final StringRedisTemplate redisTemplate;

    public JwtService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ── Token generation ──────────────────────────────────────────

    public String generateAccessToken(UserDetails userDetails, String role, String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role",   role);
        claims.put("email",  userDetails.getUsername());
        claims.put("userId", userId);
        return buildToken(claims, userDetails.getUsername(), jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails.getUsername(), refreshExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // ── Token validation ──────────────────────────────────────────

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(token)
                    && !isTokenBlacklisted(token);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Uses private Claims-returning method internally — no type mismatch
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    // ── Public claims access for AuthController /me endpoint ──────
    // Returns Map<String, Object> so controller doesn't depend on jjwt Claims type
    public Map<String, Object> extractAllClaims(String token) {
        Claims claims = parseClaims(token);
        Map<String, Object> result = new HashMap<>();
        result.put("sub",    claims.getSubject());
        result.put("userId", claims.get("userId", String.class));
        result.put("role",   claims.get("role",   String.class));
        result.put("email",  claims.get("email",  String.class));
        result.put("exp",    claims.getExpiration());
        return result;
    }

    // ── Token blacklisting (logout) ───────────────────────────────

    public void blacklistToken(String token) {
        try {
            Claims claims = parseClaims(token);
            long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue()
                        .set("blacklist:" + token, "true", ttl, TimeUnit.MILLISECONDS);
                log.info("Token blacklisted, TTL: {}ms", ttl);
            }
        } catch (Exception e) {
            log.warn("Could not blacklist token: {}", e.getMessage());
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
    }

    // ── Private: always returns jjwt Claims (used internally only) ─
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}