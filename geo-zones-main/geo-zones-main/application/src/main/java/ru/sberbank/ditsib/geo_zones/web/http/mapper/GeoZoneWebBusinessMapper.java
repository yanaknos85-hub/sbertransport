package ru.sberbank.ditsib.geo_zones.web.http.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import ru.sberbank.ditsib.geo_zones.use_cases.model.GeoZone;
import ru.sberbank.ditsib.geo_zones.use_cases.model.GeoZoneWithChildren;
import ru.sberbank.ditsib.geo_zones.web.http.dto.GeoZoneDto;
import ru.sberbank.ditsib.geo_zones.web.http.dto.GeoZoneWithChildrenDto;
import ru.sberbank.ditsib.geo_zones.web.http.dto.NewGeoZoneDto;

import java.util.List;

/**
 * Маппер веб в бизнес-модель.
 */
@Mapper
public interface GeoZoneWebBusinessMapper {
    
    /**
     * Веб в бизнес.
     *
     * @param source веб.
     *
     * @return бизнес.
     */
    @Mapping(target = "id", ignore = true)
    GeoZone toBusiness(NewGeoZoneDto source);
    
    /**
     * Бизнес в веб.
     *
     * @param source бизнес.
     *
     * @return веб.
     */
    GeoZoneDto toDto(GeoZone source);
    
    /**
     * Бизнес в веб.
     *
     * @param source бизнес.
     *
     * @return веб.
     */
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<GeoZoneDto> toDto(List<GeoZone> source);
    
    
    /**
     * Бизнес в веб.
     *
     * @param source бизнес.
     *
     * @return веб.
     */
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<GeoZoneWithChildrenDto> toDtoWithChildren(List<GeoZoneWithChildren> source);
}
