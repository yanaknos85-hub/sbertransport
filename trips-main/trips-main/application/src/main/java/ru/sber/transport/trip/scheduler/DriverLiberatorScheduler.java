package ru.sber.transport.trip.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.trip.web.service.DriverService;

/**
 * @deprecated Шедулер освобождения водителей от зависших поездок (Предназначен только на время зависания дефекта)
 */
@Deprecated
@RequiredArgsConstructor
@Service
@Slf4j
public class DriverLiberatorScheduler {

    private final DriverService driverService;

    @Value("${driver.liberator.isEnabled:false}")
    private boolean isEnabled;

    @Scheduled(cron = "${driver.liberator.rate:0 * * * * *}")
    @Transactional
    public void liberateDrivers() {
        if (!isEnabled) {
            log.trace("Driver liberator scheduler is not enabled. Make the necessary settings on the service;");
            return;
        }
        log.trace("Driver liberator scheduler process started;");
        driverService.liberateDrivers();
        log.trace("Driver liberator scheduler process finished;");
    }

}
