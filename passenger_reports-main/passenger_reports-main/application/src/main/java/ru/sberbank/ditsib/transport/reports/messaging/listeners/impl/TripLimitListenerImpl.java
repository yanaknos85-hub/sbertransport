package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.messaging.messages.LimitActionResultMessage;
import ru.sberbank.ditsib.transport.reports.dao.LimitRepository;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.TripLimitListener;
import ru.sberbank.ditsib.transport.reports.model.Limit;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.service.RequestService;

import java.util.UUID;

@RequiredArgsConstructor
@Component("tripLimitInput")
@Slf4j
public class TripLimitListenerImpl implements TripLimitListener   {
    private final RequestService requestService;
    private final LimitRepository limitRepository;

    @Override
    public void handle(LimitActionResultMessage message) {
        var status = message.getLimitReservationStatus();
        if ("RESERVED_FROM_DEPARTMENT".equals(status)
         || "RESERVED_FROM_EMPLOYEE".equals(status)
         || "LIMIT_CANCELLED".equals(status))
        {
            UUID requestId = message.getTripRequestId();
            Request request = requestService.findById(requestId).orElse(null);
            
            if (request != null && message.getLimitId() != null) {
                var limit = limitRepository.findById(message.getLimitId())
                                             .orElseGet(() -> limitRepository.saveAndFlush(Limit.builder()
                                                                                                 .id(message.getLimitId())
                                                                                                 .humanReadableId(message.getHumanReadableId())
                                                                                                 .sum(0L)
                                                                                                 .balance(0L)
                                                                                                 .build()));
                request.setLimit(limit);
                requestService.save(request);
            } else {
                log.warn("TripLimitListenerImpl: handle: request " + requestId + " not found!");
            }
        }

    }
}
