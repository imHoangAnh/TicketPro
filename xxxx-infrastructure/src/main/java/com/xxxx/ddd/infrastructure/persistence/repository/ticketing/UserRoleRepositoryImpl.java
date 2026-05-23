package com.xxxx.ddd.infrastructure.persistence.repository.ticketing;

import com.xxxx.ddd.domain.model.entity.UserRole;
import com.xxxx.ddd.domain.respository.ticketing.UserRoleRepository;
import com.xxxx.ddd.infrastructure.persistence.mapper.ticketing.UserRoleJPAMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRoleRepositoryImpl implements UserRoleRepository {

    private final UserRoleJPAMapper userRoleJPAMapper;

    public UserRoleRepositoryImpl(UserRoleJPAMapper userRoleJPAMapper) {
        this.userRoleJPAMapper = userRoleJPAMapper;
    }

    @Override
    public UserRole save(UserRole userRole) {
        return userRoleJPAMapper.save(userRole);
    }

    @Override
    public List<UserRole> findByUserId(Long userId) {
        return userRoleJPAMapper.findByUserId(userId);
    }
}
