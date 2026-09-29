package ru.sberbank.ditsib.transport.request.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.request.service.ContractorTripService;

@Slf4j
@Component
@RequiredArgsConstructor
public class InProgressTripScheduler {

    private final ContractorTripService contractorTripService;

    @Scheduled(cron = "${scheduler.in-progress-trip.cron}")
    @SchedulerLock(name = "inProgressTripScheduler",
            lockAtLeastFor = "${scheduler.in-progress-trip.lock-at-least-for}",
            lockAtMostFor = "${scheduler.in-progress-trip.lock-at-most-for}")
    public void schedule() {
        log.info("start in progress trip scheduler");
        contractorTripService.processTripsInProgress();
        log.info("end in progress trip scheduler");
    }
}
