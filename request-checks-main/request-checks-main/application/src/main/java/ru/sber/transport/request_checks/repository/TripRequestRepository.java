package ru.sber.transport.request_checks.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.request_checks.entity.TripRequestEntity;
import ru.sber.transport.request_checks.util.ExcludedRequestStatuses;
import ru.sber.transport.request_checks.util.TimeUtils;

@Repository
@RequiredArgsConstructor
public class TripRequestRepository {

    private static final String FIND_BY_ID_SQL = """
        SELECT id, human_readable_id, transport_type, passenger_id, desired_date, time_zone, distance, duration, status
        FROM request_checks.trip_request
        WHERE id = :id
        """;

    private static final String UPSERT_SQL = """
        INSERT INTO request_checks.trip_request
        (id, human_readable_id, transport_type, passenger_id, desired_date, time_zone, distance, duration, status)
        VALUES (:id, :humanReadableId, :transportType, :passengerId, :desiredDate, :timeZone, :distance, :duration, :status)
        ON CONFLICT (id) DO UPDATE SET
            human_readable_id = EXCLUDED.human_readable_id,
            transport_type = EXCLUDED.transport_type,
            passenger_id = EXCLUDED.passenger_id,
            desired_date = EXCLUDED.desired_date,
            time_zone = EXCLUDED.time_zone,
            distance = EXCLUDED.distance,
            duration = EXCLUDED.duration,
            status = EXCLUDED.status
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String COUNT_MULTIPOINT_REQUESTS_SQL = """
        SELECT COUNT(*)
        FROM request_checks.trip_request tr
        WHERE tr.passenger_id = :passengerId
          AND tr.desired_date >= :startOfDay
          AND tr.desired_date < :startOfNextDay
          AND tr.status IS NOT NULL
          AND tr.status NOT IN (:excludedStatuses)
          AND (
              SELECT COUNT(*)
              FROM request_checks.waypoint w
              WHERE w.trip_request_id = tr.id
          ) > 2
        """;

    private static final String SUM_DURATION_BY_DATE_SQL = """
        SELECT COALESCE(SUM(tr.duration), 0)
        FROM request_checks.trip_request tr
        WHERE tr.passenger_id = :passengerId
          AND tr.desired_date >= :startOfDay
          AND tr.desired_date < :startOfNextDay
          AND tr.status IS NOT NULL
          AND tr.status NOT IN (:excludedStatuses)
        """;

    private static final String SUM_DISTANCE_BY_PASSENGER_AND_MONTH_SQL = """
        SELECT COALESCE(SUM(tr.distance), 0)
        FROM request_checks.trip_request tr
        WHERE tr.passenger_id = :passengerId
          AND tr.desired_date >= :start
          AND tr.desired_date < :end
          AND tr.status NOT IN (:excludedStatuses)
        """;

    private final RowMapper<TripRequestEntity> rowMapper = (rs, rowNum) ->
        new TripRequestEntity(
            rs.getObject("id", UUID.class),
            rs.getString("human_readable_id"),
            rs.getString("transport_type"),
            rs.getObject("passenger_id", UUID.class),
            rs.getObject("desired_date", OffsetDateTime.class),
            rs.getString("time_zone"),
            rs.getObject("distance", Integer.class),
            rs.getObject("duration", Long.class),
            rs.getString("status")
        );

    @Transactional(readOnly = true)
    public Optional<TripRequestEntity> findById(UUID id) {
        try {
            val entity = jdbcTemplate.queryForObject(
                FIND_BY_ID_SQL,
                Map.of("id", id),
                rowMapper
            );
            return Optional.ofNullable(entity);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public void save(TripRequestEntity entity) {
        val params = new BeanPropertySqlParameterSource(entity);
        jdbcTemplate.update(UPSERT_SQL, params);
    }

    @Transactional(readOnly = true)
    public long countMultipointRequests(UUID passengerId,
        LocalDateTime desiredDate,
        String timeZone) {
        val zoneId = TimeUtils.parseZoneId(timeZone);
        val dateInZone = desiredDate.atOffset(ZoneOffset.UTC).atZoneSameInstant(zoneId).toLocalDate();
        val startOfDay = dateInZone.atStartOfDay(zoneId).toOffsetDateTime();
        val startOfNextDay = startOfDay.plusDays(1);

        val params = new MapSqlParameterSource("passengerId", passengerId)
            .addValue("startOfDay", startOfDay)
            .addValue("startOfNextDay", startOfNextDay)
            .addValue("excludedStatuses", ExcludedRequestStatuses.EXCLUDED_STATUSES);

        val multipointRequestsCount = jdbcTemplate.queryForObject(COUNT_MULTIPOINT_REQUESTS_SQL, params, Long.class);
        return multipointRequestsCount != null ? multipointRequestsCount : 0L;
    }

    @Transactional(readOnly = true)
    public long sumDurationByDate(UUID passengerId,
                                  OffsetDateTime desiredDate,
                                  String timeZone,
                                  int dayStartHour,
                                  String dayStartTz) {

        ZoneId dayStartZoneId = TimeUtils.parseZoneId(dayStartTz);
        ZoneId dataZoneId = TimeUtils.parseZoneId(timeZone);

        ZonedDateTime desiredInDayStartTz = desiredDate.atZoneSameInstant(dayStartZoneId);
        LocalDate dateInDayStartTz = desiredInDayStartTz.toLocalDate();

        ZonedDateTime dayStartZdt = dateInDayStartTz.atTime(dayStartHour, 0).atZone(dayStartZoneId);
        if (desiredInDayStartTz.isBefore(dayStartZdt)) {
            dayStartZdt = dayStartZdt.minusDays(1);
        }
        ZonedDateTime dayEndZdt = dayStartZdt.plusDays(1);

        OffsetDateTime startOfDayInDataTz = dayStartZdt.withZoneSameInstant(dataZoneId).toOffsetDateTime();
        OffsetDateTime startOfNextDayInDataTz = dayEndZdt.withZoneSameInstant(dataZoneId).toOffsetDateTime();

        val params = new MapSqlParameterSource("passengerId", passengerId)
                .addValue("startOfDay", startOfDayInDataTz)
                .addValue("startOfNextDay", startOfNextDayInDataTz)
                .addValue("excludedStatuses", ExcludedRequestStatuses.EXCLUDED_STATUSES);

        Long totalSeconds = jdbcTemplate.queryForObject(SUM_DURATION_BY_DATE_SQL, params, Long.class);
        return totalSeconds != null ? totalSeconds : 0L;
    }

    public int sumDistanceByPassengerAndDesiredTime(UUID passengerId,
                                                       OffsetDateTime start,
                                                       OffsetDateTime end) {
        var params = new MapSqlParameterSource()
                .addValue("passengerId", passengerId)
                .addValue("start", start)
                .addValue("end", end)
                .addValue("excludedStatuses", ExcludedRequestStatuses.EXCLUDED_STATUSES);

        Integer sum = jdbcTemplate.queryForObject(SUM_DISTANCE_BY_PASSENGER_AND_MONTH_SQL, params, Integer.class);
        return sum != null ? sum : 0;
    }
}
