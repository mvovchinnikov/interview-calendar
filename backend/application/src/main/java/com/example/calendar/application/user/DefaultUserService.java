package com.example.calendar.application.user;

import com.example.calendar.domain.model.Role;
import com.example.calendar.domain.model.User;
import com.example.calendar.domain.repository.UserRepository;
import java.util.UUID;

public class DefaultUserService implements UserService {
    private final UserRepository userRepository;

    public DefaultUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getDeveloper(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found"));
        ensureDeveloper(user);
        return user;
    }

    @Override
    public User getDeveloperByToken(String token) {
        User user = userRepository.findByPublicToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Developer token not found"));
        ensureDeveloper(user);
        return user;
    }

    private void ensureDeveloper(User user) {
        if (user.getRole() != Role.DEV) {
            throw new IllegalStateException("User is not a developer");
        }
    }
}
