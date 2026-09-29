package ru.sberbank.ditsib.transport.request.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.request.scheduler.deadline.DeadlineChecker;

import java.util.List;

/**
 * Шедулер для работы с проверками дедлайнов
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class DeadlineScheduler {
    
    private final List<DeadlineChecker> deadlineCheckerList;
    
    @Scheduled(cron = "${SCHEDULED_DEADLINE_CHECK:0 * * * * *}")
    @SchedulerLock(name = "SCHEDULED_DEADLINE_CHECK")
    public void requestDeadlineChecker() {
        log.info("Начинаем проверку контрольных сроков");
        log.debug("Список проверок: {}", deadlineCheckerList);
        for (var dc : deadlineCheckerList) {
            try {
                dc.execute();
            } catch (Exception e) {
                log.error("Во время проверки КС \"{}\" произошла ошибка", dc.info(), e);
            }
        }
    }
}

