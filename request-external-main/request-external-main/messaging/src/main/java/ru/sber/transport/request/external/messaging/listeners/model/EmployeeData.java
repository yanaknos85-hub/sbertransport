package ru.sber.transport.request.external.messaging.listeners.model;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.request.external.model.Employee;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

/**
 * Данные сотрудника
 */
@RequiredArgsConstructor
public final class EmployeeData implements Employee {

    @Delegate
    private final EmployeeMessage delegate;

}
