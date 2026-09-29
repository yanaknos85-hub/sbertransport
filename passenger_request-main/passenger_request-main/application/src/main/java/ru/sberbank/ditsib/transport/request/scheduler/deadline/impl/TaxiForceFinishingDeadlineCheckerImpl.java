package ru.sberbank.ditsib.transport.request.scheduler.deadline.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sberbank.ditsib.transport.request.database.dao.TaxiTripRepository;
import ru.sberbank.ditsib.transport.request.database.model.TaxiTrip;
import ru.sberbank.ditsib.transport.request.database.model.corp.TechnicalUser;
import ru.sberbank.ditsib.transport.request.scheduler.deadline.DeadlineChecker;
import ru.sberbank.ditsib.transport.request.service.impl.RequestForTaxiServiceImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaxiForceFinishingDeadlineCheckerImpl implements DeadlineChecker {

    private final TaxiTripRepository taxiTripRepository;

    private final RequestForTaxiServiceImpl requestService;

    @Value("${dispatcher.trip.taxi.close.force.batch:30000}")
    private Long batch;

    @Override
    public String info() {
        return "Принудительное завершение поездок на такси без фактических данных";
    }

    @Override
    public void execute() {
        log.info("ForcedFinishingTaxiTripSchedulerImpl: Let's start looking for taxi trip to finish");

        var tripsForClosing = Collections.<TaxiTrip>emptyList();
        try {
            var time = LocalDateTime.now().minusDays(3);
            var tripsIdForClosing = taxiTripRepository.findTripForClosing(time).stream()
                    .limit(batch)
                    .map(UUID::fromString)
                    .toList();
            tripsForClosing = taxiTripRepository.findAllById(tripsIdForClosing);
        } catch (Exception e) {
            log.error("ForcedFinishingTaxiTripSchedulerImpl: Failed to get trips", e);
            return;
        }

        log.info("ForcedFinishingTaxiTripSchedulerImpl: Found {} taxi trip for finish", tripsForClosing.size());

        for (var trip : tripsForClosing) {
            try {
                //Не пришли финальные данные - Закрыто системой статус код 103 closed_date_time = desired_date + 3
                if (InboundTaxiTripStatus.ORDER_FINISHED.equals(trip.getStatus())) {
                    trip.getRequests().forEach(r -> {
                        try {
                            r.setRequestClosedDatetime(r.getDesiredDate().plusDays(3));
                            r.setStatusCode(TripRequestStatus.TaxiStatusCode.TAXI_TRIP_CLOSED_BY_SYSTEM.getCode());
                            requestService.save(r);
                        } catch (Exception e) {
                            log.error("ForcedFinishingTaxiTripSchedulerImpl: Failed to finish request {} {}", r.getHumanReadableId(),
                                      r.getStatus(),
                                      e);
                        }
                    });
                } else {
                    //Если заявка в статусе, начиная с "Водитель ожидает в точке отправления",
                    //не получила финишный статус в течение 3 дней с желаемой даты, закрываем её как успешную
                    //со статус кодом 103
                    trip.setStatus(InboundTaxiTripStatus.ORDER_FINISHED);
                    trip.getRequests().forEach(r -> {
                        try {
                            r.setRequestClosedDatetime(r.getDesiredDate().plusDays(3));
                            r.setStatusCode(TripRequestStatus.TaxiStatusCode.TAXI_TRIP_CLOSED_BY_SYSTEM.getCode());
                            requestService.finish(r, TechnicalUser.get());
                        } catch (Exception e) {
                            log.error("ForcedFinishingTaxiTripSchedulerImpl: Failed to finish request {} {}", r.getHumanReadableId(), r.getStatus(),
                                      e);
                        }
                    });
                }
                taxiTripRepository.save(trip);
            } catch (Exception e) {
                log.error("ForcedFinishingTaxiTripSchedulerImpl: Failed to handle trip {}", trip.getHumanReadableId(), e);
            }
        }

    }

}
