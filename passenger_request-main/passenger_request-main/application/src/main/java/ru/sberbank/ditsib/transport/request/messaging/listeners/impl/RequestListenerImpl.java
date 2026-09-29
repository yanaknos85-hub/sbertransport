package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.messaging.Message;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.dto.CancelDTO;
import ru.sberbank.ditsib.transport.request.service.TransportTypeService;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.*;

@Slf4j
@RequiredArgsConstructor
@Transactional
public class RequestListenerImpl implements Consumer<Message<RequestMessage>> {

    private final List<TransportTypeService<? extends Request>> services;
    private final JpaRepository<? extends Request, UUID> requestRepository;

    public void accept(Message<RequestMessage> message) {
        handle(message.getPayload().getId(), message.getPayload());
    }

    private void handle(UUID key, RequestMessage message) {
        log.debug("RequestId: {}, sla expired: {}", message.getId(), message.getIsSlaExpired());
        var request = requestRepository.findById(key);
        if (message.getTransportType().equals(TAXI.name())) {
            var service = getService(RequestForTaxi.class);
            request.ifPresent(value -> {
                var status = TripRequestStatus.TaxiStatusCode.findByCode(message.getStatusCode());
                if (status.isPresent()) {
                    var taxiStatusCode = status.get();
                    if (taxiStatusCode == TripRequestStatus.TaxiStatusCode.TAXI_CANCELLED_BY_EXPIRATION_TIME) {
                        var cancelDto = createCancelObject(status.get().getCode(), status.get().getDescription());
                        service.cancel(value, cancelDto, value.getAuthor());
                    } else if (taxiStatusCode == TripRequestStatus.TaxiStatusCode.TAXI_TRIP_FINISHED_NOT_RATED) {
                        service.finishByExpiration((RequestForTaxi) value, value.getAuthor());
                    }
                }
            });
        } else if (message.getTransportType().equals(PERSONAL.name())) {
            request.ifPresent(value -> {
                if (message.getIsSlaExpired() != null && message.getIsSlaExpired()) {
                    getService(RequestForPersonal.class).markSlaExpired(key);
                } else {
                    var status = TripRequestStatus.PersonalStatusCode.findByCode(message.getStatusCode());
                    if (status.isPresent()) {
                        var cancelDto = createCancelObject(status.get().getCode(), status.get().getDescription());
                        getService(RequestForPersonal.class).cancel(value, cancelDto, value.getAuthor());
                    }
                }
            });
        } else if (message.getTransportType().equals(CARSHARING.name())) {
            request.ifPresent(value -> {
                var status = TripRequestStatus.CarsharingStatusCode.findByCode(message.getStatusCode());
                if (status.isPresent()) {
                    var cancelDto = createCancelObject(status.get().getCode(), status.get().getDescription());
                    getService(RequestForCarsharing.class).cancel(value, cancelDto, value.getAuthor());
                }
            });
        } else if (message.getTransportType().equals(PUBLIC.name())) {
            request.ifPresent(value -> {
                if (message.getIsSlaExpired() != null && message.getIsSlaExpired()) {
                    getService(RequestForPublic.class).markSlaExpired(key);
                } else {
                    var status = TripRequestStatus.PublicStatusCode.findByCode(message.getStatusCode());
                    if (status.isPresent()) {
                        var cancelDto = createCancelObject(status.get().getCode(), status.get().getDescription());
                        getService(RequestForPublic.class).cancel(value, cancelDto, value.getAuthor());
                    }
                }
            });
        }
    }

    private CancelDTO createCancelObject(int code, String description) {
        return CancelDTO.builder()
                .code(code)
                .reason(description)
                .build();
    }

    private <T extends Request> TransportTypeService<T> getService(Class<T> valueClass) {
        var type = ResolvableType.forClassWithGenerics(TransportTypeService.class, valueClass);
        var service = services.stream()
                .filter(type::isInstance)
                .findFirst()
                .orElseThrow();
        return ReflectionUtils.cast(service);
    }
}
