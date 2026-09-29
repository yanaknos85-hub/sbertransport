package ru.sberbank.ditsib.transport.reports.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public enum RequestCarsharingStatus {
    CARSHARING_AWAITING_APPROVAL,
    CARSHARING_APPROVED,
    CARSHARING_DECLINED,
    CARSHARING_AWAITING_SEARCH,
    CARSHARING_TRIP_IN_PROGRESS,
    CARSHARING_AWAITING_TRIP_APPROVAL,
    CARSHARING_TRIP_FINISHED,
    CARSHARING_CANCELLED
}
