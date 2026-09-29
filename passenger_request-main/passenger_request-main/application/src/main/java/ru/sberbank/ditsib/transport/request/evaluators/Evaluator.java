package ru.sberbank.ditsib.transport.request.evaluators;

import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.RequestRating;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;

public interface Evaluator {
    Request rate(Request request, RequestRating rating, Employee user);
}
