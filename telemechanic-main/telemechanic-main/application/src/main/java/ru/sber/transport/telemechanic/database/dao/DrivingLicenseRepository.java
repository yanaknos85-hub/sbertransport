package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.telemechanic.database.model.DrivingLicense;

import java.util.UUID;

public interface DrivingLicenseRepository extends JpaRepository<DrivingLicense, UUID> {
}
