package ru.sber.transport.telemechanic.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sber.transport.telemechanic.service.EwbService;

/**
 * Планировщик. Изменение статусов для ЭПЛ
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EwbStatusUpdateScheduler {
    
    private final EwbService service;
    
    @Scheduled(cron = "${scheduler.status.ewb.cron}")
    @SchedulerLock(name = "ewbStatusUpdateScheduler",
                   lockAtLeastFor = "${scheduler.status.ewb.lock-at-least-for}",
                   lockAtMostFor = "${scheduler.status.ewb.lock-at-most-for}")
    public void updateStatus() {
        log.info("Start ewb status scheduler");
        service.statusAutoUpdate();
        log.info("End ewb status scheduler");
    }
}
