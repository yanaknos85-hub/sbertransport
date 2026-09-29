package ru.sberbank.ditsib.transport.request.evaluators.impl;

import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.RequestRating;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.evaluators.Evaluator;

public class EvaluatorDummyImpl implements Evaluator {
    @Override
    public Request rate(Request request, RequestRating rating, Employee user) {
        return request;
    }
}
