package ru.sberbank.ditsib.transport.reports.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TaxiStatusCode {
    DEFAULT(0, "Отменено системой"),
    FINISHED_RATED(101, "Завершена с оценкой"),
    FINISHED_NOT_RATED(102, "Завершена без оценки"),
    CLOSED_BY_SYSTEM(103, "Завершена системой"),
    BY_USER(201, "Отмена пользователем"),
    NOT_AGREED(202, "Не согласовано"),
    NOT_AGREED_BY_TIME(203, "Не согласовано по истечению срока"),
    BY_DISPATCHER(204, "Отмена  диспетчером"),
    CANCELLED_BY_DRIVER(207, "Поездка отменена водителем");

    private final int code;
    private final String name;
}