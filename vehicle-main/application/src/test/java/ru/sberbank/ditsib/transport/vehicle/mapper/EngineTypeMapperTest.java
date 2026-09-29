package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sberbank.ditsib.transport.vehicle.database.model.EngineType;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("Тест маппера типов двигателей")
class EngineTypeMapperTest {

    @Test
    void testEngineTypeToEngineTypeMessage() {
        var engineTypeId = UUID.randomUUID();
        var title = "Тестовый тип";
        var source = new EngineType(engineTypeId, title);
        var deleted = true;

        var result = new EngineTypeMapperImpl().engineTypeToEngineTypeMessage(source, deleted);

        assertThat(result.id()).isEqualTo(engineTypeId);
        assertThat(result.title()).isEqualTo(title);
        assertThat(result.deleted()).isTrue();
    }
}
