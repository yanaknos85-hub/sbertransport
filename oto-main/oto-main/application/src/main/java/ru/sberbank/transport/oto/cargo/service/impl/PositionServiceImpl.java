package ru.sberbank.transport.oto.cargo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.transport.oto.cargo.database.dao.PositionRepository;
import ru.sberbank.transport.oto.cargo.database.model.Position;
import ru.sberbank.transport.oto.cargo.service.PositionService;

import java.util.Optional;
import java.util.UUID;
@Slf4j
@RequiredArgsConstructor
@Service
public class PositionServiceImpl implements PositionService {

    private final PositionRepository repository;

    @Override
    public Position findOrCreatePositionById(UUID id) {
        var employee = repository.findById(id);
        return employee.orElseGet(() -> repository.save(Position.builder().id(id).build()));
    }
    
    @Override
    public Optional<Position> get(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    public void delete(Position position) {
        repository.delete(position);
    }
    
    @Override
    public Position save(Position position) {
        return repository.save(position);
    }
    
}
