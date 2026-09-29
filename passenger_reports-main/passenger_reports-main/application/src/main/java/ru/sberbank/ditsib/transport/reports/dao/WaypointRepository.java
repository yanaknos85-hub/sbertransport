package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.Address;
import ru.sberbank.ditsib.transport.reports.model.Waypoint;

import java.util.List;
import java.util.UUID;

@Repository
public interface WaypointRepository extends JpaRepository<Waypoint, UUID> {

    List<Waypoint> findAllByAddressIn(List<Address> addresses);
}
