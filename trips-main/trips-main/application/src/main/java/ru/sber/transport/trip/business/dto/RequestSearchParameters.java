package ru.sber.transport.trip.business.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jooq.TableField;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.TripsRecord;
import ru.sberbank.ditsib.request.SortField;

/**
 * Параметры поиска поездок.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum RequestSearchParameters implements SortField {

    HUMAN_READABLE_ID("humanReadableId", "digitId", Tables.TRIPS_.DIGIT_ID),

    REQUEST_HUMAN_READABLE_ID("requestHumanReadableId", "request.humanReadableId", Tables.TRIPS_.REQUESTS),

    EXPECTED_START_TIME("expectedStartTime", "expectedStartTime", Tables.TRIPS_.EXPECTED_START_TIME),

    FACT_START_TIME("factStartTime", "factStartTime", Tables.TRIPS_.FACT_START_TIME),

    DESIRE_DATE_START("desireDateStart", null, null),

    DESIRE_DATE_END("desireDateEnd", null, null),

    DRIVER_IDS("driverIds", null, null),

    EXPECTED_TIME("expectedTime", null, null),

    AUTOPARK_ID("autoparkId", null, null),

    TAXI_CLASS("taxiClass", null, Tables.TRIPS_.TAXI_CLASS);

    private final String name;

    private final String sortFieldName;

    private final TableField<TripsRecord, ?> sortField;
}
