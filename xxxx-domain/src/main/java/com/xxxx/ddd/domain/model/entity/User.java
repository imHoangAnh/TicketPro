package com.xxxx.ddd.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 190)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "provider", length = 32)
    private String provider;

    @Column(name = "provider_id", length = 190)
    private String providerId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static User registerLocal(String email, String passwordHash, String fullName) {
        return new User()
                .setEmail(email)
                .setPasswordHash(passwordHash)
                .setFullName(fullName)
                .setEnabled(true)
                .setProvider("LOCAL");
    }

    @PrePersist
    void onCreate() {
        validate();
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        validate();
        updatedAt = LocalDateTime.now();
    }

    public void validate() {
        if (isBlank(email) || !email.contains("@")) {
            throw new IllegalArgumentException("email must be valid");
        }
        if (isBlank(passwordHash)) {
            throw new IllegalArgumentException("passwordHash is required");
        }
        if (isBlank(fullName)) {
            throw new IllegalArgumentException("fullName is required");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

