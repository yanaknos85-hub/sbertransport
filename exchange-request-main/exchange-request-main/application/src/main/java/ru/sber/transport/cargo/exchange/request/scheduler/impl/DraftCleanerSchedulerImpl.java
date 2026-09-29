package ru.sber.transport.cargo.exchange.request.scheduler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.sber.transport.cargo.exchange.request.scheduler.DraftCleanerScheduler;
import ru.sber.transport.cargo.exchange.request.service.RequestService;


@RequiredArgsConstructor
@Service
@Slf4j
public class DraftCleanerSchedulerImpl implements DraftCleanerScheduler {
    private final RequestService requestService;

    @Override
    @Scheduled(cron = "${draft.schedule}")
    @SchedulerLock(name = "draft.cleaner", lockAtMostFor = "PT1H")
    public void process() {
        log.info("DraftCleanerSchedulerImpl started");
        long deletedCount = requestService.deleteExpiredDrafts();
        log.info("DraftCleanerSchedulerImpl finished. Deleted {} drafts", deletedCount);
    }
}
