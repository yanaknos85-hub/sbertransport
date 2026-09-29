package ru.sberbank.ditsib.transport.reports.service;


import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;

import java.util.UUID;

/*
 * Сервис для экспорта данных в xlsx
 */
public interface XlsxExporter {
    
    TransportTypeEnum transportType();
    
    void exportToXlsx(String filename, String token, RequestReportDTO requestReportDTO);

    /**
     * Создание xlsx из набора request с полями для загрузки в систему платежей
     */
    void exportToPaymentXlsx(String filename, RequestReportDTO reportDTO, UUID userId);
}