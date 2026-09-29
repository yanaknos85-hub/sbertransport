package ru.sberbank.ditsib.transport.vehicle.exception;

import java.util.UUID;

public class AccessiblePositionNotFoundException extends NotFoundException {
    private static final String MSG = "Не найдена в справочнике позиция с идентификатором %s";

    public AccessiblePositionNotFoundException(UUID id) {
        super(MSG.formatted(id));
    }
}
