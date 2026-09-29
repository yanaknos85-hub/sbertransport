package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.personal.PersonalTariff;

import java.util.UUID;

/**
 * Репозиторий работы с тарифами личного транспорта.
 */
public interface PersonalTariffRepository extends JpaRepository<PersonalTariff, UUID> {
}
