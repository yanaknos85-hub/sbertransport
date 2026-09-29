package ru.sberbank.ditsib.transport.request.database.dao;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.request.database.model.TripPurpose;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for business trip purpose
 */
@Repository
public interface TripPurposeRepository extends JpaRepository<TripPurpose, UUID> {


    /**
     * Get purpose by id.
     *
     * @return trip purpose.
     */
    @Query("SELECT purpose from TripPurpose purpose where purpose.id = :uuid")
    Optional<TripPurpose> findById(@NotNull UUID uuid);

    @Query(value = """
            select tp1_0.id_uuid, tp1_0.active, tp1_0.organization, tp1_0.purpose
            from request.trip_purpose tp1_0,
                 (select active,
                         creation_time,
                         author_id,
                         trip_purpose
                  from request.request_for_carsharing
                  union all
                  select active,
                         creation_time,
                         author_id,
                         trip_purpose
                  from request.request_for_group_transfer
                  union all
                  select active,
                         creation_time,
                         author_id,
                         trip_purpose
                  from request.request_for_personal
                  union all
                  select active,
                         creation_time,
                         author_id,
                         trip_purpose
                  from request.request_for_public
                  union all
                  select active,
                         creation_time,
                         author_id,
                         trip_purpose
                  from request.request_for_taxi) r1_0,
                 request.employee e1_0
            where (r1_0.active = true)
              and tp1_0.id_uuid = r1_0.trip_purpose
              and r1_0.author_id = e1_0.id
              and e1_0.user_id= :userId
            order by r1_0.creation_time desc fetch first :limit rows only
            """,
            nativeQuery = true)
    List<TripPurpose> findAllByUserId(UUID userId, int limit);
}
