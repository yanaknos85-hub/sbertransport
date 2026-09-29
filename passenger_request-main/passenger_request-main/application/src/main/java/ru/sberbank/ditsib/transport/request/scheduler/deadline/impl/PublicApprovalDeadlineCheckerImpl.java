package ru.sberbank.ditsib.transport.request.scheduler.deadline.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForPublicRepository;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPublic;
import ru.sberbank.ditsib.transport.request.database.model.corp.TechnicalUser;
import ru.sberbank.ditsib.transport.request.dto.CancelDTO;
import ru.sberbank.ditsib.transport.request.scheduler.deadline.DeadlineChecker;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicApprovalDeadlineCheckerImpl implements DeadlineChecker {
    
    private final RequestService requestService;
    private final RequestForPublicRepository requestForPublicRepository;
    
    @Override
    public String info() {
        return "Проверка исполнения контрольных сроков согласования заявки на общественный транспорт (с отменой заявок по истечению контрольного срока)";
    }
    
    /**
     * Проверка исполнения контрольных сроков согласования заявки на общественный транспорт (с отменой заявок по истечению контрольного срока)
     */
    public void execute() {
        try {
            log.debug("PublicApprovalDeadlineCheckerImpl.execute() - start");
            executeCheck();
            log.debug("PublicApprovalDeadlineCheckerImpl.execute() - end");
        } catch (Throwable ex) {
            log.error("Произошла ошибка при проверке контрольного срока согласования у заявок на общественный транспорт");
            log.error(ex.getMessage());
        }
    }
    
    private void executeCheck() {
        var now = LocalDateTime.now();
        List<RequestForPublic> requestsWithViolation =
                requestForPublicRepository.findRequestForPublicWithApprovalDeadlineViolation(now, TripRequestStatus.PUBLIC_AWAITING_APPROVAL);
        for (RequestForPublic request : requestsWithViolation) {
            cancelRequestForPublicWithApprovalDeadlineViolation(request);
        }
    }
    
    private void cancelRequestForPublicWithApprovalDeadlineViolation(RequestForPublic request) {
        try {
            log.info("В связи с нарушением контрольного срока на согласование заявка: {} будет отменена", request.getHumanReadableId());
            cancelRequest(request);
            log.info("Заявка: {} отменена", request.getHumanReadableId());
        } catch (Throwable ex) {
            log.error("Произошла ошибка при отмене заявки: {}", request.getHumanReadableId());
            log.error(ex.getMessage());
        }
    }
    
    private void cancelRequest(RequestForPublic request) {
        CancelDTO cancelDTO = CancelDTO.builder()
                                       .reason("Заявка отменена в связи с истечением контрольного срока на согласование")
                                       .code(TripRequestStatus.PublicStatusCode.PUBLIC_DECLINED_AT_EXPIRATION.getCode())
                                       .build();
        requestService.cancel(request, cancelDTO, TechnicalUser.get(), true);
    }
}
