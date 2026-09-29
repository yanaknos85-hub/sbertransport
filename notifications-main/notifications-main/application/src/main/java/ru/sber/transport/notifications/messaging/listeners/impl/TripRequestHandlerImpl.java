package ru.sber.transport.notifications.messaging.listeners.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.sber.transport.notifications.database.model.approve.ApproveStatus;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.request.SharedRide;
import ru.sber.transport.notifications.database.model.request.TaxiTrip;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.dto.contractor.DriverDTO;
import ru.sber.transport.notifications.dto.contractor.VehicleDTO;
import ru.sber.transport.notifications.mapper.trip_request.ExpectedDataMapper;
import ru.sber.transport.notifications.mapper.trip_request.WaypointMapper;
import ru.sber.transport.notifications.messaging.listeners.TripRequestHandler;
import ru.sber.transport.notifications.services.NotificationCommand;
import ru.sber.transport.notifications.services.SharedRequestService;
import ru.sber.transport.notifications.services.TripApproveService;
import ru.sber.transport.notifications.services.TripRequestService;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Реализация слушателя заявки на поездку.
 */
@RequiredArgsConstructor
@Component
@Slf4j
class TripRequestHandlerImpl implements TripRequestHandler {

    private final TripRequestService tripRequestService;

    private final TripApproveService tripApproveService;

    private final WaypointMapper waypointMapper;

    private final ExpectedDataMapper expectedDataMapper;

    private final List<NotificationCommand<TripRequest>> notificationCommands;

    private final SharedRequestService sharedRequestService;

    private final ObjectMapper objectMapper;

    @Override
    public void handle(RequestMessage message) {
        var requestId = message.getId();
        if (message.isDeleted()) {
            tripRequestService.delete(requestId);
        } else {
            SharedRide sharedRide = null;
            if (message.getRideId() != null) {
                sharedRide =
                        sharedRequestService.get(message.getRideId())
                                .orElseGet(() -> sharedRequestService.save(
                                        SharedRide.builder()
                                                .id(message.getRideId())
                                                .active(true)
                                                .passengers(1)
                                                .tariffId(message.getTariffId())
                                                .build()));
            }
            var request = new TripRequest();
            TripRequest previousRequest = null;
            try {
                request = tripRequestService.get(requestId);
                previousRequest = objectMapper.readValue(objectMapper.writeValueAsString(request), TripRequest.class);
            } catch (NoSuchElementException | JsonProcessingException e) {
                // ignore
            }
            request.setSharedRide(sharedRide);
            if (request.getSharedRide() != null) {
                var connectedRequests = tripRequestService.getAllBySharedRideId(request.getSharedRide().getId());
                request.getSharedRide().getRequestIds().addAll(connectedRequests.stream()
                        .map(TripRequest::getId)
                        .collect(Collectors.toSet()));
            }
            fillTrip(message, request);

            var approve = tripApproveService.getByRequestId(requestId)
                    .map(TripApprove::getApproveStatus).orElse(ApproveStatus.NEW);

            request.setWaypoints(waypointMapper.toEntity(message.getWaypoints()));
            request.setPurposeId(message.getPurposeId());
            request.setTripClass(message.getTripClass());
            request.setTariffId(message.getTariffId());
            request.setTransportType(TransportTypeEnum.valueOf(message.getTransportType()));
            request.setStatus(message.getStatus());
            request.setPassengerId(message.getPassengerId());
            request.setPassengerCount(message.getPassengerCount());
            request.setId(message.getId());
            request.setHumanReadableId(message.getHumanReadableId());
            request.setFinishedTime(message.getFinishedTime());
            request.setExpected(expectedDataMapper.toEntity(message.getExpected()));
            request.setDesiredDate(message.getDesiredDate());
            request.setCoopTrip(message.isCoopTrip());
            request.setCommentForDriver(message.getCommentForDriver());
            request.setCreationTime(message.getCreationTime());
            request.setAuthorId(message.getAuthorId());
            request.setApprovalId(message.getApprovalId());
            request.setApprovalState(message.getApprovalState());
            request.setApprovalDate(message.getApprovalDate());
            if (message.getVehicleData() != null) {
                request.setVehicle(VehicleDTO.builder()
                        .brand(message.getVehicleData().getBrand())
                        .model(message.getVehicleData().getModel())
                        .stateNumber(message.getVehicleData().getStateNumber())
                        .color(message.getVehicleData().getColor())
                        .carInfo(Stream.of(message.getVehicleData().getColor(), message.getVehicleData().getBrand(), message.getVehicleData().getModel(), message.getVehicleData().getStateNumber() == null ? "" : "номер: " + message.getVehicleData().getStateNumber())
                                .filter(StringUtils::hasText)
                                .collect(Collectors.joining(" ")))
                        .build());
            }
            if (message.getDriverData() != null) {
                request.setDriver(DriverDTO.builder()
                        .lastName(message.getDriverData().getLastName())
                        .firstName(message.getDriverData().getFirstName())
                        .patronymic(message.getDriverData().getPatronymic())
                        .phoneNumber(message.getDriverData().getPhoneNumber())
                        .driverFio(Stream.of(message.getDriverData().getLastName(), message.getDriverData().getFirstName(), message.getDriverData().getPatronymic())
                                .filter(StringUtils::hasText)
                                .collect(Collectors.joining(" ")))
                        .build());
            }

            request.setApproveStatusDescription(approve.getDescription());
            request.setRequestStatusDescription(TripRequestStatus.valueOf(request.getStatus()).getDescription());
            request.setStatusCode(message.getStatusCode());
            request.setTimeZone(message.getTimeZone());
            request.setInformation(message.getInformation());

            request.setStatusCode(message.getStatusCode());
            tripRequestService.save(request);

            log.debug(String.format("Request with ID %s saved", request.getId()));

            try {
                TripRequest finalPreviousRequest = previousRequest;
                TripRequest finalRequest = request;
                notificationCommands.stream().filter(nc -> nc.validate(finalPreviousRequest, finalRequest))
                        .forEach(command -> {
                            try {
                                command.sendNotification(finalRequest);
                            } catch (JsonProcessingException e) {
                                log.error("Processing failed", e);
                            }
                        });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void fillTrip(RequestMessage message, TripRequest request) {
        if (message.getResolution() != null && message.getTransportType().equals("TAXI")) {
            var taxiTrip = new TaxiTrip();
            taxiTrip.setResolution(message.getResolution());
            taxiTrip.setTripFactDuration(message.getTripFactDuration());
            request.setTaxiTrip(taxiTrip);
        }
    }
}
