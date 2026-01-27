package io.github.drawat123.geo_logistics_orchestrator.config;

import org.aspectj.weaver.ast.And;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // clients will connect to: ws://<host>:<port>/ws-registry
        registry.addEndpoint("/ws-registry")
                .setAllowedOriginPatterns("*") // remove in production
//                .setAllowedOriginPatterns("https://app.yourdomain.com") use this example in production
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
         * Example:
         * messagingTemplate.convertAndSend("/topic/orders", orderDto);
         */
        registry.enableSimpleBroker("/topic");

        /*
         * For Client -> Server messaging.
         * If client sends: /app/assign
         * And you have:
         *
         * @MessageMapping("/assign")
         * public void assign(OrderDTO dto) { }
         *
         * Then Spring will route the message to this method.
         */
        //registry.setApplicationDestinationPrefixes("/app");
    }
}
