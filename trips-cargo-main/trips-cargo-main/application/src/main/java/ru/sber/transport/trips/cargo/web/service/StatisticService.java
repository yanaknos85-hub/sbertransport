package ru.sber.transport.trips.cargo.web.service;

import ru.sber.transport.trips.cargo.business.dto.TripAssignStatisticDto;

import java.util.UUID;

public interface StatisticService {

    /**
     * Получение статистики по назначению трипов.
     *
     * @param contractorId идентификатор контрагента.
     * @param contractorId идентификатор автопарка.
     * @return статистика по назначению трипов.
     */
    TripAssignStatisticDto getAssignStatistic(UUID contractorId, UUID autoparkId);
}
