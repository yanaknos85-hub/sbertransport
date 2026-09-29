package ru.sberbank.ditsib.transport.request.evaluators.impl;

import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.exceptions.IllegalCallerResponseException;
import ru.sberbank.ditsib.transport.request.database.dao.RequestRepository;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
import ru.sberbank.ditsib.transport.request.database.model.RequestHistoryElementForTaxi;
import ru.sberbank.ditsib.transport.request.database.model.RequestRating;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.evaluators.Evaluator;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestRatingSender;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

public class EvaluatorTaxiImpl implements Evaluator {
    
    private final RequestRepository requestRepository;
    private final RequestRatingSender requestRatingSender;
    
    public EvaluatorTaxiImpl(RequestRepository requestRepository, RequestRatingSender requestRatingSender) {
        this.requestRepository = requestRepository;
        this.requestRatingSender = requestRatingSender;
    }
    
    @Override
    public Request rate(Request request, RequestRating rating, Employee user) {
        var toRate = (RequestForTaxi) request;
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
        if (toRate.getStatus() == null) {
            toRate.setStatusCode(TripRequestStatus.TaxiStatusCode.TAXI_TRIP_FINISHED_RATED.getCode());
        }
        toRate.getHistoryItemsForTaxi().add(RequestHistoryElementForTaxi
                                                    .builder()
                                                    .changeDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                                                    .requestForTaxi(toRate)
                                                    .code(TripRequestStatus.TaxiStatusCode.TAXI_TRIP_FINISHED_RATED.getCode())
                                                    .comment("Пользователь " + user.getFIO() + " выставил оценку " + rating.getRating())
                                                    .status(Optional.ofNullable(toRate.getStatus()).orElse(TripRequestStatus.TAXI_TRIP_FINISHED))
                                                    .initiator(user.getId())
                                                    .build());
        var savedRequest = requestRepository.save(toRate);
        requestRatingSender.send(savedRequest.getId(), rating);
        return savedRequest;
    }
}
