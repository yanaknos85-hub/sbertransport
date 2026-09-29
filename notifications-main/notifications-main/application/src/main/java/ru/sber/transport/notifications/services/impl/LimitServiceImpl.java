package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.notifications.database.dao.messages.limits.LimitRepository;
import ru.sber.transport.notifications.database.model.limits.Limit;
import ru.sber.transport.notifications.services.LimitService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис по работе с лимитами.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LimitServiceImpl implements LimitService {
    
    private final LimitRepository limitRepository;
    
    @Override
    public Optional<Limit> get(UUID id) {
        return limitRepository.findById(id);
    }
    
    @Override
    public Optional<Limit> get(UUID departmentId, TransportTypeEnum transportTypeEnum) {
        return limitRepository.findByDepartmentIdAndTransportTypeAndYear(departmentId, transportTypeEnum.name(),
                                                                         LocalDateTime.now(Clock.systemUTC()).getYear(),
                PageRequest.of(0, 1)).stream().findFirst();
    }
    
    @Override
    public void save(Limit limit) {
        limitRepository.save(limit);
    }
}
