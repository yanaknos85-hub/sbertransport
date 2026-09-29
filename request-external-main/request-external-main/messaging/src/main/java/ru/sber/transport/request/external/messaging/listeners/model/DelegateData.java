package ru.sber.transport.request.external.messaging.listeners.model;

import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import ru.sber.transport.request.external.model.Delegate;
import ru.sberbank.ditsib.transport.messaging.messages.DelegateMessage;

/**
 * Данные о делегате
 */
@RequiredArgsConstructor
public class DelegateData implements Delegate {

    @lombok.experimental.Delegate
    private final DelegateMessage delegate;

    @Override
    public UUID delegateId() {
        return getDelegateId();
    }

    @Override
    public UUID supervisorId() {
        return getSupervisorId();
    }

    @Override
    public LocalDate startDate() {
        return getStartDate();
    }

    @Override
    public LocalDate endDate() {
        return getEndDate();
    }

    @Override
    public boolean active() {
        return !delegate.isDeleted();
    }
}
