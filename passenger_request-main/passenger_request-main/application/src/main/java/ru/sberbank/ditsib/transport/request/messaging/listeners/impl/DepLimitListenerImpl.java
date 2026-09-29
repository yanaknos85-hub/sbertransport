package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.request.messaging.message.LimitMessage;
import ru.sberbank.ditsib.transport.request.database.dao.DepLimitRepository;
import ru.sberbank.ditsib.transport.request.database.model.DepLimit;
import ru.sberbank.ditsib.transport.request.mappers.DepLimitMapper;

import java.util.Optional;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Slf4j
public class DepLimitListenerImpl implements Consumer<Message<LimitMessage>> {
    
    private final DepLimitMapper depLimitMapper;
    
    private final DepLimitRepository depLimitRepository;
    
    public static final String DEPARTMENT_TYPE = "DEPARTMENT";
    
    public void accept(Message<LimitMessage> message) {
        handleDepLimits(message.getPayload());
    }
    
    private void handleDepLimits(LimitMessage message) {
        // Если transportType указан, значит в сообщении инфа о распределении, а не о лимите
        if (!DEPARTMENT_TYPE.equals(message.getLimitType()) || message.getTransportType() != null) {
            return;
        }
        if (message.isDeleted()) {
            try {
                var depLimit = depLimitRepository.findByIdAndActive(message.getLimitId(), true);
                if (depLimit.isPresent()) {
                    depLimit.get().setActive(false);
                    depLimitRepository.save(depLimit.get());
                }
            } catch (Exception e) {
                log.warn("DepLimitListenerImpl: handleDepLimits: depLimit not found: " + message.getLimitId());
            }
        } else {
            Optional<DepLimit> depLimitOpt = depLimitRepository.findByDepartmentIdAndYearAndActive(message.getDepartmentId(), message.getYear(),
                                                                                                   true);
            if (depLimitOpt.isEmpty()) {
                var limit = depLimitMapper.toModel(message);
                limit.setActive(true);
                depLimitRepository.save(limit);
            } else {
                var depLimit = depLimitOpt.get();
                depLimit.setOwnerId(message.getOwnerId());
                depLimit.setActive(true);
                depLimitRepository.save(depLimit);
            }
        }
    }
}
