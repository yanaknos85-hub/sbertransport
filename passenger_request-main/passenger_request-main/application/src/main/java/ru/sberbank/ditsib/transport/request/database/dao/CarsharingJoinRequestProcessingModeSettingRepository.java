package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingJoinRequestProcessingMode;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingJoinRequestProcessingModeSetting;

public interface CarsharingJoinRequestProcessingModeSettingRepository extends
        JpaRepository<CarsharingJoinRequestProcessingModeSetting, CarsharingJoinRequestProcessingMode> {
}
