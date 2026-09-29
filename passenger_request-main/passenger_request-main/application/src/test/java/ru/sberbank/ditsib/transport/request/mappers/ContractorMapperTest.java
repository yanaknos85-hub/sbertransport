package ru.sberbank.ditsib.transport.request.mappers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mapstruct.factory.Mappers;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.contractor.messages.ContractorMessage;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
class ContractorMapperTest {
    
    private final ContractorMapper mapper = Mappers.getMapper(ContractorMapper.class);
    
    @Test
    void messageToEntity() {
        ContractorMessage message = new ContractorMessage(UUID.randomUUID(),
                                                          "Тындекс.Шеринг",
                                                          "154616115618",
                                                          "561151684",
                                                          500,
                                                          "Полож А.",
                                                          "322-223-322",
                                                          null,
                                                          null,
                                                          null,
                                                          null,
                                                          false,
                                                          null,
                                                          null,
                                                          null,
                                                          null,
                                                          null, 1, true);
        
        Contractor contractor = mapper.messageToEntity(message);
        
        assertThat(contractor).isNotNull();
        assertThat(contractor.getId()).isEqualTo(message.getId());
        assertThat(contractor.getName()).isEqualTo(message.name());
        assertThat(contractor.getMsrn()).isEqualTo(message.msrn());
        assertThat(contractor.getTin()).isEqualTo(message.tin());
        assertThat(contractor.isDeleted()).isEqualTo(message.deleted());
    }
}