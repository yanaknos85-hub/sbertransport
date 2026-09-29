package ru.sber.transport.notifications.messaging.listeners.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.messaging.listeners.DelegateHandler;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.DelegateMessage;
import ru.sber.transport.notifications.database.model.coprorate.Delegate;
import ru.sber.transport.notifications.services.DelegateService;
import ru.sber.transport.notifications.services.NotificationCommand;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Component
@RequiredArgsConstructor
class DelegateHandlerImpl implements DelegateHandler {

    private final List<NotificationCommand<Delegate>> notificationCommands;
    
    private final ObjectMapper objectMapper;
    
    private final DelegateService delegateService;
    
    @Override
    public void handle(DelegateMessage message) {
        if (message.isDeleted()) {
            delegateService.delete(message.getId());
        } else {
            var delegate = new Delegate();
            Delegate previousDelegate = null;
            try {
                delegate = delegateService.get(message.getId()).orElse(null);
                previousDelegate = objectMapper.readValue(objectMapper.writeValueAsString(delegate), Delegate.class);
            } catch (NoSuchElementException | JsonProcessingException e) {
                // ignore
            }
            
            delegate = Delegate.builder()
                               .id(message.getId())
                               .delegateId(message.getDelegateId())
                               .startDate(message.getStartDate()).endDate(message.getEndDate())
                               .supervisorId(message.getSupervisorId())
                               .transportType(TransportTypeEnum.fromId(message.getTransportTypeId()).orElse(null))
                               .build();
            
            delegateService.save(delegate);
    
            log.debug(String.format("Delegate with ID %s saved", delegate.getId()));
            
            try {
                var finalDelegate = delegate;
                var finalPreviousDelegate = previousDelegate;
                notificationCommands.stream().filter(nc -> nc.validate(finalPreviousDelegate, finalDelegate))
                                    .forEach(command -> {
                                        try {
                                            command.sendNotification(finalDelegate);
                                        } catch (JsonProcessingException e) {
                                            log.error("Processing failed", e);
                                        }
                                    });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
