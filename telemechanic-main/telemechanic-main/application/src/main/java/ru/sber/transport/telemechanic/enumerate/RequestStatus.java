package ru.sber.transport.telemechanic.enumerate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Schema(title = "Статус листа проверок", description = "Статус листа проверок")
public enum RequestStatus {
    
    IN_PROGRESS("В процессе проверки"),
    WARNING("Проверки завершены с замечаниями"),
    DONE("Проверки завершены успешно"),
    ON_THE_LINE("На линии"),
    IN_GARAGE("В гараже"),
    FINISHED("Завершено"),
    DECLINED("Отклонено"),
    EXPIRED("Истекло"),
    CANCELED("Отменено");

    @Schema(title = "Описание статуса проверки", description = "Описание статуса проверки")
    private final String description;
}