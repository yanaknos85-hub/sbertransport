package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.request.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.request.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.request.service.GeoZoneService;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса для работы с геозонами.
 */
@RequiredArgsConstructor
@Component
class GeoZoneServiceImpl implements GeoZoneService {
    
    private final GeoZoneRepository repository;
    
    @Override
    public Optional<GeoZone> get(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    public void delete(GeoZone geoZone) {
        if (repository.existsById(geoZone.getId())) {
            repository.delete(geoZone);
        }
    }
    
    @Override
    public void save(GeoZone geoZone) {
        repository.save(geoZone);
    }
}
