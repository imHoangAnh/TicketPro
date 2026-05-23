package com.xxxx.ddd.domain.respository.ticketing;

import com.xxxx.ddd.domain.model.entity.UserRole;

import java.util.List;

public interface UserRoleRepository {
    UserRole save(UserRole userRole);

    List<UserRole> findByUserId(Long userId);
}
