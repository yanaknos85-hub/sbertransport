package ru.sber.transport.request.external.business.impl;

import static ru.sber.transport.request.external.model.State.NEW;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;
import ru.sber.transport.request.external.model.RequestFilter;
import ru.sber.transport.request.external.model.State;

/**
 * Фильтр для выборки заявок для напоминания
 *
 * @param startTimeTo время, до которого необходимо выбрать заявки
 */
record ReminderFilter(OffsetDateTime startTimeTo) implements RequestFilter {

    @Override
    public UUID organizationId() {
        return null;
    }

    @Override
    public String approverName() {
        return null;
    }

    @Override
    public String passengerName() {
        return null;
    }

    @Override
    public List<UUID> passenger() {
        return List.of();
    }

    @Override
    public List<UUID> approver() {
        return List.of();
    }

    @Override
    public Boolean isStrictlyApprover() {
        return null;
    }

    @Override
    public OffsetDateTime startTimeFrom() {
        return null;
    }

    @Override
    public List<State> states() {
        return List.of(NEW);
    }

    @Override
    public String humanReadableId() {
        return null;
    }

    @Override
    public List<Integer> balanceUnitSet() {
        return List.of();
    }

    @Override
    public String costCenter() {
        return null;
    }

    @Override
    public Pair<OffsetDateTime, OffsetDateTime> orderPaymentFormationStartRange() {
        return null;
    }

    @Override
    public Set<UUID> departments() {
        return Set.of();
    }

}