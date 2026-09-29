package ru.sber.transport.request.external.providers.order;

import static ru.sber.transport.database.external_request.Tables.ASSESSMENTS;
import static ru.sber.transport.database.external_request.Tables.DELEGATES;
import static ru.sber.transport.database.external_request.Tables.DEPARTMENT;
import static ru.sber.transport.database.external_request.Tables.EMPLOYEE;
import static ru.sber.transport.database.external_request.Tables.FRAUD;
import static ru.sber.transport.database.external_request.Tables.ORGANIZATION;
import static ru.sber.transport.database.external_request.Tables.TRIP_ORDER;
import static ru.sber.transport.database.external_request.Tables.TRIP_ORDER_HISTORY;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.SelectField;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectOnConditionStep;
import org.jooq.SelectSelectStep;
import org.jooq.impl.DSL;
import ru.sber.database.fields.SearchField;
import ru.sber.database.types.TsVector;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.database.external_request.enums.OrderState;
import ru.sber.transport.database.external_request.tables.TripOrder;
import ru.sber.transport.request.external.model.RequestFilter;

/**
 * Формирование запроса к БД
 */
class DataQuery {

    /**
     * Поле с человеко-читаемым ID заказа
     */
    public static final Field<String> HUMAN_READABLE_ID = DSL.field(DSL.name("human_readable_id"), String.class);

    /**
     * Связь с сотрудником (пассажиром)
     */
    public static final Name PASSENGER = DSL.name("passenger");

    /**
     * МВЗ
     */
    public static final Field<String> COST_CENTER = DSL.field(
        PASSENGER.append(EMPLOYEE.COST_CENTER.getUnqualifiedName()), String.class);

    /**
     * Связь с сотрудником (согласующим)
     */
    public static final Name ACTUAL_APPROVER = DSL.name("actual_approver");

    /**
     * Связь с потенциальным согласующим
     */
    public static final Name POTENTIAL_APPROVER = DSL.name("potential_approver");

    /**
     * Связь с делегатом потенциального согласующего
     */
    public static final Name POTENTIAL_APPROVER_DELEGATE = DSL.name("potential_approver_delegate");

    private static final TripOrder table = Tables.TRIP_ORDER;

    private final DSLContext context;

    private final RequestFilter filter;

    private final boolean joinApprover;

    private final boolean isStrictlyApprover;

    private final List<UUID> exclusions;

    /**
     * Создание запроса к БД
     *
     * @param context    сессия
     * @param filter     фильтр
     * @param exclusions исключения
     */
    DataQuery(DSLContext context, RequestFilter filter, UUID... exclusions) {
        this(context, filter, null, null, exclusions);
    }

    /**
     * Создание запроса к БД
     *
     * @param context      сессия
     * @param filter       фильтр
     * @param exclusions   исключения
     * @param joinApprover флаг принудительного джойна согласовантов
     */
    DataQuery(DSLContext context, RequestFilter filter, Boolean joinApprover, Boolean isStrictlyApprover,
        UUID... exclusions) {
        this.context = context;
        this.filter = filter;
        this.joinApprover = Optional.ofNullable(joinApprover)
            .orElseGet(() -> {
                final var approver = filter.approver();
                return CollectionUtils.isNotEmpty(approver) || StringUtils.isNoneBlank(filter.approverName());
            });
        this.isStrictlyApprover = Optional.ofNullable(isStrictlyApprover).orElseGet(() ->
            Optional.ofNullable(filter.isStrictlyApprover()).orElse(false));
        this.exclusions = List.of(exclusions);
    }

    /**
     * Получение запроса на данные
     *
     * @return запрос на данные
     */
    SelectConditionStep<Record> getDataQuery() {
        final var baseDataQuery = getBaseDataQuery(joinApprover, isStrictlyApprover);
        final var query = getQuery(baseDataQuery);
        return appendFilters(query, filter);
    }

