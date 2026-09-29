package ru.sberbank.ditsib.transport.reports.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.reports.model.Stats;
import ru.sberbank.ditsib.transport.reports.service.StatsService;

import java.util.Calendar;
import java.util.List;

/**
 * Планировщик отчета такси.
 */
@Component
@RequiredArgsConstructor
@Slf4j
class StatsScheduler {
    
    StatsService statsService;
    
    @Scheduled(cron = "${files.schedulers.taxi:-}")
    @SchedulerLock(name = "FILES_SCHEDULERS_TAXI")
    public void statsScheduled() {
        try {
            log.info("Запускаем планировщик отчета такси");
            tick();
        } catch (Exception e) {
            log.error("Ошибка во время планировки отчёта по такси", e);
        }
    }
    
    private void tick() {
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int prevYear = currentYear;
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int prevMonth = currentMonth - 1;
        if (prevMonth == -1) {
            prevMonth = 11;
            prevYear = currentYear - 1;
        }
        List<Stats> listStats = statsService.findByYearAndMonth(currentYear, currentMonth);
        listStats.addAll(statsService.findByYearAndMonth(prevYear, prevMonth));
    }
}
