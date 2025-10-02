package com.example.calendar.infrastructure.persistence.repository;

import com.example.calendar.domain.model.User;
import com.example.calendar.domain.repository.UserRepository;
import com.example.calendar.infrastructure.persistence.entity.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryAdapter implements UserRepository {
    private final JpaUserRepository userRepository;

    public UserRepositoryAdapter(JpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id).map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByPublicToken(String token) {
        return userRepository.findByPublicToken(token).map(UserEntity::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity saved = userRepository.save(UserEntity.fromDomain(user));
        return saved.toDomain();
    }
}
