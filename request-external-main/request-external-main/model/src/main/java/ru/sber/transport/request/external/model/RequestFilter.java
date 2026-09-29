package ru.sber.transport.request.external.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Фильтр заявок на поездку
 */
public interface RequestFilter {

    /**
     * Идентификатор организации
     *
     * @return идентификатор организации
     */
    UUID organizationId();

    /**
     * Имя согласующего
     *
     * @return имя согласующего
     */
    String approverName();

    /**
     * Имя пассажира
     *
     * @return имя пассажира
     */
    String passengerName();

    /**
     * Список идентификаторов пассажиров
     *
     * @return список идентификаторов пассажиров
     */
    List<UUID> passenger();

    /**
     * Список идентификаторов согласующих
     *
     * @return список идентификаторов согласующих
     */
    List<UUID> approver();

    /**
     * Флаг для отсечения потенциальных утверждающих.
     *
     * @return true если требуется искать только по полю согласующих.
     */
    Boolean isStrictlyApprover();

    /**
     * Дата начала заявки - от
     *
     * @return дата начала заявки - от
     */
    OffsetDateTime startTimeFrom();

    /**
     * Дата начала заявки - до
     *
     * @return дата начала заявки - до
     */
    OffsetDateTime startTimeTo();

    /**
     * Список статусов заявок
     *
     * @return список статусов заявок
     */
    List<State> states();

    /**
     * Фильтр заявки по ее человекопонятному идентификатору
     *
     * @return идентификатор заявки
     */
    String humanReadableId();

    /**
     * Фильтр заявки по БЕ (БЕ - Бизнес Единица)
     *
     * @return список БЕ
     */
    List<Integer> balanceUnitSet();

    /**
     * Фильтр заявки по МВЗ
     *
     * @return МВЗ
     */
    String costCenter();

    /**
     * Диапазон дат формирования приказов
     *
     * @return диапазон дат формирования приказов
     */
    Pair<OffsetDateTime, OffsetDateTime> orderPaymentFormationStartRange();

    /**
     * Список отделов
     *
     * @return список отделов
     */
    Set<UUID> departments();
}
