package com.nexamed.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Calls user-service to auto-create a profile after registration.
 * fallback = if user-service is down, registration still succeeds.
 */
@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @PostMapping("/api/users/internal/create")
    Map<String, Object> createProfile(@RequestBody Map<String, Object> request);
}