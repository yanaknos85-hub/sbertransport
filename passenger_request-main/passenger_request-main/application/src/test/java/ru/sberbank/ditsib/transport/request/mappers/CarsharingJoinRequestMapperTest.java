package ru.sberbank.ditsib.transport.request.mappers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mapstruct.factory.Mappers;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.carsharing.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
class CarsharingJoinRequestMapperTest {
    
    private CarsharingJoinRequestMapper mapper = Mappers.getMapper(CarsharingJoinRequestMapper.class);
    
    @Test
    void updateAndNewDtoToJoinRequest() {
        Set<ContractorAndJoinStatusDTO> contractors = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            contractors.add(ContractorAndJoinStatusDTO
                                    .builder()
                                    .employeeChoice(true)
                                    .contractor(ContractorDTO.builder()
                                                             .id(UUID.randomUUID())
                                                             .name("Contractor" + (i+1))
                                                             .msrn("msrn" + (i+1))
                                                             .tin("tin" + (i+1))
                                                             .build()).build());
        }
        UpdateCarsharingJoinRequestDTO updateDto =
                UpdateCarsharingJoinRequestDTO.builder()
                                              .phone("+7-977-77-77-565")
                                              .email("email@email.email")
                                              .contractors(contractors)
                                              .build();
        
        CarsharingJoinRequest joinRequest1 = mapper.updateDtoToJoinRequest(updateDto);
        assertThat(joinRequest1).isNotNull();
        
        NewCarsharingJoinRequestDTO dto =
                NewCarsharingJoinRequestDTO.builder()
                                           .phone("+7-977-77-77-565")
                                           .email("email@email.email")
                                           .contractors(contractors)
                                           .rulesP144Agree(true)
                                           .personalDataAgree(true)
                                           .build();
        
        CarsharingJoinRequest joinRequest2 = mapper.newDtoToJoinRequest(dto);
        assertThat(joinRequest2).isNotNull();
    }
    
    @Test
    void joinRequestToShortDtoAndDto() {
        Set<ContractorAndJoinStatus> contractors = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            contractors.add(ContractorAndJoinStatus
                                    .builder()
                                    .employeeChoice(true)
                                    .joinStatus(CorporateCarsharingJoinStatus.JOINED)
                                    .contractor(Contractor.builder()
                                                          .id(UUID.randomUUID())
                                                          .name("Contractor" + (i+1))
                                                          .msrn("msrn" + (i+1))
                                                          .tin("tin" + (i+1))
                                                          .deleted(false)
                                                          .build())
                                    .build());
        }
        CarsharingJoinRequest joinRequest =
                CarsharingJoinRequest.builder()
                                     .id(UUID.randomUUID())
                                     .humanReadableId("CR-0001-1")
                                     .employee(Employee.builder().id(UUID.randomUUID()).build())
                                     .phone("+7-977-77-77-565")
                                     .email("email@email.email")
                                     .contractors(contractors)
                                     .personalDataAgree(false)
                                     .rulesP144Agree(false)
                                     .text(new CarsharingJoinRequestText())
                                     .build();
        
        GetCarsharingJoinRequestShortDTO shortDto = mapper.joinRequestToShortDto(joinRequest);
        GetCarsharingJoinRequestDTO dto = mapper.joinRequestToDto(joinRequest);
        
        assertThat(shortDto).isNotNull();
        assertThat(dto).isNotNull();
    }
    
    @Test
    void updateTextToDto() {
        var textDTO = UpdateCarsharingJoinRequestTextDTO.builder()
                                                        .beforeFioFirstPart("beforeFioFirstPart")
                                                        .beforeFioThirdPart("beforeFioThirdPart")
                                                        .afterPhone("afterPhone")
                                                        .afterEmail("afterEmail")
                                                        .afterCarsharings("afterCarsharings")
                                                        .withRulesP144Agree("withRulesP144Agree")
                                                        .withPersonalDataAgree("withPersonalDataAgree")
                                                        .build();
    
        var text = mapper.updateJoinRequestTextToDto(textDTO);
        
        assertThat(text).isNotNull();
        assertThat(text.getBeforeFioFirstPart()).isEqualTo(textDTO.getBeforeFioFirstPart());
        assertThat(text.getAfterEmail()).isEqualTo(textDTO.getAfterEmail());
        assertThat(text.getWithPersonalDataAgree()).isEqualTo(textDTO.getWithPersonalDataAgree());
        assertThat(text.getBeforeFioSecondPart()).isNotEqualTo(null);
    }
}