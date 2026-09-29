package ru.sberbank.ditsib.transport.srm.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.srm.model.SrmRequestDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sberbank.ditsib.transport.srm.controller.impl.mapper.RequestMapper;
import ru.sberbank.ditsib.transport.srm.dto.internal.SrmMultipleRequestDTO;
import ru.sberbank.ditsib.transport.srm.messaging.senders.SharedRideSender;
import ru.sberbank.ditsib.transport.srm.service.SrmService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class SrmControllerServiceImplTest {

    @InjectMocks
    private SrmControllerServiceImpl srmControllerService;
    @Mock
    private RequestMapper requestMapper;
    @Mock
    private SrmService srmService;
    @Mock
    private SharedRideSender sharedRideSender;

    @Test
    void addNew() {
        var srmRequestDTO1 = Instancio.of(SrmRequestDTO.class)
                .set(field(SrmRequestDTO::getRequestId), UUID.randomUUID())
                .set(field(SrmRequestDTO::getPickupTime), null)
                .create();
        var srmRequestDTO2 = Instancio.of(SrmRequestDTO.class)
                .set(field(SrmRequestDTO::getRequestId), UUID.randomUUID())
                .set(field(SrmRequestDTO::getPickupTime), null)
                .create();
        var srmMultipleRequestDTO1 = Instancio.create(SrmMultipleRequestDTO.class);
        var srmMultipleRequestDTO2 = Instancio.create(SrmMultipleRequestDTO.class);
        var expected = Instancio.create(SrmSharedRideDTO.class);
        doReturn(srmMultipleRequestDTO1).when(requestMapper)
                .mapSrmRequestDtoToSrmMultipleRequestDto(srmRequestDTO1);
        doReturn(srmMultipleRequestDTO2).when(requestMapper)
                .mapSrmRequestDtoToSrmMultipleRequestDto(srmRequestDTO2);
        doReturn(expected).when(srmService)
                .addNew(srmMultipleRequestDTO1, null, true);
        doReturn(null).when(srmService)
                .addNew(srmMultipleRequestDTO2, null, true);
        doNothing().when(sharedRideSender).send(expected);
        assertThat(srmControllerService.addNew(srmRequestDTO1))
                .usingRecursiveComparison()
                .isEqualTo(expected);
        assertThat(srmControllerService.addNew(srmRequestDTO2))
                .isNull();
    }
}