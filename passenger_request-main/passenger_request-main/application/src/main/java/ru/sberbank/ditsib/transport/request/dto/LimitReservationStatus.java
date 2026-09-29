package ru.sberbank.ditsib.transport.request.dto;

import java.util.List;

import static java.util.Arrays.asList;

public enum LimitReservationStatus {
    RESERVED_FROM_EMPLOYEE,
    RESERVED_FROM_DEPARTMENT,
    LIMIT_NOT_FOUND,
    LIMIT_NOT_SUFFICIENT,
    LIMIT_CANCELLED,
    LIMIT_SPENT,
    ERROR_EMPLOYEE_NOT_FOUND,
    ERROR_NOT_AUTHORIZED,
    ACTION_TYPE_ERROR,
    LIMIT_SPENT_FAILED,
    LIMIT_CANCEL_FAILED,
    LIMIT_RESERVATION_FAILED,
    LIMIT_RESERVED;
    
    public static List<LimitReservationStatus> getFailStatuses() {
        return asList(LIMIT_NOT_FOUND, LIMIT_NOT_SUFFICIENT, LIMIT_SPENT_FAILED);
    }
}