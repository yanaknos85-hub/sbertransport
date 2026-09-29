package ru.sber.transport.trip.web.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.trip.business.model.Request;
import ru.sber.transport.trip.business.model.Trip;
import ru.sber.transport.trip.business.model.Waypoint;
import ru.sber.transport.trip.web.service.impl.file_resolvers.utils.StringHandlerUtil;
import ru.sber.transport.trip.web.dto.PassengersTripDTO;
import ru.sber.transport.trip.web.service.PassengerInfoExtractorService;

import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Component
public class PassengerInfoExtractorServiceImpl implements PassengerInfoExtractorService {
    private static final String DELIMITER = "; ";
    private static final String NOT_DATA = "Н/Д";
    public static final String SINGLE_PASSENGER = "Индивидуальная";
    public static final String GROUP_PASSENGER = "Совместная";

    @Override
    public PassengersTripDTO extractPassengersInfo(List<Request> requests, Trip item) {

        String timeZone;
        String passengers;
        String type;
        String requestHumanReadableIds;
        String comments;
        int count;
        if (requests == null || requests.isEmpty()) {
            timeZone = item.getTimeZone();
            var passengersInfoPrepare =  getPassengerInfoFromWaypoint(item.getWaypoints());
            if (passengersInfoPrepare.isEmpty()) {
                passengers = NOT_DATA;
                type = NOT_DATA;
            } else {
                passengers = String.join(DELIMITER, passengersInfoPrepare.values());
                type = passengersInfoPrepare.size() < 2 ? SINGLE_PASSENGER : GROUP_PASSENGER;
            }
            count = item.getPassengerCount();
            requestHumanReadableIds = item.getExternalHumanReadableId();
            comments = item.getComment();
        } else {
            timeZone = Optional.ofNullable(requests.get(0).getTimeZone()).orElse(ZoneOffset.UTC.getId());
            passengers = requests.stream().map(Request.class::cast).map(Request::getPassenger).map(StringHandlerUtil::mapName).map(StringHandlerUtil::mapNotNull).collect(Collectors.joining(DELIMITER));
            type = requests.get(0).isCoopTrip() ? GROUP_PASSENGER : SINGLE_PASSENGER;
            count = requests.stream().map(Request.class::cast).mapToInt(Request::getPassengerCount).sum();
            requestHumanReadableIds = requests.stream().map(Request::getHumanReadableId).map(StringHandlerUtil::mapNotNull).collect(Collectors.joining(DELIMITER));
            comments = requests.stream().map(Request::getComment).filter(Objects::nonNull).collect(Collectors.joining(DELIMITER));
        }
        return new PassengersTripDTO(
                        timeZone,
                        passengers,
                        type,
                        requestHumanReadableIds,
                        comments,
                        count
                    );
    }

    protected HashMap<String, String> getPassengerInfoFromWaypoint(List<Waypoint> waypointList) {
        var passengersInfoHashMap = new HashMap<String, String>();

        if (waypointList == null || waypointList.isEmpty()) { return passengersInfoHashMap; }

        for (var waypoint : waypointList) {
            var contact = waypoint.contact();
            var passengerList = waypoint.passengers();
            if (passengerList != null && !passengerList.isEmpty()) {
                passengerList.forEach(passenger -> {
                    if (passengersInfoHashMap.containsKey(passenger.phone())) { return; }

                    StringBuilder passsengerName = new StringBuilder();
                    if (passenger.firstName() != null) { passsengerName.append(passenger.firstName()); }
                    if (passenger.patronymic() != null) {
                        if (!passsengerName.isEmpty()) { passsengerName.append(" ");}
                        passsengerName.append(passenger.patronymic());
                    }
                    passengersInfoHashMap.put(passenger.phone(), StringHandlerUtil.mapNotNull(passsengerName.toString()));
                });
            } else if (contact != null) {
                if (passengersInfoHashMap.containsKey(contact.phone())) {
                    continue;
                }
                passengersInfoHashMap.put(contact.phone(), StringHandlerUtil.mapNotNull(contact.name()));
            }
        }
        return passengersInfoHashMap;
    }


}
