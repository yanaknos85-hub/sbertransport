package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Telematics;

import java.util.List;
import java.util.UUID;

public interface TelematicsRepository extends JpaRepository<Telematics, UUID> {

    List<Telematics> findByImeiOrTitleIgnoreCase(String imei, String title);
}
