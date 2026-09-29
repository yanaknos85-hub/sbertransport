package ru.sberbank.ditsib.transport.reports.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum PersonalStatusCode {
    DEFAULT(0, "Отменено системой"),
    BY_USER(201, "Отмена пользователем"),
    NOT_AGREED(202, "Не согласовано"),
    NOT_AGREED_BY_TIME(203, "Не согласовано по истечению срока"),
    NOT_APPROVED(204, "Не утверждено"),
    NOT_APPROVED_BY_TIME(205, "Не утверждено по истечению срока"),
    BY_TIME(206, "По истечению срока"),
    BY_DRIVER(207, "Отмена водителем ЛТ");

    private final int code;
    private final String name;
}