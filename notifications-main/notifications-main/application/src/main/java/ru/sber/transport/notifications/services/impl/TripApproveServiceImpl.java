package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.database.dao.messages.approve.TripApproveRepository;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.services.TripApproveService;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса по работе с согласованиями поездок.
 */
@RequiredArgsConstructor
@Component
class TripApproveServiceImpl implements TripApproveService {
    
    private final TripApproveRepository repository;
    
    @Override
    public TripApprove get(UUID id) {
        return repository.findById(id).orElseGet(() -> save(new TripApprove(id)));
    }
    
    @Override
    public TripApprove save(TripApprove tripApprove) {
        return repository.save(tripApprove);
    }
    
    @Override
    public Optional<TripApprove> getByRequestId(UUID id) {
        return repository.findByRequestId(id);
    }
}
