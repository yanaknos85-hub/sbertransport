package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.database.dao.PositionRepository;
import ru.sber.transport.telemechanic.database.model.Position;
import ru.sber.transport.telemechanic.exception.AwaitingSynchronizationException;
import ru.sber.transport.telemechanic.service.PositionService;
import ru.sber.transport.telemechanic.service.grpc.Positions;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of position service.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class PositionServiceImpl implements PositionService {
    
    private final PositionRepository repository;
    private final Positions positions;
    
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
    
    @SneakyThrows
    @Override
    public void saveGrpcEntity(String message, UUID id) {
        this.save(Optional.ofNullable(positions.one(id)).orElseThrow(() -> {
            log.info(message);
            throw new AwaitingSynchronizationException("Awaiting an position synchronization");
        }));
    }
}
