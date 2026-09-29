package ru.sberbank.ditsib.transport.reports.dto;

import java.util.UUID;

/**
 * Интерфейс для отчетов.
 */
public interface ReportResponseDTO {
    
    /**
     * @return получить идентификатор.
     */
    UUID getId();
    
    /**
     * @return получить подразделение.
     */
    DepartmentShortDTO getDepartment();
    
}
