package ru.sberbank.ditsib.transport.reports.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.reports.controller.ReportPdfTtnController;
import ru.sberbank.ditsib.transport.reports.service.ReportPdfTtnService;

import java.util.UUID;

@RestController
@E2EController
@RequiredArgsConstructor
public class ReportPdfTtnControllerImpl implements ReportPdfTtnController {
    
    private final ReportPdfTtnService reportPdfTtnService;
    
    @Override
    public byte[] getReportPdfTtn(UUID requestId) {
        System.setProperty("java.awt.headless", "true");
        return reportPdfTtnService.getReport(requestId);
    }
}
