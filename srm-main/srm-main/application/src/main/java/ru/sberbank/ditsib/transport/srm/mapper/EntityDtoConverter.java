package ru.sberbank.ditsib.transport.srm.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.constants.EventType;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sber.transport.srm.model.SrmWaypointFinalDTO;
import ru.sber.transport.srm.model.SrmWaypointGetDTO;
import ru.sberbank.ditsib.transport.srm.model.SrmRequestKpi;
import ru.sberbank.ditsib.transport.srm.model.SrmSharedRide;
import ru.sberbank.ditsib.transport.srm.model.SrmWaypoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EntityDtoConverter {

    private final EntityDTOMapper mapper;

    public SrmSharedRideDTO sharedRideToSharedRideDto(SrmSharedRide source) {
        SrmSharedRideDTO sharedRideDTO = mapper.sharedRideToSharedRideDto(source);
        updateWaypointRequestKpi(source, sharedRideDTO);
        enrichWithWaypointsFinal(sharedRideDTO);
        return sharedRideDTO;
    }

    public void updateWaypointRequestKpi(SrmSharedRide sharedRide, SrmSharedRideDTO sharedRideDTO) {
        for (final var waypoint : sharedRideDTO.getWaypoints()) {
            final var requestKpi = getRequestKpiById(sharedRide.getRequestKpiList(), waypoint.getRequestKpiId());
            Optional.ofNullable(requestKpi)
                    .map(SrmRequestKpi::getOrgRequestId)
                    .ifPresent(waypoint::setRequestKpiId);
        }
    }

    private SrmRequestKpi getRequestKpiById(List<SrmRequestKpi> requestKpiList, UUID requestKpiId) {
        for (SrmRequestKpi requestKpi : requestKpiList) {
            if (requestKpi.getId().equals(requestKpiId)) {
                return requestKpi;
            }
        }
        return null;
    }

    private void enrichWithWaypointsFinal(SrmSharedRideDTO sharedRideDTO) {
        if (sharedRideDTO.getWaypoints().isEmpty()) {
            return;
        }
        List<SrmWaypointFinalDTO> waypointsNew = new ArrayList<>();
        if (sharedRideDTO.getWaypoints().size() == 1) {
            SrmWaypointGetDTO singleWaypoint = sharedRideDTO.getWaypoints().get(0);
            List<SrmWaypointFinalDTO.RequestData> requestDataList = new ArrayList<>();
            requestDataList.add(new SrmWaypointFinalDTO.RequestData(singleWaypoint.getRequestKpiId(), singleWaypoint.getEventType()));
            waypointsNew.add(createNewWaypoint(singleWaypoint, 0d, requestDataList));
        } else {
            double distance = 0;
            List<SrmWaypointFinalDTO.RequestData> requestDataList = new ArrayList<>();
            for (int i = 0; i < sharedRideDTO.getWaypoints().size() - 1; i++) {
                final var currentWaypoint = sharedRideDTO.getWaypoints().get(i);
                final var nextWaypoint = sharedRideDTO.getWaypoints().get(i + 1);
                requestDataList.add(new SrmWaypointFinalDTO.RequestData(currentWaypoint.getRequestKpiId(), currentWaypoint.getEventType()));
                distance += Optional.ofNullable(currentWaypoint.getDistanceFromPrevWaypoint()).orElse(0D);
                if (!currentWaypoint.getAddress().equals(nextWaypoint.getAddress())) {
                    final var commonEventType = getCommonEventType(requestDataList);
                    waypointsNew.add(createNewWaypoint(currentWaypoint, commonEventType, distance, requestDataList));
                    distance = 0;
                    requestDataList.clear();
                }
            }
            final var lastWaypoint = sharedRideDTO.getWaypoints().get(sharedRideDTO.getWaypoints().size() - 1);
            requestDataList.add(new SrmWaypointFinalDTO.RequestData(lastWaypoint.getRequestKpiId(), lastWaypoint.getEventType()));
            distance += Optional.ofNullable(lastWaypoint.getDistanceFromPrevWaypoint()).orElse(0D);
            waypointsNew.add(createNewWaypoint(lastWaypoint, distance, requestDataList));
        }
        for (int i = 0; i < waypointsNew.size(); i++) {
            var waypoint = waypointsNew.get(i);
            waypoint.setOrderingIndex(i);
        }
        sharedRideDTO.getWaypointsFinal().addAll(waypointsNew);
    }

    private String getCommonEventType(List<SrmWaypointFinalDTO.RequestData> requestDataList) {
        if (allSameType(EventType.BOARDING.name(), requestDataList)) {
            return EventType.BOARDING.name();
        }
        if (allSameType(EventType.UNBOARDING.name(), requestDataList)) {
            return EventType.UNBOARDING.name();
        }
        return EventType.WAIT.name();
    }

    private boolean allSameType(String type, List<SrmWaypointFinalDTO.RequestData> requestDataList) {
        for (SrmWaypointFinalDTO.RequestData requestData : requestDataList) {
            if (!type.equals(requestData.getEventType())) {
                return false;
            }
        }
        return true;
    }

    private SrmWaypointFinalDTO createNewWaypoint(
            SrmWaypointGetDTO waypointOrigin,
            String eventType,
            double distance,
            List<SrmWaypointFinalDTO.RequestData> requestDataList
    ) {
        return SrmWaypointFinalDTO.builder()
                .id(waypointOrigin.getId())
                .address(waypointOrigin.getAddress())
                .latitude(waypointOrigin.getLatitude())
                .longitude(waypointOrigin.getLongitude())
                .startTime(waypointOrigin.getStartTime())
                .endTime(waypointOrigin.getEndTime())
                .eventType(eventType != null ? eventType : waypointOrigin.getEventType())
                .distanceFromPrevWaypoint(distance)
                .requestDataList(deepCopy(requestDataList))
                .waitingTime(waypointOrigin.getWaitingTime())
                .build();
    }

    private SrmWaypointFinalDTO createNewWaypoint(
            SrmWaypointGetDTO waypointOrigin,
            double distance,
            List<SrmWaypointFinalDTO.RequestData> requestDataList
    ) {
        return SrmWaypointFinalDTO.builder()
                .address(waypointOrigin.getAddress())
                .id(waypointOrigin.getId())
                .latitude(waypointOrigin.getLatitude())
                .longitude(waypointOrigin.getLongitude())
                .startTime(waypointOrigin.getStartTime())
                .endTime(waypointOrigin.getEndTime())
                .eventType(waypointOrigin.getEventType())
                .distanceFromPrevWaypoint(distance)
                .requestDataList(deepCopy(requestDataList))
                .waitingTime(waypointOrigin.getWaitingTime())
                .build();
    }

    private List<SrmWaypointFinalDTO.RequestData> deepCopy(List<SrmWaypointFinalDTO.RequestData> requestDataList) {
        List<SrmWaypointFinalDTO.RequestData> requestDataListNew = new ArrayList<>();
        for (SrmWaypointFinalDTO.RequestData requestData : requestDataList) {
            requestDataListNew.add(new SrmWaypointFinalDTO.RequestData(requestData.getRequestKpiId(),
                    requestData.getEventType()));
        }
        return requestDataListNew;
    }

    public List<SrmWaypoint> getWaypointsFinal(SrmSharedRide sharedRideDTO) {
        if (sharedRideDTO.getWaypoints().isEmpty()) {
            return new ArrayList<>();
        }
        List<SrmWaypoint> waypointsNew = new ArrayList<>();
        if (sharedRideDTO.getWaypoints().size() == 1) {
            SrmWaypoint singleWaypoint = sharedRideDTO.getWaypoints().get(0);
            waypointsNew.add(createNewWaypoint(singleWaypoint, 0d));
        } else {
            double distance = 0;
            List<SrmWaypointFinalDTO.RequestData> requestDataList = new ArrayList<>();
            for (int i = 0; i < sharedRideDTO.getWaypoints().size() - 1; i++) {
                SrmWaypoint currentWaypoint = sharedRideDTO.getWaypoints().get(i);
                SrmWaypoint nextWaypoint = sharedRideDTO.getWaypoints().get(i + 1);
                requestDataList.add(new SrmWaypointFinalDTO.RequestData(currentWaypoint.getRequestKpiId(), currentWaypoint.getEventType().toString()));
                distance += Optional.ofNullable(currentWaypoint.getDistanceFromPrevWaypoint()).orElse(0D);
                if (!currentWaypoint.getAddress().equals(nextWaypoint.getAddress())) {
                    String commonEventType = getCommonEventType(requestDataList);
                    waypointsNew.add(createNewWaypoint(currentWaypoint, commonEventType, distance));
                    distance = 0;
                    requestDataList.clear();
                }
            }
            SrmWaypoint lastWaypoint = sharedRideDTO.getWaypoints().get(sharedRideDTO.getWaypoints().size() - 1);
            requestDataList.add(new SrmWaypointFinalDTO.RequestData(lastWaypoint.getRequestKpiId(), lastWaypoint.getEventType().toString()));
            distance += Optional.ofNullable(lastWaypoint.getDistanceFromPrevWaypoint()).orElse(0D);
            waypointsNew.add(createNewWaypoint(lastWaypoint, distance));
        }
        for (int i = 0; i < waypointsNew.size(); i++) {
            var waypoint = waypointsNew.get(i);
            waypoint.setOrderingIndex(i);
        }
        return waypointsNew;
    }

    private SrmWaypoint createNewWaypoint(
            SrmWaypoint waypointOrigin,
            double distance
    ) {
        return SrmWaypoint.builder()
                .address(waypointOrigin.getAddress())
                .latitude(waypointOrigin.getLatitude())
                .longitude(waypointOrigin.getLongitude())
                .startTime(waypointOrigin.getStartTime())
                .endTime(waypointOrigin.getEndTime())
                .eventType(waypointOrigin.getEventType())
                .distanceFromPrevWaypoint(distance)
                .build();
    }

    private SrmWaypoint createNewWaypoint(
            SrmWaypoint waypointOrigin,
            String eventType,
            double distance
    ) {
        return SrmWaypoint.builder()
                .address(waypointOrigin.getAddress())
                .latitude(waypointOrigin.getLatitude())
                .longitude(waypointOrigin.getLongitude())
                .startTime(waypointOrigin.getStartTime())
                .endTime(waypointOrigin.getEndTime())
                .eventType(eventType != null ? EventType.valueOf(eventType) : waypointOrigin.getEventType())
                .distanceFromPrevWaypoint(distance)
                .build();
    }
}
