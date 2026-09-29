package ru.sberbank.ditsib.transport.srm.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.srm.model.SrmRequestKpi;
import ru.sberbank.ditsib.transport.srm.model.SrmSharedRide;
import ru.sberbank.ditsib.transport.srm.model.SrmWaypoint;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EntityCopyConverter {
    
    private final EntityCopyMapper mapper;
    
    public SrmSharedRide sharedRideToSharedRide(SrmSharedRide source) {
        SrmSharedRide sharedRide = mapper.sharedRideToSharedRide(source);
        List<SrmWaypoint> list = source.getWaypoints();
        if ( list != null ) {
            sharedRide.setWaypoints(deepCopyWaypoints( list ) );
        }
        List<SrmRequestKpi> list1 = source.getRequestKpiList();
        if ( list1 != null ) {
            sharedRide.setRequestKpiList(deepCopyRequestKpis( list1 ) );
        }
        return sharedRide;
    }
    
    private List<SrmWaypoint> deepCopyWaypoints(List<SrmWaypoint> waypointList) {
        List<SrmWaypoint> waypointListNew = new ArrayList<>();
        for (SrmWaypoint waypoint : waypointList) {
            waypointListNew.add(mapper.map(waypoint));
        }
        return waypointListNew;
    }
    
    private List<SrmRequestKpi> deepCopyRequestKpis(List<SrmRequestKpi> requestKpiList) {
        List<SrmRequestKpi> requestKpiListNew = new ArrayList<>();
        for (SrmRequestKpi requestKpi : requestKpiList) {
            requestKpiListNew.add(mapper.map(requestKpi));
        }
        return requestKpiListNew;
    }
}
