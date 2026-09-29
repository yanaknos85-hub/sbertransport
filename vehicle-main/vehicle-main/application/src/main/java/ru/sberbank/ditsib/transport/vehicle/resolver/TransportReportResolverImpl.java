package ru.sberbank.ditsib.transport.vehicle.resolver;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sberbank.ditsib.transport.vehicle.dto.files.TransportReportDto;
import ru.sberbank.ditsib.transport.vehicle.helper.UserAuthorizationHelper;
import ru.sberbank.ditsib.transport.vehicle.service.TransportService;
import ru.sberbank.ditsib.transport.vehicle.service.corp.EmployeeService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ru.sberbank.ditsib.transport.vehicle.constants.Role.ROLE_ADMIN_DATA_MASTER;

/**
 * Экспорт из справочника ТС
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class TransportReportResolverImpl implements DataExporter<TransportReportDto> {
    private final EmployeeService employeeService;
    private final TransportService transportService;

    private static final String CONTRACTOR_ID_FILTER = "contractorId";
    private static final String AUTOPARK_ID_FILTER = "autoparkId";
    
    @SneakyThrows
    @Override
    public List<TransportReportDto> exportData(Map<String, ?> parameters, JwtAuthenticationToken authentication) {
        if (parameters.containsKey(CONTRACTOR_ID_FILTER)) {
            var contractorId = UUID.fromString(String.valueOf(parameters.get(CONTRACTOR_ID_FILTER)));
            var autoparkId = parameters.containsKey(AUTOPARK_ID_FILTER) ? UUID.fromString(String.valueOf(parameters.get(AUTOPARK_ID_FILTER))) : null;
            return transportService.getInfoForTransportReportForContractor(contractorId, autoparkId);
        } else {
            var roles = UserAuthorizationHelper.getRoles(authentication);
            if (roles.contains(ROLE_ADMIN_DATA_MASTER.name())) {
                return transportService.getInfoForTransportReport(null);
            }
            var userId = UserAuthorizationHelper.getUserId(authentication);
            var organizationId = employeeService.getByUserId(userId).getOrganization().getId();
            return transportService.getInfoForTransportReport(organizationId);
        }
    }
    
    @Override
    public String getCaption() {
        return "КОНФИДЕНЦИАЛЬНО";
    }
    
}
