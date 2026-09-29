package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForCarsharingReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;
import ru.sberbank.ditsib.transport.reports.service.AnalysisXlsxService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisXlsxServiceImpl implements AnalysisXlsxService {
    
    @Override
    public byte[] analyzePublic(RequestForPublicReportDTO requestDTO) {
        return new byte[0];
    }
    
    @Override
    public byte[] analyzeTaxi(RequestForTaxiReportDTO requestDTO) {
        return new byte[0];
    }
    
    @Override
    public byte[] analyzeCarsharing(RequestForCarsharingReportDTO requestDTO) {
        return new byte[0];
    }
    
    @Override
    public byte[] analyzePersonal(RequestForPersonalReportDTO requestDTO) {
        return new byte[0];
    }
}
