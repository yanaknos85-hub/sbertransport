package ru.sberbank.ditsib.transport.reports.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;
import ru.sberbank.ditsib.transport.reports.controller.AnalysisController;
import ru.sberbank.ditsib.transport.reports.dto.analytic.GeneralAnalyticalReportRequestDTO;
import ru.sberbank.ditsib.transport.reports.dto.analytic.GeneralAnalyticalReportResponseDTO;
import ru.sberbank.ditsib.transport.reports.service.AnalysisService;

import java.time.LocalDate;
import java.util.stream.Collectors;


@RestController
@E2EController
@RequiredArgsConstructor
public class AnalysisControllerImpl implements AnalysisController {
    private final AnalysisService analysisService;
    
    @Override
    public GeneralAnalyticalReportResponseDTO getGeneralAnalyticalReportData(int version, GeneralAnalyticalReportRequestDTO request,
                                                                             @E2EUser("principal") JwtAuthenticationToken authentication) {
        if (version == 1) {
            request.setMonthList(request.getMonthList().stream().map(month -> month + 1).collect(Collectors.toList())); //NOSONAR
        }
    
        if (request.getYear() == null) {
            request.setYear(LocalDate.now().getYear());
        }
        
        return analysisService.getGeneralAnalyticalReportData(request, authentication);
    }
}
