package ru.sber.transport.request.external.application.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import ru.sber.transport.request.external.business.TripOrdersService;

/**
 * Конфигурация задач по расписанию
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "schedulers.enabled", havingValue = "true", matchIfMissing = true)
@EnableScheduling
@RequiredArgsConstructor
public class Schedulers {

    private final ObjectProvider<TripOrdersService> tripOrders;

    /**
     * Инициализация задач по расписанию
     */
    @PostConstruct
    public void init() {
        log.info("Initializing schedulers");
    }

    /**
     * Задача по рассылке напоминаний о поездке
     */
    @SchedulerLock(name = "reminder", lockAtMostFor = "PT1M")
    @Scheduled(cron = "0 * * * * *")
    public void reminders() {
        tripOrders.ifAvailable(TripOrdersService::remind);
    }

}