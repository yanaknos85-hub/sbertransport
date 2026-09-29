package ru.sberbank.ditsib.transport.reports.mappers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sberbank.ditsib.transport.reports.dto.response.PersonalResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.PublicResponseDTO;
import ru.sberbank.ditsib.transport.reports.model.ExpectedData;
import ru.sberbank.ditsib.transport.reports.model.Request;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class EntityDTOMapperTest {
    
    private EntityDTOMapper mapper = new EntityDTOMapperImpl(new PositionMapperImpl(), new DepartmentMapperImpl());
    
    @Test
    @DisplayName("Проверка перевода LocalDateTime в LocalDate в маппере")
    void localDateTimeToLocalDateMapperTest() {
        LocalDateTime now = LocalDateTime.now();
        Request request = Request.builder()
                                 .expected(
                                         ExpectedData.builder()
                                                     .time(Duration.of(10, ChronoUnit.MINUTES))
                                                     .build()
                                          )
                                 .approvalDate(now).build();
        PublicResponseDTO publicDTO = mapper.requestToGetPublicDTO(request);
        PersonalResponseDTO personalDTO = mapper.requestToGetPersonalDTO(request);
        
        assertThat(publicDTO.getApproveDate()).isEqualTo(now);
        assertThat(personalDTO.getApproveDate()).isEqualTo(now);
        
    }
}
