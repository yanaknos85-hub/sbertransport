package ru.sber.transport.telemechanic.resolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestExcelAllOrganizationsDto;
import ru.sber.transport.telemechanic.exception.BusinessException;
import ru.sber.transport.telemechanic.service.MedicRequestService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * С учетом использования иденитчных ДТО для динамической выгрузки реестра excel, необходимо использовать разные ДТО для каждого резолвера, т.к. в
 * файле export.yml мы указываем url и поиск нужного резолвера происходит по ДТО в дженерике
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class MedicRequestRegistryAllOrganizationsResolverImpl implements DataExporter<MedicRequestExcelAllOrganizationsDto> {
    
    private static final String CAPTION = "КОНФИДЕНЦИАЛЬНО";
    
    private final MedicRequestService medicRequestService;
    
    @NotNull
    @Override
    public List<MedicRequestExcelAllOrganizationsDto> exportData(@NotNull Map<String, ?> parameters, @NotNull JwtAuthenticationToken authentication) {
        try {
            return medicRequestService.searchExcelForAllOrganizations(parameters);
        } catch (BusinessException e) {
            log.info(e.getMessage());
            log.debug(e.getMessage(), e);
            return Collections.emptyList();
        }
        
    }
    
    @Nullable
    @Override
    public String getCaption() {
        return CAPTION;
    }
}
