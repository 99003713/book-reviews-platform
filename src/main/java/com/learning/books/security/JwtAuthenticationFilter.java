package com.learning.books.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter:
 * - Extracts Bearer token from Authorization header
 * - Validates token
 * - Loads UserDetails and sets Authentication in SecurityContext
 *
 * Note: Make sure JwtUtil and CustomUserDetailsService are defined as beans (@Component/@Service).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        try {
            // if already authenticated, skip
            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                String header = request.getHeader("Authorization");
                String token = null;
                if (header != null && header.startsWith("Bearer ")) {
                    token = header.substring(7);
                }

                if (token != null && jwtUtil.validateToken(token)) {
                    Claims claims = jwtUtil.getClaims(token);
                    String username = claims.getSubject();

                    if (username != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        var auth = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("JWT authentication successful for user={}", username);
                    }
                }
            }
        } catch (Exception ex) {
            // On any problem, clear context and continue; auth entry point will return 401 if needed.
            SecurityContextHolder.clearContext();
            log.debug("JWT processing failed: {}", ex.getMessage());
        }

        chain.doFilter(request, response);
    }

    /**
     * Optionally skip filtering for endpoints that must be public (login/signup).
     * This prevents the filter from attempting to parse tokens for those endpoints.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // skip auth endpoints
        return path.startsWith("/api/v1/auth/");
    }
}