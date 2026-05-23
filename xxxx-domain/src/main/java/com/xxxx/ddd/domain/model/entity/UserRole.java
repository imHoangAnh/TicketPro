package com.xxxx.ddd.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(
        name = "user_roles",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_role", columnNames = {"user_id", "role_id"})
)
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    public static UserRole assign(Long userId, Long roleId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId is required");
        }
        if (roleId == null || roleId <= 0) {
            throw new IllegalArgumentException("roleId is required");
        }
        return new UserRole().setUserId(userId).setRoleId(roleId);
    }
}
