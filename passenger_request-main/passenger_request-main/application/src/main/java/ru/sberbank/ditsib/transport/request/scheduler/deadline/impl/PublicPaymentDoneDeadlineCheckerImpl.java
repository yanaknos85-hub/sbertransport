package ru.sberbank.ditsib.transport.request.scheduler.deadline.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForPublicRepository;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPublic;
import ru.sberbank.ditsib.transport.request.scheduler.deadline.DeadlineChecker;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicPaymentDoneDeadlineCheckerImpl implements DeadlineChecker {
    private final RequestService requestService;
    private final RequestForPublicRepository requestForPublicRepository;
    
    @Override
    public String info() {
        return "Проверка исполнения контрольных сроков выплаты компенсации по заявке на личный транспорт";
    }
    
    /**
     * Проверка исполнения контрольных сроков выплаты компенсации по заявке на личный транспорт
     */
    public void execute() {
        try {
            log.debug("PublicPaymentDoneDeadlineCheckerImpl.execute() - start");
            executeCheck();
            log.debug("PublicPaymentDoneDeadlineCheckerImpl.execute() - end");
        } catch (Throwable ex) {
            log.error("Произошла ошибка при проверке контрольного срока выплаты компенсации у заявок на общественный транспорт");
            log.error(ex.getMessage());
        }
    }
    
    private void executeCheck() {
        var now = LocalDateTime.now();
        var requestsWithViolation =
                requestForPublicRepository.findRequestForPublicForSettingPaymentDoneDeadlineViolation(
                        now,
                        TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION,
                        TripRequestStatus.PUBLIC_PAYMENT_AWAITING,
                        DeadlineState.RED);
        for (var request : requestsWithViolation) {
            processRequestForPublicWithPaymentDoneDeadlineViolation(request);
        }
    }
    
    private void processRequestForPublicWithPaymentDoneDeadlineViolation(RequestForPublic request) {
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
    
    private void processRequest(RequestForPublic request) {
        requestService.setPaymentDoneDeadlineState(request, DeadlineState.RED);
    }
}
