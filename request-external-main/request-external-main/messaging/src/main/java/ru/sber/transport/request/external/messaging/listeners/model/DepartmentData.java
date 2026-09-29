package ru.sber.transport.request.external.messaging.listeners.model;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.request.external.model.Department;
import ru.sber.transport.request.external.model.DepartmentStatus;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

/**
 * Данные подразделения
 */
@RequiredArgsConstructor
public final class DepartmentData implements Department {

    @Delegate
    private final DepartmentMessage message;

    @Override
    public UUID getHeadId() {
        return getDepartmentHeadId();
    }

    @Override
    public String getName() {
        return getDepartmentName();
    }

    @Override
    public DepartmentStatus getStatus() {
        return message.isDeleted() ? DepartmentStatus.INACTIVE : DepartmentStatus.ACTIVE;
    }

    @Override
    public Integer getLevel() {
        return 1;
    }
}
