package ru.sberbank.ditsib.transport.request.dto.dispatcherRoom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DispatcherRequestSortOption {
    INDICATION("Цветовая индикация"),
    EXPECTED_DISTANCE("Предварительный километраж поездки"),
    EXPECTED_COST("Предварительная стоимость поездки"),
    CREATION_DATE("Время создания поездки"),
    DESIRED_DATE("Желаемая дата и время отправления"),
    REQUEST_HUMAN_ID("ID поездки человекочитаемый");
    
    private final String description;
}
