package ru.sberbank.ditsib.transport.request.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.request.controller.SyncController;
import ru.sberbank.ditsib.transport.request.database.dao.RequestRepository;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@Slf4j
@E2EController
public class SyncControllerImpl implements SyncController {

    private final RequestRepository requestRepository;

    private final Map<TransportTypeEnum, RequestSender<Request>> requestSenders;

    @Override
    public List<String> getRequestIdForSynchronize(LocalDate creationDateFrom, LocalDate creationDateTo) {
        return postRequestIdForSynchronize(creationDateFrom, creationDateTo, false);
    }

    /**
     * @param creationDateFrom Начальная дата периода отбора заявок по времени создания
     * @param creationDateTo Конечная дата периода отбора заявок по времени создания
     *
     * @return Список id заявок для синхронизации
     */
    @Transactional
    @Override
    public List<String> postRequestIdForSynchronize(LocalDate creationDateFrom, LocalDate creationDateTo) {
        return postRequestIdForSynchronize(creationDateFrom, creationDateTo, true);
    }

    private List<String> postRequestIdForSynchronize(
            LocalDate creationDateFrom, LocalDate creationDateTo,
            boolean needSend
    ) {
        var userId = ControllerUtils.currentUser();

        log.info("Пользователь с id = {} запросил получение списка id заявок, данные по которым необходимо повторно отправить в топик service" +
                ".request, указав при этом период поиска по дате создания с {} по {}", userId, creationDateFrom, creationDateTo);


        var creationTimeFrom = creationDateFrom.atStartOfDay();
        var creationTimeTo = creationDateTo.atTime(LocalTime.MAX);

        var requestIdForSynchronize = requestRepository.findAllRequestIdForSynchronize(creationTimeFrom, creationTimeTo);
        log.info("Найдено {} заявок для синхронизации", requestIdForSynchronize.size());

        if (needSend) {
            log.info("Начало отправки заявок для синхронизации");
            requestIdForSynchronize
                    .stream()
                    .map(UUID::fromString)
                    .forEach(this::send);
            log.info("Окончание отправки заявок для синхронизации");
        }

        return requestIdForSynchronize;
    }

    public void send(UUID requestId) {
        try {
            log.info("Отправляем для синхронизации заявку с id = {}", requestId);
            requestRepository.findById(requestId)
                    .ifPresentOrElse(
                            it -> requestSenders.get(it.getTransportType()).send(it),
                            () -> log.warn("Не удалось получить данные заявки с id = {}", requestId)
                    );
        } catch (RuntimeException ex) {
            log.error("При отправке для синхронизации заявки с id = {} произошла ошибка", requestId);
            log.error(ex.getMessage());
        }
    }
}
