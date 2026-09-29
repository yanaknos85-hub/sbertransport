package ru.sber.transport.request.external.providers.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.types.DayToSecond;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.business.providers.OrganizationsProvider;
import ru.sber.transport.business.providers.TripOrdersMetaProvider;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.database.external_request.enums.AssessmentType;
import ru.sber.transport.database.external_request.enums.OrderState;
import ru.sber.transport.database.external_request.enums.Tariff;
import ru.sber.transport.database.external_request.tables.TripOrder;
import ru.sber.transport.database.external_request.tables.records.AssessmentsRecord;
import ru.sber.transport.database.external_request.tables.records.EmployeeRecord;
import ru.sber.transport.database.external_request.tables.records.FraudRecord;
import ru.sber.transport.database.external_request.tables.records.TripOrderRecord;
import ru.sber.transport.database.external_request.tables.records.WaypointRecord;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.request.external.model.Assessments;
import ru.sber.transport.request.external.model.EditTripOrderData;
import ru.sber.transport.request.external.model.Modifiable;
import ru.sber.transport.request.external.model.Organization;
import ru.sber.transport.request.external.model.Page;
import ru.sber.transport.request.external.model.PriceData;
import ru.sber.transport.request.external.model.RequestFilter;
import ru.sber.transport.request.external.model.TripOrderData;
import ru.sber.transport.request.external.model.triporder.TripOrderCreateDTO;
import ru.sber.transport.request.external.providers.exceptions.DatabaseLayerException;
import ru.sber.transport.request.external.providers.order.model.AssessmentsModel;
import ru.sber.transport.request.external.providers.order.model.DatabasePage;
import ru.sber.transport.request.external.providers.order.model.EmployeeModel;
import ru.sber.transport.request.external.providers.order.model.FraudModel;
import ru.sber.transport.request.external.providers.order.model.TripDatabaseModel;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static ru.sber.transport.database.external_request.Tables.ASSESSMENTS;
import static ru.sber.transport.database.external_request.Tables.DEPARTMENT;
import static ru.sber.transport.database.external_request.Tables.EMPLOYEE;
import static ru.sber.transport.database.external_request.Tables.FRAUD;
import static ru.sber.transport.database.external_request.Tables.ORDERED_REQUEST_WAYPOINT;
import static ru.sber.transport.database.external_request.Tables.ORGANIZATION;
import static ru.sber.transport.database.external_request.Tables.TRIP_ORDER;
import static ru.sber.transport.database.external_request.Tables.TRIP_ORDER_HISTORY;
import static ru.sber.transport.database.external_request.Tables.WAYPOINT;
import static ru.sber.transport.request.external.providers.order.DataQuery.ACTUAL_APPROVER;
import static ru.sber.transport.request.external.providers.order.DataQuery.HUMAN_READABLE_ID;
import static ru.sber.transport.request.external.providers.order.DataQuery.PASSENGER;

/**
 * Реализация провайдера заявок
 */
@Slf4j
@Transactional
@RequiredArgsConstructor
public class TripOrdersMetaDataProviderImpl implements TripOrdersProvider, TripOrdersMetaProvider, JooqRepository<TripOrder, TripOrderRecord, UUID> {

    private static final int ONE_UPDATED = 1;

    private final OrganizationsProvider organizationsProvider;

    private final EmployeesProvider employeesProvider;

