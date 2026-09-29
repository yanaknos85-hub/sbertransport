package ru.sber.transport.etrn.schedulers.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.etrn.schedulers.EtrnLockSchedulingProcessor;
import ru.sber.transport.etrn.service.LockService;

@RequiredArgsConstructor
@Service
@Slf4j
class EtrnLockSchedulingProcessorImpl implements EtrnLockSchedulingProcessor {

    private final LockService lockService;

    @Override
    @Scheduled(cron = "${schedule.etrn.etrnAutoUnlockFreq}")
    @SchedulerLock(name = "etrn.releaseEtrnLocks", lockAtMostFor = "PT1H")
    @Transactional
    public void releaseEtrnLocks() {
        log.debug("ETRN_AUTO_UNLOCK START: проверка истёкших блокировок");
        lockService.scheduleAutoUnlock();
        log.debug("ETRN_AUTO_UNLOCK END: проверка завершена");
    }
}
