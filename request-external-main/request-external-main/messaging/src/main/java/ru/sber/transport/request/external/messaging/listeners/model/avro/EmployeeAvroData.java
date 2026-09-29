package ru.sber.transport.request.external.messaging.listeners.model.avro;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.messages.corporate.avro.EmployeeMessage;
import ru.sber.transport.request.external.model.Employee;

/**
 * Данные сотрудника
 */
@RequiredArgsConstructor
public final class EmployeeAvroData implements Employee {

    @Delegate
    private final EmployeeMessage delegate;

}
