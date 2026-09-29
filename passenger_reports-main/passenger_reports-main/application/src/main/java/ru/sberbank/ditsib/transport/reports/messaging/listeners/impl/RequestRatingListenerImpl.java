package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.messaging.messages.trip.TripRatingMessage;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.RequestRatingListener;
import ru.sberbank.ditsib.transport.reports.model.RequestRating;
import ru.sberbank.ditsib.transport.reports.service.RequestService;

@RequiredArgsConstructor
@Component("requestRatingInput")
@Slf4j
public class RequestRatingListenerImpl implements RequestRatingListener   {
    private final RequestService requestService;

    @Override
    public void handle(TripRatingMessage message) {
        requestService.findById(message.getRequestId()).ifPresent(request -> {

            request.setRequestRating(RequestRating.builder().
                    advantages(message.getAdvantages()).
                    drawbacks(message.getDrawbacks()).
                    rating(message.getRating()).
                    ratingComment(message.getRatingComment()).build());

            requestService.save(request);
        });
    }

}
