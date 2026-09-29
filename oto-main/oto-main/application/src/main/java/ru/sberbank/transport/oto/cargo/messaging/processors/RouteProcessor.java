package ru.sberbank.transport.oto.cargo.messaging.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.route.messaging.RouteMessage;
import ru.sberbank.transport.oto.cargo.mappers.RouteMapper;
import ru.sberbank.transport.oto.cargo.service.RouteService;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RouteProcessor {
    
    private final RouteService service;
    private final RouteMapper mapper;
    
    public void handle(RouteMessage message) {
        log.debug("Редактирование маршрута, id: {}, humanReadableId: {}", message.id(), message.humanReadableId());
        service.save(mapper.messageToEntity(message));
    }
}
