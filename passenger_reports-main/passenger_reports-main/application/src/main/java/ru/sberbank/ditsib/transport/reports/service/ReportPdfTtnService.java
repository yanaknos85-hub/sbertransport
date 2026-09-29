package ru.sberbank.ditsib.transport.reports.service;

import java.util.UUID;

/**
 * Сервис создания транспортной накладной в формате pdf
 */
public interface ReportPdfTtnService {

    /**
     * Получение транспортной накладной
     * @param requestId id запроса
     * @return накладная в формате pdf
     */
    byte[] getReport(UUID requestId);
}
