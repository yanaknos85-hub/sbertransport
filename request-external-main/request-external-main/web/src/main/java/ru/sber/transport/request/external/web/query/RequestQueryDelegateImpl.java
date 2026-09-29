package ru.sber.transport.request.external.web.query;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sber.transport.business.providers.DepartmentsProvider;
import ru.sber.transport.business.providers.TripOrderHistoriesProvider;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.business.providers.TripOrdersMetaProvider;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.request.external.model.TripOrderData;
import ru.sber.transport.request.external.model.TripOrderHistory;
import ru.sber.transport.request.external.web.model.FullResponseTripOrderData;
import ru.sber.transport.request.external.web.model.ListResponseTripOrderData;
import ru.sber.transport.request.external.web.model.RegistryResponseTripOrderData;
import ru.sber.transport.request.external.web.model.WebRequestFilter;
import ru.sber.transport.web.api.ExternalRequestQueryApi;
import ru.sber.transport.web.model.ExternalRequestPage;
import ru.sber.transport.web.model.ExternalRequestPageContentInner;
import ru.sber.transport.web.model.Format;
import ru.sber.transport.web.model.GetReportRequest;
import ru.sber.transport.web.model.ListExternalRequest;
import ru.sber.transport.web.model.OrderKind;
import ru.sber.transport.web.model.Page;
import ru.sber.transport.web.model.RegistryFilterRqDepartments;
import ru.sber.transport.web.model.Sort;
import ru.sber.transport.web.model.SortDirection;
import ru.sber.transport.web.model.State;
import ru.sber.transport.web.model.TripOrderStatusHistory;

/**
 * Реализация сервиса для работы с заявками на поездку
 */
@Slf4j
@RequiredArgsConstructor
public class RequestQueryDelegateImpl implements ExternalRequestQueryApi {

    private final DepartmentsProvider departmentsProvider;

    private final TripOrdersProvider tripOrdersProvider;

    private final TripOrderHistoriesProvider tripOrderHistoriesProvider;

    private final TripOrdersMetaProvider tripOrdersMetaProvider;

    private final EmployeeOrganizationFunction employeeOrganizationFunction;