    /**
     * Получение запроса на количество
     *
     * @return запрос на количество
     */
    SelectConditionStep<Record> getCountQuery() {
        return appendFilters(getQuery(getBaseCountQuery(joinApprover, isStrictlyApprover)), filter);
    }

    private SelectConditionStep<Record> getQuery(SelectOnConditionStep<Record> baseQuery) {
        return exclusions.isEmpty() ? baseQuery.where(DSL.trueCondition())
            : baseQuery.where(table.ID.notIn(exclusions));
    }

    @NotNull
    private SelectOnConditionStep<Record> getBaseDataQuery(boolean joinApprover, boolean isStrictlyApprover) {
        final var fields = new ArrayList<SelectFieldOrAsterisk>();

        fields.add(TRIP_ORDER.ID);
        fields.add(TRIP_ORDER.DATE);
        fields.add(TRIP_ORDER.DIGIT_ID);
        fields.add(TRIP_ORDER.STATUS);
        fields.add(TRIP_ORDER.TARIFF);
        fields.add(TRIP_ORDER.PURPOSE_ID);
        fields.add(TRIP_ORDER.ACTUAL_COST);
        fields.add(TRIP_ORDER.PLANNED_COST);
        fields.add(TRIP_ORDER.COMMENT);
        fields.add(TRIP_ORDER.PLANNED_DURATION);
        fields.add(TRIP_ORDER.PLANNED_DISTANCE);
        fields.add(TRIP_ORDER.REASON);
        fields.add(TRIP_ORDER.PASSENGER_ID);
        fields.add(TRIP_ORDER.RECEIPT);
        fields.add(TRIP_ORDER.TIME_ZONE);
        fields.add(DSL.field(PASSENGER.append(EMPLOYEE.ID.getUnqualifiedName())));
        fields.add(DSL.field(PASSENGER.append(EMPLOYEE.LAST_NAME.getUnqualifiedName())));
        fields.add(DSL.field(PASSENGER.append(EMPLOYEE.FIRST_NAME.getUnqualifiedName())));
        fields.add(DSL.field(PASSENGER.append(EMPLOYEE.PATRONYMIC.getUnqualifiedName())));
        fields.add(DSL.field(PASSENGER.append(EMPLOYEE.COST_CENTER.getUnqualifiedName())));
        fields.add(DSL.field(PASSENGER.append(EMPLOYEE.PERSONNEL_NUMBER.getUnqualifiedName())));
        if (joinApprover) {
            fields.add(DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.ID.getUnqualifiedName())));
            fields.add(DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.LAST_NAME.getUnqualifiedName())));
            fields.add(DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.FIRST_NAME.getUnqualifiedName())));
            fields.add(DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.PATRONYMIC.getUnqualifiedName())));
            fields.add(DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.COST_CENTER.getUnqualifiedName())));
            fields.add(DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.PERSONNEL_NUMBER.getUnqualifiedName())));
        }
        fields.add(ASSESSMENTS.ID);
        fields.add(ASSESSMENTS.ORDER_ID);
        fields.add(ASSESSMENTS.TYPE);
        fields.add(ASSESSMENTS.COMMENT);
        fields.add(ASSESSMENTS.RATING);
        fields.add(FRAUD.COMMENT);
        fields.add(
            DSL.concat(DSL.value("YA-"), DSL.lpad(ORGANIZATION.DIGIT_ID.cast(String.class), 4, "0"), DSL.val("-"),
                DSL.lpad(table.DIGIT_ID.cast(String.class), 8, "0")).as(HUMAN_READABLE_ID));
        fields.add(TRIP_ORDER.ECONOMY);

        return joins(joinApprover, isStrictlyApprover, context.selectDistinct(fields));
    }

    @NotNull
    private SelectOnConditionStep<Record> getBaseCountQuery(boolean joinApprover, boolean isStrictlyApprover) {
        final var countFields = new ArrayList<SelectField<?>>();

        countFields.add(DSL.countDistinct(TRIP_ORDER.ID).as("trip_order_count"));

        return joins(joinApprover, isStrictlyApprover, context.select(countFields));
    }

    @NotNull
    private SelectOnConditionStep<Record> joins(boolean joinApprover, boolean strictlyApprover,
        SelectSelectStep<Record> query) {
        var fromQuery = query.from(table)
            .innerJoin(EMPLOYEE.as(PASSENGER))
            .on(table.PASSENGER_ID.eq(DSL.field(PASSENGER.append(EMPLOYEE.ID.getUnqualifiedName()), UUID.class)))
            .innerJoin(ORGANIZATION)
            .on(DSL.field(PASSENGER.append(EMPLOYEE.ORGANIZATION_ID.getUnqualifiedName()), UUID.class)
                .eq(ORGANIZATION.ID))
            .leftJoin(ASSESSMENTS).on(table.ID.eq(ASSESSMENTS.ORDER_ID))
            .leftJoin(FRAUD).on(table.ID.eq(FRAUD.ID))
            .innerJoin(TRIP_ORDER_HISTORY).on(table.ID.eq(TRIP_ORDER_HISTORY.ORDER_ID));

        if (joinApprover) {
            final var now = LocalDate.now();
            fromQuery = fromQuery
                .leftJoin(EMPLOYEE.as(ACTUAL_APPROVER)).on(DSL.field(TRIP_ORDER.APPROVER_ID.eq(
                    DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.ID.getUnqualifiedName()), UUID.class))));
            if (!strictlyApprover) {
                fromQuery = fromQuery
                    .innerJoin(DEPARTMENT).on(DEPARTMENT.ID.eq(
                        DSL.field(PASSENGER.append(EMPLOYEE.DEPARTMENT_ID.getUnqualifiedName()), UUID.class)))
                    .innerJoin(EMPLOYEE.as(POTENTIAL_APPROVER))
                    .on(DSL.field(POTENTIAL_APPROVER.append(EMPLOYEE.ID.getUnqualifiedName()))
                        .eq(DEPARTMENT.HEAD_ID))
                    .leftJoin(DELEGATES).on(DELEGATES.SUPERVISOR_ID.eq(
                            DSL.field(POTENTIAL_APPROVER.append(EMPLOYEE.ID.getUnqualifiedName()), UUID.class))
                        .and(DELEGATES.START_DATE.le(now)).and(DELEGATES.END_DATE.ge(now)))
                    .leftJoin(EMPLOYEE.as(POTENTIAL_APPROVER_DELEGATE)).on(DELEGATES.DELEGATE_ID.eq(
                        DSL.field(POTENTIAL_APPROVER_DELEGATE.append(EMPLOYEE.ID.getUnqualifiedName()),
                            UUID.class)));
            }
        }

        return fromQuery;
    }

    private <S extends Record> SelectConditionStep<S> appendFilters(SelectConditionStep<S> wheredQuery,
        RequestFilter filter) {
        final var states = filter.states();
        if (CollectionUtils.isNotEmpty(states)) {
            wheredQuery = wheredQuery.and(
                table.STATUS.in(states.parallelStream().map(Enum::name).map(OrderState::valueOf).toList()));
        }
        final var passengerIds = filter.passenger();
        if (CollectionUtils.isNotEmpty(passengerIds)) {
            wheredQuery = wheredQuery.and(table.PASSENGER_ID.in(passengerIds));
        }

        wheredQuery = wheredQuery.and(Optional.ofNullable(filter.organizationId())
            .map(ORGANIZATION.ID::eq).orElse(DSL.noCondition()));

        final var passengerName = filter.passengerName();
        if (StringUtils.isNoneBlank(passengerName)) {
            wheredQuery = wheredQuery.and(new SearchField(
                DSL.field(PASSENGER.append(EMPLOYEE.DOCUMENT.getUnqualifiedName()), TsVector.class)).match(
                passengerName));
        }

        final var approverIds = filter.approver();
        if (CollectionUtils.isNotEmpty(approverIds)) {
            if (isStrictlyApprover) {
                wheredQuery = wheredQuery.and(TRIP_ORDER.APPROVER_ID.in(approverIds));
            } else {
                wheredQuery = wheredQuery.and(DSL.or(
                    TRIP_ORDER.APPROVER_ID.in(approverIds),
                    DSL.field(POTENTIAL_APPROVER.append(EMPLOYEE.ID.getUnqualifiedName()), UUID.class)
                        .in(approverIds),
                    DSL.field(POTENTIAL_APPROVER_DELEGATE.append(EMPLOYEE.ID.getUnqualifiedName()), UUID.class)
                        .in(approverIds)
                ));
            }
        }

        final var approverName = filter.approverName();
        if (StringUtils.isNoneBlank(approverName)) {
            if (isStrictlyApprover) {
                wheredQuery = wheredQuery.and(
                    new SearchField(
                        DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.DOCUMENT.getUnqualifiedName()),
                            TsVector.class)).match(
                        approverName));
            } else {
                wheredQuery = wheredQuery.and(DSL.or(
                    new SearchField(
                        DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.DOCUMENT.getUnqualifiedName()),
                            TsVector.class)).match(
                        approverName),
                    new SearchField(
                        DSL.field(POTENTIAL_APPROVER.append(EMPLOYEE.DOCUMENT.getUnqualifiedName()),
                            TsVector.class)).match(
                        approverName),
                    new SearchField(
                        DSL.field(POTENTIAL_APPROVER_DELEGATE.append(EMPLOYEE.DOCUMENT.getUnqualifiedName()),
                            TsVector.class)).match(approverName)
                ));
            }
        }

        wheredQuery = wheredQuery.and(Optional.ofNullable(filter.startTimeFrom())
            .map(table.DATE::ge).orElse(DSL.noCondition()));

        wheredQuery = wheredQuery.and(Optional.ofNullable(filter.startTimeTo())
            .map(table.DATE::le).orElse(DSL.noCondition()));

        final var humanReadableId = filter.humanReadableId();
        if (StringUtils.isNoneBlank(humanReadableId)) {
            wheredQuery = wheredQuery.and(DSL.concat(DSL.lpad(ORGANIZATION.DIGIT_ID.cast(String.class), 4, "0"),
                    DSL.lpad(table.DIGIT_ID.cast(String.class), 8, "0"))
                .likeIgnoreCase("%%%s%%".formatted(humanReadableId.replace("YA-", "").replace("-", ""))));
        }

        final var balanceUnitSet = filter.balanceUnitSet();
        if (CollectionUtils.isNotEmpty(balanceUnitSet)) {
            final var balanceUnitStrings = balanceUnitSet.stream()
                .map(value -> String.format("%04d", value))
                .toList();
            wheredQuery = wheredQuery.and(DSL.left(COST_CENTER, 4).in(balanceUnitStrings));
        }

        final var costCenter = filter.costCenter();
        if (StringUtils.isNoneBlank(costCenter)) {
            wheredQuery = wheredQuery.and(COST_CENTER.eq(costCenter));
        }

        wheredQuery = wheredQuery.and(Optional.ofNullable(filter.orderPaymentFormationStartRange())
            .map(range -> TRIP_ORDER_HISTORY.MODIFIED_AT.between(filter.orderPaymentFormationStartRange().getLeft(),
                filter.orderPaymentFormationStartRange().getRight()))
            .orElse(DSL.noCondition()));

        wheredQuery = wheredQuery.and(Optional.ofNullable(filter.orderPaymentFormationStartRange())
            .map(range -> TRIP_ORDER_HISTORY.STATUS.eq(OrderState.ORDER_PAYMENT_FORMATION))
            .orElse(DSL.noCondition()));

        final var departmentsIds = filter.departments();
        if (CollectionUtils.isNotEmpty(departmentsIds)) {
            wheredQuery = wheredQuery.and(DEPARTMENT.ID.in(departmentsIds));
        }

        return wheredQuery;
    }

}