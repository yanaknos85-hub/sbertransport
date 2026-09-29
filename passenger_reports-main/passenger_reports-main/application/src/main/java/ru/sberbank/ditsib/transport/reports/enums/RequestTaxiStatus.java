package ru.sberbank.ditsib.transport.reports.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public enum RequestTaxiStatus {
    TAXI_AWAITING_APPROVAL,
    TAXI_APPROVED,
    TAXI_AWAITING_SEARCH,
    TAXI_DRIVER_SEARCH,
    TAXI_DRIVER_FOUND,
    TAXI_DRIVER_ON_THE_WAY,
    TAXI_DRIVER_ARRIVED,
    TAXI_FREE_TIME_EXPIRED,
    TAXI_WAYPOINT_ARRIVED,
    TAXI_TRIP_IN_PROGRESS,
    TAXI_TRIP_FINISHED,
    TAXI_CANCELLED
}
