package ru.sberbank.ditsib.transport.request.service.corp.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.request.database.dao.PositionRepository;
import ru.sberbank.ditsib.transport.request.database.model.corp.Position;
import ru.sberbank.ditsib.transport.request.service.corp.PositionService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of position service.
 */
@RequiredArgsConstructor
@Service
@Transactional
public class PositionServiceImpl implements PositionService {
    
    private final PositionRepository repository;
    
    @Override
    public Optional<Position> get(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    public void delete(Position position) {
        position.setActive(false);
        repository.save(position);
    }
    
    @Override
    public Position save(Position position) {
        return repository.save(position);
    }
    
    @Override
    public List<Position> getByIds(Set<UUID> positionIds) {
        return repository.findAllById(positionIds);
    }
    
}
