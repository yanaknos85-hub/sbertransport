package ru.sberbank.ditsib.geo_zones.providers.geo_zone.mappers;

import org.mapstruct.*;
import ru.sberbank.ditsib.geo_zones.use_cases.model.GeoZone;
import ru.sberbank.ditsib.geo_zones.use_cases.model.GeoZoneWithChildren;

import java.util.List;

@Mapper
public interface GeoZoneBusinessMapper {
    
    @Mapping(target = "parentId", source = "parent.id")
    GeoZone toBusiness(ru.sberbank.ditsib.geo_zones.providers.geo_zone.model.GeoZone source);
    
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "parentCode", source = "parent.code")
    GeoZoneWithChildren toBusinessFull(ru.sberbank.ditsib.geo_zones.providers.geo_zone.model.GeoZone source);
    
    @Mapping(target = "parent.id", source = "parentId")
    @Mapping(target = "children", ignore = true)
    void fillModel(@MappingTarget ru.sberbank.ditsib.geo_zones.providers.geo_zone.model.GeoZone target, GeoZone source);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<GeoZone> toBusiness(List<ru.sberbank.ditsib.geo_zones.providers.geo_zone.model.GeoZone> source);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<GeoZoneWithChildren> toBusinessFull(List<ru.sberbank.ditsib.geo_zones.providers.geo_zone.model.GeoZone> source);
    
}
