package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForCarsharingReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;

/**
 * Сервис экспорта аналитических данных
 */
public interface AnalysisXlsxService {
    
    /**
     * Получить аналитические данные по виду транспорта публичный
     *
     * @param requestDTO параметры фильтрации
     *
     * @return аналитические данные
     */
    byte[] analyzePublic(RequestForPublicReportDTO requestDTO);
    
    /**
     * Получить аналитические данные по виду транспорта такси
     *
     * @param requestDTO параметры фильтрации
     *
     * @return аналитические данные
     */
    byte[] analyzeTaxi(RequestForTaxiReportDTO requestDTO);
    
    /**
     * Получить аналитические данные по виду транспорта каршэринг
     *
     * @param requestDTO параметры фильтрации
     *
     * @return аналитические данные
     */
    byte[] analyzeCarsharing(RequestForCarsharingReportDTO requestDTO);
    
    /**
     * Получить аналитические данные по виду транспорта личный
     *
     * @param requestDTO параметры фильтрации
     *
     * @return аналитические данные
     */
    byte[] analyzePersonal(RequestForPersonalReportDTO requestDTO);
    
}
