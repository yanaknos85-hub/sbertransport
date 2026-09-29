package ru.sberbank.ditsib.transport.request.scheduler.deadline.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForPersonalRepository;
import ru.sberbank.ditsib.transport.request.dto.CheckinDTO;
import ru.sberbank.ditsib.transport.request.scheduler.deadline.DeadlineChecker;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonalForceStartTripDeadlineCheckerImpl implements DeadlineChecker {
    
    private final RequestForPersonalRepository repository;
    
    private final RequestService requestService;
    
    @Override
    public String info() {
        return "Проверка начала поездки на личном транспорте";
    }
    
    /**
     * Проверка начала поездки по таймеру.
     */
    @Override
    public void execute() {
        try {
            log.debug("STARTDEBUG: checkRequestDeadlines: checkPersonalTransportRequests start");
            log.info("Start check personal request deadlines");
            checkPersonalTransportRequests();
            log.info("Finished check personal request deadlines");
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }
    
    
    private void checkPersonalTransportRequests() {
        var now = LocalDateTime.now(ZoneOffset.UTC);
        repository.findAllByStatusAndDesiredDateBefore(TripRequestStatus.PERSONAL_APPROVED, now).forEach(request -> {
            var checkinDTO = new CheckinDTO();
            checkinDTO.setRequestId(request.getId());
            requestService.startTrip(request, checkinDTO);
        });
    }
}
