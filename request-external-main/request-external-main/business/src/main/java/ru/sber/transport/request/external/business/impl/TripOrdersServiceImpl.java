package ru.sber.transport.request.external.business.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.business.providers.AssessmnentProvider;
import ru.sber.transport.business.providers.AvailableClasses;
import ru.sber.transport.business.providers.DelegatesProvider;
import ru.sber.transport.business.providers.DepartmentsProvider;
import ru.sber.transport.business.providers.DurationRequestCheckProvider;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.business.providers.FilesProvider;
import ru.sber.transport.business.providers.FraudsProvider;
import ru.sber.transport.business.providers.GeoZonesProvider;
import ru.sber.transport.business.providers.LimitsProvider;
import ru.sber.transport.business.providers.OverrunCheckProvider;
import ru.sber.transport.business.providers.PricesProvider;
import ru.sber.transport.business.providers.TripOrderHistoriesProvider;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.request.external.business.TripOrdersService;
import ru.sber.transport.request.external.business.exception.BusinessException;
import ru.sber.transport.request.external.business.exception.CreatingException;
import ru.sber.transport.request.external.business.exception.DataConflictException;
import ru.sber.transport.request.external.business.exception.StatusSwitchForbiddenException;
import ru.sber.transport.request.external.messaging.mapper.ReceiptMapper;
import ru.sber.transport.request.external.messaging.mapper.RequestMapper;
import ru.sber.transport.request.external.messaging.message.FraudMonitoringMessage;
import ru.sber.transport.request.external.messaging.senders.FraudMonitoringSender;
import ru.sber.transport.request.external.messaging.senders.NotificationSender;
import ru.sber.transport.request.external.messaging.senders.ReceiptSender;
import ru.sber.transport.request.external.messaging.senders.RequestPayoutSender;
import ru.sber.transport.request.external.messaging.senders.RequestSender;
import ru.sber.transport.request.external.model.EditTripOrderData;
import ru.sber.transport.request.external.model.Employee;
import ru.sber.transport.request.external.model.FileData;
import ru.sber.transport.request.external.model.State;
import ru.sber.transport.request.external.model.TripOrderData;
import ru.sber.transport.request.external.model.TripOrderHistory;
import ru.sber.transport.request.external.model.duration.DurationFraudData;
import ru.sber.transport.request.external.model.overrun.OverrunFraudData;
import ru.sber.transport.request.external.model.singletripduration.SingleTripDurationFraudData;
import ru.sber.transport.request.external.model.triporder.TripOrderCreateDTO;
import ru.sber.transport.request.external.providers.exceptions.DurationLimitExceededException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static ru.sber.transport.request.external.model.State.CANCELLED;
import static ru.sber.transport.request.external.model.State.CONFIRMATION;
import static ru.sber.transport.request.external.model.State.CONFIRMATION_NEEDED;
import static ru.sber.transport.request.external.model.State.CONFIRMED;
import static ru.sber.transport.request.external.model.State.GENAI_CHECK;
import static ru.sber.transport.request.external.model.State.NEW;
import static ru.sber.transport.request.external.model.State.ORDER_PAYMENT_FORMATION;
import static ru.sber.transport.request.external.model.State.PAYMENT_AWAITING;

