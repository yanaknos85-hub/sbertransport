package ru.sber.transport.request.external.messaging.listeners.model.avro;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.messages.corporate.avro.DepartmentMessage;
import ru.sber.transport.request.external.model.Department;
import ru.sber.transport.request.external.model.DepartmentStatus;

/**
 * Данные подразделения
 */
@RequiredArgsConstructor
public final class DepartmentAvroData implements Department {

    @Delegate
    private final DepartmentMessage message;

    @Override
    public DepartmentStatus getStatus() {
        return message.getDeleted() ? DepartmentStatus.INACTIVE : DepartmentStatus.ACTIVE;
    }

    @Override
    public Integer getLevel() {
        return 1;
    }
}
