package ru.sberbank.ditsib.transport.request.scheduler.deadline.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForPersonalRepository;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPersonal;
import ru.sberbank.ditsib.transport.request.scheduler.deadline.DeadlineChecker;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonalTripApprovalDeadlineCheckerImpl implements DeadlineChecker {
    private final RequestService requestService;
    private final RequestForPersonalRepository requestForPersonalRepository;
    
    @Override
    public String info() {
        return "Проверка исполнения контрольных сроков утверждения маршрута заявки на личный транспорт (с утверждением маршрута заявок по истечению" +
               " контрольного срока)";
    }
    
    /**
     * Проверка исполнения контрольных сроков утверждения маршрута заявки на личный транспорт (с утверждением маршрута заявок по истечению
     * контрольного срока)
     */
    public void execute() {
        try {
            log.debug("PersonalTripApprovalDeadlineCheckerImpl.execute() - start");
            executeCheck();
            log.debug("PersonalTripApprovalDeadlineCheckerImpl.execute() - end");
        } catch (Throwable ex) {
            log.error("Произошла ошибка при проверке контрольного срока утверждения маршрута у заявок на личный транспорт");
            log.error(ex.getMessage());
        }
    }
    
    private void executeCheck() {
        var now = LocalDateTime.now();
        List<RequestForPersonal> requestsWithViolation =
                requestForPersonalRepository.findRequestForPersonalWithTripApprovalDeadlineViolation(
                        now,
                        TripRequestStatus.PERSONAL_AWAITING_TRIP_APPROVAL);
        for (RequestForPersonal request : requestsWithViolation) {
            approveRequestForPersonalWithTripApprovalDeadlineViolation(request);
        }
    }
    
    private void approveRequestForPersonalWithTripApprovalDeadlineViolation(RequestForPersonal request) {
        try {
            log.info("В связи с нарушением контрольного срока на утверждение маршрута маршрут в заявке: {} будет утвержден автоматически",
                     request.getHumanReadableId());
            approveRequest(request);
            log.info("Маршрут в заявке: {} утвержден", request.getHumanReadableId());
        } catch (Throwable ex) {
            log.error("Произошла ошибка при утверждении маршрута заявки: {}", request.getHumanReadableId());
            log.error(ex.getMessage());
        }
    }
    
    private void approveRequest(RequestForPersonal request) {
        requestService.approveRequestForPersonalWithTripApprovalDeadlineViolation(request);
    }
}
