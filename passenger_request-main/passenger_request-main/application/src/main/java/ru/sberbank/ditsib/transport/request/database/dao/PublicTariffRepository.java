package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.PublicTariff;

import java.util.UUID;

/**
 * Репозиторий работы с тарифами общественного транспорта.
 */
public interface PublicTariffRepository extends JpaRepository<PublicTariff, UUID> {
}
