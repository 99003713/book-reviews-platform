package com.learning.books.controller;

import com.learning.books.dto.auth.*;
import com.learning.books.dto.common.ApiResponse;
import com.learning.books.dto.user.UserDto;
import com.learning.books.entity.User;
import com.learning.books.enums.Role;
import com.learning.books.repository.UserRepository;
import com.learning.books.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Authentication controller: signup & login.
 * - Uses ApiResponse<T> for all responses (consistent shape)
 * - Returns 201 CREATED on successful signup and 200 OK on successful login
 * - Returns 400/401/409 as appropriate for client errors
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserDto>> signup(@Valid @RequestBody SignupRequest req) {
        log.info("Signup request for email={}", req.getEmail());

        if (userRepository.existsByEmail(req.getEmail())) {
            log.warn("Signup failed - email already taken: {}", req.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.<UserDto>builder()
                            .success(false)
                            .message("Email already taken")
                            .data(null)
                            .build());
        }

        // Default role to USER if caller didn't specify one (safer for open signups)
        Role role = req.getRole() == null ? Role.USER : req.getRole();

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .build();

        User saved = userRepository.save(user);
        log.info("User created with id={}", saved.getId());

        UserDto userDto = UserDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<UserDto>builder()
                        .success(true)
                        .message("User registered successfully")
                        .data(userDto)
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest req) {
        log.info("Login attempt for email={}", req.getEmail());

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );

            // set authentication for the current request context
            SecurityContextHolder.getContext().setAuthentication(auth);

            // load user entity (authentication succeeded so user should exist)
            User user = userRepository.findByEmail(req.getEmail())
                    .orElseThrow(() -> new IllegalStateException("User authenticated but not found"));

            String token = jwtUtil.generateToken(user.getEmail(), user.getId());

            JwtResponse jwtResponse = new JwtResponse(token, "Bearer", user.getId(), user.getEmail(), user.getName());

            return ResponseEntity.ok(ApiResponse.<JwtResponse>builder()
                    .success(true)
                    .message("Login successful")
                    .data(jwtResponse)
                    .build());

        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for email={}: {}", req.getEmail(), ex.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<JwtResponse>builder()
                            .success(false)
                            .message("Invalid email or password")
                            .data(null)
                            .build());
        } catch (Exception ex) {
            log.error("Unexpected error during login for email={}: {}", req.getEmail(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<JwtResponse>builder()
                            .success(false)
                            .message("Internal server error")
                            .data(null)
                            .build());
        }
    }
}