package ru.sberbank.ditsib.transport.tariff.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.sberbank.ditsib.transport.tariff.database.dao.ContractorRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.BaseTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.TaxiTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.NewScooterTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.NewTaxiTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.TariffSearchDTO;
import ru.sberbank.ditsib.transport.tariff.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.TariffSender;
import ru.sberbank.ditsib.transport.tariff.service.ContractService;
import ru.sberbank.ditsib.transport.tariff.service.GeoZoneService;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.SCOOTER;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.TAXI;

@ExtendWith(MockitoExtension.class)
class TariffControllerServiceImplTest {
    @InjectMocks
    private TariffControllerServiceImpl tariffControllerService;
    @Mock
    private TariffService tariffService;
    @Mock
    private ContractService contractService;
    @Mock
    private TariffSender tariffSender;
    @Mock
    private GeoZoneService geoZoneService;
    @Mock
    private EntityDTOMapper mapper;
    @Mock
    private ContractorRepository contractorRepository;
    @Captor
    private ArgumentCaptor<BaseTariff> baseTariffArgumentCaptor;

    @Test
    void search() {
        var tariffSearchDTO = Instancio.create(TariffSearchDTO.class);
        var organizationId = UUID.randomUUID();
        var dataMaster = Instancio.of(boolean.class)
                .create();
        var pageRequest = PageRequest.of(0, 10);
        var baseTariffs = Instancio.ofList(BaseTariff.class)
                .size(5)
                .create();
        var tariffPage = new PageImpl<>(baseTariffs, pageRequest, 5);
        doReturn(tariffPage).when(tariffService).search(tariffSearchDTO, organizationId, dataMaster, pageRequest);

        assertThat(tariffControllerService.search(tariffSearchDTO, organizationId, dataMaster, pageRequest))
                .isNotEmpty();
    }

    @Test
    void editEntity() {
        var tariffId = UUID.randomUUID();
        var newBaseTariffDto = Instancio.create(NewTaxiTariffDTO.class);
        var taxiTariff = Instancio.create(TaxiTariff.class);
        var geoZone = Instancio.create(GeoZone.class);
        var baseTariff = Instancio.of(BaseTariff.class)
                .set(field(BaseTariff::getTransportType), TAXI)
                .create();
        doReturn(Optional.of(baseTariff)).when(tariffService).get(TAXI.getId(), tariffId);
        doReturn(taxiTariff).when(tariffService).newDtoToTaxiTariff(any(NewTaxiTariffDTO.class));
        doReturn(Optional.of(geoZone)).when(geoZoneService).get(taxiTariff.getRegionId());
        doReturn(baseTariff).when(tariffService).save(baseTariffArgumentCaptor.capture());
        doNothing().when(tariffSender).send(baseTariff, false);
        tariffControllerService.edit(TAXI.getId(), tariffId, newBaseTariffDto);
        var active = baseTariffArgumentCaptor.getValue();
        assertThat(active)
                .usingRecursiveComparison()
                .ignoringFields("humanReadableId",
                        "organization",
                        "region",
                        "id",
                        "serviceType",
                        "transportType",
                        "active")
                .isEqualTo(taxiTariff);
        assertThat(active.getHumanReadableId()).isEqualTo(baseTariff.getHumanReadableId());
        assertThat(active.getOrganization().getId()).isEqualTo(baseTariff.getOrganization().getId());
        assertThat(active.getRegion()).isEqualTo(geoZone.getName());
        assertThat(active.getServiceType()).isEqualTo(baseTariff.getServiceType());
        assertThat(active.getTransportType()).isEqualTo(baseTariff.getTransportType());
        assertThat(active.isActive()).isTrue();
    }

    @Test
    void editEntityNoTariffInDb() {
        var tariffId2 = UUID.randomUUID();
        var newBaseTariffDto2 = Instancio.create(NewScooterTariffDTO.class);
        var baseTariff2 = Instancio.of(BaseTariff.class)
                .set(field(BaseTariff::getTransportType), SCOOTER)
                .create();
        doReturn(Optional.of(baseTariff2)).when(tariffService).get(SCOOTER.getId(), tariffId2);
        doReturn(null).when(mapper).newDtoToScooterTariff(any(NewScooterTariffDTO.class));
        doReturn(baseTariff2).when(tariffService).save(baseTariffArgumentCaptor.capture());
        doNothing().when(tariffSender).send(baseTariff2, false);
        tariffControllerService.edit(SCOOTER.getId(), tariffId2, newBaseTariffDto2);
        var actual2 = baseTariffArgumentCaptor.getValue();
        assertThat(actual2)
                .usingRecursiveComparison()
                .ignoringFields("organization", "regionId")
                .isEqualTo(baseTariff2);
        assertThat(actual2.getOrganization().getId()).isEqualTo(newBaseTariffDto2.getOrganizationId());
        assertThat(actual2.getRegionId()).isEqualTo(newBaseTariffDto2.getRegionId().iterator().next());
    }
}
