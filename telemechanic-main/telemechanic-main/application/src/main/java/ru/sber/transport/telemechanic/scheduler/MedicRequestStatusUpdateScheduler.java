package ru.sber.transport.telemechanic.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sber.transport.telemechanic.service.TelemedicineService;

/**
 * Планировщик. Изменение статусов для заявок на мед. осмотр
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MedicRequestStatusUpdateScheduler {
    
    private final TelemedicineService service;
    
    @Scheduled(cron = "${scheduler.status.medic-request.cron}")
    @SchedulerLock(name = "medicRequestStatusUpdateScheduler",
                   lockAtLeastFor = "${scheduler.status.medic-request.lock-at-least-for}",
                   lockAtMostFor = "${scheduler.status.medic-request.lock-at-most-for}")
    public void updateStatus() {
        log.info("Start medic request status scheduler");
        service.statusAutoUpdate();
        log.info("End medic request status scheduler");
    }
}
