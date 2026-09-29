package ru.sberbank.ditsib.transport.request.scheduler.deadline.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForGroupTransferRepository;
import ru.sberbank.ditsib.transport.request.database.model.RequestForGroupTransfer;
import ru.sberbank.ditsib.transport.request.database.model.corp.TechnicalUser;
import ru.sberbank.ditsib.transport.request.dto.CancelDTO;
import ru.sberbank.ditsib.transport.request.scheduler.deadline.DeadlineChecker;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupTransferApprovalDeadlineCheckerImpl implements DeadlineChecker {
    
    private final RequestService requestService;
    private final RequestForGroupTransferRepository repository;
    
    @Override
    public String info() {
        return "Проверка исполнения контрольных сроков согласования заявки на такси (с отменой заявок по истечению контрольного срока)";
    }
    
    /**
     * Проверка исполнения контрольных сроков согласования заявки на такси (с отменой заявок по истечению контрольного срока)
     */
    public void execute() {
        try {
            log.debug("GroupTransferApprovalDeadlineCheckerImpl.execute() - start");
            executeCheck();
            log.debug("GroupTransferApprovalDeadlineCheckerImpl.execute() - end");
        } catch (Throwable ex) {
            log.error("Произошла ошибка при проверке контрольного срока согласования у заявок на такси");
            log.error(ex.getMessage());
        }
    }
    
    private void executeCheck() {
        var now = LocalDateTime.now();
        List<RequestForGroupTransfer> requestsWithViolation =
                repository.findRequestForGroupTransferWithApprovalDeadlineViolation(now, TripRequestStatus.GROUP_TRANSFER_AWAITING_APPROVAL);
        for (var request : requestsWithViolation) {
            cancelRequestForGroupTransferWithApprovalDeadlineViolation(request);
        }
    }
    
    private void cancelRequestForGroupTransferWithApprovalDeadlineViolation(RequestForGroupTransfer request) {
        try {
            log.info("В связи с нарушением контрольного срока на согласование заявка на групповой трансфер: {} будет отменена",
                     request.getHumanReadableId());
            cancelRequest(request);
            log.info("Заявка: {} отменена", request.getHumanReadableId());
        } catch (Throwable ex) {
            log.error("Произошла ошибка при отмене заявки на групповой трансфер: {}", request.getHumanReadableId());
            log.error(ex.getMessage());
        }
    }
    
    private void cancelRequest(RequestForGroupTransfer request) {
        CancelDTO cancelDTO = CancelDTO.builder()
                                       .reason("Заявка отменена в связи с истечением контрольного срока на согласование")
                                       .code(TripRequestStatus.GroupTransferStatusCode.GROUP_TRANSFER_CANCELLED_BY_EXPIRATION_TIME.getCode())
                                       .build();
        requestService.cancel(request, cancelDTO, TechnicalUser.get(), true);
    }
}