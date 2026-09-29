package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.messaging.messages.RequestStatusOverdueMessage;
import ru.sberbank.ditsib.transport.reports.dao.RequestStatusOverdueRepository;
import ru.sberbank.ditsib.transport.reports.mappers.RequestStatusOverdueMapper;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.RequestStatusOverdueListener;

@Slf4j
@RequiredArgsConstructor
@Component("requestStatusOverdueInput")
public class RequestStatusOverdueListenerImpl implements RequestStatusOverdueListener   {
    
    private final RequestStatusOverdueMapper mapper;
    
    private final RequestStatusOverdueRepository repository;
    
    @Override
    public void handle(@Payload RequestStatusOverdueMessage message) {
        log.info("Message received "  + message.getId());
        repository.save(mapper.toModel(message));
    }
}
