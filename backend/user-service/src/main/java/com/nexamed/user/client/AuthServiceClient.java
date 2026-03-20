package com.nexamed.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * OpenFeign client for calling auth-service.
 *
 * name = "auth-service" must match spring.application.name in auth-service.
 * Eureka resolves "auth-service" → actual host:port automatically.
 * No hardcoded URLs — lb:// load balancing handled by Spring Cloud LoadBalancer.
 *
 * Usage example in a service:
 *   Map<String, String> user = authServiceClient.validateToken("Bearer " + token);
 */
@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    /**
     * Validates a JWT and returns user info.
     * Calls GET /api/auth/me on auth-service.
     */
    @GetMapping("/api/auth/me")
    Map<String, String> validateToken(@RequestHeader("Authorization") String bearerToken);
}