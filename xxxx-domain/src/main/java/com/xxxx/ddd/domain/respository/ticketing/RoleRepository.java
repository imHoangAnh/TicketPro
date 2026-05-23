package com.xxxx.ddd.domain.respository.ticketing;

import com.xxxx.ddd.domain.model.entity.Role;
import com.xxxx.ddd.domain.model.enums.RoleName;

import java.util.Optional;

public interface RoleRepository {
    Role save(Role role);

    Optional<Role> findById(Long roleId);

    Optional<Role> findByName(RoleName name);
}
