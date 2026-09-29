package ru.sberbank.ditsib.transport.srm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.srm.model.tariff.PersonalTariff;

import java.util.UUID;

/**
 * Репозиторий тарифов такси
 */
@Repository
public interface PersonalTariffRepository extends JpaRepository<PersonalTariff, UUID> {
}
