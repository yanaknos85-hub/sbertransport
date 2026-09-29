package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.PersonalCar;

import java.util.UUID;

@Repository
public interface PersonalCarRepository extends JpaRepository<PersonalCar, UUID> {
}
