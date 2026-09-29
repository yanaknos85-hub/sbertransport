package ru.sberbank.transport.oto.cargo.service;

import ru.sberbank.ditsib.transport.request.messaging.TemplateForCargoMessage;
import ru.sberbank.transport.oto.cargo.database.model.template.TemplateForCargo;

import java.util.Optional;
import java.util.UUID;

public interface TemplateForCargoService {
    
    void save(TemplateForCargoMessage message);
    
    Optional<TemplateForCargo> findById(UUID id);
    
}
