package ru.sberbank.ditsib.transport.request.provider;


import ru.sber.transport.integrations.messaging.OrdersLocationMessage;

import java.util.UUID;

public interface CarLocationProvider {

    void sendLocationsToSubscribers(OrdersLocationMessage ordersLocationMessage);

    void deactivateTask(UUID requestId);
}
