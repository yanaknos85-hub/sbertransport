package ru.sberbank.ditsib.transport.vehicle.service;

import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.AccessiblePositionDto;

import java.util.List;

/**
 * Операции со справочником должностей для закрепления
 */
public interface AccessiblePositionService {
    /**
     * Поиск всех должностей
     * @return список должностей {@link AccessiblePositionDto}
     */
    List<AccessiblePositionDto> findAll();
}
