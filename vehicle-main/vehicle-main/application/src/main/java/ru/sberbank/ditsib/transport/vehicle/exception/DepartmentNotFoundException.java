package ru.sberbank.ditsib.transport.vehicle.exception;

import java.util.UUID;

/**
 * Exception is thrown if department is not found
 */
public class DepartmentNotFoundException extends NotFoundException {
    private static final String MESSAGE = "Не найдено подразделение с идентификатором %s";

    public DepartmentNotFoundException(UUID id) {
        super(MESSAGE.formatted(id));
    }
}
