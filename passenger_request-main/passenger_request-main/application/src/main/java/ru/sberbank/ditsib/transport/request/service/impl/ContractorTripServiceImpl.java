package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.logging.annotations.E2ELogging;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.taxi.TaxiTariff;
import ru.sberbank.ditsib.transport.request.dto.CancelDTO;
import ru.sberbank.ditsib.transport.request.exceptions.UpdateRequestException;
import ru.sberbank.ditsib.transport.request.messaging.senders.TaxiTripSender;
import ru.sberbank.ditsib.transport.request.service.ContractorTripService;
import ru.sberbank.ditsib.transport.request.service.PublishTripService;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus.ORDER_CANCELLED_BY_CLIENT;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
class ContractorTripServiceImpl implements ContractorTripService {
    private static final long NOT_EXPIRED_DAYS_FOR_UPDATE_REQUESTS = 3;
    private static final long NOT_EXPIRED_DAYS_FOR_NEW_REQUESTS = 1;
    private final TaxiTripRepository taxiTripRepository;
    private final GroupTransferTripRepository groupTransferTripRepository;
    private final RequestForTaxiRepository requestForTaxiRepository;
    private final RequestForGroupTransferRepository requestForGroupTransferRepository;
    private final RequestService requestService;
    private final PublishTripService publishTripService;
    private final TaxiTripSender taxiTripSender;
    private final TaxiTariffRepository taxiTariffRepository;
    private final AtomicInteger processInProgressCallCount = new AtomicInteger(0);
    /**
     * Показывает насколько часто будут опрашиваться поезки в статусе завершено, но без фактических данных
     */
    @Value("${trips.inProgress.afterTripFinishRate:4}")
    private int afterTripFinishProcessRate;
    
    @Transactional
    @Override
    public void processNewTrips() {
        try {
            var notExpiredTime = LocalDate.now().minusDays(NOT_EXPIRED_DAYS_FOR_NEW_REQUESTS).atStartOfDay();
            var readyToProcess = requestForTaxiRepository.findNewReadyToSend(notExpiredTime).stream()
                    .filter(request -> durationFilter(request.getDesiredDate(), request.getTriggerTime()))
                    .toList();
            processNewTrips(readyToProcess);
        } catch (Exception e) {
            log.error("Error during processNewTrips", e);
        }
        
        try {
            var notExpiredTime = LocalDate.now().minusDays(NOT_EXPIRED_DAYS_FOR_NEW_REQUESTS).atStartOfDay();
            var readyToProcess = requestForGroupTransferRepository.findNewReadyToSend(notExpiredTime).stream()
                    .filter(request -> durationFilter(request.getDesiredDate(), request.getTriggerTime()))
                    .toList();
            processNewTripsTransfer(readyToProcess);
        } catch (Exception e) {
            log.error("Error during processNewTrips (GROUP_TRANSFER)", e);
        }
    }

    @Transactional
    @Override
    public void processTripsInProgress() {
        var n = processInProgressCallCount.incrementAndGet();
        var notExpiredTime = LocalDate.now().minusDays(NOT_EXPIRED_DAYS_FOR_UPDATE_REQUESTS).atStartOfDay();
        try {
            // Уже завершённые поездки опрашиваем в n (afterTripFinishProcessRate) раз реже, чем в активной стадии
            if (n % afterTripFinishProcessRate == 0) {
                log.info("scheduleTrips: process in progress and finished trips");
                processTripInProgress(taxiTripRepository.findTripsIdInProgressAndFinished(notExpiredTime));
            } else {
                log.info("scheduleTrips: process only in progress");
                processTripInProgress(taxiTripRepository.findTripsIdInProgress(notExpiredTime));
            }
        } catch (Exception ex) {
            log.error("Ошибка при поиске заявок для обновления статуса: ", ex);
        }

        try {
            // Уже завершённые поездки опрашиваем в n (afterTripFinishProcessRate) раз реже, чем в активной стадии
            if (n % afterTripFinishProcessRate == 0) {
                log.info("scheduleTrips: process in progress and finished group transfer trips");
                processTripInProgressTransfer(groupTransferTripRepository.findTripsIdInProgressAndFinished(notExpiredTime));
            } else {
                log.info("scheduleTrips: process only in progress group transfer");
                processTripInProgressTransfer(groupTransferTripRepository.findTripsIdInProgress(notExpiredTime));
            }
        } catch (Exception ex) {
            log.error("Ошибка при поиске заявок для обновления статуса: ", ex);
        }
    }
    
