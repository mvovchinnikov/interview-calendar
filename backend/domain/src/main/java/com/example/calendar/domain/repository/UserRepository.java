package com.example.calendar.domain.repository;

import com.example.calendar.domain.model.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);

    Optional<User> findByPublicToken(String token);

    User save(User user);
}
