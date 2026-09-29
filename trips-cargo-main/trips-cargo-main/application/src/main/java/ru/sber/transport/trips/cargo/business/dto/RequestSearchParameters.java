package ru.sber.transport.trips.cargo.business.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jooq.TableField;
import ru.sber.transport.trips_cargo.database.trips_cargo.Tables;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.TripsRecord;
import ru.sberbank.ditsib.request.SortField;

/**
 * Параметры поиска поездок.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum RequestSearchParameters implements SortField {

    HUMAN_READABLE_ID("humanReadableId", "digitId", Tables.TRIPS.DIGIT_ID),

    REQUEST_HUMAN_READABLE_ID("requestHumanReadableId", "request.humanReadableId", Tables.TRIPS.REQUESTS),

    START_TIME("startTime", "startTime", Tables.TRIPS.START_TIME),

    DESIRE_DATE_START("desireDateStart", null, null),

    DESIRE_DATE_END("desireDateEnd", null, null),

    DRIVER_IDS("driverIds", null, null),

    AUTOPARK_ID("autoparkId", null, Tables.TRIPS.AUTOPARK_ID);

    private final String name;

    private final String sortFieldName;

    private final TableField<TripsRecord, ?> sortField;
}
