package ru.sber.transport.telemechanic.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sber.transport.telemechanic.service.RequestService;

/**
 * Планировщик. Изменение статусов для заявок на тех. осмотр
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestStatusUpdateScheduler {
    
    private final RequestService service;
    
    @Scheduled(cron = "${scheduler.status.request.cron}")
    @SchedulerLock(name = "requestStatusUpdateScheduler",
                   lockAtLeastFor = "${scheduler.status.request.lock-at-least-for}",
                   lockAtMostFor = "${scheduler.status.request.lock-at-most-for}")
    public void updateStatus() {
        log.info("Start request status scheduler");
        service.statusAutoUpdate();
        log.info("End request status scheduler");
    }
}
