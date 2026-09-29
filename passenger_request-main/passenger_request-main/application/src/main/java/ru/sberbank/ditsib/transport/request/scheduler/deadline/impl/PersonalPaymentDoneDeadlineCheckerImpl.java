package ru.sberbank.ditsib.transport.request.scheduler.deadline.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
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
public class PersonalPaymentDoneDeadlineCheckerImpl implements DeadlineChecker {
    private final RequestService requestService;
    private final RequestForPersonalRepository requestForPersonalRepository;
    
    @Override
    public String info() {
        return "Проверка исполнения контрольных сроков выплаты компенсации по заявке на личный транспорт";
    }
    
    /**
     * Проверка исполнения контрольных сроков выплаты компенсации по заявке на личный транспорт
     */
    public void execute() {
        try {
            log.debug("PersonalPaymentDoneDeadlineCheckerImpl.execute() - start");
            executeCheck();
            log.debug("PersonalPaymentDoneDeadlineCheckerImpl.execute() - end");
        } catch (Throwable ex) {
            log.error("Произошла ошибка при проверке контрольного срока выплаты компенсации у заявок на личный транспорт");
            log.error(ex.getMessage());
        }
    }
    
    private void executeCheck() {
        var now = LocalDateTime.now();
        List<RequestForPersonal> requestsWithViolation =
                requestForPersonalRepository.findRequestForPersonalForSettingPaymentDoneDeadlineViolation(
                        now,
                        TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION,
                        TripRequestStatus.PERSONAL_PAYMENT_AWAITING,
                        DeadlineState.RED);
        for (RequestForPersonal request : requestsWithViolation) {
            processRequestForPersonalWithPaymentDoneDeadlineViolation(request);
        }
    }
    
    private void processRequestForPersonalWithPaymentDoneDeadlineViolation(RequestForPersonal request) {
        try {
            log.info("В связи с нарушением контрольного срока выплаты компенсации по заявке: {} будет снижен SLA",
                     request.getHumanReadableId());
            processRequest(request);
            log.info("Признак нарушения контрольного срока в заявке: {} проставлен", request.getHumanReadableId());
        } catch (Throwable ex) {
            log.error("Произошла ошибка при обработке нарушения контрольного срока по выплате компенсации по заявке: {}",
                      request.getHumanReadableId());
            log.error(ex.getMessage());
        }
    }
    
    private void processRequest(RequestForPersonal request) {
        requestService.setPaymentDoneDeadlineState(request, DeadlineState.RED);
    }
}
