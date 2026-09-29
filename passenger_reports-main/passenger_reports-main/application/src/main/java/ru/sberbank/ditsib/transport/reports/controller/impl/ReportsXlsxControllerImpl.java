package ru.sberbank.ditsib.transport.reports.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.controller.ReportsXlsxController;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaskResultDto;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForCarsharingReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;
import ru.sberbank.ditsib.transport.reports.service.XlsxExporter;
import ru.sberbank.ditsib.transport.reports.utils.ReportsXlsxUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@E2EController
@RequiredArgsConstructor
@Slf4j
public class ReportsXlsxControllerImpl extends BaseFileExporterControllerImpl implements ReportsXlsxController {
    private final Map<TransportTypeEnum, XlsxExporter> xlsxExporters;
    
    @Value("${reports.filename_prefix:Реестр_поездок_}")
    private String filenamePrefix;
    
    @Override
    public TaskResultDto downloadRequestsForPublicInXls(
            RequestForPublicReportDTO requestDTO, @E2EUser("principal") JwtAuthenticationToken authentication
                                                       ) {
        var args = getArguments(requestDTO, "", authentication);
        final var token = "Bearer " + authentication.getToken().getTokenValue();
        xlsxExporters.get(TransportTypeEnum.PUBLIC).exportToXlsx(token, args.getFilename(), requestDTO);
        return getUrl(args.getFilename());
    }
    
    @Override
    public TaskResultDto downloadRequestsForTaxiInXls(
            RequestForTaxiReportDTO requestDTO, @E2EUser("principal") JwtAuthenticationToken authentication
                                                     ) {
        var args = getArguments(requestDTO, "", authentication);
        final var token = "Bearer " + authentication.getToken().getTokenValue();
        xlsxExporters.get(TransportTypeEnum.TAXI).exportToXlsx(token, args.getFilename(), requestDTO);
        return getUrl(args.getFilename());
    }
    
    @Override
    public TaskResultDto downloadRequestsForPersonalInXls(
            RequestForPersonalReportDTO requestDTO, @E2EUser("principal") JwtAuthenticationToken authentication
                                                         ) {
        var args = getArguments(requestDTO, "", authentication);
        final var token = "Bearer " + authentication.getToken().getTokenValue();
        xlsxExporters.get(TransportTypeEnum.PERSONAL).exportToXlsx(token, args.getFilename(), requestDTO);
        return getUrl(args.getFilename());
    }
    
    @Override
    public TaskResultDto downloadRequestsForCarsharingInXls(
            RequestForCarsharingReportDTO requestDTO, @E2EUser("principal") JwtAuthenticationToken authentication
                                                           ) {
        var args = getArguments(requestDTO, "каршеринг_", authentication);
        final var token = "Bearer " + authentication.getToken().getTokenValue();
        xlsxExporters.get(TransportTypeEnum.CARSHARING).exportToXlsx(token, args.getFilename(), requestDTO);
        return getUrl(args.getFilename());
    }
    
    @Override
    public TaskResultDto downloadRequestsForPersonalPaymentInXls(
            RequestForPersonalReportDTO requestDTO, @E2EUser("principal") JwtAuthenticationToken authentication
                                                                ) {
        var args = getArguments(requestDTO, "ЛТ_", authentication);
        final var token = "Bearer " + authentication.getToken().getTokenValue();
        xlsxExporters.get(TransportTypeEnum.PERSONAL).exportToXlsx(token, args.getFilename(), requestDTO);
        
        return getUrl(args.getFilename());
    }
    
    @Override
    public TaskResultDto downloadRequestsForPublicPaymentInXls(
            RequestForPublicReportDTO requestDTO, @E2EUser("principal") JwtAuthenticationToken authentication
                                                              ) {
        var args = getArguments(requestDTO, "ОТ_", authentication);
        
        final var token = "Bearer " + authentication.getToken().getTokenValue();
        xlsxExporters.get(TransportTypeEnum.PUBLIC).exportToXlsx(token, args.getFilename(), requestDTO);
        
        return getUrl(args.getFilename());
    }
    
    private String getResponseFilename(
            @E2EUser("principal") JwtAuthenticationToken authentication, LocalDateTime creationDateFrom, LocalDateTime creationDateTo, String filenameTag
                                      ) {
        var filename = filenamePrefix + filenameTag + creationDateFrom.format(DateTimeFormatter.ofPattern("ddMMyyyy")) + "_"
                       + creationDateTo.format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".xlsx";
        var auditMessage =
                "Пользователь " + authentication.getName() + " запросил выгрузку реестра поездок: " + filename;
        log.info(auditMessage);
        return filename;
    }
    
    private ReportsXlsxUtils.ArgsContainer getArguments(
            RequestReportDTO requestDTO, String filenameTag,
            @E2EUser("principal") JwtAuthenticationToken authentication
                                                       ) {
        ReportsXlsxUtils.ArgsContainer prepareArgs = ReportsXlsxUtils.getCreateDateArgs(requestDTO);
        String filename = getResponseFilename(authentication,
                                              prepareArgs.getCreationDateFrom(),
                                              prepareArgs.getCreationDateTo(),
                                              filenameTag);
        
        prepareArgs.setFilename(filename);
        return prepareArgs;
    }
}
