package ru.sber.transport.request.external.resolver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
import ru.sber.transport.request.external.model.TripOrderData;
import ru.sber.transport.request.external.resolver.model.TripOrderRegistry;
import ru.sber.transport.request.external.web.model.WebRequestFilter;
import ru.sber.transport.request.external.web.util.WebParamUtils;
import ru.sber.transport.web.model.State;
import ru.sberbank.utils.reflection.ReflectionUtils;

/**
 * Экспортер данных заявок на транспорт.
 */
@RequiredArgsConstructor
public class RequestsExporter implements DataExporter<TripOrderRegistry> {

    private final TripOrdersProvider tripOrdersProvider;

    private final TripOrderHistoriesProvider tripOrderHistoriesProvider;

    private final EmployeeOrganizationFunction employeeOrganizationFunction;

    @Override
    public @Nullable String getCaption() {
        return "КОНФИДЕНЦИАЛЬНО";
    }

    @Override
    public @NotNull List<TripOrderRegistry> exportData(@NotNull Map<String, ?> map,
        @NotNull JwtAuthenticationToken jwtAuthenticationToken) {
        SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);
        final var dataMaster = ControllerUtils.isDataMaster();
        var organizationId = WebParamUtils.toUUID(get(map, "organizationId"));

        if (organizationId == null && !dataMaster) {
            organizationId = employeeOrganizationFunction.apply(ControllerUtils.currentUser());
        }

        final var filter = WebRequestFilter.builder()
            .humanReadableId(get(map, "humanReadableId"))
            .costCenter(get(map, "costCenter"))
            .balanceUnitSet(WebParamUtils.parseList(map.get("balanceUnitSet"), Integer::valueOf))
            .startTimeTo(WebParamUtils.parseOffsetDateTime(map.get("startTo")))
            .startTimeFrom(WebParamUtils.parseOffsetDateTime(map.get("startFrom")))
            .status(WebParamUtils.parseList(map.get("status"), State::valueOf))
            .approver(WebParamUtils.parseList(map.get("approver"), UUID::fromString))
            .isStrictlyApprover(true)
            .passenger(WebParamUtils.parseList(map.get("passenger"), UUID::fromString))
            .approverName(get(map, "approverName"))
            .passengerName(get(map, "passengerName"))
            .organizationId(organizationId)
            .build();
        var page = 0;
        final var size = 20;
        var pages = 0;
        final var response = new LinkedList<TripOrderRegistry>();
        do {
            final var data = tripOrdersProvider.getRegistry(filter, page++, size, "humanReadableId", true);
            final var historyMap = tripOrderHistoriesProvider.get(
                data.content().stream().map(TripOrderData::getId).toList());
            pages = data.page().count();
            response.addAll(data.content().stream()
                .map(it -> new TripOrderRegistry(it, historyMap.getOrDefault(it.getId(), new ArrayList<>()))).toList());
        } while (page <= pages);
        return response;
    }

    private <T> T get(@NotNull final Map<String, ?> map, @NotNull final String key) {
        return Optional.ofNullable(map.get(key))
            .or(() -> Optional.ofNullable(ReflectionUtils.cast(map.get(key + "[]")))).map(ReflectionUtils::<T>cast)
            .orElse(null);
    }

}
