package ru.sberbank.ditsib.transport.request.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sberbank.ditsib.transport.request.database.dao.DriverRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForTaxiRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RouteHistoryElementRepository;
import ru.sberbank.ditsib.transport.request.database.dao.TaxiTripRepository;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.driversData.Driver;
import ru.sberbank.ditsib.transport.request.dto.mapper.DriverDTOMapper;
import ru.sberbank.ditsib.transport.request.mappers.CarMapper;
import ru.sberbank.ditsib.transport.request.messaging.senders.TaxiTripSender;
import ru.sberbank.ditsib.transport.request.service.InProgressMessageProcessor;
import ru.sberbank.ditsib.transport.request.service.RequestService;
import ru.sberbank.ditsib.transport.request.util.StatusProcessingHelper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class InProgressTaxiMessageProcessorImpl implements InProgressMessageProcessor {
    private final TaxiTripSender taxiTripSender;
    private final RequestService requestService;
    private final TaxiTripRepository taxiTripRepository;
    private final RequestForTaxiRepository requestForTaxiRepository;
    private final DriverRepository driverRepository;
    private final RouteHistoryElementRepository routeHistoryElementRepository;
    private final ObjectMapper objectMapper;
    private final DriverDTOMapper driverDTOMapper;
    private final CarMapper carMapper;

    @Override
    public void process(InContractorTaxiTripInProgressMessage message) {
        var taxiTripList = taxiTripRepository.findByHumanReadableId(message.humanId());
        if (taxiTripList.isEmpty()) {
            log.info("Taxi trip for humanReadableId {} not found", message.humanId());
        } else {
            taxiTripList.forEach(taxiTrip -> saveTaxiTrip(taxiTrip, message));
        }
    }

    @Override
    public TransportTypeEnum transportType() {
        return TransportTypeEnum.TAXI;
    }

    private void saveTaxiTrip(TaxiTrip taxiTrip, InContractorTaxiTripInProgressMessage message) {
        if (StatusProcessingHelper.cantChangeTaxiTripStatus(message.status(), taxiTrip.getStatus())) {
            log.info(
                    "Поездка на такси {} имеет статус {}, изменение на статус {} невозможно, данные из InContractorTaxiTripInProgressMessage с id {} были проигнорированы",
                    taxiTrip.getHumanReadableId(),
                    taxiTrip.getStatus(),
                    message.status(),
                    message.getId());
            return;
        }
        var isUpdated = new AtomicBoolean(false);
        isUpdated.compareAndSet(false, updateTaxiId(taxiTrip, message.taxiId()));
        isUpdated.compareAndSet(false, updateDriverAndAssignedCar(taxiTrip, message));
        isUpdated.compareAndSet(false, updateStatus(taxiTrip, message.status()));
        isUpdated.compareAndSet(false, updateResolution(taxiTrip, message.resolution()));
        isUpdated.compareAndSet(false, updateLastKnownPosition(taxiTrip, message));
        isUpdated.compareAndSet(false, updateTripFactDistance(taxiTrip, message.distance()));
        isUpdated.compareAndSet(false, updateTripFactPrice(taxiTrip, message.price()));
        isUpdated.compareAndSet(false, updateTripFactWaitTime(taxiTrip, message.waitTime()));
        isUpdated.compareAndSet(false, updateTripFactDuration(taxiTrip, message.waitTimeOW()));
        isUpdated.compareAndSet(false, updateTripFinishTime(taxiTrip, message.finishTime()));
        updateRelatedRequests(taxiTrip, message);
        if (isUpdated.get()) {
            saveAndNotifyTaxiTrip(taxiTrip);
        }
    }

    private void saveAndNotifyTaxiTrip(TaxiTrip taxiTrip) {
        log.info("Updated taxiTrip with id {}", taxiTrip.getId());
        var saved = taxiTripRepository.save(taxiTrip);
        switch (saved) {
            case CoopTaxiTrip coop -> taxiTripSender.send(coop);
            case SingleTaxiTrip single -> taxiTripSender.send(single);
            default -> log.warn("Unknown trip type: {}", saved.getClass().getSimpleName());
        }
    }

    private boolean updateLastKnownPosition(TaxiTrip taxiTrip, InContractorTaxiTripInProgressMessage message) {
        if (message.geoTime() == null || message.geoLocation() == null) {
            return false;
        } else {
            taxiTrip.setLastKnownPosition(routeHistoryElementRepository.save(RouteHistoryElement.builder()
                    .geoTime(message.geoTime())
                    .latitude(message.geoLocation().latitude())
                    .longitude(message.geoLocation().longitude())
                    .taxiTrip(taxiTrip)
                    .build()));
            return true;
        }
    }

    private static boolean updateTaxiId(TaxiTrip taxiTrip, String taxiId) {
        if (taxiTrip.getTaxiId() == null && taxiId != null) {
            taxiTrip.setTaxiId(taxiId);
            return true;
        } else {
            return false;
        }
    }

    private boolean updateDriverAndAssignedCar(TaxiTrip taxiTrip, InContractorTaxiTripInProgressMessage message) {
        if (message.driver() == null) {
            return false;
        } else {
            boolean isDriverUpdated = setDriverIfNull(taxiTrip, message.driver());
            boolean isCarUpdated = setAssignedCarIfNull(taxiTrip, message.vehicle());
            return isDriverUpdated || isCarUpdated;
        }
    }

    @SneakyThrows(JsonProcessingException.class)
    private boolean setDriverIfNull(TaxiTrip taxiTrip, InContractorTaxiTripInProgressMessage.Driver driverData) {
        if (taxiTrip.getDriver() == null || taxiTrip.getDriver().equals("{}")) {
            var driver = objectMapper.convertValue(driverData, new TypeReference<>() {
            });
            var jsonDriver = objectMapper.writeValueAsString(driver);
            log.debug("Processing taxi trip driver data: jsonDriver = {}", jsonDriver);
            if (jsonDriver.equals("{}")) {
                return false;
            } else {
                taxiTrip.setDriver(jsonDriver);
                return true;
            }
        } else {
            return false;
        }
    }

    private boolean setAssignedCarIfNull(TaxiTrip taxiTrip, InContractorTaxiTripInProgressMessage.Vehicle vehicle) {
        var carInfo = carMapper.toModel(vehicle);
        return updateIfNotNullAndNotEqual(
                carInfo,
                taxiTrip.getAssignedCar(),
                taxiTrip::setAssignedCar
        );
    }

    private static boolean updateStatus(TaxiTrip taxiTrip, InboundTaxiTripStatus status) {
        return updateIfNotNullAndNotEqual(
                status,
                taxiTrip.getStatus(),
                taxiTrip::setStatus
        );
    }

    private static boolean updateResolution(TaxiTrip taxiTrip, String resolution) {
        return updateIfNotNullAndNotEqual(
                resolution,
                taxiTrip.getResolution(),
                taxiTrip::setResolution
        );
    }

    private static boolean updateTripFactDistance(TaxiTrip taxiTrip, Double distance) {
        return updateIfNotNullAndNotEqual(
                distance,
                taxiTrip.getTripFactDistance(),
                taxiTrip::setTripFactDistance
        );
    }

    private static boolean updateTripFactPrice(TaxiTrip taxiTrip, Double price) {
        if (price == null || taxiTrip.getTripFactPrice() != null &&
                Integer.valueOf(price.intValue()).equals(taxiTrip.getTripFactPrice())) {
            return false;
        } else {
            taxiTrip.setTripFactPrice(price.intValue());
            return true;
        }
    }

    private static boolean updateTripFactWaitTime(TaxiTrip taxiTrip, Integer waitTime) {
        if (waitTime == null) {
            return false;
        } else {
            var newWaitTime = Duration.ofMinutes(waitTime);
            if (newWaitTime.equals(taxiTrip.getTripFactWaitTime())) {
                return false;
            } else {
                taxiTrip.setTripFactWaitTime(newWaitTime);
                return true;
            }
        }
    }

    private static boolean updateTripFactDuration(TaxiTrip taxiTrip, Integer waitTimeOW) {
        if (waitTimeOW == null) {
            return false;
        } else {
            var newDuration = Duration.ofMinutes(waitTimeOW);
            if (newDuration.equals(taxiTrip.getTripFactDuration())) {
                return false;
            } else {
                taxiTrip.setTripFactDuration(newDuration);
                return true;
            }
        }
    }

    private static boolean updateTripFinishTime(TaxiTrip taxiTrip, LocalDateTime finishTime) {
        return updateIfNotNullAndNotEqual(
                finishTime,
                taxiTrip.getTripFinishTime(),
                taxiTrip::setTripFinishTime
        );
    }

    private void updateRelatedRequests(TaxiTrip taxiTrip, InContractorTaxiTripInProgressMessage message) {
        var tripRequestStatus = TripRequestStatus.fromContractorStatus(message.status());
        for (var request : taxiTrip.getRequests()) {
            var isUpdated = new AtomicBoolean(false);
            isUpdated.compareAndSet(false, updatedRequestSentToContractor(request));
            isUpdated.compareAndSet(false, updateRequestDriver(message.driver(), request));
            isUpdated.compareAndSet(false, updateRequestResolution(message.resolution(), request));
            isUpdated.compareAndSet(false, updateRequestDriverArrivedDateTime(message.performerArrivalTime(), request));
            isUpdated.compareAndSet(false, updateRequestClosedDateTime(message.status(), message.price(), request));
            if (isUpdated.get()) {
                request.setTaxiTrip(taxiTrip);
                request = requestForTaxiRepository.save(request);
            }
            if (tripRequestStatus == null || tripRequestStatus.equals(request.getStatus())) {
                log.info("Предполагаемый статус заявки, рассчитаный на основании статуса поездки, пуст или равен статусу заявки, tripRequestStatus:{}", tripRequestStatus);
                return;
            }
            log.debug("Start save request");
            if (TripRequestStatus.TAXI_TRIP_FINISHED.equals(tripRequestStatus)) {
                requestService.finish(request, request.getAuthor());
            } else {
                requestService.changeState(request, tripRequestStatus);
            }
        }
    }

    /**
     * Фиксируем время после статуса 7, а именно дата/времени получение фактическим данным (ориентировка на стоимость) по поездке
     *
     * @param status  статус
     * @param price   стоимость
     * @param request заявка
     * @return статус обновления заявки
     */
    private static boolean updateRequestClosedDateTime(InboundTaxiTripStatus status, Double price, RequestForTaxi request) {
        boolean isFinished = InboundTaxiTripStatus.ORDER_FINISHED.equals(status);
        boolean hasPrice = price != null && price > 0;
        boolean notClosedYet = request.getRequestClosedDatetime() == null;
        if (notClosedYet && isFinished && hasPrice) {
            request.setRequestClosedDatetime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
            return true;
        } else {
            return false;
        }
    }

    private boolean updateRequestDriverArrivedDateTime(LocalDateTime performerArrivalTime, RequestForTaxi request) {
        if (performerArrivalTime == null || (request.getDriverArrivedDatetime() != null &&
                !request.getDriverArrivedDatetime().isAfter(performerArrivalTime))) {
            return false;
        } else {
            request.setDriverArrivedDatetime(performerArrivalTime);
            request.setDeadlineState(requestService.calcDriverArrivedDeadline(
                    TransportTypeEnum.TAXI,
                    LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                    request.getDriverArrivedDatetime(),
                    request.getDriverArrivedDeadline()));
            return true;
        }
    }

    private static boolean updateRequestResolution(String resolution, RequestForTaxi request) {
        return updateIfNotNullAndNotEqual(
                resolution,
                request.getResolution(),
                request::setResolution
        );
    }

    private boolean updateRequestDriver(InContractorTaxiTripInProgressMessage.Driver tripDriver, RequestForTaxi request) {
        var driver = driverDTOMapper.map(tripDriver);
        if (driver == null) {
            return false;
        } else {
            if (driver.getId() == null) {
                var existDriver = driverRepository.findByLastNameAndFirstNameAndPatronymicAndContactPhoneAndActive(
                                Optional.ofNullable(driver.getLastName()).orElse(""),
                                Optional.ofNullable(driver.getFirstName()).orElse(""),
                                Optional.ofNullable(driver.getPatronymic()).orElse(""),
                                Optional.ofNullable(driver.getContactPhone()).orElse(""),
                                true)
                        .stream()
                        .findFirst();
                return setRequestDriver(request, existDriver, driver);
            } else {
                return setRequestDriver(request, driverRepository.findById(driver.getId()), driver);
            }
        }
    }

    private boolean setRequestDriver(RequestForTaxi request, Optional<Driver> existDriver, Driver driver) {
        if (existDriver.isEmpty()) {
            driver.setId(Optional.ofNullable(driver.getId()).orElse(UUID.randomUUID()));
            driver = driverRepository.save(driver);
            request.setDriver(driver);
            return true;
        } else if (!existDriver.get().equals(request.getDriver())) {
            request.setDriver(existDriver.get());
            return true;
        } else {
            return false;
        }
    }

    private static boolean updatedRequestSentToContractor(RequestForTaxi request) {
        if (request.isSentToContractor()) {
            return false;
        }
        request.setSentToContractor(true);
        return true;
    }

    private static <T> boolean updateIfNotNullAndNotEqual(T newValue, T currentValue, Consumer<T> setter) {
        if (newValue == null || newValue.equals(currentValue)) {
            return false;
        } else {
            setter.accept(newValue);
            return true;
        }
    }
}
