package ru.sberbank.ditsib.transport.request.evaluators.impl;

import ru.sberbank.ditsib.transport.exceptions.IllegalCallerResponseException;
import ru.sberbank.ditsib.transport.request.database.dao.RequestRepository;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPublic;
import ru.sberbank.ditsib.transport.request.database.model.RequestHistoryElementForPublic;
import ru.sberbank.ditsib.transport.request.database.model.RequestRating;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.evaluators.Evaluator;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestRatingSender;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

public class EvaluatorPublicImpl implements Evaluator {
    
    private final RequestRepository requestRepository;
    private final RequestRatingSender requestRatingSender;
    
    public EvaluatorPublicImpl(RequestRepository requestRepository, RequestRatingSender requestRatingSender) {
        this.requestRepository = requestRepository;
        this.requestRatingSender = requestRatingSender;
    }
    
    @Override
    public Request rate(Request request, RequestRating rating, Employee user) {
        var toRate = (RequestForPublic) request;
        if (!toRate.getPassenger().getId().equals(user.getId())) {
            throw new IllegalCallerResponseException();
        }
        if (rating == null || rating.getRating() == null) {
            //Ignore wrong rating
            return toRate;
        }
        if (Optional.ofNullable(toRate.getRequestRating()).map(RequestRating::getRating).isPresent()) {
            //Rating is changed only once
            return toRate;
        }
        toRate.setRequestRating(rating);
        toRate.getHistoryItemsForPublic().add(RequestHistoryElementForPublic
                                                      .builder()
                                                      .changeDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                                                      .requestForPublic(toRate)
                                                      .comment("Пользователь " + user.getFIO() + " выставил оценку " + rating.getRating())
                                                      .status(request.getStatus())
                                                      .initiator(user.getId())
                                                      .build());
        var savedRequest = requestRepository.save(toRate);
        requestRatingSender.send(savedRequest.getId(), rating);
        return savedRequest;
    }
}
