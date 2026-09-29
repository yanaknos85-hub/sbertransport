package ru.sber.transport.request.external.business.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sber.transport.request.external.model.State;

/**
 * Возникает, когда невозможно выполнить переход заявки в другое состояние
 */
@RequiredArgsConstructor
@Getter
public class StatusSwitchForbiddenException extends RuntimeException {

    /**
     * Желаемое состояние
     */
    private final State newState;

    /**
     * Роль, которая должна запросить переход
     */
    private final String allowedRole;

}
