package ru.sberbank.transport.oto.cargo.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.transport.oto.cargo.database.model.Address;
import ru.sberbank.transport.oto.cargo.database.model.Waypoint;

import java.util.List;
import java.util.UUID;

@Repository
public interface WaypointRepository extends JpaRepository<Waypoint, UUID> {

    List<Waypoint> findAllByAddressIn(List<Address> addresses);
  
    List<Waypoint> findAllByRequestIdIn(List<UUID> requestIds);
}
