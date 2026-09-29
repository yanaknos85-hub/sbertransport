package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.util.ReflectionTestUtils;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.service.impl.RequestForCarsharingServiceImpl;
import ru.sberbank.ditsib.transport.request.service.impl.RequestForPersonalServiceImpl;
import ru.sberbank.ditsib.transport.request.service.impl.RequestForTaxiServiceImpl;
import ru.sberbank.ditsib.transport.request.service.publicTransport.impl.RequestForPublicServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestListenerImplTest {

    @InjectMocks
    private RequestListenerImpl requestListener;
    @Spy
    private JpaRepository<? extends Request, UUID> requestRepository;

    @Test
    void accept() {
        var requestMessageTaxi = Instancio.of(RequestMessage.class)
                .set(field(RequestMessage::getTransportType), TransportTypeEnum.TAXI.name())
                .set(field(RequestMessage::getIsSlaExpired), null)
                .set(field(RequestMessage::getStatusCode), null)
                .create();
        var requestMessageTaxiNotFound = Instancio.of(RequestMessage.class)
                .set(field(RequestMessage::getTransportType), TransportTypeEnum.TAXI.name())
                .set(field(RequestMessage::getIsSlaExpired), null)
                .set(field(RequestMessage::getStatusCode), TripRequestStatus.TaxiStatusCode.TAXI_TRIP_FINISHED_RATED.getCode())
                .create();
        var requestTaxi = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getTransportType), TransportTypeEnum.TAXI)
                .create();
        var requestMessagePersonal = Instancio.of(RequestMessage.class)
                .set(field(RequestMessage::getTransportType), TransportTypeEnum.PERSONAL.name())
                .set(field(RequestMessage::getIsSlaExpired), null)
                .set(field(RequestMessage::getStatusCode), null)
                .create();
        var requestMessagePersonalNotFound = Instancio.of(RequestMessage.class)
                .set(field(RequestMessage::getTransportType), TransportTypeEnum.PERSONAL.name())
                .set(field(RequestMessage::getIsSlaExpired), null)
                .set(field(RequestMessage::getStatusCode), TripRequestStatus.PersonalStatusCode.PERSONAL_NOT_APPROVED_BY_EXPIRATION_TIME.getCode())
                .create();
        var requestPersonal = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getTransportType), TransportTypeEnum.PERSONAL)
                .create();
        var requestMessageCarsharing = Instancio.of(RequestMessage.class)
                .set(field(RequestMessage::getTransportType), TransportTypeEnum.CARSHARING.name())
                .set(field(RequestMessage::getIsSlaExpired), null)
                .set(field(RequestMessage::getStatusCode), null)
                .create();
        var requestMessageCarsharingNotFound = Instancio.of(RequestMessage.class)
                .set(field(RequestMessage::getTransportType), TransportTypeEnum.CARSHARING.name())
                .set(field(RequestMessage::getIsSlaExpired), null)
                .set(field(RequestMessage::getStatusCode), TripRequestStatus.CarsharingStatusCode.CARSHARING_TRIP_FINISHED_RATED.getCode())
                .create();
        var requestCarsharing = Instancio.of(RequestForCarsharing.class)
                .set(field(RequestForCarsharing::getTransportType), TransportTypeEnum.CARSHARING)
                .create();
        var requestMessagePublic = Instancio.of(RequestMessage.class)
                .set(field(RequestMessage::getTransportType), TransportTypeEnum.PUBLIC.name())
                .set(field(RequestMessage::getIsSlaExpired), null)
                .set(field(RequestMessage::getStatusCode), null)
                .create();
        var requestMessagePublicNotFound = Instancio.of(RequestMessage.class)
                .set(field(RequestMessage::getTransportType), TransportTypeEnum.PUBLIC.name())
                .set(field(RequestMessage::getIsSlaExpired), null)
                .set(field(RequestMessage::getStatusCode), TripRequestStatus.PublicStatusCode.PUBLIC_NOT_APPROVED_BY_EXPIRATION_TIME.getCode())
                .create();
        var requestPublic = Instancio.of(RequestForPublic.class)
                .set(field(RequestForPublic::getTransportType), TransportTypeEnum.PUBLIC)
                .create();
        var requestForTaxiTransportTypeService = Instancio.create(RequestForTaxiServiceImpl.class);
        var requestForPublicTransportTypeService = Instancio.create(RequestForPublicServiceImpl.class);
        var requestForPersonalTransportTypeService = Instancio.create(RequestForPersonalServiceImpl.class);
        var requestForCarsharingTransportTypeService = Instancio.create(RequestForCarsharingServiceImpl.class);
        var services = List.of(requestForTaxiTransportTypeService,
                requestForPublicTransportTypeService,
                requestForPersonalTransportTypeService,
                requestForCarsharingTransportTypeService);
        ReflectionTestUtils.setField(requestListener, "services", services);
        doReturn(Optional.of(requestTaxi)).when(requestRepository).findById(requestMessageTaxi.getId());
        doReturn(Optional.of(requestPersonal)).when(requestRepository).findById(requestMessagePersonal.getId());
        doReturn(Optional.of(requestPublic)).when(requestRepository).findById(requestMessagePublic.getId());
        doReturn(Optional.of(requestCarsharing)).when(requestRepository).findById(requestMessageCarsharing.getId());
        doReturn(Optional.empty()).when(requestRepository).findById(requestMessageTaxiNotFound.getId());
        doReturn(Optional.empty()).when(requestRepository).findById(requestMessagePersonalNotFound.getId());
        doReturn(Optional.empty()).when(requestRepository).findById(requestMessagePublicNotFound.getId());
        doReturn(Optional.empty()).when(requestRepository).findById(requestMessageCarsharingNotFound.getId());

        requestListener.accept(MessageBuilder.withPayload(requestMessageTaxi).build());
        requestListener.accept(MessageBuilder.withPayload(requestMessagePersonal).build());
        requestListener.accept(MessageBuilder.withPayload(requestMessagePublic).build());
        requestListener.accept(MessageBuilder.withPayload(requestMessageCarsharing).build());
        requestListener.accept(MessageBuilder.withPayload(requestMessageTaxiNotFound).build());
        requestListener.accept(MessageBuilder.withPayload(requestMessagePersonalNotFound).build());
        requestListener.accept(MessageBuilder.withPayload(requestMessagePublicNotFound).build());
        requestListener.accept(MessageBuilder.withPayload(requestMessageCarsharingNotFound).build());
        verify(requestRepository, times(8)).findById(any(UUID.class));
    }
}