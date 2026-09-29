package ru.sber.transport.request.external.business.exception;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Создание заказа невозможно
 */
@Getter
@RequiredArgsConstructor
public class CreatingException extends RuntimeException {

    /**
     * Идентификатор сотрудника
     */
    private final UUID id;
}
