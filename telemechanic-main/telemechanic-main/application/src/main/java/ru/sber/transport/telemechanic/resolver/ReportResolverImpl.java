package ru.sber.transport.telemechanic.resolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.telemechanic.dto.RegistryExcelDto;
import ru.sber.transport.telemechanic.dto.ReportQueryParamDto;
import ru.sber.transport.telemechanic.helper.CheckRoleHelper;
import ru.sber.transport.telemechanic.helper.UserAuthorizationHelper;
import ru.sber.transport.telemechanic.service.EmployeeService;
import ru.sber.transport.telemechanic.service.RegistryExcelService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Экспорт реестра
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class ReportResolverImpl implements DataExporter<RegistryExcelDto> {
    
    private final EmployeeService employeeService;
    private final RegistryExcelService registryExcelService;
    
    private static final String CAPTION = "КОНФИДЕНЦИАЛЬНО";
    
    @Override
    @Transactional
    public List<RegistryExcelDto> exportData(Map<String, ?> parameters, JwtAuthenticationToken authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        var roles = UserAuthorizationHelper.getRoles(authentication);
        var authenticatedEmployee = employeeService.getByUserId(userId);
        if (!CheckRoleHelper.checkHaveAllOrganizationsRoleForReport(roles)) {
            return registryExcelService.getAllRegistryByOrganizationId(authenticatedEmployee.getDepartment().getOrganization().getId(),
                                                                       mapFilterParameterIntoQueryParameter(parameters));
        } else {
            return registryExcelService.getAllRegistryByOrganizationId(null, mapFilterParameterIntoQueryParameter(parameters));
        }
    }

    private ReportQueryParamDto mapFilterParameterIntoQueryParameter(Map<String, ?> parameters) {
        var queryParamBuilder = ReportQueryParamDto.builder();
        if (Objects.nonNull(parameters) && !parameters.isEmpty()) {
            var humanReadableId = Optional.ofNullable(parameters.get("humanReadableId")).map(value -> "%" + value + "%").orElse(null);
            var personnelNumber = Optional.ofNullable(parameters.get("personnelNumber")).map(String::valueOf).orElse(null);
            
            var startCreationTime = Optional.ofNullable(parameters.get("startCreationTime"))
                                            .map(str -> LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(String.valueOf(str)))).orElse(null);
            var endCreationTime = Optional.ofNullable(parameters.get("endCreationTime"))
                                          .map(str -> LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(String.valueOf(str)))).orElse(null);
            
            var organizationId = Optional.ofNullable(parameters.get("organizationId")).map(String::valueOf).orElse(null);
            var departmentIds =
                    Optional.ofNullable(parameters.get("departmentIds")).map(ids -> Arrays.stream(ids.toString().split(",")).collect(
                            Collectors.toSet())).orElse(null);
            
            if (Objects.nonNull(humanReadableId)) {
                queryParamBuilder = queryParamBuilder.humanReadableId(humanReadableId);
            }
            if (Objects.nonNull(personnelNumber)) {
                queryParamBuilder = queryParamBuilder.personnelNumber(personnelNumber);
            }
            if (Objects.nonNull(startCreationTime)) {
                queryParamBuilder = queryParamBuilder.startCreationTime(startCreationTime);
            }
            if (Objects.nonNull(endCreationTime)) {
                queryParamBuilder = queryParamBuilder.endCreationTime(endCreationTime);
            }
            if (Objects.nonNull(organizationId)) {
                queryParamBuilder = queryParamBuilder.organizationId(organizationId);
            }
            if (Objects.nonNull(departmentIds) && !departmentIds.isEmpty()) {
                queryParamBuilder = queryParamBuilder.departmentIds(departmentIds.stream().map(UUID::fromString).collect(Collectors.toSet()));
            }
        }
        return queryParamBuilder.build();
    }
    
    @Override
    public String getCaption() {
        return CAPTION;
    }
}