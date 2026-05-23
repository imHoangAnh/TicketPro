package com.xxxx.ddd.infrastructure.persistence.repository.ticketing;

import com.xxxx.ddd.domain.model.entity.User;
import com.xxxx.ddd.domain.respository.ticketing.UserRepository;
import com.xxxx.ddd.infrastructure.persistence.mapper.ticketing.UserJPAMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJPAMapper userJPAMapper;

    public UserRepositoryImpl(UserJPAMapper userJPAMapper) {
        this.userJPAMapper = userJPAMapper;
    }

    @Override
    public User save(User user) {
        return userJPAMapper.save(user);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return userJPAMapper.findById(userId);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJPAMapper.findByEmail(email);
    }
}
