package ru.sberbank.ditsib.transport.reports.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.reports.enums.InboxMessageStatusEnum;
import ru.sberbank.ditsib.transport.reports.model.InboxMessage;
import ru.sberbank.ditsib.transport.reports.service.InboxMessageService;

import java.util.Comparator;

@Component
@RequiredArgsConstructor
@Slf4j
class InboxMessageScheduler {
    
    private final InboxMessageService service;
    
    @Scheduled(cron = "${SCHEDULER_INBOX_MESSAGES_READ:*/10 * * * * *}")
    @SchedulerLock(name = "SCHEDULER_INBOX_MESSAGES_READ")
    public void processNewMessages() {
        log.info("Запускаем обработку входящих сообщений");
        service.findAllByStatus(InboxMessageStatusEnum.NEW.name())
               .stream()
               .sorted(Comparator.comparing(InboxMessage::getReceivedAt))
               .forEach(service::process);
        log.info("Обработка входящих сообщений завершена");
    }
    
    @Scheduled(cron = "${SCHEDULER_INBOX_MESSAGES_DELETE: 0 0 3 * * *}")
    @SchedulerLock(name = "SCHEDULER_INBOX_MESSAGES_DELETE")
    public void delete() {
        log.info("Запускаем очистку таблицы с сообщениями из кафки");
        int count = service.deleteAllOlderThan(14);
        log.info("Очистка таблицы с сообщениями из кафки завершена. Удалено {} записей", count);
    }
    
}
