package ru.sberbank.ditsib.transport.reports.service.impl;

import org.apache.commons.lang3.RandomStringUtils;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.reports.dao.InboxMessageRepository;
import ru.sberbank.ditsib.transport.reports.enums.InboxMessageClassNameEnum;
import ru.sberbank.ditsib.transport.reports.enums.InboxMessageStatusEnum;
import ru.sberbank.ditsib.transport.reports.model.InboxMessage;
import ru.sberbank.ditsib.transport.reports.scheduler.handlers.InboxMessageHandler;
import ru.sberbank.ditsib.transport.reports.scheduler.handlers.RequestInboxMessageHandlerImpl;
import ru.sberbank.ditsib.transport.reports.service.InboxMessageService;
import ru.sberbank.ditsib.transport.reports.service.TripRequestService;

import java.util.List;
import java.util.UUID;

import static org.instancio.Select.all;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EmbeddedPostgres
@ExtendWith(MockitoExtension.class)
class InboxMessageServiceImplTest {
    
    private InboxMessageService service;
    
    @Mock
    private TripRequestService tripRequestServiceImpl;

    @Autowired
    private InboxMessageRepository repository;
    
    @BeforeEach
    void setUp() {
        InboxMessageHandler handler = new RequestInboxMessageHandlerImpl(tripRequestServiceImpl);
        service = new InboxMessageServiceImpl(repository, List.of(handler));
    }
    
    @Test
    void shouldUpdateStatusToDone() {
        RequestMessage requestMessage = Instancio.of(RequestMessage.class)
                                                 .generate(all(Object.class), gen -> gen.oneOf("randomValue", true, 123))
                                                 .create();
        InboxMessage inboxMessage = new InboxMessage(UUID.randomUUID(), requestMessage.getId(), requestMessage);
        repository.save(inboxMessage);
        InboxMessage result = repository.findAll().get(0);
        
        service.process(result);

        Mockito.verify(tripRequestServiceImpl, times(1)).processMessage(any(RequestMessage.class));
        
        assertEquals(InboxMessageStatusEnum.DONE.name(), result.getStatus());
        assertNotNull(result.getUpdatedAt());
    }
    
    @Test
    void shouldUpdateStatusToError_whenExceptionThrown() {
        RequestMessage requestMessage = Instancio.of(RequestMessage.class)
                                                 .generate(all(Object.class), gen -> gen.oneOf("randomValue", true, 123))
                                                 .create();
        InboxMessage inboxMessage =
                Instancio.of(InboxMessage.class)
                         .set(Select.field("payload"), (Object) requestMessage)
                         .set(Select.field("entityId"), requestMessage.getId())
                         .set(Select.field("status"), "NEW")
                         .set(Select.field("className"), InboxMessageClassNameEnum.REQUEST_MESSAGE.getValue())
                         .ignore(field(InboxMessage::getUpdatedAt))
                         .ignore(field(InboxMessage::getErrorReason))
                         .create();
        repository.save(inboxMessage);
    
        doThrow(new RuntimeException("Error has happened")).when(tripRequestServiceImpl).processMessage(any(RequestMessage.class));
        
        service.process(inboxMessage);
        
        InboxMessage result = repository.findAll().get(0);
        assertEquals(InboxMessageStatusEnum.ERROR.name(), result.getStatus());
        assertNotNull(result.getUpdatedAt());
    }
    
    @Test
    void shouldUpdateStatusToError_whenHandlerNotFound() {
        RequestMessage requestMessage = Instancio.of(RequestMessage.class)
                                                 .generate(all(Object.class), gen -> gen.oneOf("randomValue", true, 123))
                                                 .create();
        InboxMessage inboxMessage =
                Instancio.of(InboxMessage.class)
                         .set(Select.field("payload"), (Object) requestMessage)
                         .set(Select.field("entityId"), requestMessage.getId())
                         .set(Select.field("status"), "NEW")
                         .set(Select.field("className"), RandomStringUtils.randomAlphabetic(5))
                         .ignore(field(InboxMessage::getUpdatedAt))
                         .ignore(field(InboxMessage::getErrorReason))
                         .create();
        repository.save(inboxMessage);
    
        Mockito.verify(tripRequestServiceImpl, times(0)).processMessage(any(RequestMessage.class));

        service.process(inboxMessage);
        
        InboxMessage result = repository.findAll().get(0);
        assertEquals( InboxMessageStatusEnum.ERROR.name(), result.getStatus());
        assertNotNull(result.getUpdatedAt());
        assertEquals("Не найден обработчик для данного типа сообщения", result.getErrorReason());
    }
}
