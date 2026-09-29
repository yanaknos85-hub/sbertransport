package ru.sberbank.ditsib.transport.vehicle.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.create.VehicleCreateDto;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class TransportValidationTest {
    private Validator validator;
    private static final VehicleCreateDto VEHICLE_CREATE_DTO = createVehicleCreateDto();
    
    @BeforeEach
    void setUp() {
        var factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @MethodSource
    @ParameterizedTest(name = "{0}")
    void validateVehicleCreateDto(String testCase, VehicleCreateDto dto, String validationMessage) {
        assertViolation(validator.validate(dto), validationMessage);
    }
    
    static Stream<Arguments> validateVehicleCreateDto() {
        return Stream.of(
                Arguments.of(
                        "ID = null",
                        VEHICLE_CREATE_DTO.withId(null),
                        "ID справочник Автомобиль должен быть задан"
                            ),
                Arguments.of(
                        "stateNumber = null",
                        VEHICLE_CREATE_DTO.withStateNumber(null),
                        "Государственный номер должен быть задан"
                            ),
                Arguments.of(
                        "year = null",
                        VEHICLE_CREATE_DTO.withYear(null),
                        "Год выпуска должен быть задан"
                            ),
                Arguments.of(
                        "year = before 1900",
                        VEHICLE_CREATE_DTO.withYear(0),
                        "Год выпуска должен быть позже 1900-го года"
                            ),
                Arguments.of(
                        "year = after 9999",
                        VEHICLE_CREATE_DTO.withYear(10000),
                        "Год выпуска должен быть не позже 9999-го года"
                            ),
                Arguments.of(
                        "currentMileage = null",
                        VEHICLE_CREATE_DTO.withCurrentMileage(null),
                        "Текущий пробег должен быть задан"
                            ),
                Arguments.of(
                        "currentMileage = negative",
                        VEHICLE_CREATE_DTO.withCurrentMileage(-1),
                        "Текущий пробег должен быть положительным числом, либо 0"
                            ),
                Arguments.of(
                        "currentMileage = greater than 999999",
                        VEHICLE_CREATE_DTO.withCurrentMileage(1000000),
                        "Текущий пробег должен быть не больше 999999"
                            ),
                Arguments.of(
                        "vinCode = null",
                        VEHICLE_CREATE_DTO.withVinCode(null),
                        "VIN-номер должен быть задан"
                            ),
                Arguments.of(
                        "vinCode = empty",
                        VEHICLE_CREATE_DTO.withVinCode(""),
                        "VIN-номер должен быть задан"
                            ),
                Arguments.of(
                        "vinCode = greater than 17",
                        VEHICLE_CREATE_DTO.withVinCode("1234567890123456789"),
                        "VIN-номер не может быть больше 17 символов"
                            ),
                Arguments.of(
                        "assetNumber = null",
                        VEHICLE_CREATE_DTO.withAssetNumber(null),
                        "Номер основного средства должен быть задан"
                            ),
                Arguments.of(
                        "assetNumber = empty",
                        VEHICLE_CREATE_DTO.withAssetNumber(""),
                        "Номер основного средства должен быть задан"
                            ),
                Arguments.of(
                        "assetNumber = greater than 50",
                        VEHICLE_CREATE_DTO.withAssetNumber("123456789012345678901234567890123456789012345678901234"),
                        "Номер основного средства не может быть больше 50 символов"
                            ),
                Arguments.of(
                        "inventoryNumber = null",
                        VEHICLE_CREATE_DTO.withInventoryNumber(null),
                        "Инвентарный номер должен быть задан"
                            ),
                Arguments.of(
                        "inventoryNumber = empty",
                        VEHICLE_CREATE_DTO.withInventoryNumber(""),
                        "Инвентарный номер должен быть задан"
                            ),
                Arguments.of(
                        "inventoryNumber = greater than 50",
                        VEHICLE_CREATE_DTO.withInventoryNumber("123456789012345678901234567890123456789012345678901234"),
                        "Инвентарный номер не может быть больше 50 символов"
                            ),
                Arguments.of(
                        "bodyNumber = greater than 17",
                        VEHICLE_CREATE_DTO.withBodyNumber("1234567890123456789"),
                        "Номер кузова не может быть больше 17 символов"
                            ),
                Arguments.of(
                        "chassisNumber = greater than 17",
                        VEHICLE_CREATE_DTO.withChassisNumber("1234567890123456789"),
                        "Номер шасси не может быть больше 17 символов"
                            ),
                Arguments.of(
                        "subtypeId = null",
                        VEHICLE_CREATE_DTO.withSubtypeId(null),
                        "ID подвида должен быть задан"
                            ),
                Arguments.of(
                        "bodyColor = null",
                        VEHICLE_CREATE_DTO.withBodyColor(null),
                        "Цвет кузова должен быть задан"
                            ),
                Arguments.of(
                        "bodyColor = empty",
                        VEHICLE_CREATE_DTO.withBodyColor(""),
                        "Цвет кузова должен быть задан"
                            ),
                Arguments.of(
                        "bodyColor = greater than 50",
                        VEHICLE_CREATE_DTO.withBodyColor("123456789012345678901234567890123456789012345678901234"),
                        "Цвет кузова не может быть больше 50 символов"
                            )
                        );
    }
    
    private void assertViolation(Set<ConstraintViolation<VehicleCreateDto>> violations, String message) {
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals(message, violations.stream()
                                        .map(ConstraintViolation::getMessage)
                                        .findFirst()
                                        .orElseThrow(() -> new AssertionError("Not found message")));
        
    }
    
    private static VehicleCreateDto createVehicleCreateDto() {
        return new VehicleCreateDto(
                UUID.randomUUID(),
                "А777АА77",
                2010,
                10000,
                "123456",
                "12384928932",
                "1233883",
                "123838",
                "17238728",
                UUID.randomUUID(),
                null,
                "Белый"
        );
    }
}
