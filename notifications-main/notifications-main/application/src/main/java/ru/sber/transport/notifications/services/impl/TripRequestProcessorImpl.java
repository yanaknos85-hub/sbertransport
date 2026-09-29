package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.services.Processor;
import ru.sber.transport.notifications.services.TripApproveService;

/**
 * Реализация процессора для заявок на поездку.
 */
@RequiredArgsConstructor
@Component
@Slf4j
class TripRequestProcessorImpl implements Processor<TripRequest> {
    
    private final TripApproveService approveService;

    @Qualifier("approveProcessor")
    private final Processor<TripApprove> approveProcessor;

    @Override
    public void process(TripRequest data) throws JsonProcessingException {
        log.debug(String.format("Processing request with ID %s", data.getId()));
        var approveOptional = approveService.getByRequestId(data.getId());
        log.debug(String.format("Approval for request with ID %s %s found", data.getId(),
                                    approveOptional.isEmpty() ? "NOT" : ""));
        if(approveOptional.isPresent()) {
            approveProcessor.process(approveOptional.get());
        }
    }
}
