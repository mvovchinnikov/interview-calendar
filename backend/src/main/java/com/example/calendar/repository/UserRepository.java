package com.example.calendar.repository;

import com.example.calendar.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByPublicToken(String publicToken);
}
