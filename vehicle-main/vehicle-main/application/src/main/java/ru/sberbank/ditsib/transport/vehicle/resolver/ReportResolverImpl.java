package ru.sberbank.ditsib.transport.vehicle.resolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sberbank.ditsib.transport.vehicle.constants.ReportType;
import ru.sberbank.ditsib.transport.vehicle.dto.files.ReportDto;
import ru.sberbank.ditsib.transport.vehicle.dto.files.ReportQueryParametersDto;
import ru.sberbank.ditsib.transport.vehicle.exception.ExcelFilterException;
import ru.sberbank.ditsib.transport.vehicle.helper.UserAuthorizationHelper;
import ru.sberbank.ditsib.transport.vehicle.service.TransportService;
import ru.sberbank.ditsib.transport.vehicle.service.corp.EmployeeService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Экспорт отчета по показателям одометра
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class ReportResolverImpl implements DataExporter<ReportDto> {
    
    private static final String YEAR_PARAMETER_NAME = "year";
    private static final String REPORT_TYPE_PARAMETER_NAME = "reportType";
    private static final String PARAMETER_NOT_FOUND_MESSAGE = "Не заполнен параметр фильтрации ";
    
    private final EmployeeService employeeService;
    private final TransportService transportService;
    
    @Override
    public List<ReportDto> exportData(Map<String, ?> parameters, JwtAuthenticationToken authentication) {
        try {
            return transportService.createReport(mapFilterParameterToQueryParameters(parameters, authentication));
        } catch (ExcelFilterException exception) {
            log.info(exception.getMessage());
            log.debug(exception.getMessage(), exception);
            return Collections.emptyList();
        }
    }

    private ReportQueryParametersDto mapFilterParameterToQueryParameters(Map<String, ?> parameters, JwtAuthenticationToken authenticationToken) {
        if (parameters == null || parameters.isEmpty()) {
            throw new ExcelFilterException("Не заполнены параметры фильтрации");
        }
        var userId = UserAuthorizationHelper.getUserId(authenticationToken);
        var organizationId = employeeService.getByUserId(userId).getOrganization().getId();
        var year = Optional.ofNullable(parameters.get(YEAR_PARAMETER_NAME))
                           .map(String::valueOf)
                           .map(Integer::valueOf)
                           .orElseThrow(() -> new ExcelFilterException(PARAMETER_NOT_FOUND_MESSAGE + YEAR_PARAMETER_NAME));
        var reportType = Optional.ofNullable(parameters.get(REPORT_TYPE_PARAMETER_NAME))
                                 .map(String::valueOf)
                                 .map(ReportType::valueOf)
                                 .orElseThrow(() -> new ExcelFilterException(PARAMETER_NOT_FOUND_MESSAGE + REPORT_TYPE_PARAMETER_NAME));
        return new ReportQueryParametersDto(organizationId, year, reportType);
    }
}
