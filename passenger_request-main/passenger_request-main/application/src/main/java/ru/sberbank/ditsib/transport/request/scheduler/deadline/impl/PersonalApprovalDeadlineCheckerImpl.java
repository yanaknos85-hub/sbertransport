package ru.sberbank.ditsib.transport.request.scheduler.deadline.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForPersonalRepository;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPersonal;
import ru.sberbank.ditsib.transport.request.database.model.corp.TechnicalUser;
import ru.sberbank.ditsib.transport.request.dto.CancelDTO;
import ru.sberbank.ditsib.transport.request.scheduler.deadline.DeadlineChecker;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonalApprovalDeadlineCheckerImpl implements DeadlineChecker {
    
    private final RequestService requestService;
    private final RequestForPersonalRepository requestForPersonalRepository;
    
    @Override
    public String info() {
        return "Проверка исполнения контрольных сроков согласования заявки на личный транспорт (с отменой заявок по истечению контрольного срока)";
    }
    
    /**
     * Проверка исполнения контрольных сроков согласования заявки на личный транспорт (с отменой заявок по истечению контрольного срока)
     */
    public void execute() {
        try {
            log.debug("PersonalApprovalDeadlineCheckerImpl.execute() - start");
            executeCheck();
            log.debug("PersonalApprovalDeadlineCheckerImpl.execute() - end");
        } catch (Throwable ex) {
            log.error("Произошла ошибка при проверке контрольного срока согласования у заявок на личный транспорт");
            log.error(ex.getMessage());
        }
    }
    
    private void executeCheck() {
        var now = LocalDateTime.now();
        List<RequestForPersonal> requestsWithViolation =
                requestForPersonalRepository.findRequestForPersonalWithApprovalDeadlineViolation(now, TripRequestStatus.PERSONAL_AWAITING_APPROVAL);
        for (RequestForPersonal request : requestsWithViolation) {
            cancelRequestForPersonalWithApprovalDeadlineViolation(request);
        }
    }
    
    private void cancelRequestForPersonalWithApprovalDeadlineViolation(RequestForPersonal request) {
        try {
            log.info("В связи с нарушением контрольного срока на согласование заявка: {} будет отменена", request.getHumanReadableId());
            cancelRequest(request);
            log.info("Заявка: {} отменена", request.getHumanReadableId());
        } catch (Throwable ex) {
            log.error("Произошла ошибка при отмене заявки: {}", request.getHumanReadableId());
            log.error(ex.getMessage());
        }
    }
    
    private void cancelRequest(RequestForPersonal request) {
        CancelDTO cancelDTO = CancelDTO.builder()
                                       .reason("Заявка отменена в связи с истечением контрольного срока на согласование")
                                       .code(TripRequestStatus.PersonalStatusCode.PERSONAL_DECLINED_BY_EXPIRATION_TIME.getCode())
                                       .build();
        requestService.cancel(request, cancelDTO, TechnicalUser.get(), true);
    }
}
