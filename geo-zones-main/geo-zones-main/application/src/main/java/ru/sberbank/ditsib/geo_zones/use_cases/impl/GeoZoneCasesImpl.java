package ru.sberbank.ditsib.geo_zones.use_cases.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.geo_zones.use_cases.GeoZoneCases;
import ru.sberbank.ditsib.geo_zones.use_cases.model.GeoZone;
import ru.sberbank.ditsib.geo_zones.use_cases.model.GeoZoneWithChildren;
import ru.sberbank.ditsib.geo_zones.use_cases.providers.GeoZoneProvider;
import ru.sberbank.ditsib.geo_zones.web.http.dto.WaypointDto;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса геозон.
 */
@RequiredArgsConstructor
@Component
@Transactional
@Slf4j
class GeoZoneCasesImpl implements GeoZoneCases {
    
    private final GeoZoneProvider geoZoneProvider;
    
    @Override
    public GeoZone save(UUID id, @NonNull GeoZone data) {
        var geoZone = Optional.ofNullable(id).map(this::getZone).orElseGet(GeoZone::new);
        data.setId(id);
        
        var parent = data.getParentId();
        if (parent != null) {
            getZone(parent);
            geoZone.setParentId(parent);
        } else {
            if (id == null) {
                geoZoneProvider.getByCodeFirstLevel(data.getCode())
                               .ifPresent(item -> {
                                   throw new DuplicateDataException(GeoZone.class, "code", item.getCode());
                               });
                geoZoneProvider.getByName(data.getName())
                               .ifPresent(item -> {
                                   throw new DuplicateDataException(GeoZone.class, "name", item.getName());
                               });
            } else {
                geoZoneProvider.getByCodeFirstLevel(data.getCode(), id)
                               .ifPresent(item -> {
                                   throw new DuplicateDataException(GeoZone.class, "code", item.getCode());
                               });
                geoZoneProvider.getByName(data.getName(), id)
                               .ifPresent(item -> {
                                   throw new DuplicateDataException(GeoZone.class, "name", item.getName());
                               });
            }
        }
        
        return geoZoneProvider.save(data);
    }
    
    @Override
    public GeoZone save(@NonNull GeoZone data) {
        var geoZone = Optional.ofNullable(data.getId()).map(this::getZone).orElseGet(GeoZone::new);
        
        var parent = data.getParentId();
        if (parent != null) {
            getZone(parent);
            geoZone.setParentId(parent);
        } else {
            if (data.getId() == null) {
                geoZoneProvider.getByCodeFirstLevel(data.getCode())
                               .ifPresent(item -> {
                                   throw new DuplicateDataException(GeoZone.class, "code", item.getCode());
                               });
                geoZoneProvider.getByName(data.getName())
                               .ifPresent(item -> {
                                   throw new DuplicateDataException(GeoZone.class, "name", item.getName());
                               });
            } else {
                geoZoneProvider.getByCodeFirstLevel(data.getCode(), data.getId())
                               .ifPresent(item -> {
                                   throw new DuplicateDataException(GeoZone.class, "code", item.getCode());
                               });
                geoZoneProvider.getByName(data.getName(), data.getId())
                               .ifPresent(item -> {
                                   throw new DuplicateDataException(GeoZone.class, "name", item.getName());
                               });
            }
        }
        
        geoZone.setName(data.getName());
        geoZone.setCode(data.getCode());
        geoZone.setTimeZone(data.getTimeZone());
        return geoZoneProvider.save(geoZone);
    }
    
    @Override
    public void delete(@NonNull UUID id) {
        var geoZone = getZone(id);
        
        geoZoneProvider.delete(geoZone);
    }
    
    @Override
    public GeoZone get(@NonNull UUID id) {
        return getZone(id);
    }
    
    @Override
    public List<GeoZone> getAll() {
        return geoZoneProvider.get();
    }
    
    @Override
    public List<GeoZone> getChildren(@NonNull UUID parentId) {
        return geoZoneProvider.getChildren(parentId);
    }
    
    @Override
    public List<GeoZoneWithChildren> getRoots() {
        return geoZoneProvider.getRoots();
    }
    
    @Override
    public GeoZone search(WaypointDto waypoint) {
        log.debug("Looking for geozone for address: {}", waypoint);
        return geoZoneProvider.search(waypoint.getRegion(), waypoint.getDistrict(), waypoint.getCity(), waypoint.getStreet(),
                                      waypoint.getHouse());
    }
    
    @Override
    public List<GeoZone> searchBranch(WaypointDto waypoint) {
        log.debug("Looking for geozone  branch for address: {}", waypoint);
        return geoZoneProvider.searchBranch(waypoint.getRegion(), waypoint.getDistrict(), waypoint.getCity(), waypoint.getStreet(),
                                            waypoint.getHouse());
    }
    
    @Override
    public Optional<GeoZone> find(String code) {
        return Optional.ofNullable(code).flatMap(geoZoneProvider::getByCode);
    }
    
    /**
     * Получение геозоны.
     *
     * @param id идентификатор.
     *
     * @return геозона.
     *
     * @throws EntityNotFoundException зона не найдена.
     */
    private GeoZone getZone(UUID id) {
        return geoZoneProvider.get(id)
                          .orElseThrow(() -> new EntityNotFoundException(GeoZone.class, id));
    }
}
