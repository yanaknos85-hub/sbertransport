package ru.sberbank.ditsib.transport.srm.service;

import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.srm.dto.DistanceMatrixResponseDTO;
import ru.sberbank.ditsib.transport.srm.model.SrmWaypoint;

import java.util.List;

/**
 * Сервис для работы с маджентой
 */
public interface GeoService {

    /**
     * Запрос матрицы расстояний из геосервиса
     *
     * @param waypointList остановки
     * @return DTO с матрицей расстояний
     */
    DistanceMatrixResponseDTO getDistanceMatrix(List<SrmWaypoint> waypointList, TransportTypeEnum transportType);

    /**
     * Запрос матрицы расстояний из геосервиса асинхронный
     *
     * @param waypointList остановки
     * @return DTO с матрицей расстояний
     */
    DistanceMatrixResponseDTO getDistanceMatrixAsync(List<SrmWaypoint> waypointList, TransportTypeEnum transportType);

}
