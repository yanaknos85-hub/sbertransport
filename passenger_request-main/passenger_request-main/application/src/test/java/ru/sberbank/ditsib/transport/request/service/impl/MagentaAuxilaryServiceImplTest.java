package ru.sberbank.ditsib.transport.request.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.srm.model.SrmRequestDTO;
import ru.sber.transport.srm.model.SrmRequestKpiDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPersonal;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.service.SrmService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class MagentaAuxilaryServiceImplTest {

    @InjectMocks
    private MagentaAuxilaryServiceImpl magentaAuxilaryService;
    @Mock
    private EntityDTOMapper entityDTOMapper;
    @Mock
    private SrmService srmService;

    @Test
    void processCoopRequest() {
        var token = Instancio.create(String.class);
        var rideId = UUID.randomUUID();
        var request1 = Instancio.create(RequestForPersonal.class);
        var request2 = Instancio.create(RequestForPersonal.class);
        var request3 = Instancio.create(RequestForPersonal.class);
        var srmRequestDTO1 = Instancio.create(SrmRequestDTO.class);
        var srmRequestDTO2 = Instancio.create(SrmRequestDTO.class);
        var srmRequestDTO3 = Instancio.create(SrmRequestDTO.class);
        var srmRequestKpiDTO = Instancio.of(SrmRequestKpiDTO.class)
                .set(field(SrmRequestKpiDTO::getId), request1.getId())
                .create();
        var srmSharedRideDTO1 = Instancio.of(SrmSharedRideDTO.class)
                .set(field(SrmSharedRideDTO::getRequestKpiList), List.of(Instancio.create(SrmRequestKpiDTO.class),
                        Instancio.create(SrmRequestKpiDTO.class),
                        srmRequestKpiDTO))
                .create();
        var srmSharedRideDTO2 = Instancio.create(SrmSharedRideDTO.class);
        doReturn(srmRequestDTO1).when(entityDTOMapper).requestToSrmRequestDTO(request1);
        doReturn(srmRequestDTO2).when(entityDTOMapper).requestToSrmRequestDTO(request2);
        doReturn(srmRequestDTO3).when(entityDTOMapper).requestToSrmRequestDTO(request3);
        doReturn(srmSharedRideDTO1).when(srmService).addRequestToSharedRide(rideId, srmRequestDTO1, token);
        doReturn(srmSharedRideDTO2).when(srmService).postNewSharedRide(srmRequestDTO2, token);
        doReturn(null).when(srmService).postNewSharedRide(srmRequestDTO3, token);
        var actual1 = magentaAuxilaryService.processCoopRequest(rideId, request1, token);
        assertThat(actual1)
                .usingRecursiveComparison()
                .isEqualTo(srmSharedRideDTO1);
        assertThat(request1.getRideId()).isEqualTo(srmSharedRideDTO1.getId());
        assertThat(request1.getCostSharePart()).isEqualTo(srmRequestKpiDTO.getCostSharePart());
        assertThat(request1.getSavingsCash()).isEqualTo(srmRequestKpiDTO.getSavingsCash());
        assertThat(request1.getSavingsProcents()).isEqualTo(srmRequestKpiDTO.getSavingsProcents().longValue());

        var actual2 = magentaAuxilaryService.processCoopRequest(null, request2, token);
        assertThat(actual2)
                .usingRecursiveComparison()
                .isEqualTo(srmSharedRideDTO2);
        assertThat(request2.getRideId()).isEqualTo(srmSharedRideDTO2.getId());
        assertThat(request2.isSharedRideOwner()).isFalse();
        assertThat(request2.getCostSharePart()).isEqualTo(srmSharedRideDTO2.getRequestKpiList().get(0).getCostSharePart());
        assertThat(request2.getSavingsCash()).isEqualTo(srmSharedRideDTO2.getRequestKpiList().get(0).getSavingsCash());
        assertThat(request2.getSavingsProcents()).isEqualTo(srmSharedRideDTO2.getRequestKpiList().get(0).getSavingsProcents().longValue());

        var actual3 = magentaAuxilaryService.processCoopRequest(null, request3, token);
        assertThat(actual3)
                .usingRecursiveComparison()
                .isNull();
    }
}