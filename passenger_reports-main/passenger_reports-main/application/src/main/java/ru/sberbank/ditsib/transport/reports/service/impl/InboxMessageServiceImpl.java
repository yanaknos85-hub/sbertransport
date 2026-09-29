package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dao.InboxMessageRepository;
import ru.sberbank.ditsib.transport.reports.enums.InboxMessageStatusEnum;
import ru.sberbank.ditsib.transport.reports.model.InboxMessage;
import ru.sberbank.ditsib.transport.reports.scheduler.handlers.InboxMessageHandler;
import ru.sberbank.ditsib.transport.reports.service.InboxMessageService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboxMessageServiceImpl implements InboxMessageService {
    
    private final InboxMessageRepository inboxMessageRepository;
    private final List<InboxMessageHandler> handlers;
    
    @Override
    public void process(InboxMessage message) {
        handlers.stream()
                .filter(h -> h.canHandle(message))
                .findFirst()
                .ifPresentOrElse(h -> {
                    try {
                        h.handle(message);
                        message.setStatus(InboxMessageStatusEnum.DONE.name());
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                        message.setStatus(InboxMessageStatusEnum.ERROR.name());
                        message.setErrorReason(ex.getMessage());
                    } finally {
                        message.setUpdatedAt(LocalDateTime.now());
                        inboxMessageRepository.save(message);
                    }
                }, () -> {
                    message.setErrorReason("Не найден обработчик для данного типа сообщения");
                    message.setUpdatedAt(LocalDateTime.now());
                    message.setStatus(InboxMessageStatusEnum.ERROR.name());
                    inboxMessageRepository.save(message);
                });
    }
    
    @Override
    public List<InboxMessage> findAllByStatus(String status) {
        return inboxMessageRepository.findAllByStatus(status);
    }
    
    @Override
    public int deleteAllOlderThan(int days) {
        return inboxMessageRepository.deleteAllOlderThan(LocalDateTime.now().minusDays(days));
    }
}
