package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.reports.dao.InboxMessageRepository;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.TripRequestListener;
import ru.sberbank.ditsib.transport.reports.model.InboxMessage;
import ru.sberbank.ditsib.transport.reports.service.TripRequestService;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component("tripRequestInput")
@Slf4j
public class TripRequestListenerImpl implements TripRequestListener {

    private final InboxMessageRepository inboxMessageRepository;
    private final TripRequestService tripRequestService;

    @Override
    public void handle(Message<RequestMessage> message) {
        try {
            var payload = message.getPayload();
            var headers = message.getHeaders();
            var entityId = payload.getId();
            var messageId = Optional.ofNullable(headers.get("messageId"))
                    .map(String::valueOf)
                    .map(UUID::fromString)
                    .orElse(null);
            if (messageId == null) {
                tripRequestService.processMessage(payload);
            } else {
                inboxMessageRepository.save(new InboxMessage(messageId, entityId, payload));
            }
        } catch (DataIntegrityViolationException e) {
            log.error("Получено повторное сообщение из топика service.request с requestId = {}. Сохранение в inbox не произведено",
                    message.getPayload().getId());
        } catch (IllegalArgumentException e) {
            log.error("Получено сообщение из топика service.request с messageId = {}. Формат messageId не корректен. Сохранение в inbox не произведено",
                    message.getPayload().getId());
        }
    }
}
