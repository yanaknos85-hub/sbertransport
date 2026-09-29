package ru.sber.transport.trips.cargo.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.trips.cargo.web.service.DriverAssigningService;

/**
 * Шедулер отправки поездок на исполнение контрагенту
 */
@RequiredArgsConstructor
@Service
@Slf4j
class DriverAssigningProcessor {
    
    private final DriverAssigningService driverAssigningService;

    @Scheduled(cron = "${driver.assigning.cron:0 * * * * *}")
    @Transactional
    public void scheduleDriverAssigning() {
        driverAssigningService.assignDrivers();
    }
}
