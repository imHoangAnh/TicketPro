package com.xxxx.ddd.infrastructure.persistence.mapper.ticketing;

import com.xxxx.ddd.domain.model.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleJPAMapper extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUserId(Long userId);
}
