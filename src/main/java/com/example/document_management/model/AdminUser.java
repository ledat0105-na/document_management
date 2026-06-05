package com.example.document_management.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "admin_users")
@Getter
@Setter
@NoArgsConstructor
public class AdminUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(nullable = false)
    private String passwordHash;

    private String resetToken;

    private Instant resetTokenExpiresAt;
}



