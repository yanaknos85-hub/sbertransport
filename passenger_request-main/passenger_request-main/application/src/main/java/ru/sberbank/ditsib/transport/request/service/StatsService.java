package ru.sberbank.ditsib.transport.request.service;

import ru.sber.transport.request.model.StatsDTO;

import java.util.List;

/**
 * Сервис для работы с геозонами.
 */
public interface StatsService {
    
    /**
     * Получение стастистики.
     *
     * @return стастистика.
     */
    List<StatsDTO> processStats();
    
    /**
     * Получение стастистики.
     *
     * @return стастистика.
     */
    List<StatsDTO> processStatsForMonthAndYear(int month, int year);
    
}
