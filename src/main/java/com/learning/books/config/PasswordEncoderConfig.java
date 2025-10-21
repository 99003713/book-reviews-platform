package com.learning.books.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Defines a global PasswordEncoder bean for the application.
 * This bean will be auto-injected wherever PasswordEncoder is required.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt is the recommended algorithm for secure password hashing
        return new BCryptPasswordEncoder();
    }
}