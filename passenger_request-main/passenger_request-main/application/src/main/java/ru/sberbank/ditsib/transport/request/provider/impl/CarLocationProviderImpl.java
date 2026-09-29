package ru.sberbank.ditsib.transport.request.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.integrations.messaging.OrdersLocationMessage;
import ru.sberbank.ditsib.transport.request.mappers.CarLocationMapper;
import ru.sberbank.ditsib.transport.request.provider.CarLocationProvider;
import ru.sberbank.ditsib.transport.request.service.CarLocationService;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarLocationProviderImpl implements CarLocationProvider {

    private final CarLocationMapper carLocationMapper;
    private final CarLocationService carLocationService;

    @Override
    public void sendLocationsToSubscribers(OrdersLocationMessage payload) {
        if (payload == null) {
            log.info("Payload is null");
            return;
        }
        if (payload.orderLocations() == null) {
            log.info("Payload with id {} has orderLocations is null", payload.id());
            return;
        }
        var ordersLocation = carLocationMapper.ordersLocationMessageListToOrdersLocationDtoList(payload.orderLocations());
        ordersLocation.forEach(carLocationService::sendLocationToSubscriber);
    }

    @Override
    public void deactivateTask(UUID requestId) {
        carLocationService.deactivateTask(requestId);
    }
}
