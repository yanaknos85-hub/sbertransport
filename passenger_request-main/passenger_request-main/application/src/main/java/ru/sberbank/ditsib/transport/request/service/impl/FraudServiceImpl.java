package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.dao.FraudRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestRepository;
import ru.sberbank.ditsib.transport.request.database.dao.TypedRequestRepository;
import ru.sberbank.ditsib.transport.request.database.model.FraudData;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;
import ru.sberbank.ditsib.transport.request.service.FraudService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
public class FraudServiceImpl implements FraudService {

    private final FraudRepository fraudRepository;

    private final RequestRepository requestRepository;

    private final Map<TransportTypeEnum, TypedRequestRepository<Request>> requestRepositories;

    private final Map<TransportTypeEnum, RequestSender<Request>> senders;

    @Transactional
    @Override
    public void saveAndSend(FraudData fraudData) {
        this.save(fraudData).ifPresent(
                fraud -> {
                    var requestId = fraud.getRequest().getId();

                    requestRepository.findTransportType(requestId)
                            .ifPresent(transportType ->
                                    requestRepositories.get(transportType)
                                            .findById(requestId).
                                            ifPresent(request -> {
                                                log.debug("Sending request data {} fot type {}. Fraud data size: {}",
                                                        fraud.getId(), transportType, Optional.ofNullable(request.getFraudData()).map(List::size).orElse(0));
                                                senders.get(transportType).send(request);
                                            })
                            );
                }
        );
    }

    @Transactional
    @Override
    public Optional<FraudData> save(FraudData fraudData) {
        var type = fraudData.getType();
        var request = fraudData.getRequest();

        if (request == null) {
            log.warn("Cannot save fraud: request is null, type={}", type);
            return Optional.empty();
        }

        var requestId = request.getId();
        if (requestId == null) {
            log.warn("Cannot save fraud: request.id is null, type={}", type);
            return Optional.empty();
        }

        if (fraudRepository.existsByRequestIdAndType(requestId, type)) {
            log.info("Fraud of type {} for request {} already exists, skipping", type, requestId);
            return Optional.empty();
        }
        return Optional.of(fraudRepository.saveAndFlush(fraudData));
    }
}
