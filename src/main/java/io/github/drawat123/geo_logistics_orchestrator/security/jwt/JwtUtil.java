package io.github.drawat123.geo_logistics_orchestrator.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expirationMs}")
    private long expirationMs;

    public String generateToken(String email, List<String> roles) {
        return Jwts.builder()
                .subject(email)                 // 1. Who is this token for? (The User)
                .claim("roles", roles)
                .issuedAt(new Date())              // 2. When was it created? (Now)
                .expiration(new Date((new Date()).getTime() + expirationMs))   // 3. When does it die?
                .signWith(key())                 // 4. Lock it with our Secret Key
                .compact();                           // 5. Turn it into a string
    }

    public Boolean validateToken(String token) {
        try {
            // Validates that the token isn't expired
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            // Log error (SignatureException, ExpiredJwtException, etc.)
            return false;
        }
    }

    private SecretKey key() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // NEW: Helper to get roles out without hitting DB
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("roles", List.class));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key()) // Use the key to unlock the token
                .build()
                .parseSignedClaims(token)     // Parse the string
                .getPayload();                // Get the data (The Claims)
    }
}