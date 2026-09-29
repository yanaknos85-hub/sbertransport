package ru.sberbank.transport.oto.cargo.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.sberbank.transport.oto.cargo.database.model.WaypointContact;

import java.util.List;
import java.util.UUID;

public interface WaypointContactRepository extends JpaRepository<WaypointContact, UUID> {
    
    @Modifying
    @Query("delete from WaypointContact c where c.waypoint.id in (:ids)")
    int deleteByWaypointIds(List<UUID> ids);
    
}