package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.GroupTransferTariff;

import java.util.UUID;

public interface GroupTransferTariffRepository extends JpaRepository<GroupTransferTariff, UUID> {
}
