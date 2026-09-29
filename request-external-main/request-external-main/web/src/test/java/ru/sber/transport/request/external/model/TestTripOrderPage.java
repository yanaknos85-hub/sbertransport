package ru.sber.transport.request.external.model;

import java.util.List;

public record TestTripOrderPage(List<TripOrderData> content, PageData page,
                         SortData sort) implements Page<TripOrderData> {
}