package io.github.drawat123.geo_logistics_orchestrator.config;

import io.github.drawat123.geo_logistics_orchestrator.security.websocket.WebSocketAuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;
    private final WebSocketAuthInterceptor authInterceptor;

    public WebSocketConfig(WebSocketAuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // clients will connect to: ws://<host>:<port>/ws-registry
        registry.addEndpoint("/ws-registry")
                .setAllowedOriginPatterns(allowedOrigins.split(",")) // remove in production
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        /*
         * For Server -> Client messaging.
         * Enables Spring’s in-memory message broker.
         *
         * If the server sends a message to destinations starting with /topic,
         * the broker will route and deliver it to all subscribed clients.
         *
         * "/topic" = For public broadcasts (e.g., Admin Dashboard showing all active dots)
         * "/queue" = For private 1-to-1 messages (e.g., "Driver A, here is your new job")
         *
         * Example:
         * messagingTemplate.convertAndSend("/topic/orders", orderDto);
         */
        registry.enableSimpleBroker("/topic", "/queue");

        /*
         * For Client -> Server messaging.
         * If client sends: /app/assign
         * And you have:
         *
         * @MessageMapping("/assign")
         * public void assign(OrderDTO dto) { }
         *
         * Then Spring will route the message to this method.
         *
         * Messages sent to "/app/..." will be routed to @MessageMapping controllers
         * Essential for drivers sending high-frequency GPS updates
         */
        registry.setApplicationDestinationPrefixes("/app");

        /*
         * USER SPECIFIC PREFIX (The Magic Sauce)
         * Allows you to send to "user123" and Spring automatically routes it to
         * "/user/queue/notifications" for that specific user's session.
         */
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add our interceptor to the "Inbound" channel (Client -> Server)
        registration.interceptors(authInterceptor);
    }
}
