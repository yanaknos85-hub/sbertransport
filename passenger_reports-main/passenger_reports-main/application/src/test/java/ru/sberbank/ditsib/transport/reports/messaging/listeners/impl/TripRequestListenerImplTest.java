package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.reports.dao.InboxMessageRepository;
import ru.sberbank.ditsib.transport.reports.model.InboxMessage;
import ru.sberbank.ditsib.transport.reports.service.TripRequestService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripRequestListenerImplTest {

    @InjectMocks
    private TripRequestListenerImpl tripRequestListener;
    @Mock
    private InboxMessageRepository inboxMessageRepository;
    @Mock
    private TripRequestService tripRequestService;

    @Test
    void handle() {
        var payload = Instancio.create(RequestMessage.class);
        var headers1 = Map.of(
                "traceId", "f45e28cc6c79f8e508813adcb053e117",
                "messageId", "e6870193-bc70-4a8e-acd9-05a55634542a",
                "e2e_user", "ShargaevA-V",
                "fraudHandled", Collections.emptyList(),
                "e2e_operation", "saveRequest",
                "e2e_traceId", "3f0a6686-c7d8-4a06-93de-2b00b91ff72d",
                "transportType", "PERSONAL"
        );
        var headers2 = Map.of(
                "traceId", "f45e28cc6c79f8e508813adcb053e117",
                "messageId", "null",
                "e2e_user", "ShargaevA-V",
                "fraudHandled", Collections.emptyList(),
                "e2e_operation", "saveRequest",
                "e2e_traceId", "3f0a6686-c7d8-4a06-93de-2b00b91ff72d",
                "transportType", "PERSONAL"
        );
        var headers3 = new HashMap<String, Object>();
        headers3.put("messageId", null);
        var message1 = MessageBuilder.createMessage(payload, new MessageHeaders(headers1));
        var message2 = MessageBuilder.createMessage(payload, new MessageHeaders(headers2));
        var message3 = MessageBuilder.createMessage(payload, new MessageHeaders(headers3));
        var message4 = MessageBuilder.createMessage(payload, new MessageHeaders(Collections.emptyMap()));
        var inboxMessage = Instancio.create(InboxMessage.class);
        doReturn(inboxMessage).when(inboxMessageRepository).save(any(InboxMessage.class));
        doNothing().when(tripRequestService).processMessage(payload);
        tripRequestListener.handle(message1);
        tripRequestListener.handle(message2);
        tripRequestListener.handle(message3);
        tripRequestListener.handle(message4);
        verify(inboxMessageRepository).save(any(InboxMessage.class));
        verify(tripRequestService, times(2)).processMessage(any(RequestMessage.class));
    }
}