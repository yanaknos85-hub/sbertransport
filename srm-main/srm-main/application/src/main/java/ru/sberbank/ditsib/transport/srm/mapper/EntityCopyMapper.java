package ru.sberbank.ditsib.transport.srm.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.srm.model.SrmRequestKpi;
import ru.sberbank.ditsib.transport.srm.model.SrmSharedRide;
import ru.sberbank.ditsib.transport.srm.model.SrmWaypoint;

/**
 * Mapper for converting  entity to dto and back
 */
@Mapper
public interface EntityCopyMapper {
    
    SrmSharedRide sharedRideToSharedRide(SrmSharedRide source);
    
    SrmWaypoint map(SrmWaypoint source);
    
    SrmRequestKpi map(SrmRequestKpi source);
}
