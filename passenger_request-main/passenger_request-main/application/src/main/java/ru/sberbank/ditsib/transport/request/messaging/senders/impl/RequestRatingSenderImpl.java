package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.request.database.model.RequestRating;
import ru.sberbank.ditsib.transport.request.mappers.RatingMapper;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestRatingSender;

import java.util.UUID;

/**
 * Реализация отправителя.
 */
@RequiredArgsConstructor
@Component
class RequestRatingSenderImpl implements RequestRatingSender {

    @Qualifier("requestRatingOutput")
    private final ObjectProvider<OutputBridge> requestRatingOutput;

    private final RatingMapper mapper;

    @Override
    public void send(UUID requestId, RequestRating request) {
        requestRatingOutput.ifAvailable(outputBridge -> outputBridge.send(
                mapper.toMessage(requestId, request)));
    }
}
