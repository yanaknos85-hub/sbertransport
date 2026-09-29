package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.request.database.model.GroupTransferTrip;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Репозиторий поездок на групповой трансфер
 */
@Repository
public interface GroupTransferTripRepository extends JpaRepository<GroupTransferTrip, UUID> {
    
    Optional<GroupTransferTrip> findFirstByHumanReadableId(String id);
    
    @Query(nativeQuery = true, value = "select id from request.group_transfer_trip " +
                                       " where id in (select distinct group_transfer_trip_id from request.request_for_group_transfer where " +
                                       " request_closed_datetime is null and active = true" +
                                       " and group_transfer_trip_id is not null" +
                                       " and (desired_date > :notExpiredTime or finished_time is not null and finished_time > :notExpiredTime)) " +
                                       " and status in ('SENT_TO_CONTRACTOR', 'WAITING_FOR_ASSIGNMENT', 'DRIVER_ASSIGNED', 'DRIVER_APPROVED', " +
                                       "'DRIVER_ON_THE_WAY', 'DRIVER_ARRIVED', 'TRIP_IN_PROGRESS')" +
                                       " and group_transfer_id is not null")
    Set<UUID> findTripsIdInProgress(LocalDateTime notExpiredTime);
    
    @Query(nativeQuery = true, value = "select id from request.group_transfer_trip " +
                                       " where id in (select distinct group_transfer_trip_id from request.request_for_group_transfer where " +
                                       " request_closed_datetime is null and active = true" +
                                       " and group_transfer_trip_id is not null" +
                                       " and (desired_date > :notExpiredTime or finished_time is not null and finished_time > :notExpiredTime)) " +
                                       " and status in ('SENT_TO_CONTRACTOR', 'WAITING_FOR_ASSIGNMENT', 'DRIVER_ASSIGNED', 'DRIVER_APPROVED', " +
                                       "'DRIVER_ON_THE_WAY', 'DRIVER_ARRIVED', 'TRIP_IN_PROGRESS', 'ORDER_FINISHED')" +
                                       " and group_transfer_id is not null")
    Set<UUID> findTripsIdInProgressAndFinished(LocalDateTime notExpiredTime);

}
