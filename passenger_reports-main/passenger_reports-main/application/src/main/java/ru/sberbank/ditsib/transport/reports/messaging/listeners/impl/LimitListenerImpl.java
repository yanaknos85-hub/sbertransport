package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.messaging.messages.LimitMessage;
import ru.sberbank.ditsib.transport.reports.dao.LimitRepository;
import ru.sberbank.ditsib.transport.reports.mappers.LimitsMapper;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.LimitListener;

@RequiredArgsConstructor
@Component("limitInput")
@Slf4j
public class LimitListenerImpl implements LimitListener   {
    private final LimitRepository limitRepository;
    private final LimitsMapper limitsMapper;

    @Override
    public void handleLimit(LimitMessage message) {
        if (!message.isDeleted()) {
            var oldLimit = limitRepository.findById(message.getId());
            var newLimit = limitsMapper.fromMessage(message);
            newLimit.setActive(true);
            if (oldLimit.isPresent()) {
                limitRepository.save(limitsMapper.update(newLimit, oldLimit.get()));
            } else {
                limitRepository.save(newLimit);
            }
        } else {
            var limit = limitRepository.findById(message.getId());
            if(limit.isPresent()){
                limit.get().setActive(false);
                limitRepository.save(limit.get());
            }
        }
    }
}
