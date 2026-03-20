package com.nexamed.auth.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public Map<String, Object> createProfile(Map<String, Object> request) {
        log.warn("user-service unavailable — profile creation deferred for userId: {}",
                request.get("userId"));
        return Map.of("status", "deferred");
    }
}