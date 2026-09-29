package ru.sber.transport.telemechanic.resolver;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.telemechanic.database.dao.EwbRegistryDynamicRepository;
import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistryExcelSelfOrganizationDto;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistrySelfOrganizationRequest;
import ru.sber.transport.telemechanic.enumerate.EwbRegistryField;
import ru.sber.transport.telemechanic.helper.UserAuthorizationHelper;
import ru.sber.transport.telemechanic.mapper.EwbRegistryMapper;
import ru.sber.transport.telemechanic.service.EmployeeService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * С учетом использования иденитчных ДТО для динамической выгрузки реестра excel,
 * необходимо использовать разные ДТО для каждого резолвера, т.к.
 * в файле export.yml мы указываем url и поиск нужного резолвера происходит по ДТО в дженерике
 */

@Component
@RequiredArgsConstructor
public class EwbRegistrySelfOrganizationResolverImpl implements DataExporter<EwbRegistryExcelSelfOrganizationDto> {
    
    private final EwbRegistryDynamicRepository repository;
    private final EmployeeService employeeService;
    
    private static final String CAPTION = "КОНФИДЕНЦИАЛЬНО";
    
    @NotNull
    @Override
    public List<EwbRegistryExcelSelfOrganizationDto> exportData(@NotNull Map<String, ?> parameters, @NotNull JwtAuthenticationToken authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        var authenticatedEmployee = employeeService.getByUserId(userId);
        var request = createRequestFromParameters(parameters);
        var ewbs = repository.findEwbRegistryExcel(request.fieldSet(),
                                                   request.searchText(),
                                                   request.humanReadableId(),
                                                   authenticatedEmployee.getOrganization().getId(),
                                                   null,
                                                   request.period());
        return EwbRegistryMapper.recordToEwbRegistryExcelSelfOrganizationDto(ewbs, request.fieldSet());
        
    }
    
    @Nullable
    @Override
    public String getCaption() {
        return CAPTION;
    }
    
    private EwbRegistrySelfOrganizationRequest createRequestFromParameters(Map<String, ?> parameters) {
        var fields = Optional.ofNullable(parameters.get("fieldSet")).map(str -> String.valueOf(str).split(",")).orElse(null);
        var startTime = Optional.ofNullable(parameters.get("start"))
                                .map(str -> LocalDateTime.parse(String.valueOf(str), DateTimeFormatter.ISO_DATE_TIME)).orElse(null);
        var endTime = Optional.ofNullable(parameters.get("end"))
                              .map(str -> LocalDateTime.parse(String.valueOf(str), DateTimeFormatter.ISO_DATE_TIME)).orElse(null);
        return new EwbRegistrySelfOrganizationRequest(
                fields != null ? Arrays.stream(fields)
                                       .map(EwbRegistryField::valueOf)
                                       .collect(Collectors.toCollection(LinkedHashSet::new))
                               : null,
                Optional.ofNullable(parameters.get("searchText")).map(str -> "%" + str + "%").orElse(null),
                Optional.ofNullable(parameters.get("humanReadableId")).map(str -> "%" + str + "%").orElse(null),
                startTime != null && endTime != null ? new DateRange(startTime, endTime) : null,
                null,
                null
        );
    }
}
