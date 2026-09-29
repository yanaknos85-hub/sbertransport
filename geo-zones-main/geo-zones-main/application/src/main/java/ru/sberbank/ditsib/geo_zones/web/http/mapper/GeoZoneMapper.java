package ru.sberbank.ditsib.geo_zones.web.http.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sberbank.ditsib.geo_zones.providers.geo_zone.model.GeoZone;
import ru.sberbank.ditsib.geo_zones.use_cases.model.GeoZoneWithChildren;
import ru.sberbank.ditsib.geo_zones.web.http.dto.GeoZoneDto;
import ru.sberbank.ditsib.geo_zones.web.http.dto.GeoZoneWithChildrenDto;
import ru.sberbank.ditsib.transport.messaging.messages.GeoZoneMessage;

/**
 * Маппер геозон.
 */
@Mapper
public interface GeoZoneMapper {
    
    void toModel(@MappingTarget GeoZone geoZone, GeoZone data);
    
    @Mapping(target = "parentId", source = "parent.id")
    GeoZoneDto toDto(GeoZone geoZone);
    
    @Mapping(target = "parentCode", source = "parentCode")
    @Mapping(target = "parentId", ignore = true)
    GeoZoneWithChildrenDto toDto(GeoZoneWithChildren geoZone);
    
    @Mapping(target = "parentId", source = "geoZone.parent.id")
    GeoZoneMessage toMessage(GeoZone geoZone, boolean deleted);
    
}
