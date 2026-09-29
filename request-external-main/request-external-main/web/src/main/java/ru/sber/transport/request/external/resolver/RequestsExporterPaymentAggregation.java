package ru.sber.transport.request.external.resolver;

import static ru.sber.transport.request.external.web.util.WebParamUtils.parseList;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sber.transport.business.providers.TripOrderHistoriesProvider;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.request.external.business.impl.PaymentAwaitingRequest;
import ru.sber.transport.request.external.model.TripOrderData;
import ru.sber.transport.request.external.resolver.model.TripOrderRegistryPaymentAggregation;
import ru.sber.transport.request.external.web.model.WebRequestFilter;
import ru.sber.transport.request.external.web.util.WebParamUtils;
import ru.sber.transport.web.model.State;
import ru.sberbank.utils.reflection.ReflectionUtils;

/**
 * Экспортер данных заявок на транспорт.
 */
@RequiredArgsConstructor
public class RequestsExporterPaymentAggregation implements DataExporter<TripOrderRegistryPaymentAggregation> {

    private final TripOrdersProvider tripOrdersProvider;

    private final TripOrderHistoriesProvider tripOrderHistoriesProvider;

    private final EmployeeOrganizationFunction employeeOrganizationFunction;

    @Override
    public @Nullable String getCaption() {
        return "КОНФИДЕНЦИАЛЬНО";
    }

    @Override
    public @NotNull List<TripOrderRegistryPaymentAggregation> exportData(@NotNull Map<String, ?> map, @NotNull JwtAuthenticationToken jwtAuthenticationToken) {
        SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);
        final var dataMaster = ControllerUtils.isDataMaster();
        var organizationId = WebParamUtils.toUUID(get(map, "organizationId"));

        if (organizationId == null && !dataMaster) {
            organizationId = employeeOrganizationFunction.apply(ControllerUtils.currentUser());
        }

        final var filter = WebRequestFilter.builder()
                .humanReadableId(get(map, "humanReadableId"))
                .startTimeTo(WebParamUtils.parseOffsetDateTime(map.get("startTo")))
                .startTimeFrom(WebParamUtils.parseOffsetDateTime(map.get("startFrom")))
                .status(List.of(State.ORDER_PAYMENT_FORMATION))
                .approver(parseList(map.get("approver"), UUID::fromString))
                .passenger(parseList(map.get("passenger"), UUID::fromString))
                .approverName(get(map, "approverName"))
                .passengerName(get(map, "passengerName"))
                .organizationId(organizationId)
                .build();
        var page = 0;
        final var size = 20;
        var pages = 0;
        final var tripOrderData = new LinkedList<TripOrderData>();
        do {
            final var data = tripOrdersProvider.getRegistry(filter, page++, size, "humanReadableId", true);
            pages = data.page().count();
            tripOrderData.addAll(data.content().stream().toList());
        } while (page <= pages);

        tripOrderData.forEach(it -> {
            tripOrdersProvider.edit(it.getApprover().getId(), it.getId(), new PaymentAwaitingRequest(it), Set.of("=status"));
        });

        return tripOrderData.stream().map(TripOrderRegistryPaymentAggregation::new)
                .collect(Collectors.toMap(
                        TripOrderRegistryPaymentAggregation::getPersonnelNumber,
                        Function.identity(),
                        (dto1, dto2) -> new TripOrderRegistryPaymentAggregation(
                                dto1.getPaymentCode(),
                                dto1.getResource(),
                                dto1.getFactCost().add(dto2.getFactCost()),
                                dto1.getPersonnelNumber(),
                                dto1.getFio(),
                                dto1.getPeriodFormationForPayment()
                        )
                ))
                .values()
                .stream()
                .toList();
    }

    private <T> T get(@NotNull final Map<String, ?> map, @NotNull final String key) {
        return Optional.ofNullable(map.get(key))
                .or(() -> Optional.ofNullable(ReflectionUtils.cast(map.get(key + "[]")))).map(ReflectionUtils::<T>cast).orElse(null);
    }
}
