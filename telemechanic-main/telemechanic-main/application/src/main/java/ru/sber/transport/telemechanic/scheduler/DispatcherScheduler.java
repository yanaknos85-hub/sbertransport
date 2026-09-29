package ru.sber.transport.telemechanic.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sber.transport.telemechanic.service.DispatcherService;

@Slf4j
@Component
@RequiredArgsConstructor
public class DispatcherScheduler {
    
    private final DispatcherService dispatcherService;
    
    @Scheduled(cron = "${scheduler.dispatcher.cron}")
    @SchedulerLock(name = "dispatcherScheduler",
                   lockAtLeastFor = "${scheduler.dispatcher.lock-at-least-for}",
                   lockAtMostFor = "${scheduler.dispatcher.lock-at-most-for}")
    public void schedule() {
        log.info("start dispatcher scheduler");
        dispatcherService.deactivateDispatchers();
        log.info("end dispatcher scheduler");
    }
}
