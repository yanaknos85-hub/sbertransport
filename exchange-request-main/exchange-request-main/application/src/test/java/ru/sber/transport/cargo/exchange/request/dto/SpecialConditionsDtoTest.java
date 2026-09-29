package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SpecialConditionsDto should")
public class SpecialConditionsDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("be created via constructor with all fields")
    void createViaConstructor() {
        // Arrange
        UUID id = UUID.randomUUID();
        SpecialConditionsDto dto = new SpecialConditionsDto(
                id,
                true,
                "3",
                true,
                (short) -18,
                (short) 4,
                false,
                "Требуется сопровождение ГИБДД"
        );

        // Assert
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getIsDangerous()).isTrue();
        assertThat(dto.getDangerousClass()).isEqualTo("3");
        assertThat(dto.getHasTemperature()).isTrue();
        assertThat(dto.getTempMin()).isEqualTo((short) -18);
        assertThat(dto.getTempMax()).isEqualTo((short) 4);
        assertThat(dto.getIsOversized()).isFalse();
        assertThat(dto.getOtherConditions()).contains("сопровождение");
    }

    @Test
    @DisplayName("be created via builder")
    void createViaBuilder() {
        // Arrange
        UUID id = UUID.randomUUID();
        SpecialConditionsDto dto = SpecialConditionsDto.builder()
                .id(id)
                .isDangerous(true)
                .dangerousClass("9")
                .hasTemperature(false)
                .tempMin(null)
                .tempMax(null)
                .isOversized(true)
                .otherConditions("Хрупкий груз, осторожно!")
                .build();

        // Assert
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getIsDangerous()).isTrue();
        assertThat(dto.getDangerousClass()).isEqualTo("9");
        assertThat(dto.getHasTemperature()).isFalse();
        assertThat(dto.getTempMin()).isNull();
        assertThat(dto.getTempMax()).isNull();
        assertThat(dto.getIsOversized()).isTrue();
        assertThat(dto.getOtherConditions()).isEqualTo("Хрупкий груз, осторожно!");
    }

    @Test
    @DisplayName("serialize and deserialize correctly with Jackson")
    void serializeAndDeserializeWithJackson() throws JsonProcessingException {
        // Arrange
        SpecialConditionsDto dto = SpecialConditionsDto.builder()
                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .isDangerous(true)
                .dangerousClass("5")
                .hasTemperature(true)
                .tempMin((short) -10)
                .tempMax((short) 25)
                .isOversized(true)
                .otherConditions("Не подвергать вибрации")
                .build();

        // Act
        String json = objectMapper.writeValueAsString(dto);
        SpecialConditionsDto deserialized = objectMapper.readValue(json, SpecialConditionsDto.class);

        // Assert
        assertThat(deserialized.getId()).isEqualTo(dto.getId());
        assertThat(deserialized.getIsDangerous()).isEqualTo(dto.getIsDangerous());
        assertThat(deserialized.getDangerousClass()).isEqualTo("5");
        assertThat(deserialized.getHasTemperature()).isTrue();
        assertThat(deserialized.getTempMin()).isEqualTo((short) -10);
        assertThat(deserialized.getTempMax()).isEqualTo((short) 25);
        assertThat(deserialized.getIsOversized()).isTrue();
        assertThat(deserialized.getOtherConditions()).isEqualTo("Не подвергать вибрации");

        // Проверим наличие ключевых полей в JSON
        assertThat(json).contains("isDangerous");
        assertThat(json).contains("dangerousClass");
        assertThat(json).contains("tempMin");
        assertThat(json).contains("otherConditions");
    }
}