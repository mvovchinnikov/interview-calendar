package com.example.calendar.infrastructure.config;

import com.example.calendar.application.user.DefaultUserService;
import com.example.calendar.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TransactionalUserService extends DefaultUserService {
    public TransactionalUserService(UserRepository userRepository) {
        super(userRepository);
    }
}
