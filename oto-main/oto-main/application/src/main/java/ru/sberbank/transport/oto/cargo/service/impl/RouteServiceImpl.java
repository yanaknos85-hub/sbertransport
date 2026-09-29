package ru.sberbank.transport.oto.cargo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.transport.oto.cargo.database.dao.RouteRepository;
import ru.sberbank.transport.oto.cargo.database.model.Routelist;
import ru.sberbank.transport.oto.cargo.service.RouteService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {
    private final RouteRepository repository;
    
    @Override
    public Optional<Routelist> getOptional(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    public Routelist save(Routelist routelist) {
        return repository.save(routelist);
    }
}
