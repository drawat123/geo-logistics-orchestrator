package io.github.drawat123.geo_logistics_orchestrator.security.websocket;

import io.github.drawat123.geo_logistics_orchestrator.security.jwt.JwtUtil;
import lombok.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    public WebSocketAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        // 1. Wrap the message to access STOMP headers easily
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 2. Check if this is a CONNECT frame (the very first message)
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            // 3. Extract the Token from the STOMP "Authorization" header
            // Note: Frontend must send this! (e.g. stompClient.connect({ 'Authorization': token }, ...))
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // 4. Validate Token (Using your existing JwtUtil)
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    List<String> roles = jwtUtil.extractRoles(token);

                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();

                    // 5. Create Authentication Object
                    UserDetails userDetails = new User(username, "", authorities);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                    // 6. ATTACH the User to the WebSocket Session
                    // This is crucial. Spring uses this 'accessor.setUser' to know who this socket belongs to.
                    accessor.setUser(auth);
                }
            }
        }
        return message;
    }
}