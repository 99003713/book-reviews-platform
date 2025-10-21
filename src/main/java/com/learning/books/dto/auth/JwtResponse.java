package com.learning.books.dto.auth;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String name;
}
