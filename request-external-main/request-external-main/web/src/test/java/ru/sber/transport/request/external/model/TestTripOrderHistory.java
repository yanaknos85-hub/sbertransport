package ru.sber.transport.request.external.model;

import java.time.OffsetDateTime;

public record TestTripOrderHistory(State getStatus, OffsetDateTime getModifiedAt, String getComment,
                                   String getReason) implements TripOrderHistory {

}