/**
 * Бизнес-логика работы с заявками на поездки
 */
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TripOrdersServiceImpl implements TripOrdersService {

    public static final String FRAUD_SOURCE = "external_request";

    public static final int METER_IN_KILOMETER = 1000;

    private final EmployeesProvider employeesProvider;

    private final TripOrdersProvider tripOrdersProvider;

    private final AssessmnentProvider assessmnentProvider;

    private final PricesProvider pricesProvider;

    private final RequestSender sender;

    private final List<NotificationSender> notificationSenders;

    private final Clock clock;

    private final FilesProvider filesProvider;

    private final AvailableClasses availableClasses;

    private final ObjectProvider<LimitsProvider> limits;

    private final DepartmentsProvider departmentsProvider;

    private final DelegatesProvider delegatesProvider;

    private final GeoZonesProvider geoZonesProvider;

    private final TripOrderHistoriesProvider tripOrderHistoriesProvider;

    private final RequestPayoutSender requestPayoutSender;

    private final RequestMapper mapper;

    private final DurationRequestCheckProvider durationRequestCheckProvider;

    private final FraudMonitoringSender fraudMonitoringSender;

    private final OverrunCheckProvider overrunCheckProvider;

    private final FraudsProvider fraudsProvider;

    private final long singleTripDurationThresholdMillis;

    private final ReceiptMapper receiptMapper;

    private final ReceiptSender receiptScannerSender;

    @Override
    public void remind() {
        final var count = 100;
        final var nextTime = OffsetDateTime.now(clock).plusMinutes(1);
        var pageNumber = 0;
        var pageCount = 0;
        do {
            final var page = tripOrdersProvider.get(new ReminderFilter(nextTime), pageNumber, count, "date", true);
            final var pageData = page.page();
            pageCount = pageData.count();
            if (pageData.total() > 0) {
                log.debug("Found {} new orders before {}", pageData.total(), nextTime);
                for (final var item : page.content()) {
                    final var notificationTime = item.getDate().plus(item.getPlanned().getDuration())
                        .plus(Duration.ofMinutes(10));
                    final var between = Duration.between(OffsetDateTime.now(clock), notificationTime);
                    log.debug("Found order {} will be notified at {} (via {})", item.getId(), notificationTime,
                        between);
                    final var tripId = item.getId();
                    if (between.isZero() || between.isNegative()) {
                        edit(null, true, item.getPassenger().getOrganizationId(), tripId, new RemindRequest(item),
                            Set.of("=status"));
                    }
                }
            }
            pageNumber++;
        } while (pageNumber < pageCount);
    }

    @Override
    public TripOrderData create(UUID passengerId, TripOrderCreateDTO source) {
        log.info("Create new trip order for {}", passengerId);
        final var passenger = employeesProvider.get(passengerId);
        final var positionId = passenger.getPositionId();
        final var organizationId = passenger.getOrganizationId();

        if (!availableClasses.exists(organizationId, positionId, passenger.getDepartmentId())) {
            throw new CreatingException(passenger.getId());
        }

        final var expectedDuration = 0L;
        final var desiredDate = source.date();
        final var timeZone = geoZonesProvider.get(source.waypoints().getFirst()).timeZone();
        final var startPoint = source.waypoints().getFirst();
        final var geoZone = geoZonesProvider.get(startPoint);
        final var price = pricesProvider.get(source.waypoints(), source.tariff());
        final var saved = tripOrdersProvider.create(passengerId, source, price, geoZone.timeZone());
        try {
            durationRequestCheckProvider.checkDurationLimit(
                    passengerId,
                    desiredDate,
                    expectedDuration,
                    timeZone
            );
        } catch (DurationLimitExceededException e) {
            fraudsProvider.save(new DurationFraudData(saved.getId(), e.getMessage()));
            sendFraud(saved);
        }
        final var expectedDistance = price.distance();
        final var response = overrunCheckProvider.checkOverrunLimit(
                passengerId,
                desiredDate,
                (int) expectedDistance,
                timeZone
        );
        if (response != null && response.comment() != null && !response.comment().isBlank()) {
            var comment = response.comment() + " " + BigDecimal.valueOf(response.totalDistance())
                    .divide(BigDecimal.valueOf(METER_IN_KILOMETER), 2, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                + " км";
            log.debug("Overrun limit exceeded for passenger {}: {}", passengerId, comment);
            fraudsProvider.save(new OverrunFraudData(saved.getId(), comment));
            sendFraud(saved);
        }
        checkSingleTripDuration(saved, price.duration());
        reservePlanned(saved);
        sender.send(saved);
        return saved;
    }

    @Override
    public void delete(UUID organizationId, UUID id) {
        log.info("Cancelling trip order {}", id);
        tripOrdersProvider.delete(organizationId, id);
        assessmnentProvider.deleteForRequest(id);
        limits.ifAvailable(lim -> lim.cancel(id));
        tripOrdersProvider.get(organizationId, id).ifPresent(sender::send);
    }

    @Override
    public void edit(UUID userId, boolean force, UUID organizationId, @NonNull UUID id,
        @NonNull EditTripOrderData newData, @NonNull Set<String> editedFields) {
        log.debug("Editing trip order {}", id);
        final var exists = tripOrdersProvider.get(organizationId, id)
            .orElseThrow(() -> new EntityNotFoundException(TripOrderData.class, id));
        for (final var field : editedFields) {
            if (field.contains("status")) {
                editStatus(userId, force, newData, field, exists);
                if (CANCELLED.equals(newData.getStatus())) {
                    assessmnentProvider.deleteForRequest(id);
                }
            } else if (field.contains("sum")) {
                editSum(newData, field, exists);
            } else if (field.contains("assessments") && exists.getAssessments() != null
                && exists.getAssessments().getService() != null) {
                throw new DataConflictException(TripOrderData.class, "assessments", exists.getAssessments(),
                    newData.getAssessments());
            }
        }
        tripOrdersProvider.edit(userId, id, newData, editedFields);
        final var edited = tripOrdersProvider.get(organizationId, id)
            .orElseThrow(() -> new EntityNotFoundException(TripOrderData.class, id));
        final var existsActual = exists.getActual();
        final var editedActual = edited.getActual();
        if (Objects.nonNull(editedActual.getCost()) && !Objects.equals(existsActual.getCost(),
            editedActual.getCost())) {
            reserveActual(edited);
        }
        if (!ORDER_PAYMENT_FORMATION.equals(newData.getStatus())) {
            sender.send(edited);
        } else {
            final var modifiedAt = tripOrderHistoriesProvider.get(edited.getId())
                .stream()
                .filter(it -> ORDER_PAYMENT_FORMATION.equals(it.getStatus()))
                .findFirst()
                .map(TripOrderHistory::getModifiedAt)
                .orElseThrow(() -> new EntityNotFoundException(TripOrderHistory.class, id));
            requestPayoutSender.send(mapper.toRequestPayoutMessage(newData, edited, modifiedAt));
            sender.send(edited);
        }
        if (exists.getStatus() != newData.getStatus()) {
            statusChanged(newData, edited, exists);
        }
    }

    @Override
    public void attachFile(UUID organizationId, UUID requestId, String fileName, String contentType, Path path) throws BusinessException {
        try {
            tripOrdersProvider.attachFile(requestId, fileName);
            filesProvider.add(requestId, path, contentType);
        } catch (RuntimeException ex) {
            log.warn("Ошибка прикрепления чека к заявке [{}] : ", requestId, ex);
            throw new BusinessException("Чек поездки не был прикреплен");
        }
    }

    @Override
    public FileData getFile(UUID organizationId, UUID requestId, int from, int to) {
        if (!tripOrdersProvider.exists(organizationId, requestId)) {
            throw new EntityNotFoundException(TripOrderData.class, requestId);
        }
        if (to > -1) {
            return filesProvider.get(requestId, from, to);
        } else {
            return filesProvider.get(requestId);
        }
    }

    @Override
    public void deleteFile(UUID organizationId, UUID requestId) {
        if (!tripOrdersProvider.exists(organizationId, requestId)) {
            throw new EntityNotFoundException(TripOrderData.class, requestId);
        }
        filesProvider.delete(requestId);
        tripOrdersProvider.detachFile(requestId);
    }

    private boolean checkPassenger(State newState, boolean force, UUID userId, TripOrderData order) {
        if (!force && !Objects.equals(userId, order.getPassenger().getId())) {
            throw new StatusSwitchForbiddenException(newState, "PASSENGER");
        }
        return true;
    }

    private boolean checkApprover(boolean force, UUID userId, TripOrderData order) {
        final var department = departmentsProvider.get(order.getPassenger().getDepartmentId());
        final var delegatesData = delegatesProvider.get(department.getHeadId());
        final var isHead = Objects.equals(userId, department.getHeadId());
        final var isDelegate =
            !delegatesData.isEmpty() && delegatesData.parallelStream().map(Employee::getId).anyMatch(userId::equals);
        if (!force && (!isHead && !isDelegate)) {
            throw new StatusSwitchForbiddenException(State.CONFIRMATION, "APPROVER");
        }
        return true;
    }

    private boolean allowSwitchToCancelled(State prevState) {
        return switch (prevState) {
            case NEW, CONFIRMATION_NEEDED, CONFIRMATION, DATA_NEEDED -> true;
            default -> false;
        };
    }

    private boolean allowSwitchToConfirmed(State prevState, boolean force, UUID userId, TripOrderData order) {
        return CONFIRMATION.equals(prevState) && checkApprover(force, userId, order);
    }

    private boolean allowSwitchToPayment(State prevState) {
        return PAYMENT_AWAITING.equals(prevState);
    }

    private boolean allowSwitchToGenaiCheck(State prevState) {
        return CONFIRMED.equals(prevState);
    }

    private boolean allowSwitchToOrderPaymentFormation(State prevState) {
        return GENAI_CHECK.equals(prevState) || CONFIRMED.equals(prevState);
    }

    private boolean allowSwitchToPaymentAwaiting(State prevState) {
        return ORDER_PAYMENT_FORMATION.equals(prevState);
    }

    private boolean allowSwitchToConfirmation(State prevState, boolean force, UUID userId, TripOrderData order) {
        return switch (prevState) {
            case NEW, CONFIRMATION_NEEDED, DATA_NEEDED -> checkPassenger(CONFIRMATION, force, userId, order);
            default -> false;
        };
    }

    private boolean allowSwitchToConfirmationNeeded(State prevState, boolean force, UUID userId, TripOrderData order) {
        return NEW.equals(prevState) && checkPassenger(CONFIRMATION_NEEDED, force, userId, order);
    }

    private boolean allowSwitchToNew() {
        return false;
    }

    private void confirmLimit(TripOrderData exists) {
        performLimit(lim -> lim.confirm(exists.getId()));
    }

    private void reserveActual(TripOrderData exists) {
        performLimit(lim -> lim.reserve(exists, it -> it.getActual().getCost()));
    }

    private void reservePlanned(TripOrderData saved) {
        performLimit(lim -> lim.reserve(saved, it -> it.getPlanned().getCost()));
    }

    private void checkSingleTripDuration(TripOrderData saved, java.time.Duration duration) {
        if (duration != null && duration.toMillis() > singleTripDurationThresholdMillis) {
            log.info("Single trip duration exceeded for {}: duration={}ms, threshold={}ms",
                saved.getId(), duration.toMillis(), singleTripDurationThresholdMillis);
            fraudsProvider.save(new SingleTripDurationFraudData(
                saved.getId(),
                "Превышен лимит длительности одной заявки"
            ));
            sendFraud(saved);
        }
    }

    private void cancelLimit(TripOrderData order) {
        performLimit(lim -> lim.cancel(order.getId()));
    }

    private void performLimit(Consumer<LimitsProvider> action) {
        limits.ifAvailable(action);
    }

    private void editSum(EditTripOrderData newData, String field, TripOrderData exists) {
        final var actualCost = exists.getActual().getCost();
        final var newSum =
            field.startsWith("+") ? actualCost.add(newData.getActual().getCost()) : newData.getActual().getCost();
        if (newSum.compareTo(actualCost) != 0) {
            reserveActual(exists);
        }
    }

    private void editStatus(UUID userId, boolean force, EditTripOrderData newData, String field, TripOrderData exists) {
        final var newState = newData.getStatus();
        final var prevState = field.startsWith("-") ? CANCELLED : exists.getStatus();
        final var allow = switch (newState) {
            case NEW -> allowSwitchToNew();
            case CONFIRMATION_NEEDED -> allowSwitchToConfirmationNeeded(prevState, force, userId, exists);
            case CONFIRMATION -> allowSwitchToConfirmation(prevState, force, userId, exists);
            case DATA_NEEDED, CONFIRMED, DECLINED -> allowSwitchToConfirmed(prevState, force, userId, exists);
            case GENAI_CHECK -> allowSwitchToGenaiCheck(prevState);
            case CANCELLED -> allowSwitchToCancelled(prevState);
            case ORDER_PAYMENT_FORMATION -> allowSwitchToOrderPaymentFormation(prevState);
            case PAYMENT_AWAITING -> allowSwitchToPaymentAwaiting(prevState);
            case PAYMENT_DONE, PAYMENT_NOT_DONE -> allowSwitchToPayment(prevState);
        };
        if (!allow) {
            throw new DataConflictException(TripOrderData.class, "status", exists.getStatus(), newData.getStatus());
        }
    }

    private void statusChanged(EditTripOrderData newData, TripOrderData edited, TripOrderData exists) {
        final var status = newData.getStatus();
        if (status == null) {
            return;
        }

        final var passenger = edited.getPassenger();
        final var passengerId = passenger.getId();
        switch (status) {
            case CONFIRMATION_NEEDED ->
                notificationSenders.forEach(it -> it.send(edited, "EXTERNAL_REQUEST_CONFIRMATION_NEEDED", passengerId));
            case CONFIRMATION -> {
                notificationSenders.forEach(it -> {
                    final var headId = departmentsProvider.get(passenger.getDepartmentId()).getHeadId();
                    var approvers = delegatesProvider.get(headId).stream().map(Employee::getId).toList();
                    if (approvers.isEmpty()) {
                        approvers = List.of(headId);
                    }
                    for (final var approver : approvers) {
                        it.send(edited, "EXTERNAL_REQUEST_APPROVAL_NEEDED", approver);
                    }
                });
                var requestId = edited.getId();
                sendForFraudDetection(requestId);
            }
            case DATA_NEEDED ->
                notificationSenders.forEach(it -> it.send(edited, "EXTERNAL_REQUEST_DATA_NEEDED", passengerId));
            case CONFIRMED -> {
                notificationSenders.forEach(it -> it.send(edited, "EXTERNAL_REQUEST_APPROVED", passengerId));
                confirmLimit(exists);
                edit(edited.getApprover().getId(), true, passenger.getOrganizationId(), exists.getId(),
                        new ChangeStateRequest(edited, GENAI_CHECK), Set.of("=status"));
                // Автоматический переход CONFIRMED → GENAI_CHECK выполняется через
                // UpdateStatusListener после получения ответа от ai_payout_check
            }
            case GENAI_CHECK -> {
                // Ожидает сообщение из service.request-external.update.status
                // listener обновит статус дальше на ORDER_PAYMENT_FORMATION или CANCELLED
            }
            case DECLINED -> {
                notificationSenders.forEach(it -> it.send(edited, "EXTERNAL_REQUEST_DECLINED", passengerId));
                cancelLimit(exists);
            }
            case CANCELLED -> cancelLimit(exists);
            default -> {
                // No need for reaction
            }
        }
    }

    private void sendForFraudDetection(UUID requestId) {
        var fileFromOrder = tripOrdersProvider.getFileDetailed(requestId);
        var receipt = receiptMapper.toReceiptMessage(fileFromOrder);
        receiptScannerSender.send(receipt);
    }

    @Override
    public void sendFraud(TripOrderData order) {
        final var fraud = order.getFraud();
        if (fraud != null) {
            fraudMonitoringSender.send(new FraudMonitoringMessage(
                    fraud.getId(),
                    FRAUD_SOURCE,
                    List.of(new FraudMonitoringMessage.FraudDataItem(
                            fraud.getType(),
                            fraud.getComment(),
                            null
                    ))
            ));
            log.info("Fraud monitoring message sent for request: requestId={}", fraud.getId());
        }
    }

}
