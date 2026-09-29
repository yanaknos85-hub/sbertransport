package ru.sberbank.transport.oto.cargo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.request.messaging.TemplateForCargoMessage;
import ru.sberbank.transport.oto.cargo.database.dao.TemplateForCargoRepository;
import ru.sberbank.transport.oto.cargo.database.model.template.TemplateForCargo;
import ru.sberbank.transport.oto.cargo.mappers.TemplateMapper;
import ru.sberbank.transport.oto.cargo.service.TemplateForCargoService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TemplateForCargoServiceImpl implements TemplateForCargoService {
    
    private final TemplateForCargoRepository repository;
    private final TemplateMapper mapper;
    
    @Override
    public void save(TemplateForCargoMessage message) {
        repository.save(mapper.toEntity(message));
    }
    
    public Optional<TemplateForCargo> findById(UUID id){
       return repository.findById(id);
    }
    
}
