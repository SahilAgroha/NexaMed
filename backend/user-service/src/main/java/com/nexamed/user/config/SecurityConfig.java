package com.nexamed.user.config;

import com.nexamed.user.filter.GatewayHeaderFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * User-service does NOT do JWT validation itself.
 * The API Gateway already validated the JWT and injected X-User-Id / X-User-Role headers.
 * This service just trusts those headers (after verifying they exist via GatewayHeaderFilter).
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final GatewayHeaderFilter gatewayHeaderFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/api/users/health").permitAll()
                        .requestMatchers("/api/users/internal/**").permitAll()
                        .anyRequest().permitAll()  // actual auth done via GatewayHeaderFilter
                )
                .addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}