package ru.sberbank.ditsib.geo_zones.providers.geo_zone;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.geo_zones.messaging.senders.GeoZoneSender;
import ru.sberbank.ditsib.geo_zones.providers.geo_zone.dao.GeoZoneRepository;
import ru.sberbank.ditsib.geo_zones.providers.geo_zone.mappers.GeoZoneBusinessMapper;
import ru.sberbank.ditsib.geo_zones.use_cases.model.GeoZone;
import ru.sberbank.ditsib.geo_zones.use_cases.model.GeoZoneWithChildren;
import ru.sberbank.ditsib.geo_zones.use_cases.providers.GeoZoneProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@RequiredArgsConstructor
@Component
class GeoZoneProviderImpl implements GeoZoneProvider {
    
    private final GeoZoneRepository repository;
    
    private final GeoZoneBusinessMapper mapper;

    private final GeoZoneSender sender;
    
    @Override
    public Optional<GeoZone> get(UUID id) {
        return repository.findById(id).map(mapper::toBusiness);
    }
    
    @Override
    public Optional<GeoZone> getByCodeFirstLevel(String code) {
        return repository.findByCodeAndParentIsNull(code).map(mapper::toBusiness);
    }
    
    @Override
    public Optional<GeoZone> getByCode(String code) {
        return repository.findByCode(code).map(mapper::toBusiness);
    }
    
    @Override
    public Optional<GeoZone> getByName(String name) {
        return repository.findByNameAndParentIsNull(name).map(mapper::toBusiness);
    }
    
    @Override
    public Optional<GeoZone> getByCodeFirstLevel(String code, UUID exclusion) {
        return repository.findByCodeAndIdIsNot(code, exclusion).map(mapper::toBusiness);
    }
    
    @Override
    public Optional<GeoZone> getByName(String name, UUID exclusion) {
        return repository.findByNameAndIdIsNot(name, exclusion).map(mapper::toBusiness);
    }
    
    @Override
    public GeoZone save(GeoZone geoZone) {
        var model = Optional.ofNullable(geoZone.getId())
                            .map(repository::findById)
                            .flatMap(Function.identity())
                            .orElseGet(ru.sberbank.ditsib.geo_zones.providers.geo_zone.model.GeoZone::new);
        mapper.fillModel(model, geoZone);
        model.setParent(Optional.ofNullable(geoZone.getParentId()).flatMap(repository::findById).orElse(null));
        model = repository.save(model);
        sender.send(model, false);
        return mapper.toBusiness(model);
    }
    
    @Override
    public void delete(GeoZone geoZone) {
        Optional.ofNullable(geoZone.getId()).map(repository::findById).flatMap(Function.identity())
                .ifPresent(repository::delete);
        var model = new ru.sberbank.ditsib.geo_zones.providers.geo_zone.model.GeoZone();
        mapper.fillModel(model, geoZone);
        sender.send(model, true);
    }
    
    @Override
    public List<GeoZone> get() {
        return mapper.toBusiness(repository.findAll());
    }
    
    @Override
    public List<GeoZone> getChildren(UUID parentId) {
        return mapper.toBusiness(repository.findByParentId(parentId));
    }
    
    @Override
    public List<GeoZoneWithChildren> getRoots() {
        return mapper.toBusinessFull(repository.findByParentId(null));
    }
    
    @Override
    public GeoZone search(String region, String district, String city, String street, String house) {
        return repository.search(region, district, city, street, house).map(mapper::toBusiness).orElse(null);
    }
    
    @Override
    public List<GeoZone> searchBranch(String region, String district, String city, String street, String house) {
        var geoZone = repository.search(region, district, city, street, house).orElse(null);
        List<GeoZone> result = new ArrayList<>();
        while (geoZone != null) {
            result.add(mapper.toBusiness(geoZone));
            geoZone = geoZone.getParent();
        }
        return result;
    }
}
