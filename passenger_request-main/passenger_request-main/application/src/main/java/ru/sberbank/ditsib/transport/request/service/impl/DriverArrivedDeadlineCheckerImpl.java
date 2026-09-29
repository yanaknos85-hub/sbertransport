package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.request.database.model.RequestForGroupTransfer;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
import ru.sberbank.ditsib.transport.request.service.DriverArrivedDeadlineChecker;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverArrivedDeadlineCheckerImpl implements DriverArrivedDeadlineChecker {
    
    private final RequestService requestService;
    
    /**
     * Проверка исполнения контрольных сроков по такси и груповому трансферу
     * (с пометкой заявок с нарушением контрольного срока для расчета SLA)
     */
    @Override
    public int execute() {
        log.debug("DriverArrivedDeadlineCheckerImpl.execute() - start");
        final var now = LocalDateTime.now();
        log.debug("now = {}", now);
        final var requestsForTaxiWithViolation = requestService.findRequestsForTaxiWithDriverArrivedDeadlineViolation(now);
        log.debug("requestsForTaxiWithDriverArrivedDeadlineViolation.size() = {}", requestsForTaxiWithViolation.size());
        for (final var request : requestsForTaxiWithViolation) {
            requestService.updateRequestForTaxiWithDriverArrivedDeadlineViolation(request);
        }
        final var requestsForGroupTransferWithViolation =
                requestService.findRequestsForGroupTransferWithDriverArrivedDeadlineViolation(now);
        log.debug("requestsForGroupTransferWithDriverArrivedDeadlineViolation.size() = {}", requestsForGroupTransferWithViolation.size());
        for (final var request : requestsForGroupTransferWithViolation) {
            requestService.updateRequestForGroupTransferWithDriverArrivedDeadlineViolation(request);
        }
        log.debug("DriverArrivedDeadlineCheckerImpl.execute() - end");
        return requestsForTaxiWithViolation.size() + requestsForGroupTransferWithViolation.size();
    }
}
