package ru.sber.transport.request_checks.repository;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.request_checks.entity.WaypointEntity;

@Repository
@RequiredArgsConstructor
public class WaypointRepository {

    private static final String FIND_BY_TRIP_REQUEST_ID_SQL = """
        SELECT id, trip_request_id, ordering_index, latitude, longitude
        FROM request_checks.waypoint
        WHERE trip_request_id = :tripRequestId
        ORDER BY ordering_index
        """;

    private static final String UPSERT_SQL = """
        INSERT INTO request_checks.waypoint
        (id, trip_request_id, ordering_index, latitude, longitude)
        VALUES (:id, :tripRequestId, :orderingIndex, :latitude, :longitude)
        ON CONFLICT (id) DO UPDATE SET
            trip_request_id = EXCLUDED.trip_request_id,
            ordering_index = EXCLUDED.ordering_index,
            latitude = EXCLUDED.latitude,
            longitude = EXCLUDED.longitude
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<WaypointEntity> rowMapper = (rs, rowNum) ->
        new WaypointEntity(
            rs.getObject("id", UUID.class),
            rs.getObject("trip_request_id", UUID.class),
            rs.getObject("ordering_index", Integer.class),
            rs.getObject("latitude", Double.class),
            rs.getObject("longitude", Double.class)
        );

    @Transactional(readOnly = true)
    public List<WaypointEntity> findByTripRequestId(UUID tripRequestId) {
        val params = new MapSqlParameterSource("tripRequestId", tripRequestId);
        return jdbcTemplate.query(FIND_BY_TRIP_REQUEST_ID_SQL, params, rowMapper);
    }

    @Transactional
    public void save(WaypointEntity entity) {
        val params = new BeanPropertySqlParameterSource(entity);
        jdbcTemplate.update(UPSERT_SQL, params);
    }

    @Transactional
    public void saveAll(List<WaypointEntity> entities) {
        if (entities.isEmpty()) {
            return;
        }
        val batchParams = entities.stream()
            .map(BeanPropertySqlParameterSource::new)
            .toArray(SqlParameterSource[]::new);
        jdbcTemplate.batchUpdate(UPSERT_SQL, batchParams);
    }

}
