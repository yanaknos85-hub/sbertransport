package ru.sberbank.ditsib.transport.reports.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.reports.controller.AnalysisXlsxController;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForCarsharingReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;
import ru.sberbank.ditsib.transport.reports.service.AnalysisXlsxService;

@RestController
@E2EController
@RequiredArgsConstructor
public class AnalysisXlsxControllerImpl implements AnalysisXlsxController {
    private final AnalysisXlsxService analysisXlsxService;
    
    @Override
    public byte[] taxiAnalysis(RequestForTaxiReportDTO requestSearchDTO) {
        return analysisXlsxService.analyzeTaxi(requestSearchDTO);
    }
    
    @Override
    public byte[] personalAnalysis(RequestForPersonalReportDTO requestSearchDTO) {
        return analysisXlsxService.analyzePersonal(requestSearchDTO);
    }
    
    @Override
    public byte[] publicAnalysis(RequestForPublicReportDTO requestSearchDTO) {
        return analysisXlsxService.analyzePublic(requestSearchDTO);
    }
    
    @Override
    public byte[] carsharingAnalysis(RequestForCarsharingReportDTO сarsharingRequestDTO) {
        return analysisXlsxService.analyzeCarsharing(сarsharingRequestDTO);
    }
}
