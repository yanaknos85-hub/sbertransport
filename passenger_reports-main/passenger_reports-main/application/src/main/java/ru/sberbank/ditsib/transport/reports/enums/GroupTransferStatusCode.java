package ru.sberbank.ditsib.transport.reports.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum GroupTransferStatusCode {
    DEFAULT(0, "Отменено системой"),
    CLOSED_BY_SYSTEM(103, "Поездка завершена системой"),
    BY_USER(201, "Отмена пользователем"),
    NOT_AGREED(202, "Не согласовано"),
    NOT_AGREED_BY_TIME(203, "Не согласовано по истечению срока"),
    CANCELLED_BY_DRIVER(207, "Поездка отменена водителем");

    private final int code;
    private final String name;
}