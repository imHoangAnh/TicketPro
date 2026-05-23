package com.xxxx.ddd.domain.respository.ticketing;

import com.xxxx.ddd.domain.model.entity.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(Long userId);

    Optional<User> findByEmail(String email);
}
