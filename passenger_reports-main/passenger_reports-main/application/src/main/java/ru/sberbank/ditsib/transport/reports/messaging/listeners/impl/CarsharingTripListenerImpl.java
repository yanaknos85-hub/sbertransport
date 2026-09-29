package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.integrations.carsharing.messages.CarsharingDataMessage;
import ru.sberbank.ditsib.transport.reports.dao.CarsharingTripRepository;
import ru.sberbank.ditsib.transport.reports.mappers.CarsharingTripMapper;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.CarsharingTripListener;
import ru.sberbank.ditsib.transport.reports.model.CarsharingTrip;

@RequiredArgsConstructor
@Component("carsharingTripInput")
@Slf4j
public class CarsharingTripListenerImpl implements CarsharingTripListener   {
    
    private final CarsharingTripRepository repository;
    
    private final CarsharingTripMapper mapper;
    
    @Override
    public void handle(CarsharingDataMessage message) {
        log.info("Получено новое сообщение CarsharingDataMessage. Message id {}, rentId {}", message.getId(), message.getRentId());
        var trip = repository.findFirstByRentId(message.getRentId()).orElse(CarsharingTrip.builder().build());
        mapper.toModel(message, trip);
        repository.saveAndFlush(trip);
    }
    
}
