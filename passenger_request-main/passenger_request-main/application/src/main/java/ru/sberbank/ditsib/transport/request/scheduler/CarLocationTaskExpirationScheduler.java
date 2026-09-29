package ru.sberbank.ditsib.transport.request.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.request.service.CarLocationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarLocationTaskExpirationScheduler {

    private final CarLocationService carLocationService;

    @Scheduled(cron = "${scheduler.car-location-task-expiration.cron}")
    @SchedulerLock(name = "carLocationTaskExpirationScheduler",
            lockAtLeastFor = "${scheduler.car-location-task-expiration.lock-at-least-for}",
            lockAtMostFor = "${scheduler.car-location-task-expiration.lock-at-most-for}")
    public void schedule() {
        log.info("start car location task expiration scheduler");
        carLocationService.deleteExpiredTasks();
        log.info("end car location task expiration scheduler");
    }
}
