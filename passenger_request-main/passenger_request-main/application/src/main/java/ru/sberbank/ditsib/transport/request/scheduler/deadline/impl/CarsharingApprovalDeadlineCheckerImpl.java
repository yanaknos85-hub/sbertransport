package ru.sberbank.ditsib.transport.request.scheduler.deadline.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForCarsharingRepository;
import ru.sberbank.ditsib.transport.request.database.model.RequestForCarsharing;
import ru.sberbank.ditsib.transport.request.database.model.corp.TechnicalUser;
import ru.sberbank.ditsib.transport.request.dto.CancelDTO;
import ru.sberbank.ditsib.transport.request.scheduler.deadline.DeadlineChecker;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarsharingApprovalDeadlineCheckerImpl implements DeadlineChecker {
    
    private final RequestService requestService;
    private final RequestForCarsharingRepository requestForCarsharingRepository;
    
    @Override
    public String info() {
        return "Проверка исполнения контрольных сроков согласования заявки на каршеринг (с отменой заявок по истечению контрольного срока)";
    }
    
    /**
     * Проверка исполнения контрольных сроков согласования заявки на каршеринг (с отменой заявок по истечению контрольного срока)
     */
    public void execute() {
        try {
            log.debug("CarsharingApprovalDeadlineCheckerImpl.execute() - start");
            executeCheck();
            log.debug("CarsharingApprovalDeadlineCheckerImpl.execute() - end");
        } catch (Throwable ex) {
            log.error("Произошла ошибка при проверке контрольного срока согласования у заявок на каршеринг");
            log.error(ex.getMessage());
        }
    }
    
    public void executeCheck() {
        var now = LocalDateTime.now();
        List<RequestForCarsharing> requestsWithViolation =
                requestForCarsharingRepository.findAllWithApprovalDeadlineViolation(now,
                                                                                    TripRequestStatus.CARSHARING_AWAITING_APPROVAL);
        for (RequestForCarsharing request : requestsWithViolation) {
            cancelRequestForCarsharingWithApprovalDeadlineViolation(request);
        }
    }
    
    private void cancelRequestForCarsharingWithApprovalDeadlineViolation(RequestForCarsharing request) {
        try {
            log.info("В связи с нарушением контрольного срока на согласование заявка: {} будет отменена", request.getHumanReadableId());
            cancelRequest(request);
            log.info("Заявка: {} отменена", request.getHumanReadableId());
        } catch (Throwable ex) {
            log.error("Произошла ошибка при отмене заявки: {}", request.getHumanReadableId());
            log.error(ex.getMessage());
        }
    }
    
    private void cancelRequest(RequestForCarsharing request) {
        CancelDTO cancelDTO = CancelDTO.builder()
                                       .reason("Заявка отменена в связи с истечением контрольного срока на согласование")
                                       .code(TripRequestStatus.CarsharingStatusCode.CARSHARING_DECLINED_BY_EXPIRATION_TIME.getCode())
                                       .build();
        requestService.cancel(request, cancelDTO, TechnicalUser.get(), true);
    }
}