    @Override
    public CompletableFuture<ResponseEntity<ListExternalRequest>> get(UUID requestId,
        Optional<OffsetDateTime> ifModifiedSince) {
        final var employeeOrganization = getEmployeeOrganization();
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        return CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            if (ifModifiedSince.isPresent()) {
                final var meta = tripOrdersMetaProvider.meta(employeeOrganization, requestId);
                if (meta.modifiedAt().isBefore(ifModifiedSince.get())) {
                    return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                        .eTag(meta.hash())
                        .lastModified(meta.modifiedAt().toInstant())
                        .build();
                }
            }
            final var result = tripOrdersProvider.get(employeeOrganization, requestId)
                .orElseThrow(() -> new EntityNotFoundException(TripOrderData.class, requestId));
            final var meta = tripOrdersMetaProvider.meta(employeeOrganization, requestId);
            return ResponseEntity.ok()
                .eTag(meta.hash())
                .lastModified(meta.modifiedAt().toInstant())
                .body(new ListResponseTripOrderData(result));
        });
    }

    @Override
    public CompletableFuture<ResponseEntity<ExternalRequestPage>> getAll(Optional<OrderKind> kind,
        Optional<String> humanReadableId, Optional<List<Integer>> balanceUnitSet, Optional<String> costCenter,
        Optional<List<State>> status, Optional<List<UUID>> passenger, Optional<List<UUID>> approver,
        Optional<OffsetDateTime> startFrom, Optional<OffsetDateTime> startTo, Optional<String> passengerName,
        Optional<String> approverName, Optional<Format> format, Optional<Integer> page, Optional<Integer> size,
        Optional<String> sort, Optional<SortDirection> direction) {
        final var employeeOrganization = getEmployeeOrganization();
        final var effectiveKind = kind.orElse(OrderKind.PASSENGER);
        final var passengers =
            OrderKind.APPROVAL.equals(effectiveKind) || ControllerUtils.isDataMaster() ? passenger.orElseGet(List::of)
                : List.of(ControllerUtils.currentUser());
        final var approvals =
            OrderKind.PASSENGER.equals(effectiveKind) || ControllerUtils.isDataMaster() ? approver.orElseGet(List::of)
                : List.of(ControllerUtils.currentUser());
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        return CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            final var sortField = sort.orElse("humanReadableId");
            final var sortDirection = direction.orElse(SortDirection.ASC);
            final var filter = WebRequestFilter.builder()
                .approverName(approverName.orElse(null))
                .approver(approvals)
                .passenger(passengers)
                .passengerName(passengerName.orElse(null))
                .startTimeFrom(startFrom.orElse(null))
                .startTimeTo(startTo.orElse(null))
                .status(status.orElseGet(List::of))
                .organizationId(employeeOrganization)
                .humanReadableId(humanReadableId.orElse(null))
                .balanceUnitSet(balanceUnitSet.orElseGet(List::of))
                .costCenter(costCenter.orElse(null));

            final var effectiveFormat = format.orElse(Format.LIST);
            final var contentPage = switch (effectiveFormat) {
                case LIST -> tripOrdersProvider.get(filter.build(), page.orElse(0), size.orElse(20), sortField,
                    sortDirection.equals(SortDirection.ASC));
                case FULL -> tripOrdersProvider.getFull(filter.build(), page.orElse(0), size.orElse(20), sortField,
                    sortDirection.equals(SortDirection.ASC));
                case REGISTRY -> {
                    filter.isStrictlyApprover(true);
                    yield tripOrdersProvider.getRegistry(filter.build(), page.orElse(0), size.orElse(20), sortField,
                        sortDirection.equals(SortDirection.ASC));
                }
            };

            final var content = contentPage.content().stream()
                .map(it -> createResponse(it, effectiveFormat))
                .toList();
            final var sortData = contentPage.sort();
            final var pageData = contentPage.page();
            final var result = new ExternalRequestPage(content,
                new Sort(sortData.field(), sortData.asc() ? Sort.DirectionEnum.ASC : Sort.DirectionEnum.DESC),
                new Page(pageData.number(), pageData.size(), pageData.last(), pageData.first(), pageData.total(),
                    pageData.count()));
            return ResponseEntity.ok(result);
        });
    }

    @Override
    public CompletableFuture<ResponseEntity<List<TripOrderStatusHistory>>> getStatusHistory(UUID requestId) {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        return CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            var encounteredStatuses = new HashSet<>();
            final var tripOrderHistory = tripOrderHistoriesProvider.get(requestId);
            final var tripOrderHistoryList = tripOrderHistory
                .stream()
                .sorted(Comparator.comparing(TripOrderHistory::getModifiedAt))
                .filter(item -> encounteredStatuses.add(item.getStatus()))
                .map(item -> {
                    var tripOrderStatusHistory = new TripOrderStatusHistory();
                    tripOrderStatusHistory.setStatus(State.valueOf(item.getStatus().name()));
                    tripOrderStatusHistory.setModifiedAt(item.getModifiedAt());
                    tripOrderStatusHistory.setComment(item.getComment());
                    tripOrderStatusHistory.setReason(item.getReason());
                    return tripOrderStatusHistory;
                })
                .toList();
            return ResponseEntity.ok(tripOrderHistoryList);
        });
    }

    @Override
    public CompletableFuture<ResponseEntity<Void>> meta(UUID requestId, Optional<OffsetDateTime> ifModifiedSince) {
        final var employeeOrganization = getEmployeeOrganization();
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        return CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            if (ifModifiedSince.isPresent()) {
                final var meta = tripOrdersMetaProvider.meta(employeeOrganization, requestId);
                if (meta.modifiedAt().isBefore(ifModifiedSince.get())) {
                    return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                        .eTag(meta.hash())
                        .lastModified(meta.modifiedAt().toInstant())
                        .build();
                }
            }
            final var meta = tripOrdersMetaProvider.meta(employeeOrganization, requestId);
            return ResponseEntity.accepted()
                .eTag(meta.hash())
                .lastModified(meta.modifiedAt().toInstant())
                .build();
        });
    }

    @Override
    public CompletableFuture<ResponseEntity<ExternalRequestPage>> getReport(GetReportRequest getReportRequest) {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();

        final var rqFilter = getReportRequest;
        final var registryFilter = rqFilter.getFilter();

        final var childrenDepartments = departmentsProvider.getChildrenDepartments(
            getDepartmentIds(registryFilter.getDepartments()));

        return CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.getContext().setAuthentication(authentication);

            final var sortField = Optional.of(rqFilter.getSort()).orElse("humanReadableId");
            final var sortDirection = Optional.of(rqFilter.getDirection()).orElse(SortDirection.ASC);
            final var filter = WebRequestFilter.builder()
                .approverName(registryFilter.getApproverName())
                .approver(registryFilter.getApprover())
                .isStrictlyApprover(true)
                .passenger(registryFilter.getPassenger())
                .passengerName(registryFilter.getPassengerName())
                .startTimeFrom(registryFilter.getStartFrom())
                .startTimeTo(registryFilter.getStartTo())
                .status(registryFilter.getStatus())
                .organizationId(registryFilter.getOrganizationId())
                .humanReadableId(registryFilter.getHumanReadableId())
                .balanceUnitSet(registryFilter.getBalanceUnits())
                .costCenter(registryFilter.getCostCenter())
                .orderPaymentFormationStartRange(
                    Optional.ofNullable(registryFilter.getOrderPaymentFormationStartRange())
                        .map(it -> new ImmutablePair<>(it.getStartDate(), it.getEndDate()))
                        .orElse(null))
                .departments(childrenDepartments)
                .build();

            final var contentPage = tripOrdersProvider.getRegistry(filter,
                Optional.ofNullable(getReportRequest.getPage()).orElse(0),
                Optional.ofNullable(getReportRequest.getSize()).orElse(20),
                sortField, sortDirection.equals(SortDirection.ASC));

            final var content = contentPage.content().stream()
                .map(
                    tripOrderData -> (ExternalRequestPageContentInner) new RegistryResponseTripOrderData(tripOrderData))
                .toList();

            final var sortData = contentPage.sort();
            final var pageData = contentPage.page();

            final var result = new ExternalRequestPage(
                content,
                new Sort(sortData.field(), sortData.asc() ? Sort.DirectionEnum.ASC : Sort.DirectionEnum.DESC),
                new Page(pageData.number(), pageData.size(), pageData.last(), pageData.first(), pageData.total(),
                    pageData.count())
            );
            return ResponseEntity.ok(result);
        });
    }

    private @Nullable UUID getEmployeeOrganization() {
        final var organizationId = employeeOrganizationFunction.apply(ControllerUtils.currentUser());
        return ControllerUtils.isDataMaster() ? null : organizationId;
    }

    private ExternalRequestPageContentInner createResponse(TripOrderData source, Format effectiveFormat) {
        return switch (effectiveFormat) {
            case LIST -> new ListResponseTripOrderData(source);
            case FULL -> new FullResponseTripOrderData(source);
            case REGISTRY -> new RegistryResponseTripOrderData(source);
        };
    }

    public Set<UUID> getDepartmentIds(RegistryFilterRqDepartments departments) {
        Set<UUID> uuids = new HashSet<>();
        if (departments != null) {
            if (CollectionUtils.isNotEmpty(departments.get1())) {
                uuids.addAll(departments.get1());
            }
            if (CollectionUtils.isNotEmpty(departments.get2())) {
                uuids.addAll(departments.get2());
            }
            if (CollectionUtils.isNotEmpty(departments.get3())) {
                uuids.addAll(departments.get3());
            }
            if (CollectionUtils.isNotEmpty(departments.get4())) {
                uuids.addAll(departments.get4());
            }
            if (CollectionUtils.isNotEmpty(departments.get5())) {
                uuids.addAll(departments.get5());
            }
            if (CollectionUtils.isNotEmpty(departments.get6())) {
                uuids.addAll(departments.get6());
            }
        }
        return uuids;
    }

}
