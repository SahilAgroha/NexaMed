package com.nexamed.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket config using STOMP protocol.
 *
 * Flow:
 *   Browser connects to ws://localhost:8086/ws
 *   Browser subscribes to /topic/notifications/{userId}
 *   Server pushes NotificationMessage to that topic
 *   Browser receives it instantly (no polling needed)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /topic → server-to-client broadcasts (notifications)
        // /queue → server-to-specific-user messages
        registry.enableSimpleBroker("/topic", "/queue");

        // /app → client-to-server messages prefix
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // in prod: set exact frontend URL
                .withSockJS();                  // fallback for older browsers
    }
}