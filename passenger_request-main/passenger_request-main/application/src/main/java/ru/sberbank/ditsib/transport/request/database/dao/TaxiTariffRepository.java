package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.taxi.TaxiTariff;

import java.util.UUID;

public interface TaxiTariffRepository extends JpaRepository<TaxiTariff, UUID> {
}
