package ru.sberbank.ditsib.transport.reports.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.controller.ReportsXlsxOrganizationController;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaskResultDto;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForCarsharingReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;
import ru.sberbank.ditsib.transport.reports.service.XlsxExporter;
import ru.sberbank.ditsib.transport.reports.utils.ReportsXlsxUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RestController
@E2EController
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Slf4j
class ReportsOrganizationXlsxControllerImpl extends BaseFileExporterControllerImpl implements ReportsXlsxOrganizationController {
    
    private final Map<TransportTypeEnum, XlsxExporter> xlsxExporters;
    
    @Value("${reports.filename_prefix:Реестр_поездок_}")
    private String filenamePrefix;
    
    @CheckOrganizationAccess
    @Override
    public TaskResultDto downloadRequestsForPublicInXls(
            RequestForPublicReportDTO requestDTO, @Organization UUID organizationId, @E2EUser("principal") JwtAuthenticationToken authentication
                                                       ) {
        requestDTO.setEmployeeOrganizationSet(Set.of(String.valueOf(organizationId)));
        var args = getArguments(requestDTO, "");
        final var token = "Bearer " + authentication.getToken().getTokenValue();
        xlsxExporters.get(TransportTypeEnum.PUBLIC).exportToXlsx(args.getFilename(), token, requestDTO);
        return getUrl(args.getFilename());
    }
    
    @CheckOrganizationAccess
    @Override
    public TaskResultDto downloadRequestsForTaxiInXls(
            RequestForTaxiReportDTO requestDTO, @Organization UUID organizationId, @E2EUser("principal") JwtAuthenticationToken authentication
                                                     ) {
        requestDTO.setEmployeeOrganizationSet(Set.of(String.valueOf(organizationId)));
        var args = getArguments(requestDTO, "");
        final var token = "Bearer " + authentication.getToken().getTokenValue();
        xlsxExporters.get(TransportTypeEnum.TAXI).exportToXlsx(args.getFilename(), token, requestDTO);
        return getUrl(args.getFilename());
    }
    
    @CheckOrganizationAccess
    @Override
    public TaskResultDto downloadRequestsForPersonalInXls(
            RequestForPersonalReportDTO requestDTO,
            @Organization UUID organizationId, @E2EUser("principal") JwtAuthenticationToken authentication
                                                         ) {
        requestDTO.setEmployeeOrganizationSet(Set.of(String.valueOf(organizationId)));
        var args = getArguments(requestDTO, "");
        final var token = "Bearer " + authentication.getToken().getTokenValue();
        xlsxExporters.get(TransportTypeEnum.PERSONAL).exportToXlsx(args.getFilename(), token, requestDTO);
        return getUrl(args.getFilename());
    }
    
    @CheckOrganizationAccess
    @Override
    public TaskResultDto downloadRequestsForCarsharingInXls(
            RequestForCarsharingReportDTO requestDTO, @Organization UUID organizationId, @E2EUser("principal") JwtAuthenticationToken authentication
                                                           ) {
        requestDTO.setEmployeeOrganizationSet(Set.of(String.valueOf(organizationId)));
        var args = getArguments(requestDTO, "");
        final var token = "Bearer " + authentication.getToken().getTokenValue();
        xlsxExporters.get(TransportTypeEnum.CARSHARING).exportToXlsx(args.getFilename(), token, requestDTO);
        return getUrl(args.getFilename());
    }
    
    @CheckOrganizationAccess
    @Override
    public TaskResultDto downloadRequestsForPersonalPaymentInXls(
            RequestForPersonalReportDTO requestDTO,
            @Organization UUID organizationId
                                                                ) {
        var userId = ControllerUtils.currentUser();
        
        requestDTO.setEmployeeOrganizationSet(Set.of(String.valueOf(organizationId)));
        if (requestDTO.getRequestStatusSet().isEmpty()) {
            requestDTO.setRequestStatusSet(Set.of(
                    TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION,
                    TripRequestStatus.PERSONAL_PAYMENT_AWAITING));
        }
        
        var args = getArguments(requestDTO, "ЛТ_");
        
        xlsxExporters.get(TransportTypeEnum.PERSONAL).exportToPaymentXlsx(args.getFilename(), requestDTO, userId);
        return getUrl(args.getFilename());
    }
    
    @CheckOrganizationAccess
    @Override
    public TaskResultDto downloadRequestsForPublicPaymentInXls(
            RequestForPublicReportDTO requestDTO,
            @Organization UUID organizationId
                                                              ) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userId = Optional.ofNullable(authentication)
                             .map(JwtAuthenticationToken.class::cast)
                             .map(JwtAuthenticationToken::getToken)
                             .map(Jwt::getId)
                             .map(String::valueOf).map(UUID::fromString).orElse(null);
        
        requestDTO.setEmployeeOrganizationSet(Set.of(String.valueOf(organizationId)));
        requestDTO.setRequestStatusSet(Set.of(
                TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION,
                TripRequestStatus.PUBLIC_PAYMENT_AWAITING));
        
        var args = getArguments(requestDTO, "ОТ_");
        xlsxExporters.get(TransportTypeEnum.PUBLIC).exportToPaymentXlsx(args.getFilename(), requestDTO, userId);
        return getUrl(args.getFilename());
    }
    
    private String getResponseFilename(
            LocalDateTime creationDateFrom,
            LocalDateTime creationDateTo, String filenameTag
                                      ) {
        var formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        return "%s%s%s_%s_%s.xlsx".formatted(filenamePrefix, filenameTag, creationDateFrom.format(formatter),
                                             creationDateTo.format(formatter),
                                             LocalDateTime.now(ZoneOffset.UTC).toInstant(ZoneOffset.UTC).toEpochMilli());
    }
    
    private ReportsXlsxUtils.ArgsContainer getArguments(
            RequestReportDTO requestDTO, String filenameTag
                                                       ) {
        var prepareArgs = ReportsXlsxUtils.getCreateDateArgs(requestDTO);
        var filename = getResponseFilename(prepareArgs.getCreationDateFrom(),
                                           prepareArgs.getCreationDateTo(),
                                           filenameTag);
        
        prepareArgs.setFilename(filename);
        return prepareArgs;
    }
}
