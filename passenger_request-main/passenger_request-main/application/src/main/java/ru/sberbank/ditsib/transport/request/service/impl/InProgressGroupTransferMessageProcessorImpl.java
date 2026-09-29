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
import ru.sberbank.ditsib.transport.request.database.dao.GroupTransferTripRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForGroupTransferRepository;
import ru.sberbank.ditsib.transport.request.dto.mapper.DriverDTOMapper;
import ru.sberbank.ditsib.transport.request.mappers.CarMapper;
import ru.sberbank.ditsib.transport.request.service.InProgressMessageProcessor;
import ru.sberbank.ditsib.transport.request.service.RequestService;
import ru.sberbank.ditsib.transport.request.util.StatusProcessingHelper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Transactional
@Component
@RequiredArgsConstructor
public class InProgressGroupTransferMessageProcessorImpl implements InProgressMessageProcessor {
    
    private final RequestForGroupTransferRepository requestForGroupTransferRepository;
    
    private final DriverRepository driverRepository;
    
    private final RequestService requestService;
    
    private final CarMapper carMapper;
    
    private final ObjectMapper objectMapper;
    
    private final DriverDTOMapper driverDTOMapper;
    
    private final GroupTransferTripRepository groupTransferTripRepository;
    
    @SneakyThrows(JsonProcessingException.class)
    @Override
    public void process(InContractorTaxiTripInProgressMessage message) {
        var tripOptional = groupTransferTripRepository.findFirstByHumanReadableId(message.humanId());
        
        if (tripOptional.isEmpty()) {
            log.info("Transfer trip for humanreadableid {} not found", message.humanId());
            return;
        }
        var trip = tripOptional.get();
        
        boolean updated = false;
        if (StatusProcessingHelper.cantChangeTaxiTripStatus(message.status(), trip.getStatus())) {
            log.info(
                    "Поездка на трансфере {} имеет статус {}, изменение на статус {} невозможно, данные из InContractorTaxiTripDoneMessage с id {} были проигнорированы",
                    trip.getHumanReadableId(),
                    trip.getStatus(),
                    message.status(),
                    message.getId());
            return;
        }
        if (trip.getGroupTransferId() == null) {
            if (message.taxiId() != null) {
                updated = true;
            }
            trip.setGroupTransferId(message.taxiId());
        }
        if (message.driver() != null) {
            if (trip.getDriver() == null) {
                var driver = objectMapper.convertValue(message.driver(), new TypeReference<>() {
                });
                String jsonDriver = objectMapper.writeValueAsString(driver);
                log.debug("InContractorTaxiTripInProgressListenerImpl: processTransfer: jsonDriver = {}", jsonDriver);
                trip.setDriver(jsonDriver);
                updated = true;
            }
            if (trip.getAssignedCar() == null) {
                trip.setAssignedCar(carMapper.toModel(message.vehicle()));
                updated = true;
            }
        }
        if (trip.getStatus() != message.status()) {
            trip.setStatus(message.status());
            updated = true;
        }
        if (trip.getResolution() == null || !trip.getResolution().equals(message.resolution())) {
            trip.setResolution(message.resolution());
            updated = true;
        }
        
        if (message.distance() != null && !message.distance().equals(trip.getTripFactDistance())) {
            trip.setTripFactDistance(message.distance());
            updated = true;
        }
        
        if (message.price() != null && !Integer.valueOf(message.price().intValue()).equals(trip.getTripFactPrice())) {
            trip.setTripFactPrice(message.price().intValue());
            updated = true;
        }
        
        if (message.waitTime() != null && !Duration.ofMinutes(message.waitTime()).equals(trip.getTripFactWaitTime())) {
            trip.setTripFactWaitTime(Duration.ofMinutes(message.waitTime()));
            updated = true;
        }
        
        if (message.waitTimeOW() != null && !Duration.ofMinutes(message.waitTimeOW()).equals(trip.getTripFactDuration())) {
            trip.setTripFactDuration(Duration.ofMinutes(message.waitTimeOW()));
            updated = true;
        }
        
        if (message.finishTime() != null && !message.finishTime().equals(trip.getTripFinishTime())) {
            trip.setTripFinishTime(message.finishTime());
            updated = true;
        }
        
        var tripRequestStatus = StatusProcessingHelper.inboundTaxiTripStatusToGroupTransferStatus(message.status());
        var request = trip.getRequest();
        boolean updatedRequest = false;
        
        // При первом плучение сообщения от котрагента, считаем что заявка успешно отправилась по интеграции
        if (!request.isSentToContractor()) {
            request.setSentToContractor(true);
            updatedRequest = true;
        }
        var driver = driverDTOMapper.map(message.driver());
        if (driver != null) {
            var existDriver = driverRepository.findByLastNameAndFirstNameAndPatronymicAndContactPhoneAndActive(
                                                      Optional.ofNullable(driver.getLastName()).orElse(""),
                                                      Optional.ofNullable(driver.getFirstName()).orElse(""),
                                                      Optional.ofNullable(driver.getPatronymic()).orElse(""),
                                                      Optional.ofNullable(driver.getContactPhone()).orElse(""),
                                                      true)
                                              .stream()
                                              .findFirst();
            if (existDriver.isEmpty()) {
                driver.setId(Optional.ofNullable(driver.getId()).orElse(UUID.randomUUID()));
                driver = driverRepository.save(driver);
                request.setDriver(driver);
                updatedRequest = true;
            } else if (!existDriver.get().equals(request.getDriver())) {
                request.setDriver(existDriver.get());
                updatedRequest = true;
            }
        }
        if (request.getResolution() == null || !request.getResolution().equals(message.resolution())) {
            request.setResolution(message.resolution());
            updatedRequest = true;
        }
        if (message.performerArrivalTime() != null && (request.getDriverArrivedDatetime() == null ||
                request.getDriverArrivedDatetime().isAfter(message.performerArrivalTime()))) {
                request.setDriverArrivedDatetime(message.performerArrivalTime());
                request.setDeadlineState(requestService.calcDriverArrivedDeadline(
                        TransportTypeEnum.GROUP_TRANSFER,
                        LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                        request.getDriverArrivedDatetime(),
                        request.getDriverArrivedDeadline()));
                updatedRequest = true;
            }


        var time = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        
        //Фиксируем время после статуса 7, а именно дата/времени получение фактическим данным (ориентировка на стоимость) по поездке
        if (request.getRequestClosedDatetime() == null &&
            InboundTaxiTripStatus.ORDER_FINISHED.equals(message.status()) &&
            message.price() != null && message.price() > 0) {
            request.setRequestClosedDatetime(time);
        }
        if (updatedRequest) {
            request = requestForGroupTransferRepository.save(request);
        }
        if (!tripRequestStatus.equals(request.getStatus())) {
            if (TripRequestStatus.GROUP_TRANSFER_TRIP_FINISHED.equals(tripRequestStatus)) {
                requestService.finish(request, request.getAuthor());
            } else {
                requestService.changeState(request, tripRequestStatus);
            }
        }
        
        if (updated) {
            log.info("Updated transfer trip with id {}", trip.getId());
            groupTransferTripRepository.save(trip);
        }
    }
    
    @Override
    public TransportTypeEnum transportType() {
        return TransportTypeEnum.GROUP_TRANSFER;
    }
}
