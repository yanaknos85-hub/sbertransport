package ru.sberbank.ditsib.transport.reports.scheduler.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.reports.model.InboxMessage;
import ru.sberbank.ditsib.transport.reports.service.TripRequestService;

import static ru.sberbank.ditsib.transport.reports.enums.InboxMessageClassNameEnum.REQUEST_MESSAGE;

@Component
@RequiredArgsConstructor
public class RequestInboxMessageHandlerImpl implements InboxMessageHandler {
    private final TripRequestService tripRequestService;
    
    @Override
    public void handle(InboxMessage message) {
        tripRequestService.processMessage(message.getPayload());
    }
    
    @Override
    public boolean canHandle(InboxMessage message) {
        return message.getClassName().equals(REQUEST_MESSAGE.getValue());
    }
}
