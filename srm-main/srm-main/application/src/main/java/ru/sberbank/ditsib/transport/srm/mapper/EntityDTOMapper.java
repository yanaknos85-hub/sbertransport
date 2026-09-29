package ru.sberbank.ditsib.transport.srm.mapper;

import org.mapstruct.*;
import ru.sber.transport.srm.model.SrmRequestKpiDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sber.transport.srm.model.SrmWaypointGetDTO;
import ru.sberbank.ditsib.transport.srm.model.SrmRequestKpi;
import ru.sberbank.ditsib.transport.srm.model.SrmSharedRide;
import ru.sberbank.ditsib.transport.srm.model.SrmWaypoint;

/**
 * Mapper for converting  entity to dto and back
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface EntityDTOMapper {
    
    @Mappings({
              })
    SrmSharedRideDTO sharedRideToSharedRideDto(SrmSharedRide source);
    
    @Mappings({
              })
    SrmWaypointGetDTO map(SrmWaypoint source);
    
    @Mappings({
               @Mapping(source = "orgRequestId", target = "id")
              })
    SrmRequestKpiDTO map(SrmRequestKpi source);
}
