package ru.sberbank.ditsib.transport.tariff.service;

import ru.sber.transport.tariff.model.CalculatedDto;
import ru.sber.transport.tariff.model.TripDto;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;
import ru.sberbank.ditsib.transport.tariff.dto.TransportWithCalculateDTO;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.TransportPageDTO;

import java.util.Collection;
import java.util.UUID;

/**
 * Service for calculating trips.
 */
public interface CalculatingControllerService {
    
    /**
     * Calculate trip for all tariffs.
     *
     * @param tripData data of trip.
     *
     * @return calculated data.
     */
    Collection<? extends CalculatedDto> calculate(TripDto tripData, Employee employee);
    
    /**
     * Calculate trip for many tariffs.
     *
     * @param transportTypeEnum type of tariff.
     * @param tripData data of trip.
     *
     * @return calculated data.
     */
    Collection<? extends CalculatedDto> calculate(TransportTypeEnum transportTypeEnum, TripDto tripData, Employee employee);
    
    /**
     * Рассчитать поездку для одного тарифа.
     *
     * @param transportTypeEnum тип тарифа.
     * @param tariffId идентификатор тарифа.
     * @param tripData данные поездки.
     *
     * @return рассчитанные данные.
     */
    CalculatedDto calculate(TransportTypeEnum transportTypeEnum, UUID tariffId, TripDto tripData, Employee employee);
    
    TransportPageDTO calculateTransports(
            TripDto tripData, String search, Boolean availableOnly, Employee employee, String token
                                        );
    
    TransportWithCalculateDTO calculateTransport(
            TripDto tripData, UUID transportId, long startDate, long endDate, Employee employee, String token
                                                );
}
