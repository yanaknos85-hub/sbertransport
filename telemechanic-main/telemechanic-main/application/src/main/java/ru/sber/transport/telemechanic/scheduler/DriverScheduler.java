package ru.sber.transport.telemechanic.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sber.transport.telemechanic.service.DriverService;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriverScheduler {
    
    private final DriverService driverService;
    
    @Scheduled(cron = "${scheduler.driver.cron}")
    @SchedulerLock(name = "driverScheduler",
                   lockAtLeastFor = "${scheduler.driver.lock-at-least-for}",
                   lockAtMostFor = "${scheduler.driver.lock-at-most-for}")
    public void schedule() {
        log.info("start driver scheduler");
        driverService.deactivateDrivers();
        log.info("end driver scheduler");
    }
}
