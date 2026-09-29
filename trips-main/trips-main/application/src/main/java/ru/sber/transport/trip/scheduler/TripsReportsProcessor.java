package ru.sber.transport.trip.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.trip.web.service.ReportService;

/**
 * Шедулер создания отчета по выполненым поездкам
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class TripsReportsProcessor {

    private final ReportService reportService;

    @Value("${report.processor.isEnabled:true}")
    private boolean isEnabled;

    @Scheduled(cron = "${report.processor.rate:0 */3 * * * *}")
    @Transactional
    public void scheduleReportProcessing() {
        if(isEnabled) {
            log.trace("Report processing scheduler started");
            reportService.processReport();
            log.trace("Report processing scheduler finished");
        }
    }

}