    @Transactional
    @Override
    public void processTrips(List<UUID> requestIds) {
        var tripIds = requestForTaxiRepository.findAllByIdWithTrips(requestIds).stream()
                                              .map(RequestForTaxi::getTaxiTrip)
                                              .filter(Objects::nonNull)
                                              //Можем перезапрашивать данные только по поездкам, которые уже были отправлены контрагенту
                                              .filter(t -> t.getTaxiId() != null)
                                              .map(TaxiTrip::getId)
                                              .filter(Objects::nonNull)
                                              .collect(Collectors.toSet());
        if (requestIds.size() != tripIds.size()) {
            throw new UpdateRequestException("Не удалось найти поездку по заявке");
        }
        processTripInProgress(tripIds);
    }

    private static boolean durationFilter(LocalDateTime desiredDate, Integer triggerTime) {
        return Duration.between(LocalDateTime.now(), desiredDate).toMinutes() < Optional.ofNullable(triggerTime).orElse(60);
    }
    
    private void processNewTrips(List<RequestForTaxi> requests) {
        log.info("Processing {} new trips", requests.size());
        requests.forEach(requestForTaxi -> {
            try {
                if (requestForTaxi.isCoopTrip()) {
                    processNewCoop(requestForTaxi);
                } else {
                    processNewSingle(requestForTaxi);
                }
            } catch (Exception e) {
                log.error("A error occurred while processing a new trip, id: {}", requestForTaxi.getHumanReadableId(), e);
            }
        });
    }
    
    private void processNewTripsTransfer(List<RequestForGroupTransfer> requests) {
        log.info("Processing {} new trips (GROUP_TRANSFER)", requests.size());
        requests.forEach(requestForGroupTransfer -> {
            try {
                processNewGroupTransferTrip(requestForGroupTransfer);
            } catch (Exception e) {
                log.error("A error occurred while processing a new trip, id: {}", requestForGroupTransfer.getHumanReadableId(), e);
            }
        });
    }

    @E2ELogging
    private void processNewGroupTransferTrip(RequestForGroupTransfer requestForGroupTransfer) {
        publishTripService.publishNewGroupTransferTrip(requestForGroupTransfer);
        requestForGroupTransferRepository.save(requestForGroupTransfer);
    }

    @E2ELogging
    private void processNewSingle(RequestForTaxi requestForTaxi) {
        var singleTaxiTrip = publishTripService.publishNewSingleTrip(requestForTaxi);
        taxiTripSender.send(singleTaxiTrip);
    }

    @E2ELogging
    private void processNewCoop(RequestForTaxi requestForTaxi) {
        if (requestForTaxi.getRideId() == null) {
            requestService.cancel(requestForTaxi,
                                  CancelDTO.builder()
                                          .reason("Совместная поездка не найдена")
                                          .build(),
                                  new Employee(requestForTaxi.getPassenger().getId()),
                                  true);
            log.error("Нет совместной поездки для заявки {}, отмена поездки", requestForTaxi.getHumanReadableId());
            return;
        }
        
        if (!requestForTaxi.isSharedRideOwner()) {
            log.info("Заявка {} не является заявкой инициатора, пропускаем", requestForTaxi.getHumanReadableId());
            return;
        }
        
        var activeRequests = requestForTaxiRepository.findActiveByRideId(requestForTaxi.getRideId());
        var requestsToCancel = activeRequests.stream()
                .filter(activeRequest -> !activeRequest.getId().equals(requestForTaxi.getId()) && activeRequest.getStatus() != TripRequestStatus.TAXI_APPROVED)
                .map(RequestForTaxi::getId)
                .toList();
        if(!requestsToCancel.isEmpty()) {
            //TODO зачем мы отменяем основную заявку, а не одну из не в статусе TAXI_APPROVED?
            requestService.cancel(requestForTaxi,
                    CancelDTO.builder()
                            .reason("Заявка отклонена, так как не была согласована к моменту начала совместной поездки")
                            .build(),
                    new Employee(requestForTaxi.getPassenger().getId()),
                    true);
            log.info("Заявка {} отменена, так как не была согласована к моменту начала совместной поездки", requestForTaxi.getHumanReadableId());
        }
        var requestsToSend = activeRequests.stream()
                .filter(requestToSend -> !requestsToCancel.contains(requestToSend.getId()))
                .toList();
        if(requestsToSend.isEmpty()) {
            log.info("По rideId не осталось ни одной активной заявки, пропускаем, rideId:{}, humanReadableId:{}",
                    requestForTaxi.getRideId(),
                    requestForTaxi.getHumanReadableId());
        } else {
            var coopTaxiTrip = publishTripService.publishNewCoopTrip(requestForTaxi.getRideId(),
                    requestForTaxi.getTariffId(),
                    requestForTaxi.getOutcomeTariffId(),
                    requestsToSend);
            taxiTripSender.send(coopTaxiTrip);
        }
    }
    
