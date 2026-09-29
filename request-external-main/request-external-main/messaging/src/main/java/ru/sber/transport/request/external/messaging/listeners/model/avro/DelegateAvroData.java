package ru.sber.transport.request.external.messaging.listeners.model.avro;

import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import ru.sber.transport.request.external.model.Delegate;

/**
 * Данные о делегате
 */
@RequiredArgsConstructor
public class DelegateAvroData implements Delegate {

    @lombok.experimental.Delegate
    private final ru.sber.transport.messages.corporate.avro.DelegateData delegate;

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
        return !delegate.getDeleted();
    }
}
