package com.xxxx.ddd.infrastructure.persistence.mapper.ticketing;

import com.xxxx.ddd.domain.model.entity.Role;
import com.xxxx.ddd.domain.model.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleJPAMapper extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
