package com.xxxx.ddd.infrastructure.persistence.repository.ticketing;

import com.xxxx.ddd.domain.model.entity.Role;
import com.xxxx.ddd.domain.model.enums.RoleName;
import com.xxxx.ddd.domain.respository.ticketing.RoleRepository;
import com.xxxx.ddd.infrastructure.persistence.mapper.ticketing.RoleJPAMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleJPAMapper roleJPAMapper;

    public RoleRepositoryImpl(RoleJPAMapper roleJPAMapper) {
        this.roleJPAMapper = roleJPAMapper;
    }

    @Override
    public Role save(Role role) {
        return roleJPAMapper.save(role);
    }

    @Override
    public Optional<Role> findById(Long roleId) {
        return roleJPAMapper.findById(roleId);
    }

    @Override
    public Optional<Role> findByName(RoleName name) {
        return roleJPAMapper.findByName(name);
    }
}
