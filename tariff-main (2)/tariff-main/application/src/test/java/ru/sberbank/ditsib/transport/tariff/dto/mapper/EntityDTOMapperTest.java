package ru.sberbank.ditsib.transport.tariff.dto.mapper;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mapstruct.factory.Mappers;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.model.CarSharingTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.Contract;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;
import ru.sberbank.ditsib.transport.tariff.database.model.PublicTariff;
import ru.sberbank.ditsib.transport.tariff.dto.NewPublicTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.PublicTariffDTO;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@ActiveProfiles("test")
class EntityDTOMapperTest {
    
    final EntityDTOMapper mapper = Mappers.getMapper(EntityDTOMapper.class);
    final Organization organization = Organization.builder()
                                                  .id(UUID.randomUUID())
                                                  .active(true)
                                                  .digitId(1L)
                                                  .build();
    
    private final UUID samara = UUID.randomUUID();
    
    @Test
    void newDtoToPublicTariff() {
        NewPublicTariffDTO dto = NewPublicTariffDTO.builder()
                                                   .regionId(Collections.singleton(samara))
                                                   .transportType(TransportTypeEnum.PUBLIC)
                                                   .organizationId(UUID.randomUUID())
                                                   .metroAvailability(true)
                                                   .tramAvailability(true)
                                                   .trolleybusAvailability(true)
                                                   .busAvailability(true)
                                                   .metroTicketCost(50)
                                                   .tramTicketCost(40)
                                                   .trolleybusTicketCost(30)
                                                   .busTicketCost(20)
                                                   .build();
    
        PublicTariff tariff = mapper.newDtoToPublicTariff(dto);
        
        assertThat(tariff.getBusTicketCost()).isEqualTo(dto.getBusTicketCost());
        assertThat(tariff.isMetroAvailability()).isTrue();
        assertThat(dto.getRegionId().contains(tariff.getRegionId())).isTrue();
    }
    
    @Test
    void dtoToPublicTariff() {
        PublicTariffDTO dto = PublicTariffDTO.builder()
                                             .id(UUID.randomUUID())
                                             .regionId(Collections.singleton(samara))
                                             .transportType(TransportTypeEnum.PUBLIC)
                                             .organizationId(UUID.randomUUID())
                                             .humanReadableId("PT-001")
                                             .metroAvailability(true)
                                             .tramAvailability(true)
                                             .trolleybusAvailability(true)
                                             .busAvailability(true)
                                             .metroTicketCost(50)
                                             .tramTicketCost(40)
                                             .trolleybusTicketCost(30)
                                             .busTicketCost(20)
                                             .build();
        PublicTariff tariff = mapper.dtoToPublicTariff(dto);
    
        assertThat(tariff.getBusTicketCost()).isEqualTo(dto.getBusTicketCost());
        assertThat(tariff.getMetroTicketCost()).isEqualTo(dto.getMetroTicketCost());
        assertThat(tariff.isMetroAvailability()).isTrue();
        assertThat(tariff.getOrganization().getId()).isEqualTo(dto.getOrganizationId());
        assertThat(tariff.getHumanReadableId()).isEqualTo(dto.getHumanReadableId());
    }
    
    @Test
    void publicTariffToDTO() {
        PublicTariff tariff = PublicTariff.builder()
                                          .id(UUID.randomUUID())
                                          .humanReadableId("PT-001")
                                          .region("Самарская область")
                                          .regionId(samara)
                                          .organization(organization)
                                          .transportType(TransportTypeEnum.PUBLIC)
                                          .active(true)
                                          .metroTicketCost(50)
                                          .tramTicketCost(40)
                                          .trolleybusTicketCost(30)
                                          .busTicketCost(20)
                                          .metroAvailability(true)
                                          .tramAvailability(true)
                                          .trolleybusAvailability(true)
                                          .busAvailability(true)
                                          .build();
    
        PublicTariffDTO dto = mapper.publicTariffToDTO(tariff);
        
        assertThat(dto.getHumanReadableId()).isEqualTo(tariff.getHumanReadableId());
        assertThat(dto.getRegionId().contains(tariff.getRegionId())).isTrue();
        assertThat(dto.getOrganizationId()).isEqualTo(tariff.getOrganization().getId());
        assertThat(dto.getTramAvailability()).isTrue();
        assertThat(dto.getBusTicketCost()).isEqualTo(tariff.getBusTicketCost());
    }
    
    @Test
    void carsharingTariffToDTO() {
        var tariff = CarSharingTariff.builder()
                                     .id(UUID.randomUUID()).organization(organization).region("Region")
                                     .regionId(UUID.randomUUID())
                                     .contract(Contract.builder().id(UUID.randomUUID()).contractNumber("1").build())
                                     .build();
        
        var dto = mapper.carsharingTariffToDTO(tariff);
        
        assertThat(tariff.getContract().getId()).isEqualTo(dto.getContractId());
        assertThat(dto.getRegionId().contains(tariff.getRegionId())).isTrue();
        assertThat(tariff.getOrganization().getId()).isEqualTo(dto.getOrganizationId());
        assertThat(tariff.getId()).isEqualTo(dto.getId());
    }
    
    
    
}