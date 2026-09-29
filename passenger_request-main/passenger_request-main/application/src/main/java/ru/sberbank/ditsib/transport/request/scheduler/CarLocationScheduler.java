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
public class CarLocationScheduler {

    private final CarLocationService carLocationService;

    @Scheduled(cron = "${scheduler.car-location.cron}")
    @SchedulerLock(name = "carLocationScheduler",
            lockAtLeastFor = "${scheduler.car-location.lock-at-least-for}",
            lockAtMostFor = "${scheduler.car-location.lock-at-most-for}")
    public void schedule() {
        log.info("start car location scheduler");
        carLocationService.sendRequestBatch();
        log.info("end car location scheduler");
    }
}
