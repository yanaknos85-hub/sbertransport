package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.sber.transport.authorization.exceptions.UnauthorizedException;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.reports.dao.StatsRepository;
import ru.sberbank.ditsib.transport.reports.dto.analytic.GeneralAnalyticalReportRequestDTO;
import ru.sberbank.ditsib.transport.reports.dto.analytic.GeneralAnalyticalReportResponseDTO;
import ru.sberbank.ditsib.transport.reports.model.Employee;
import ru.sberbank.ditsib.transport.reports.model.Stats;
import ru.sberbank.ditsib.transport.reports.service.AnalysisService;
import ru.sberbank.ditsib.transport.reports.service.AnalyticDataProvider;
import ru.sberbank.ditsib.transport.reports.service.EmployeeService;

import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
class AnalysisServiceImpl implements AnalysisService {
    
    private final StatsRepository statsRepository;
    private final EmployeeService employeeService;
    private final EmployeeOrganizationFunction employeeOrganizationFunction;
    
    /**
     * @param request - параметры запроса year - год, за который нужно получить данные
     * @param authentication - параметры аутентификации
     *
     * @return данные общего аналитического отчета
     */
    @Override
    public GeneralAnalyticalReportResponseDTO getGeneralAnalyticalReportData(
            GeneralAnalyticalReportRequestDTO request, JwtAuthenticationToken authentication
                                                                            ) {
        UUID userOrganization = employeeOrganizationFunction.apply(UUID.fromString(authentication.getToken().getId()));
        if (!request.getOrganizationId().contains(userOrganization)) checkUserHaveAccessToOrganizationData(authentication);
    
        final var dataMaster = Optional.ofNullable(authentication.getToken().getClaimAsBoolean("data_master")).orElse(false);
        final var organizationIdList = dataMaster ? request.getOrganizationId() : List.of(userOrganization);
    
        final List<TransportTypeEnum> transportTypes = new ArrayList<>();
        if (request.getTransportTypes() != null && !request.getTransportTypes().isEmpty()) {
            for (String transportType : request.getTransportTypes()) {
                transportTypes.add(TransportTypeEnum.valueOf(transportType));
            }
        }
        List<Stats> statsList = new ArrayList<>();
        if (request.getMonthList() == null) {
            for (TransportTypeEnum transportType : transportTypes) {
                for (UUID organizationId : organizationIdList) {
                    statsList.addAll(getStats(request.getYear(), organizationId, transportType));
                }
            }
        } else {
            for (TransportTypeEnum transportType : transportTypes) {
                for (Integer month : request.getMonthList()) {
                    for (UUID organizationId : organizationIdList) {
                        var stats = getStats(month, request.getYear(), organizationId, transportType);
                        stats.ifPresent(statsList::add);
                    }
                }
            }
        }
        log.info("getGeneralAnalyticalReportData: request: " + request + ", statsList size: " + statsList.size());
        return new GeneralAnalyticalReportResponseDTO(
                getDataProviders().stream()
                                  .map(analyticDataProvider -> analyticDataProvider.getChartData(request, statsList))
                                  .toList());
    }
    
    private Optional<Stats> getStats(int month, int year, UUID organizationId, TransportTypeEnum transportType) {
        return statsRepository.findByMonthAndYearAndOrganizationIdAndTransportType(month, year, organizationId, transportType);
    }
    
    private List<Stats> getStats(int year, UUID organizationId, TransportTypeEnum transportType) {
        return statsRepository.findByYearAndOrganizationIdAndTransportType(year, organizationId, transportType);
    }
    
    private void checkUserHaveAccessToOrganizationData(UUID organizationId, JwtAuthenticationToken authentication) {
        if (!doesUserHaveAccessToOrganizationData(organizationId, authentication)) {
            throw new UnauthorizedException(UUID.fromString(authentication.getToken().getId()));
        }
    }
    
    private void checkUserHaveAccessToOrganizationData(JwtAuthenticationToken authentication) {
        if (!doesUserHaveAccessToOrganizationData(authentication)) {
            throw new UnauthorizedException(UUID.fromString(authentication.getToken().getId()));
        }
    }
    
    private boolean doesUserHaveAccessToOrganizationData(UUID organizationId, JwtAuthenticationToken authentication) {
        final var dataMaster = Optional.ofNullable(authentication.getToken().getClaimAsBoolean("data_master")).orElse(false);
        
        if (dataMaster) {
            return true;
        }
        if (!StringUtils.hasText(authentication.getName()) || organizationId == null) {
            return false;
        }
        final var id = authentication.getToken().getId();
        return organizationId.equals(
                employeeService.getOrganizationIdByUserId(UUID.fromString(id))
                               .orElseThrow(() -> new EntityNotFoundException(Employee.class, id)));
    }
    
    private boolean doesUserHaveAccessToOrganizationData(JwtAuthenticationToken authentication) {
        return Optional.ofNullable(authentication.getToken().getClaimAsBoolean("data_master")).orElse(false);
    }
    
    private List<AnalyticDataProvider> getDataProviders() {
        List<AnalyticDataProvider> dataProviders = new ArrayList<>();
        dataProviders.add(new TotalAnalyticDataProviderImpl(statsRepository));
        dataProviders.add(new SlaAnalyticDataProviderImpl(statsRepository));
        dataProviders.add(new CsiAnalyticDataProviderImpl(statsRepository));
        return dataProviders;
    }
}
