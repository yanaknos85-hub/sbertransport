package ru.sberbank.ditsib.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.database.model.Position;
import ru.sberbank.ditsib.database.dao.PositionRepository;
import ru.sberbank.ditsib.service.PositionService;

import java.util.Optional;
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
    public void delete(Position entity) {
        entity.setActive(false);
        repository.save(entity);
    }
    
    @Override
    public Position save(Position entity) {
        return repository.save(entity);
    }
}
