package ru.sberbank.ditsib.transport.request.scheduler.deadline.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForTaxiRepository;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
import ru.sberbank.ditsib.transport.request.database.model.corp.TechnicalUser;
import ru.sberbank.ditsib.transport.request.dto.CancelDTO;
import ru.sberbank.ditsib.transport.request.scheduler.deadline.DeadlineChecker;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaxiApprovalDeadlineCheckerImpl implements DeadlineChecker {
    
    private final RequestService requestService;
    private final RequestForTaxiRepository requestForTaxiRepository;
    
    @Override
    public String info() {
        return "Проверка исполнения контрольных сроков согласования заявки на такси (с отменой заявок по истечению контрольного срока)";
    }
    
    /**
     * Проверка исполнения контрольных сроков согласования заявки на такси (с отменой заявок по истечению контрольного срока)
     */
    public void execute() {
        try {
            log.debug("TaxiApprovalDeadlineCheckerImpl.execute() - start");
            executeCheck();
            log.debug("TaxiApprovalDeadlineCheckerImpl.execute() - end");
        } catch (Throwable ex) {
            log.error("Произошла ошибка при проверке контрольного срока согласования у заявок на такси");
            log.error(ex.getMessage());
        }
    }
    
    private void executeCheck() {
        var now = LocalDateTime.now();
        List<RequestForTaxi> requestsWithViolation =
                requestForTaxiRepository.findRequestForTaxiWithApprovalDeadlineViolation(now, TripRequestStatus.TAXI_AWAITING_APPROVAL);
        for (RequestForTaxi request : requestsWithViolation) {
            cancelRequestForTaxiWithApprovalDeadlineViolation(request);
        }
    }
    
    private void cancelRequestForTaxiWithApprovalDeadlineViolation(RequestForTaxi request) {
        try {
            log.info("В связи с нарушением контрольного срока на согласование заявка: {} будет отменена", request.getHumanReadableId());
            cancelRequest(request);
            log.info("Заявка: {} отменена", request.getHumanReadableId());
        } catch (Throwable ex) {
            log.error("Произошла ошибка при отмене заявки: {}", request.getHumanReadableId());
            log.error(ex.getMessage());
        }
    }
    
    private void cancelRequest(RequestForTaxi request) {
        CancelDTO cancelDTO = CancelDTO.builder()
                                       .reason("Заявка отменена в связи с истечением контрольного срока на согласование")
                                       .code(TripRequestStatus.TaxiStatusCode.TAXI_CANCELLED_BY_EXPIRATION_TIME.getCode())
                                       .build();
        requestService.cancel(request, cancelDTO, TechnicalUser.get(), true);
    }
}