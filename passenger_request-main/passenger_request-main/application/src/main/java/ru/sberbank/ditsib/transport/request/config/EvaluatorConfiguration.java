package ru.sberbank.ditsib.transport.request.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.dao.RequestRepository;
import ru.sberbank.ditsib.transport.request.evaluators.Evaluator;
import ru.sberbank.ditsib.transport.request.evaluators.impl.*;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestRatingSender;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class EvaluatorConfiguration {
    
    @Bean
    Map<TransportTypeEnum, Evaluator> evaluators(RequestRepository requestRepository, RequestRatingSender requestRatingSender) {
        var dummyEvaluator = new EvaluatorDummyImpl();
        Map<TransportTypeEnum, Evaluator> evaluators = new HashMap<>();
        for (var transportType : TransportTypeEnum.values()) {
            switch (transportType) {
                case TAXI -> evaluators.put(transportType, new EvaluatorTaxiImpl(requestRepository, requestRatingSender));
                case PERSONAL -> evaluators.put(transportType, new EvaluatorPersonalImpl(requestRepository, requestRatingSender));
                case PUBLIC -> evaluators.put(transportType, new EvaluatorPublicImpl(requestRepository, requestRatingSender));
                case CARSHARING -> evaluators.put(transportType, new EvaluatorCarsharingImpl(requestRepository, requestRatingSender));
                case GROUP_TRANSFER -> evaluators.put(transportType, new EvaluatorGroupTransferImpl(requestRepository, requestRatingSender));
                default -> evaluators.put(transportType, dummyEvaluator);
            }
        }
        return evaluators;
    }
    
    ;
}
