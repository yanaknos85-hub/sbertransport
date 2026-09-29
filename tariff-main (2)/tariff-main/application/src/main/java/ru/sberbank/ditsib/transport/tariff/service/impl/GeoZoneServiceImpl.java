package ru.sberbank.ditsib.transport.tariff.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.tariff.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.service.GeoZoneService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    public GeoZone save(GeoZone geoZone) {
        return repository.save(geoZone);
    }
    
    @Override
    public Optional<GeoZone> find(String geoZone) {
        return repository.findByName(geoZone == null ? null : geoZone.trim());
    }
    
    @Override
    public List<GeoZone> get(Set<UUID> regionIds) {
        return repository.findAllById(regionIds);
    }
    
    @Override
    public Optional<GeoZone> search(String region) {
        return repository.findByName(region == null ? null : region.trim());
    }
}
