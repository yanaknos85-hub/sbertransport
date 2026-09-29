package ru.sberbank.ditsib.geo_zones.web.http.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.geo_zones.use_cases.GeoZoneCases;
import ru.sberbank.ditsib.geo_zones.web.http.controller.GeoZoneController;
import ru.sberbank.ditsib.geo_zones.web.http.dto.GeoZoneDto;
import ru.sberbank.ditsib.geo_zones.web.http.dto.GeoZoneWithChildrenDto;
import ru.sberbank.ditsib.geo_zones.web.http.dto.NewGeoZoneDto;
import ru.sberbank.ditsib.geo_zones.web.http.dto.WaypointDto;
import ru.sberbank.ditsib.geo_zones.web.http.mapper.GeoZoneWebBusinessMapper;

import javax.naming.OperationNotSupportedException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация контроллера зон.
 */
@RestController
@RequiredArgsConstructor
class GeoZoneControllerImpl implements GeoZoneController {
    
    private final GeoZoneCases geoZoneCases;
    
    private final GeoZoneWebBusinessMapper mapper;
    
    @Override
    public GeoZoneDto create(@Valid NewGeoZoneDto data) {
        var geoZone = mapper.toBusiness(data);
        geoZone = geoZoneCases.save(null, geoZone);
        return mapper.toDto(geoZone);
    }
    
    @Override
    public void edit(UUID id, @Valid NewGeoZoneDto data) {
        var geoZone = mapper.toBusiness(data);
        geoZoneCases.save(id, geoZone);
    }
    
    @SneakyThrows
    @Override
    public void delete(UUID id) {
        throw new OperationNotSupportedException();
    }
    
    @Override
    public GeoZoneDto get(UUID id) {
        var geoZone = geoZoneCases.get(id);
        return mapper.toDto(geoZone);
    }
    
    @Override
    public List<GeoZoneDto> getAll() {
        return mapper.toDto(geoZoneCases.getAll());
    }
    
    @Override
    public List<GeoZoneWithChildrenDto> getRoots() {
        return mapper.toDtoWithChildren(geoZoneCases.getRoots());
    }
    
    @Override
    public List<GeoZoneDto> getChildren(UUID parentId) {
        return mapper.toDto(geoZoneCases.getChildren(parentId));
    }
    
    
    @Override
    public GeoZoneDto search(WaypointDto waypoint) {
        var search = geoZoneCases.search(waypoint);
        return mapper.toDto(search);
    }
    
    @Override
    public List<GeoZoneDto> searchBranch(WaypointDto waypoint) {
        return geoZoneCases.searchBranch(waypoint).stream().map(mapper::toDto).collect(Collectors.toList());
    }
}
