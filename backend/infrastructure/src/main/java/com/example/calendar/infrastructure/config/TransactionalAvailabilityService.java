package com.example.calendar.infrastructure.config;

import com.example.calendar.application.availability.DefaultAvailabilityService;
import com.example.calendar.domain.repository.AvailabilitySlotRepository;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionalAvailabilityService extends DefaultAvailabilityService {
    public TransactionalAvailabilityService(AvailabilitySlotRepository availabilitySlotRepository, Clock clock) {
        super(availabilitySlotRepository, clock);
    }
}
