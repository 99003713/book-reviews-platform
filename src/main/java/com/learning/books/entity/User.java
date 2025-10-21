package com.learning.books.entity;

import com.learning.books.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", indexes = {@Index(columnList = "email", name = "idx_users_email")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email; // used as username

    @Column(nullable = false)
    private String password; // BCrypt-hashed

    @Column(nullable = false)
    private Role role; // simple role: USER / ADMIN
}
