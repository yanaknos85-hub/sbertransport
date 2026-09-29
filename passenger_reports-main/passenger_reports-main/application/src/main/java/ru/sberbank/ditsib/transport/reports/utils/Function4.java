package ru.sberbank.ditsib.transport.reports.utils;

@FunctionalInterface
public interface Function4<V1, V2, V3, R> {
    R apply(V1 request, V2 taxiTrip, V3 check);
}