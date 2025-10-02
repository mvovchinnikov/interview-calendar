package com.example.calendar.application.user;

import com.example.calendar.domain.model.User;
import java.util.UUID;

public interface UserService {
    User getDeveloper(UUID id);

    User getDeveloperByToken(String token);
}
