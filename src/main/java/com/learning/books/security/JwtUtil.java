package com.learning.books.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    // Use a strong secret; for demo keep in application.yml as jwt.secret (or env var)
    private final Key key;
    private final long jwtExpirationMs;

    public JwtUtil(org.springframework.core.env.Environment env) {
        String secret = env.getProperty("app.jwt.secret", "changeit_changeit_changeit_changeit");
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpirationMs = Long.parseLong(env.getProperty("app.jwt.expiration-ms", "3600000")); // 1h default
    }

    public String generateToken(String username, Long userId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMs);
        return Jwts.builder()
                .setSubject(username)
                .claim("uid", userId)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            log.warn("JWT validation failed: {}", ex.getMessage());
        }
        return false;
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}

