package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.request.dto.OrderLocationDto;

import java.util.UUID;

public interface CarLocationService {

    void addRequestToTask(UUID requestId);

    void sendRequestBatch();

    void sendLocationToSubscriber(OrderLocationDto orderLocation);

    void deactivateTask(UUID requestId);

    void deleteExpiredTasks();
}
