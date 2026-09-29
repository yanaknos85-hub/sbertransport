package ru.sberbank.ditsib.transport.reports.service;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sberbank.ditsib.transport.reports.dto.analytic.GeneralAnalyticalReportRequestDTO;
import ru.sberbank.ditsib.transport.reports.dto.analytic.GeneralAnalyticalReportResponseDTO;

/**
 * Сервис калькуляции аналитических данных
 */
public interface AnalysisService {
    
    /**
     * @param request - параметры запроса year - год, за который нужно получить данные
     * @param authentication
     *
     * @return данные общего аналитического отчета
     */
    GeneralAnalyticalReportResponseDTO getGeneralAnalyticalReportData(
            GeneralAnalyticalReportRequestDTO request, JwtAuthenticationToken authentication
                                                                     );
}
