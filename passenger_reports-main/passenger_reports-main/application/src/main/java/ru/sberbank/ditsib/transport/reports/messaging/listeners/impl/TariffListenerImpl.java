package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.TariffMessage;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.TariffListener;
import ru.sberbank.ditsib.transport.reports.service.TariffService;

import java.util.List;

@RequiredArgsConstructor
@Component("tariffInput")
public class TariffListenerImpl implements TariffListener {
    private final List<TariffService> tariffServiceList;

    @Override
    public void handle(@Payload TariffMessage message) {
        message.setId(message.getCloneId());
        TransportTypeEnum.fromId(message.getTransportTypeId()).ifPresent(transportTypeEnum -> {
            tariffServiceList.stream()
                    .filter(service -> service.getTransportType().equals(transportTypeEnum))
                    .findFirst().ifPresent(service -> {
                        service.updateOrCreate(message);
                    });
        });
    }
}