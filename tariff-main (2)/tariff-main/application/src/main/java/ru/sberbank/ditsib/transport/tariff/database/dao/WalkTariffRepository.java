package ru.sberbank.ditsib.transport.tariff.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.WalkTariff;

import java.util.UUID;

/**
 * Репозиторий для пеших тарифов.
 */
public interface WalkTariffRepository extends JpaRepository<WalkTariff, UUID> {
}
