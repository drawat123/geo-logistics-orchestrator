package io.github.drawat123.geo_logistics_orchestrator.security.jwt;

import io.github.drawat123.geo_logistics_orchestrator.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtRequestFilter(UserDetailsServiceImpl userDetailsService, JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);

            if (jwt != null) {
                String username = jwtUtil.extractUsername(jwt);

                // Only authenticate if the user is not already logged in (avoid overwriting context)
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // 1. Validate the token signature & expiration
                    // (We pass 'null' for UserDetails because we only care if the signature is valid)
                    if (jwtUtil.validateToken(jwt)) {

                        // 2. Extract Roles directly from Token (No DB Call!)
                        List<String> roles = jwtUtil.extractRoles(jwt);

                        // 3. Convert String roles to Spring Authorities
                        List<SimpleGrantedAuthority> authorities = roles.stream()
                                .map(SimpleGrantedAuthority::new)
                                .toList();

                        // 4. Create the UserDetails object manually
                        UserDetails userDetails = new User(username, "", authorities);

                        // 5. Create a standard Spring Auth object (since JWT is valid)
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                        // 6. Add request details (like IP address) for auditing
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 7. Set the Auth object in the Context so the user is now "Authenticated" for this request
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication");
            // We don't throw exception here; we let the request continue anonymously
            // so the AuthenticationEntryPoint can handle the 401 later.
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        // We look for a header like: "Authorization: Bearer eyJhbGciOiJIUzI1Ni..."
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
