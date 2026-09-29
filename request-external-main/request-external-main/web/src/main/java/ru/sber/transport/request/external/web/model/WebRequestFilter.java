package ru.sber.transport.request.external.web.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;
import ru.sber.transport.request.external.model.RequestFilter;
import ru.sber.transport.web.model.State;

/**
 * Фильтр заявок на поездку
 */
@Builder
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
public class WebRequestFilter implements RequestFilter {

    private final UUID organizationId;

    private final String approverName;

    private final String passengerName;

    private final List<UUID> passenger;

    private final List<UUID> approver;

    private final Boolean isStrictlyApprover;

    private final OffsetDateTime startTimeFrom;

    private final OffsetDateTime startTimeTo;

    private final List<State> status;

    private final String humanReadableId;

    private final List<Integer> balanceUnitSet;

    private final String costCenter;

    private final Pair<OffsetDateTime, OffsetDateTime> orderPaymentFormationStartRange;

    private final Set<UUID> departments;

    @Override
    public List<ru.sber.transport.request.external.model.State> states() {
        return Optional.ofNullable(status).orElseGet(List::of)
                .parallelStream()
                .map(Enum::name)
                .map(ru.sber.transport.request.external.model.State::valueOf)
                .toList();
    }
}
