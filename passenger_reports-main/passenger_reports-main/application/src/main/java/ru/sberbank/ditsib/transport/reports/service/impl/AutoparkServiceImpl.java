package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dao.AutoparkRepository;
import ru.sberbank.ditsib.transport.reports.model.driversData.Autopark;
import ru.sberbank.ditsib.transport.reports.service.AutoparkService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AutoparkServiceImpl implements AutoparkService {
    
    private final AutoparkRepository repository;
    
    @Override
    public Optional<Autopark> get(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    public void delete(Autopark autopark) {
        repository.delete(autopark);
    }
    
    @Override
    public Autopark save(Autopark autopark) {
        return repository.save(autopark);
    }
    
    @Override
    public Autopark findOrCreateAutoparkById(UUID id) {
        var autopark = repository.findById(id);
        return autopark.orElseGet(() -> repository.save(Autopark.builder().id(id).build()));
    }
}
