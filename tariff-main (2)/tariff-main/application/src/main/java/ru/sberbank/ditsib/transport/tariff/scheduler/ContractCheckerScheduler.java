package ru.sberbank.ditsib.transport.tariff.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.tariff.service.ContractService;

@RequiredArgsConstructor
@Component
@Slf4j
public class ContractCheckerScheduler {
    
    private final ContractService contractService;
    
    
    @Scheduled(cron = "${SCHEDULER_CONTRACT_CHECK:0 0 21 * * *}")
    @SchedulerLock(name = "SCHEDULER_CONTRACT_CHECK")
    public void checkContracts() {
        try {
            log.info("Проверка завершённых контрактов");
            contractService.checkExpiredContracts();
        } catch (Exception e) {
            log.error("Ошибка во время проверки контрактов", e);
        }
    }
}

