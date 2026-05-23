package com.xxxx.ddd.infrastructure.persistence.mapper.ticketing;

import com.xxxx.ddd.domain.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJPAMapper extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
