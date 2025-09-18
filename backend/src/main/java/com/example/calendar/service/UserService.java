package com.example.calendar.service;

import com.example.calendar.model.Role;
import com.example.calendar.model.User;
import com.example.calendar.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getDeveloper(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found"));
        if (user.getRole() != Role.DEV) {
            throw new IllegalStateException("User is not a developer");
        }
        return user;
    }

    public User getDeveloperByToken(String token) {
        User user = userRepository.findByPublicToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Developer token not found"));
        if (user.getRole() != Role.DEV) {
            throw new IllegalStateException("Token does not belong to a developer");
        }
        return user;
    }
}
