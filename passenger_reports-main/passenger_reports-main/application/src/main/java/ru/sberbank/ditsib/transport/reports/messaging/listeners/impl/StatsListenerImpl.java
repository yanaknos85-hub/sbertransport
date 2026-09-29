package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.messaging.messages.StatsMessage;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.StatsListener;
import ru.sberbank.ditsib.transport.reports.model.Stats;
import ru.sberbank.ditsib.transport.reports.service.StatsService;

import java.time.LocalDateTime;

/**
 * Реализация слушателя целей поездки.
 */
@RequiredArgsConstructor
@Component("statsInput")
class StatsListenerImpl implements StatsListener   {
    
    private final StatsService statsService;

    @Override
    public void handle(StatsMessage message) {
        Stats stats =
                statsService.getByMonthAndYearAndOrganizationIdAndServiceTypeAndTransportType(
                        message.getMonth(), message.getYear(), message.getOrganizationId(), message.getServiceType(), message.getTransportType())
                            .orElse(new Stats());
        
        stats.setCreationTime(LocalDateTime.now());
        stats.setOrganizationId(message.getOrganizationId());
        stats.setYear(message.getYear());
        stats.setMonth(message.getMonth());
        stats.setServiceType(message.getServiceType());
        stats.setTransportType(message.getTransportType());
        stats.setTotalExecuted(message.getTotalExecuted());
        stats.setTotalCanceled(message.getTotalCanceled());
        stats.setTotalNotExecuted(message.getTotalNotExecuted());
        stats.setTotalSum(message.getTotalSum());
        stats.setSlaWithoutViolation(message.getSlaWithoutViolation());
        stats.setSlaWithViolation(message.getSlaWithViolation());
        stats.setCsiStarPositive(message.getCsiStarPositive());
        stats.setCsiStarNegative(message.getCsiStarNegative());
        
        statsService.save(stats);
    }

}
