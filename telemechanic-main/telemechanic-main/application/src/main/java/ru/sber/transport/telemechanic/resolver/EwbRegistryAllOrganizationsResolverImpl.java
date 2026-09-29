package ru.sber.transport.telemechanic.resolver;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.telemechanic.database.dao.EwbRegistryDynamicRepository;
import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistryAllOrganizationsRequest;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistryExcelAllOrganizationsDto;
import ru.sber.transport.telemechanic.enumerate.EwbRegistryField;
import ru.sber.transport.telemechanic.mapper.EwbRegistryMapper;

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
public class EwbRegistryAllOrganizationsResolverImpl implements DataExporter<EwbRegistryExcelAllOrganizationsDto> {
    
    private final EwbRegistryDynamicRepository repository;
    
    private static final String CAPTION = "КОНФИДЕНЦИАЛЬНО";
    
    @NotNull
    @Override
    public List<EwbRegistryExcelAllOrganizationsDto> exportData(@NotNull Map<String, ?> parameters, @NotNull JwtAuthenticationToken authentication) {
        var request = createRequestFromParameters(parameters);
        var ewbs = repository.findEwbRegistryExcel(request.fieldSet(),
                                                   request.searchText(),
                                                   request.humanReadableId(),
                                                   request.organizationId(),
                                                   request.departmentIds(),
                                                   request.period());
        return EwbRegistryMapper.recordToEwbRegistryExcelAllOrganizationsDto(ewbs, request.fieldSet());
    }
    
    @Nullable
    @Override
    public String getCaption() {
        return CAPTION;
    }
    
    private EwbRegistryAllOrganizationsRequest createRequestFromParameters(Map<String, ?> parameters) {
        var fields = Optional.ofNullable(parameters.get("fieldSet")).map(str -> String.valueOf(str).split(",")).orElse(null);
        var departments = Optional.ofNullable(parameters.get("departmentIds")).map(str -> String.valueOf(str).split(",")).orElse(null);
        var startTime = Optional.ofNullable(parameters.get("start"))
                                .map(str -> LocalDateTime.parse(String.valueOf(str), DateTimeFormatter.ISO_DATE_TIME)).orElse(null);
        var endTime = Optional.ofNullable(parameters.get("end"))
                              .map(str -> LocalDateTime.parse(String.valueOf(str), DateTimeFormatter.ISO_DATE_TIME)).orElse(null);
        return new EwbRegistryAllOrganizationsRequest(
                fields != null ? Arrays.stream(fields)
                                       .map(EwbRegistryField::valueOf)
                                       .collect(Collectors.toCollection(LinkedHashSet::new))
                               : null,
                Optional.ofNullable(parameters.get("searchText")).map(str -> "%" + str + "%").orElse(null),
                Optional.ofNullable(parameters.get("humanReadableId")).map(str -> "%" + str + "%").orElse(null),
                Optional.ofNullable(parameters.get("organizationId")).map(str -> UUID.fromString(String.valueOf(str))).orElse(null),
                departments != null ? Arrays.stream(departments)
                                            .map(UUID::fromString)
                                            .collect(Collectors.toSet()) : null,
                startTime != null && endTime != null ? new DateRange(startTime, endTime) : null,
                null,
                null
        );
    }
}