    @Override
    public TripOrderData create(UUID passengerId, TripOrderCreateDTO source, PriceData price, String timeZone) {

        final var employee = employeesProvider.get(passengerId);
        final var organization = organizationsProvider.get(employee.getOrganizationId());
        final var digitId = organization.getDigitId();

        log.info("Getting next ID for organization {}", organization.getId());
        final var nextId = getNextDigitId(organization);
        log.info("Next ID for organization {} is {}", organization.getId(), nextId);

        final var data = new TripOrderRecord();
        data.setId(UUID.randomUUID());
        data.setPassengerId(passengerId);
        data.setDate(source.date());
        data.setDigitId(nextId);
        data.setPlannedCost(price.price());
        data.setPlannedDuration(DayToSecond.valueOf(price.duration()));
        data.setPurposeId(source.purposeId());
        data.setStatus(OrderState.NEW);
        data.setTariff(Tariff.valueOf(source.tariff().name()));
        data.setComment(source.comment());
        data.setPlannedDistance(price.distance());
        data.setTimeZone(timeZone);
        data.setEconomy(Optional.ofNullable(source.taxiCost())
                .map(taxiCost -> price.price().subtract(taxiCost))
                .orElse(null)
        );

        context().insertInto(table())
                .set(data)
                .execute();

        final var sourceWaypoints = source.waypoints();
        final var waypoints = new ArrayList<Map.Entry<WaypointRecord, Integer>>();
        for (var i = 0; i < sourceWaypoints.size(); i++) {
            final var waypoint = sourceWaypoints.get(i);

            final var saved = context().insertInto(WAYPOINT)
                    .set(WAYPOINT.ID, UUID.randomUUID())
                    .set(WAYPOINT.COUNTRY, waypoint.country())
                    .set(WAYPOINT.REGION, waypoint.region())
                    .set(WAYPOINT.CITY, waypoint.city())
                    .set(WAYPOINT.STREET, waypoint.street())
                    .set(WAYPOINT.HOUSE, waypoint.house())
                    .set(WAYPOINT.BUILDING, waypoint.building())
                    .set(WAYPOINT.STRUCTURE, waypoint.structure())
                    .set(WAYPOINT.LATITUDE, waypoint.latitude())
                    .set(WAYPOINT.LONGITUDE, waypoint.longitude())
                    .returning()
                    .fetchSingle();
            final var ordered = context().insertInto(ORDERED_REQUEST_WAYPOINT)
                    .set(ORDERED_REQUEST_WAYPOINT.ORDER_ID, data.getId())
                    .set(ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, saved.getId())
                    .set(ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, i)
                    .returning()
                    .fetchSingle();
            waypoints.add(new AbstractMap.SimpleEntry<>(saved, ordered.getOrderNumber()));
        }

        final var result = new TripDatabaseModel(data);
        result.humanReadableId("YA-%04d-%08d".formatted(digitId, nextId));
        result.costCenter(employee.getCostCenter());
        result.setWaypoints(waypoints);
        result.setLink(price.link());
        result.setPassenger(new EmployeeModel(employee));
        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TripOrderData> get(RequestFilter filter, Set<UUID> exclusions, int page, int size, String sort, boolean asc) {
        return executeQuery(new DataQuery(context(), filter), page, size, sort, asc, (target, source) -> {
            target.setPassenger(new EmployeeModel(source.get(table().PASSENGER_ID)));
            target.setAssessments(new AssessmentsModel(source));
            target.setCostCenter(source.getValue(PASSENGER.append(EMPLOYEE.COST_CENTER.getUnqualifiedName()), String.class));
            target.setFraud(new FraudModel(source));
        });
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TripOrderData> getFull(RequestFilter filter, int page, int size, String sort, boolean asc) {
        return executeQuery(new DataQuery(context(), filter), page, size, sort, asc, (target, source) -> {
            target.setPassenger(new EmployeeModel(PASSENGER, source));
            target.setAssessments(new AssessmentsModel(source));
            target.setCostCenter(source.getValue(PASSENGER.append(EMPLOYEE.COST_CENTER.getUnqualifiedName()), String.class));
            target.setFraud(new FraudModel(source));
        });
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TripOrderData> getRegistry(RequestFilter filter, int page, int size, String sort, boolean asc) {
        final var query = new DataQuery(context(), filter, true,  filter.isStrictlyApprover());

        return executeQuery(query, page, size, sort, asc, (target, source) -> {
            target.setPassenger(new EmployeeModel(PASSENGER, source));
            target.setApprover(new EmployeeModel(ACTUAL_APPROVER, source));
            target.setAssessments(new AssessmentsModel(source));
            target.setCostCenter(source.getValue(PASSENGER.append(EMPLOYEE.COST_CENTER.getUnqualifiedName()), String.class));
            target.setFraud(new FraudModel(source));
        });
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<TripOrderData> get(UUID id) {
        return get(null, id);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<TripOrderData> get(UUID organizationId, UUID id) {
        return context().select()
                .from(table())
                .innerJoin(EMPLOYEE).on(table().PASSENGER_ID.eq(EMPLOYEE.ID))
                .innerJoin(DEPARTMENT).on(EMPLOYEE.DEPARTMENT_ID.eq(DEPARTMENT.ID))
                .innerJoin(ORGANIZATION).on(EMPLOYEE.ORGANIZATION_ID.eq(ORGANIZATION.ID))
                .leftJoin(ASSESSMENTS).on(table().ID.eq(ASSESSMENTS.ORDER_ID))
                .leftJoin(FRAUD).on(table().ID.eq(FRAUD.ID))
                .leftJoin(TRIP_ORDER_HISTORY).on(table().ID.eq(TRIP_ORDER_HISTORY.ORDER_ID))
                .and(TRIP_ORDER_HISTORY.STATUS.eq(OrderState.CONFIRMED))
                .where(table().ID.eq(id))
                .and(organizationId == null ? DSL.trueCondition() : ORGANIZATION.ID.eq(organizationId))
                .fetchOptional(element -> {
                    final var waypointTable = WAYPOINT;
                    final var orderedRequestWaypointTable = ORDERED_REQUEST_WAYPOINT;
                    final var model = new TripDatabaseModel(element.into(TripOrderRecord.class));
                    model.humanReadableId("YA-%04d-%08d".formatted(element.get(ORGANIZATION.DIGIT_ID), element.get(table().DIGIT_ID)));
                    model.costCenter(element.get(EMPLOYEE.COST_CENTER));
                    model.setWaypoints(context().select()
                            .from(waypointTable)
                            .innerJoin(orderedRequestWaypointTable).on(waypointTable.ID.eq(orderedRequestWaypointTable.WAYPOINT_ID))
                            .where(orderedRequestWaypointTable.ORDER_ID.eq(element.get(table().ID)))
                            .orderBy(orderedRequestWaypointTable.ORDER_NUMBER.asc())
                            .fetch()
                            .map(elt -> new AbstractMap.SimpleEntry<>(elt.into(WaypointRecord.class), elt.get(orderedRequestWaypointTable.ORDER_NUMBER, Integer.class))));
                    model.setPassenger(new EmployeeModel(element.into(EmployeeRecord.class)));
                    model.setApprover(Optional.ofNullable(element.get(table().APPROVER_ID)).map(EmployeeModel::new).orElse(null));
                    model.setApprovalDate(element.get(TRIP_ORDER_HISTORY.MODIFIED_AT));
                    model.setAssessments(new AssessmentsModel(element));
                    model.setFraud(new FraudModel(element.into(FraudRecord.class)));
                    model.setReceiptLink(element.get(table().RECEIPT_LINK));
                    return model;
                });
    }

    @Transactional
    @Override
    public void delete(UUID organizationId, UUID id) {
        context().update(table())
                .set(table().STATUS, OrderState.CANCELLED)
                .where(table().ID.in(context().select(table().ID).from(table()).innerJoin(EMPLOYEE).on(table().PASSENGER_ID.eq(EMPLOYEE.ID)).and(EMPLOYEE.ORGANIZATION_ID.eq(organizationId))))
                .execute();
    }

    @Transactional
    @Override
    public void edit(UUID userId, UUID id, EditTripOrderData newData, Set<String> editedFields) {
        final var old = context().selectFrom(table()).where(table().ID.eq(id)).fetchSingle();
        AssessmentsRecord assessments = null;
        for (var field : editedFields) {
            final var sign = field.substring(0, 1);
            final var fieldName = field.substring(1).split("\\.");
            switch (fieldName[0]) {
                case "status" -> {
                    if (!"-".equals(sign)) {
                        old.setStatus(OrderState.valueOf(newData.getStatus().name()));
                    }
                    if (List.of(OrderState.CONFIRMED, OrderState.DECLINED, OrderState.DATA_NEEDED).contains(old.getStatus())) {
                        old.setApproverId(userId);
                    }
                }
                case "factCost" -> {
                    final var newCost = newData.getActual().getCost();
                    final var oldCost = Optional.ofNullable(old.getActualCost()).orElse(BigDecimal.ZERO);
                    old.setActualCost(switch (sign) {
                        case "-" -> oldCost.subtract(newCost);
                        case "+" -> oldCost.add(newCost);
                        default -> newCost;
                    });
                }
                case "reason" -> {
                    final var newReason = newData.getReason();
                    final var oldReason = old.getReason();
                    old.setReason(switch (sign) {
                        case "-" -> oldReason.replace(newReason, "");
                        case "+" -> oldReason + newReason;
                        default -> newReason;
                    });
                }
                case "assessments" ->
                        assessments = createAssessment(assessments, old, userId, newData.getAssessments(), sign, fieldName);

                case "receiptLink" -> {
                    if (!"-".equals(sign)) {
                        old.setReceiptLink(newData.getReceiptLink());
                    }
                }
                default -> {
                    // no need for reaction
                }
            }
        }
        if (assessments != null) {
            context().insertInto(ASSESSMENTS)
                    .set(assessments)
                    .execute();
        }
        old.setModifiedAt(OffsetDateTime.now(ZoneOffset.UTC));
        context().update(table())
                .set(old)
                .where(table().ID.eq(id))
                .execute();
    }

    @Override
    public boolean exists(UUID organizationId, UUID requestId) {
        return context().fetchExists(
                context().select().from(table())
                        .innerJoin(EMPLOYEE).on(table().PASSENGER_ID.eq(EMPLOYEE.ID))
                        .where(table().ID.eq(requestId))
                        .and(organizationId == null ? DSL.trueCondition() : EMPLOYEE.ORGANIZATION_ID.eq(organizationId)));
    }

    @Override
    public boolean exists(UUID requestId) {
        return context().fetchExists(
                context().select().from(table()
                        .where(table().ID.eq(requestId))));
    }

    @Override
    public void attachFile(UUID requestId, String fileName) throws DatabaseLayerException {
        try {
            int updated = context().update(table())
                    .set(table().RECEIPT, fileName)
                    .where(table().ID.eq(requestId))
                    .execute();
            if (ONE_UPDATED != updated) {
                throw new DatabaseLayerException(format("Отсутствует заявка [%s] для прикрепления чека", requestId));
            }
        } catch (DataAccessException ex) {
            log.warn("Информация о чеке не была прикреплена к заявке [{}] в слое данных: ", requestId, ex);
            throw new DatabaseLayerException(ex);
        }
    }

    @Override
    public String getFile(UUID requestId) {
        return context().select(table().RECEIPT)
                .from(table())
                .where(table().ID.eq(requestId))
                .fetchOptional(table().RECEIPT)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    @Override
    public TripOrderData getFileDetailed(UUID requestId) throws DatabaseLayerException {
        try {
            return context().select(TRIP_ORDER.ID, TRIP_ORDER.DATE, TRIP_ORDER.ACTUAL_COST, TRIP_ORDER.RECEIPT)
                    .from(TRIP_ORDER)
                    .where(TRIP_ORDER.ID.eq(requestId))
                    .fetchOptional(order -> {
                        final var model = new TripDatabaseModel(order.into(TripOrderRecord.class));
                        model.setId(order.get(TRIP_ORDER.ID));
                        model.setDate(order.get(TRIP_ORDER.DATE));
                        model.setActualCost(order.get(TRIP_ORDER.ACTUAL_COST));
                        model.setReceipt(order.get(TRIP_ORDER.RECEIPT));
                        return model;
                    })
                    .orElseThrow(
                            () -> new DatabaseLayerException(format("Отсутствуют данные о чеке по заявке [%s] в слое данных", requestId))
                    );
        } catch (DataAccessException ex) {
            log.warn("Не удалось получить детальную информацию о чеке по заявке [{}] из слоя данных: ", requestId, ex);
            throw new DatabaseLayerException(ex);
        }
    }

    @Override
    public void detachFile(UUID requestId) {
        context().update(table())
                .setNull(table().RECEIPT)
                .where(table().ID.eq(requestId))
                .execute();
    }

    @Override
    public TripOrder table() {
        return TRIP_ORDER;
    }

    @Override
    public Modifiable meta(UUID organizationId, UUID requestId) {
        return context().select(table().HASH, table().MODIFIED_AT)
                .from(table())
                .innerJoin(EMPLOYEE).on(table().PASSENGER_ID.eq(EMPLOYEE.ID))
                .where(table().ID.eq(requestId))
                .and(organizationId == null ? DSL.trueCondition() : EMPLOYEE.ORGANIZATION_ID.eq(organizationId))
                .fetchOptional(new RecordMapper<Record, Modifiable>() {

                    @NotNull
                    @Override
                    public Modifiable map(Record item) {
                        return new Modifiable() {
                            @Override
                            public String hash() {
                                return item.get(table().HASH);
                            }

                            @Override
                            public OffsetDateTime modifiedAt() {
                                return item.get(table().MODIFIED_AT);
                            }
                        };
                    }
                }).orElseThrow(() -> new EntityNotFoundException(TripOrderData.class, requestId));
    }

    private Map<UUID, List<Map.Entry<WaypointRecord, Integer>>> getWaypoints(List<TripDatabaseModel> content) {
        return context().select()
                .from(WAYPOINT)
                .innerJoin(ORDERED_REQUEST_WAYPOINT).on(WAYPOINT.ID.eq(ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID))
                .where(ORDERED_REQUEST_WAYPOINT.ORDER_ID.in(content.stream().map(TripOrderData::getId).toList()))
                .fetch((RecordMapper<Record, Map.Entry<UUID, Map.Entry<WaypointRecord, Integer>>>) item -> {
                    final var id = item.get(ORDERED_REQUEST_WAYPOINT.ORDER_ID, UUID.class);
                    return new AbstractMap.SimpleEntry<>(id, new AbstractMap.SimpleEntry<>(item.into(WaypointRecord.class), item.get(ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, Integer.class)));
                })
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, it -> List.of(it.getValue()), (l, r) -> Stream.concat(l.stream(), r.stream()).toList()));
    }

    private Integer getNextDigitId(Organization organization) {
        return context().transactionResult(configuration -> {
            DSL.using(configuration)
                    .selectFrom(table())
                    .forUpdate()
                    .fetch();

            return DSL.using(configuration)
                    .select(DSL.count(table().ID).add(1))
                    .from(table())
                    .innerJoin(EMPLOYEE).on(EMPLOYEE.ID.eq(table().PASSENGER_ID))
                    .where(EMPLOYEE.ORGANIZATION_ID.eq(organization.getId()))
                    .fetchOptionalInto(Integer.class)
                    .orElse(1);
        });
    }

    @NotNull
    private Page<TripOrderData> executeQuery(DataQuery query, int page, int size, String sort, boolean asc, BiConsumer<TripDatabaseModel, Record> mapper) {
        final var content = size > 0 ? requestContent(query, page, size, sort, asc, mapper) : List.<TripDatabaseModel>of();

        final var contentMap = content.stream().collect(Collectors.toMap(TripOrderData::getId, Function.identity()));
        final var waypoints = getWaypoints(content);
        contentMap.entrySet().parallelStream().forEach(it -> it.getValue().setWaypoints(waypoints.getOrDefault(it.getKey(), List.of())));
        final int total = Objects.requireNonNull(query.getCountQuery().fetchOne()).get("trip_order_count", Integer.class);

        return new DatabasePage(content.stream().map(TripOrderData.class::cast).toList(), page, size, total, asc, sort);
    }

    @NotNull
    private List<TripDatabaseModel> requestContent(DataQuery query, int page, int size, String sort, boolean asc, BiConsumer<TripDatabaseModel, Record> mapper) {
        return wrap(query.getDataQuery(), PageRequest.of(page, size, asc ? Sort.Direction.ASC : Sort.Direction.DESC, sort))
                .fetch()
                .map(it -> new AbstractMap.SimpleEntry<>(it.get(HUMAN_READABLE_ID), it))
                .stream()
                .map(it -> {
                    final var result = new TripDatabaseModel(it.getValue().into(TripOrderRecord.class));
                    result.humanReadableId(it.getKey());
                    mapper.accept(result, it.getValue());
                    return result;
                })
                .filter(distinctByKey(TripOrderData::getId))
                .toList();
    }

    private <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        var seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private AssessmentsRecord createAssessment(AssessmentsRecord target, TripOrderRecord old, UUID userId, Assessments assessments, String sign, String[] fieldName) {
        if (target == null) {
            target = new AssessmentsRecord();
            target.setId(UUID.randomUUID());
            target.setOrderId(old.getId());
            target.setEmployeeId(userId);
            target.setCreatedAt(OffsetDateTime.now());
        }
        if (!"-".equals(sign) && fieldName[1].equals("service")) {
            target.setType(AssessmentType.SERVICE);
            if (fieldName[2].equals("comment")) {
                target.setComment(assessments.getService().getComment());
            }
            if (fieldName[2].equals("rating")) {
                target.setRating((short) assessments.getService().getRating());
            }
        }
        return target;
    }

}
