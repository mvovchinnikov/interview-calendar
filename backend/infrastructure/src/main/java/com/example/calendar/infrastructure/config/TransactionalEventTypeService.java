package com.example.calendar.infrastructure.config;

import com.example.calendar.application.eventtype.DefaultEventTypeService;
import com.example.calendar.domain.repository.EventTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionalEventTypeService extends DefaultEventTypeService {
    public TransactionalEventTypeService(EventTypeRepository eventTypeRepository) {
        super(eventTypeRepository);
    }
}
