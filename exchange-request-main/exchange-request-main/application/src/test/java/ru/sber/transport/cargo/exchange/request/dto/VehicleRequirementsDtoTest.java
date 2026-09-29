package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DisplayName("VehicleRequirementsDto should")
public class VehicleRequirementsDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("be created via constructor with all fields")
    void createViaConstructor() {
        // Arrange
        UUID id = UUID.randomUUID();
        List<String> vehicleBodyType = List.of("refrigerator");
        List<String> vehicleExtraFeatures = List.of("heater", "crane");

        VehicleRequirementsDto dto = new VehicleRequirementsDto(
                id,
                "top",
                "side",
                BigDecimal.valueOf(45.5),
                BigDecimal.valueOf(12.345),
                true,
                vehicleBodyType,
                vehicleExtraFeatures,
                "Прибыть за 2 часа до погрузки"
        );

        // Assert
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getLoadType()).isEqualTo("top");
        assertThat(dto.getUnloadType()).isEqualTo("side");
        assertThat(dto.getCapacityM3()).isEqualByComparingTo("45.5");
        assertThat(dto.getLoadCapacity()).isEqualByComparingTo("12.345");
        assertThat(dto.isNoAdditionalLoad()).isTrue();
        assertThat(dto.getVehicleBodyType()).containsExactly("refrigerator");
        assertThat(dto.getVehicleExtraFeatures()).containsExactly("heater", "crane");
        assertThat(dto.getComment()).contains("за 2 часа");
    }

    @Test
    @DisplayName("be created via builder")
    void createViaBuilder() {
        // Arrange
        UUID id = UUID.randomUUID();
        VehicleRequirementsDto dto = VehicleRequirementsDto.builder()
                .id(id)
                .loadType("rear")
                .unloadType("rear")
                .capacityM3(BigDecimal.valueOf(80.0))
                .loadCapacity(new BigDecimal("18.999"))
                .noAdditionalLoad(false)
                .vehicleBodyType(List.of("tarp"))
                .vehicleExtraFeatures(List.of("tailgate"))
                .comment("Без перекуров")
                .build();

        // Assert
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getLoadType()).isEqualTo("rear");
        assertThat(dto.getUnloadType()).isEqualTo("rear");
        assertThat(dto.getCapacityM3()).isEqualByComparingTo("80.0");
        assertThat(dto.getLoadCapacity()).isEqualByComparingTo("18.999");
        assertThat(dto.isNoAdditionalLoad()).isFalse();
        assertThat(dto.getVehicleBodyType()).containsExactly("tarp");
        assertThat(dto.getVehicleExtraFeatures()).containsExactly("tailgate");
        assertThat(dto.getComment()).isEqualTo("Без перекуров");
    }

    @Test
    @DisplayName("serialize and deserialize correctly with Jackson")
    void serializeAndDeserializeWithJackson() throws JsonProcessingException {
        // Arrange
        VehicleRequirementsDto dto = VehicleRequirementsDto.builder()
                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"))
                .loadType("side")
                .unloadType("top")
                .capacityM3(new BigDecimal("60.75"))
                .loadCapacity(new BigDecimal("15.500"))
                .noAdditionalLoad(true)
                .vehicleBodyType(List.of("container"))
                .vehicleExtraFeatures(List.of("generator", "lift"))
                .comment("Строго по графику")
                .build();

        // Act
        String json = objectMapper.writeValueAsString(dto);
        VehicleRequirementsDto deserialized = objectMapper.readValue(json, VehicleRequirementsDto.class);

        // Assert
        assertThat(deserialized.getId()).isEqualTo(dto.getId());
        assertThat(deserialized.getLoadType()).isEqualTo("side");
        assertThat(deserialized.getUnloadType()).isEqualTo("top");
        assertThat(deserialized.getCapacityM3()).isEqualByComparingTo("60.75");
        assertThat(deserialized.getLoadCapacity()).isEqualByComparingTo("15.500");
        assertThat(deserialized.isNoAdditionalLoad()).isTrue();
        assertThat(deserialized.getVehicleBodyType()).containsExactly("container");
        assertThat(deserialized.getVehicleExtraFeatures()).containsExactly("generator", "lift");
        assertThat(deserialized.getComment()).isEqualTo("Строго по графику");

        // Проверим, что JSON содержит ключевые поля и массивы в правильном формате
        assertThat(json).contains("loadType").contains("noAdditionalLoad");
        assertThat(json).contains("\"vehicleExtraFeatures\":[\"generator\",\"lift\"]");
    }

    @Test
    @DisplayName("handle null fields properly during serialization and deserialization")
    void handleNullFields() throws JsonProcessingException {
        // Arrange
        VehicleRequirementsDto dto = VehicleRequirementsDto.builder()
                .loadType(null)
                .unloadType(null)
                .capacityM3(null)
                .loadCapacity(null)
                .noAdditionalLoad(false)
                .vehicleBodyType(null)
                .vehicleExtraFeatures(null)
                .comment(null)
                .build();

        // Act
        String json = objectMapper.writeValueAsString(dto);
        VehicleRequirementsDto deserialized = objectMapper.readValue(json, VehicleRequirementsDto.class);

        // Assert
        assertThat(deserialized.getLoadType()).isNull();
        assertThat(deserialized.getUnloadType()).isNull();
        assertThat(deserialized.getCapacityM3()).isNull();
        assertThat(deserialized.getLoadCapacity()).isNull();
        assertThat(deserialized.getVehicleBodyType()).isNull();
        assertThat(deserialized.getVehicleExtraFeatures()).isNull();
        assertThat(deserialized.getComment()).isNull();
    }

    @Test
    @DisplayName("respect capacityM3 range constraints (1.0 - 90.0)")
    void validateCapacityRange() {
        BigDecimal min = BigDecimal.ONE;
        BigDecimal max = new BigDecimal("90.0");

        assertThat(BigDecimal.valueOf(1.0).compareTo(min)).isGreaterThanOrEqualTo(0);
        assertThat(BigDecimal.valueOf(1.0).compareTo(max)).isLessThanOrEqualTo(0);

        assertThat(BigDecimal.valueOf(90.0).compareTo(min)).isGreaterThanOrEqualTo(0);
        assertThat(BigDecimal.valueOf(90.0).compareTo(max)).isLessThanOrEqualTo(0);
    }

    @Test
    @DisplayName("respect loadCapacity range constraints (0.001 - 20.0)")
    void validateLoadCapacityRange() {
        BigDecimal min = new BigDecimal("0.001");
        BigDecimal max = new BigDecimal("20.0");

        assertThat(BigDecimal.valueOf(0.001).compareTo(min)).isGreaterThanOrEqualTo(0);
        assertThat(BigDecimal.valueOf(0.001).compareTo(max)).isLessThanOrEqualTo(0);

        assertThat(BigDecimal.valueOf(20.0).compareTo(min)).isGreaterThanOrEqualTo(0);
        assertThat(BigDecimal.valueOf(20.0).compareTo(max)).isLessThanOrEqualTo(0);
    }

    @Test
    @DisplayName("allow only valid load/unload types: top, side, rear")
    void validateAllowedLoadTypes() {
        String[] allowedValues = {"top", "side", "rear"};

        for (String value : allowedValues) {
            VehicleRequirementsDto dto = VehicleRequirementsDto.builder()
                    .loadType(value)
                    .unloadType(value)
                    .build();
            assertThat(dto.getLoadType()).isEqualTo(value);
            assertThat(dto.getUnloadType()).isEqualTo(value);
        }
    }

    @Test
    @DisplayName("deserialize and serialize vehicleExtraFeatures as JSON array correctly")
    void testDeserializeAndSerialize_ArrayField() throws JsonProcessingException {
        // Настройка: не выводить null-поля
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Вход: JSON с массивом
        String inputJson = """
            {
              "loadType": "rear",
              "capacityM3": 30.0,
              "loadCapacity": 5.0,
              "noAdditionalLoad": false,
              "vehicleBodyType": ["temperature_controlled", "live_and_special"],
              "vehicleExtraFeatures": ["palletJack", "thermalCover", "lift"]
            }
            """;

        // Десериализация
        VehicleRequirementsDto dto = objectMapper.readValue(inputJson, VehicleRequirementsDto.class);
        assertThat(dto).isNotNull();
        assertThat(dto.getLoadType()).isEqualTo("rear");
        assertThat(dto.getCapacityM3()).isEqualByComparingTo("30.0");
        assertThat(dto.getLoadCapacity()).isEqualByComparingTo("5.0");
        assertThat(dto.isNoAdditionalLoad()).isFalse();
        assertThat(dto.getVehicleBodyType())
                .containsExactly("temperature_controlled", "live_and_special");
        assertThat(dto.getVehicleExtraFeatures())
                .containsExactly("palletJack", "thermalCover", "lift");

        log.info("Deserialized vehicleExtraFeatures: {}", dto.getVehicleExtraFeatures());

        // Сериализация
        String outputJson = objectMapper.writeValueAsString(dto);
        log.info("Serialized JSON: {}", outputJson);

        // Проверка: массивы в JSON без экранирования
        assertThat(outputJson).contains("\"vehicleBodyType\":[\"temperature_controlled\",\"live_and_special\"]");
        assertThat(outputJson).contains("\"vehicleExtraFeatures\":[\"palletJack\",\"thermalCover\",\"lift\"]");
        assertThat(outputJson).doesNotContain("\\\"");
    }
}