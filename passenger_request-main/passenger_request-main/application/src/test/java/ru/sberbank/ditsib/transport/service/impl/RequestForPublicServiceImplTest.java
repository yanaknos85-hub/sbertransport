package ru.sberbank.ditsib.transport.service.impl;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.payout.messaging.message.RequestPayoutMessage;
import ru.sberbank.ditsib.transport.constants.PublicTransportType;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPublic;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.TransportCompensation;
import ru.sberbank.ditsib.transport.request.mappers.*;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestPayoutSender;
import ru.sberbank.ditsib.transport.request.service.publicTransport.impl.RequestForPublicServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequestForPublicServiceImplTest {

    @Spy
    private RequestMapper requestMapper = new RequestMapperImpl(
            new WaypointsMapperImpl(
                    new AddressMapperImpl()
            ),
            new ExpectedDataMapperImpl(),
            new FraudMapperImpl()
    );
    @Mock
    private RequestPayoutSender requestPayoutSender;

    @InjectMocks
    private RequestForPublicServiceImpl requestForPublicService;

    @Test
    void approveRequest() {
        var request = Instancio.of(RequestForPublic.class)
                .set(Select.field(RequestForPublic::getTransportCompensation), List.of(
                        Instancio.of(TransportCompensation.class)
                                .set(Select.field(TransportCompensation::getTransportType), PublicTransportType.CITY_BUS)
                                .create()
                ))
                .create();
        requestForPublicService.approveRequest(request);

        var changeDateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        var payoutRequestMessageCaptor = ArgumentCaptor.forClass(RequestPayoutMessage.class);
        verify(requestMapper).requestToRequestPayoutMessage(any(RequestForPublic.class), changeDateCaptor.capture());
        verify(requestPayoutSender).send(payoutRequestMessageCaptor.capture());

        var actual = payoutRequestMessageCaptor.getValue();
        assertThat(actual)
                .isNotNull()
                .extracting(
                        RequestPayoutMessage::id,
                        RequestPayoutMessage::humanReadableId,
                        RequestPayoutMessage::costCenter,
                        RequestPayoutMessage::resource,
                        RequestPayoutMessage::organizationId,
                        RequestPayoutMessage::actualCost,
                        RequestPayoutMessage::employeeId,
                        RequestPayoutMessage::changeDate,
                        RequestPayoutMessage::transportType
                )
                .containsExactly(
                        request.getId(),
                        request.getHumanReadableId(),
                        request.getPassenger().getCostCenter(),
                        "26511",
                        request.getOrganizationId(),
                        BigDecimal.valueOf(request.getExpected().getCost().longValue(), 2),
                        request.getPassenger().getId(),
                        changeDateCaptor.getValue().atOffset(ZoneOffset.UTC),
                        "PUBLIC"
                );
    }
}
