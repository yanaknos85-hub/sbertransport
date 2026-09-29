package ru.sberbank.ditsib.transport.reports.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public enum RequestPublicStatus {
    PUBLIC_AWAITING_APPROVAL,
    PUBLIC_APPROVED,
    PUBLIC_TRIP_CONFIRMATION,
    PUBLIC_TRIP_CONFIRMED,
    PUBLIC_AWAITING_AFFIRMATIVE,
    PUBLIC_AFFIRMED,
    PUBLIC_ORDER_PAYMENT_FORMATION,
    PUBLIC_PAYMENT_AWAITING,
    PUBLIC_PAYMENT_DONE,
    PUBLIC_PAYMENT_NOT_DONE,
    PUBLIC_CANCELLED
}