    private void processTripInProgress(Set<UUID> tripsIds) {
        try {
            var inProgressList = taxiTripRepository.findAllById(tripsIds)
                                       .stream()
                                       .filter(taxiTrip -> !taxiTrip.getRequests().isEmpty())
                                       .toList();
            var tariffMap = taxiTariffRepository.findAllById(inProgressList.stream()
                            .map(TaxiTrip::getOutcomeTariffId)
                            .collect(Collectors.toSet()))
                    .stream()
                    .collect(Collectors.toMap(TaxiTariff::getId, Function.identity()));
            inProgressList.stream()
                    .filter(SingleTaxiTrip.class::isInstance)
                    .map(SingleTaxiTrip.class::cast)
                    .forEach(singleTaxiTrip -> publishSingleTaxiTrip(singleTaxiTrip, tariffMap));
            final var coopTaxiTripList = inProgressList.stream()
                    .filter(CoopTaxiTrip.class::isInstance)
                    .map(CoopTaxiTrip.class::cast)
                    .toList();
            final Map<UUID, List<RequestForTaxi>> activeRequestsMap = coopTaxiTripList.parallelStream()
                    .map(CoopTaxiTrip::getRideId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Function.identity(), elt -> new ArrayList<>())
                    );
            var rideIdList = coopTaxiTripList.parallelStream()
                    .map(CoopTaxiTrip::getRideId)
                    .filter(Objects::nonNull)
                    .toList();
            var activeRequests = requestForTaxiRepository.findActiveByRideIdIn(rideIdList);
            activeRequests.forEach(requestForTaxi -> activeRequestsMap.get(requestForTaxi.getRideId()).add(requestForTaxi));
            for (var coopTaxiTrip : coopTaxiTripList) {
                var activeBySharedRequestId = Optional.ofNullable(activeRequestsMap.get(coopTaxiTrip.getRideId()))
                        .orElseGet(Collections::emptyList);
                if (coopTaxiTrip.getTaxiId() == null && activeBySharedRequestId.isEmpty()) {
                    coopTaxiTrip.setStatus(ORDER_CANCELLED_BY_CLIENT);
                    coopTaxiTrip.setResolution("Поездка была отменена по причине отмены всех связанных заявок");
                    taxiTripRepository.save(coopTaxiTrip);
                    continue;
                }
                publishCoopTaxiTrip(coopTaxiTrip, tariffMap, activeRequestsMap);
            }
        } catch (Exception e) {
            log.error("Ошибка при получении данных по заявкам по списку id: ", e);
        }
    }

    @E2ELogging
    private void publishCoopTaxiTrip(CoopTaxiTrip coopTaxiTrip, Map<UUID, TaxiTariff> tariffMap, Map<UUID, List<RequestForTaxi>> activeRequestsMap) {
        try {
            var tariff = tariffMap.get(coopTaxiTrip.getOutcomeTariffId());
            if (tariff == null) {
                log.info("processTripsInProgress: Tariff for coop trip with id {} is deleted or null", coopTaxiTrip.getId());
                coopTaxiTrip.setStatus(ORDER_CANCELLED_BY_CLIENT);
                coopTaxiTrip.getRequests().forEach(requestForTaxi ->
                        requestService.cancel(requestForTaxi,
                                CancelDTO.builder()
                                        .reason("Заявка отменена, так как используемый тариф был удален или не существует")
                                        .build(),
                                new Employee(
                                        requestForTaxi.getPassenger().getId()),
                                true));
            } else {
                publishTripService.publishCoopTrip(coopTaxiTrip, activeRequestsMap.get(coopTaxiTrip.getRideId()), tariff);
            }
        } catch (Exception e) {
            log.error("Error during publication of coop trip with id {}", coopTaxiTrip.getId(), e);
        }
    }

    @E2ELogging
    private void publishSingleTaxiTrip(SingleTaxiTrip singleTaxiTrip, Map<UUID, TaxiTariff> tariffMap) {
        try {
            publishTripService.publishSingleTrip(
                    singleTaxiTrip,
                    requestForTaxiRepository.findByTripId(singleTaxiTrip.getId()),
                    tariffMap.get(singleTaxiTrip.getOutcomeTariffId()));
        } catch (Exception e) {
            log.error("Error during publication of single trip with id {}", singleTaxiTrip.getId(), e);
        }
    }

    private void processTripInProgressTransfer(Set<UUID> tripsIds) {
        try {
            var inProgressList = groupTransferTripRepository.findAllById(tripsIds)
                                                        .stream()
                                                        .filter(trip -> trip.getRequest() != null)
                                                        .toList();
            inProgressList.forEach(trip -> {
                try {
                    publishTripService.publishGroupTransferTrip(trip);
                } catch (Exception e) {
                    log.error("Error during publication of single trip with id {}", trip.getId(), e);
                }
            });
        } catch (Exception ex) {
            log.error("Ошибка при получении данных по заявкам по списку id: ", ex);
        }
    }
}
