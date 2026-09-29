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
public class NewTripScheduler {

    private final ContractorTripService contractorTripService;

    @Scheduled(cron = "${scheduler.new-trip.cron}")
    @SchedulerLock(name = "newTripScheduler",
            lockAtLeastFor = "${scheduler.new-trip.lock-at-least-for}",
            lockAtMostFor = "${scheduler.new-trip.lock-at-most-for}")
    public void schedule() {
        log.info("start new trip scheduler");
        contractorTripService.processNewTrips();
        log.info("end new trip scheduler");
    }
}
