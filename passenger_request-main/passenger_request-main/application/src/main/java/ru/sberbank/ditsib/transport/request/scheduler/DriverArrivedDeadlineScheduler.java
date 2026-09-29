package ru.sberbank.ditsib.transport.request.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.request.service.DriverArrivedDeadlineChecker;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriverArrivedDeadlineScheduler {

    private final DriverArrivedDeadlineChecker driverArrivedDeadlineChecker;

    @Scheduled(cron = "${scheduler.driver-arrived-deadline.cron}")
    @SchedulerLock(name = "driverArrivedDeadlineScheduler",
            lockAtLeastFor = "${scheduler.driver-arrived-deadline.lock-at-least-for}",
            lockAtMostFor = "${scheduler.driver-arrived-deadline.lock-at-most-for}")
    public void schedule() {
        log.info("start driver arrived deadline scheduler");
        driverArrivedDeadlineChecker.execute();
        log.info("end driver arrived deadline scheduler");
    }
}
