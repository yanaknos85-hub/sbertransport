package ru.sberbank.ditsib.transport.request.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.request.service.StatsService;

/**
 * Шедулер для работы с отправкой статистики
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class StatsScheduler {
    
    private final StatsService statsService;
    
    @Scheduled(cron = "${SCHEDULED_STATS:0 0 21 * * *}")
    @SchedulerLock(name = "SCHEDULED_STATS")
    public void scheduleSendStats() {
        log.info("Начинаем сбор и отправку статистики");
        try {
            statsService.processStats();
        } catch (Exception e) {
            log.error("Произошла ошибка во время выполнения SCHEDULED_STATS", e);
        }
    }
}

