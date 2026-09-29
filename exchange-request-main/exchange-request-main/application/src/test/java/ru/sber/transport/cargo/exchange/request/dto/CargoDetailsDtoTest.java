package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CargoDetailsDtoTest {

    private static final UUID ID = UUID.randomUUID();
    private static final BigDecimal WEIGHT_KG = BigDecimal.valueOf(450.5);
    private static final BigDecimal VOLUME_M3 = BigDecimal.valueOf(2.3);
    private static final BigDecimal DECLARED_VALUE = BigDecimal.valueOf(10000);
    private static final BigDecimal LENGTH = BigDecimal.valueOf(2.5);
    private static final BigDecimal WIDTH = BigDecimal.valueOf(1.8);
    private static final BigDecimal HEIGHT = BigDecimal.valueOf(1.5);
    private static final List<String> CARGO_TYPE = List.of("GENERAL");
    private static final List<String> CARGO_PACKAGE = List.of("BOX");
    private static final List<String> METHOD_DETERMINING_MASS = List.of("WEIGHBRIDGE", "CALCULATION");
    private static final Integer OCCUPIED_PLACES_COUNT = 5;
    private static final List<String> TYPE_OF_CONTAINER = List.of("PALLET", "BOX");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void noArgsConstructor_shouldCreateInstance_WithNullFields() {
        // When
        CargoDetailsDto dto = new CargoDetailsDto();

        // Then
        assertThat(dto.getId()).isNull();
        assertThat(dto.getWeightKg()).isNull();
        assertThat(dto.getVolumeM3()).isNull();
        assertThat(dto.getDeclaredValue()).isNull();
        assertThat(dto.getLength()).isNull();
        assertThat(dto.getWidth()).isNull();
        assertThat(dto.getHeight()).isNull();
        assertThat(dto.getCargoType()).isNull();
        assertThat(dto.getCargoPackage()).isNull();
        assertThat(dto.getMethodDeterminingMass()).isNull();
        assertThat(dto.getOccupiedPlacesCount()).isEqualTo(1); // default
        assertThat(dto.getTypeOfContainer()).containsExactly("00"); // default
    }

    @Test
    void allArgsConstructor_shouldInitializeAllFields() throws JsonProcessingException, JSONException {
        // Given
        List<String> cargoType = List.of("GENERAL", "BULK");
        List<String> cargoPackage = List.of("BOX", "PALLET");

        // When
        CargoDetailsDto dto = new CargoDetailsDto();
        dto.setId(ID);
        dto.setWeightKg(WEIGHT_KG);
        dto.setVolumeM3(VOLUME_M3);
        dto.setDeclaredValue(DECLARED_VALUE);
        dto.setLength(LENGTH);
        dto.setWidth(WIDTH);
        dto.setHeight(HEIGHT);
        dto.setCargoType(cargoType);
        dto.setCargoPackage(cargoPackage);
        dto.setMethodDeterminingMass(METHOD_DETERMINING_MASS);
        dto.setOccupiedPlacesCount(OCCUPIED_PLACES_COUNT);
        dto.setTypeOfContainer(TYPE_OF_CONTAINER);

        // Then
        assertThat(dto.getId()).isEqualTo(ID);
        assertThat(dto.getWeightKg()).isEqualTo(WEIGHT_KG);
        assertThat(dto.getVolumeM3()).isEqualTo(VOLUME_M3);
        assertThat(dto.getDeclaredValue()).isEqualTo(DECLARED_VALUE);
        assertThat(dto.getLength()).isEqualTo(LENGTH);
        assertThat(dto.getWidth()).isEqualTo(WIDTH);
        assertThat(dto.getHeight()).isEqualTo(HEIGHT);
        assertThat(dto.getCargoType()).isEqualTo(cargoType);
        assertThat(dto.getCargoPackage()).isEqualTo(cargoPackage);
        assertThat(dto.getMethodDeterminingMass()).isEqualTo(METHOD_DETERMINING_MASS);
        assertThat(dto.getOccupiedPlacesCount()).isEqualTo(OCCUPIED_PLACES_COUNT);
        assertThat(dto.getTypeOfContainer()).isEqualTo(TYPE_OF_CONTAINER);

        String json = objectMapper.writeValueAsString(dto);
        String expectedJson = """
            {
                "id": "%s",
                "weightKg": 450.5,
                "volumeM3": 2.3,
                "declaredValue": 10000,
                "length": 2.5,
                "width": 1.8,
                "height": 1.5,
                "cargoType": ["GENERAL","BULK"],
                "cargoPackage": ["BOX","PALLET"],
                "methodDeterminingMass": ["WEIGHBRIDGE","CALCULATION"],
                "occupiedPlacesCount": 5,
                "typeOfContainer": ["PALLET","BOX"]
            }
            """.formatted(ID);

        JSONAssert.assertEquals(expectedJson, json, JSONCompareMode.STRICT);
    }

    @Test
    void builder_shouldCreateInstance_WithAllFields() throws JsonProcessingException, JSONException {
        // When
        CargoDetailsDto dto = CargoDetailsDto.builder()
                .id(ID)
                .weightKg(WEIGHT_KG)
                .volumeM3(VOLUME_M3)
                .declaredValue(DECLARED_VALUE)
                .length(LENGTH)
                .width(WIDTH)
                .height(HEIGHT)
                .cargoType(CARGO_TYPE)
                .cargoPackage(CARGO_PACKAGE)
                .methodDeterminingMass(METHOD_DETERMINING_MASS)
                .occupiedPlacesCount(OCCUPIED_PLACES_COUNT)
                .typeOfContainer(TYPE_OF_CONTAINER)
                .build();

        // Then
        assertThat(dto.getId()).isEqualTo(ID);
        assertThat(dto.getWeightKg()).isEqualTo(WEIGHT_KG);
        assertThat(dto.getVolumeM3()).isEqualTo(VOLUME_M3);
        assertThat(dto.getDeclaredValue()).isEqualTo(DECLARED_VALUE);
        assertThat(dto.getLength()).isEqualTo(LENGTH);
        assertThat(dto.getWidth()).isEqualTo(WIDTH);
        assertThat(dto.getHeight()).isEqualTo(HEIGHT);
        assertThat(dto.getCargoType()).isEqualTo(CARGO_TYPE);
        assertThat(dto.getCargoPackage()).isEqualTo(CARGO_PACKAGE);
        assertThat(dto.getMethodDeterminingMass()).isEqualTo(METHOD_DETERMINING_MASS);
        assertThat(dto.getOccupiedPlacesCount()).isEqualTo(OCCUPIED_PLACES_COUNT);
        assertThat(dto.getTypeOfContainer()).isEqualTo(TYPE_OF_CONTAINER);

        String json = objectMapper.writeValueAsString(dto);
        String expectedJson = """
            {
                "id": "%s",
                "weightKg": 450.5,
                "volumeM3": 2.3,
                "declaredValue": 10000,
                "length": 2.5,
                "width": 1.8,
                "height": 1.5,
                "cargoType": ["GENERAL"],
                "cargoPackage": ["BOX"],
                "methodDeterminingMass": ["WEIGHBRIDGE","CALCULATION"],
                "occupiedPlacesCount": 5,
                "typeOfContainer": ["PALLET","BOX"]
            }
            """.formatted(ID);

        JSONAssert.assertEquals(expectedJson, json, JSONCompareMode.STRICT);
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() throws JsonProcessingException, JSONException {
        // Given
        CargoDetailsDto dto = new CargoDetailsDto();
        UUID newId = UUID.randomUUID();

        // When
        dto.setId(newId);
        dto.setWeightKg(BigDecimal.valueOf(500));
        dto.setVolumeM3(BigDecimal.valueOf(10));
        dto.setDeclaredValue(BigDecimal.valueOf(50000));
        dto.setLength(BigDecimal.valueOf(3.0));
        dto.setWidth(BigDecimal.valueOf(2.0));
        dto.setHeight(BigDecimal.valueOf(1.7));
        dto.setCargoType(List.of("DANGEROUS"));
        dto.setCargoPackage(List.of("PALLET"));
        dto.setMethodDeterminingMass(List.of("WEIGHBRIDGE"));
        dto.setOccupiedPlacesCount(10);
        dto.setTypeOfContainer(List.of("CRATE"));

        // Then
        assertThat(dto.getId()).isEqualTo(newId);
        assertThat(dto.getWeightKg()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(dto.getVolumeM3()).isEqualTo(BigDecimal.valueOf(10));
        assertThat(dto.getDeclaredValue()).isEqualTo(BigDecimal.valueOf(50000));
        assertThat(dto.getLength()).isEqualTo(BigDecimal.valueOf(3.0));
        assertThat(dto.getWidth()).isEqualTo(BigDecimal.valueOf(2.0));
        assertThat(dto.getHeight()).isEqualTo(BigDecimal.valueOf(1.7));
        assertThat(dto.getCargoType()).isEqualTo(List.of("DANGEROUS"));
        assertThat(dto.getCargoPackage()).isEqualTo(List.of("PALLET"));
        assertThat(dto.getMethodDeterminingMass()).isEqualTo(List.of("WEIGHBRIDGE"));
        assertThat(dto.getOccupiedPlacesCount()).isEqualTo(10);
        assertThat(dto.getTypeOfContainer()).isEqualTo(List.of("CRATE"));

        String json = objectMapper.writeValueAsString(dto);
        String expectedJson = """
            {
                "id": "%s",
                "weightKg": 500,
                "volumeM3": 10,
                "declaredValue": 50000,
                "length": 3.0,
                "width": 2.0,
                "height": 1.7,
                "cargoType": ["DANGEROUS"],
                "cargoPackage": ["PALLET"],
                "methodDeterminingMass": ["WEIGHBRIDGE"],
                "occupiedPlacesCount": 10,
                "typeOfContainer": ["CRATE"]
            }
            """.formatted(newId);

        JSONAssert.assertEquals(expectedJson, json, JSONCompareMode.STRICT);
    }

    @Test
    void builder_withDefaults_shouldSetDefaultValues() throws JsonProcessingException, JSONException {
        // When
        CargoDetailsDto dto = CargoDetailsDto.builder().build();

        // Then
        assertThat(dto.getOccupiedPlacesCount()).isEqualTo(1);
        assertThat(dto.getTypeOfContainer()).isNotNull();
        assertThat(dto.getTypeOfContainer()).containsExactly("00");

        String json = objectMapper.writeValueAsString(dto);
        assertThat(json).contains("\"occupiedPlacesCount\":1");
        assertThat(json).contains("\"typeOfContainer\":[\"00\"]");
    }
}