package ru.sber.transport.telemechanic.enumerate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Schema(title = "Статус проверки", description = "Описание статусов проверок")
public enum CheckStatus {
    
    IN_PROGRESS("В процессе проверки", false, 2),
    DONE("Проверка пройдена", true, 3),
    DECLINE("Проверка не пройдена", true, 1);
    
    private final String description;
    private final boolean isFinal;
    private final int monitoringOrdinal;
}
