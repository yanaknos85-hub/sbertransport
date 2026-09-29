package ru.sber.transport.cargo.exchange.request.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.transport.cargo.exchange.request.dto.*;
import ru.sber.transport.cargo.exchange.request.enums.WaypointType;
import ru.sber.transport.cargo.exchange.request.service.RequestValidationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sber.transport.cargo.exchange.request.enums.RequestStatus.DRAFT;

@SpringBootTest(classes = RequestValidationServiceImpl.class)
@DisplayName("RequestValidationServiceImpl — Юнит-тест")
class RequestValidationServiceImplTest {

    @Autowired
    private RequestValidationService validationService;

    private RequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = RequestDto.builder()
                .id(UUID.randomUUID())
                .ownerId(UUID.randomUUID())
                .humanReadableId("ОП-202602-0000001")
                .organizationId(UUID.randomUUID())
                .internalId("INT-REQ-001")
                .status(DRAFT.name())
                .useEtrn(false)
                .viewType("FIXED")
                .paymentForm("NON_CASH")
                .paymentTerms("PREPAYMENT")
                .requestCreated(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(2))
                .vatInclude(false)
                .costRequest(100.)
                .build();
    }

    @Test
    @DisplayName("Должен пройти валидацию, если все обязательные поля заполнены")
    void shouldPassValidation_WhenAllFieldsAreValid() {
        // Given
        setupValidRequest();

        // When
        var result = validationService.validate(requestDto);

        // Then
        assertThat(result.getValidation().getIsValidForPublication()).isTrue();
        assertThat(result.getValidation().getErrors()).isEmpty();
    }

    @Test
    @DisplayName("Должен вернуть ошибки, если обязательные поля отсутствуют")
    void shouldFail_WhenRequiredFieldsAreMissing() {
        // Given
        requestDto.setOwnerId(null);
        requestDto.setHumanReadableId(null);
        requestDto.setInternalId(null);
        requestDto.setRequestCreated(null);
        requestDto.setCreatedAt(null);
        requestDto.setExpiresAt(null);
        requestDto.setUseEtrn(null);
        requestDto.setViewType(null);
        requestDto.setPaymentForm(null);
        requestDto.setPaymentTerms(null);
        requestDto.setCostRequest(null);
        requestDto.setVatInclude(null);

        // When
        var result = validationService.validate(requestDto);

        // Then
        assertThat(result.getValidation().getIsValidForPublication()).isFalse();
        assertThat(result.getValidation().getErrors())
                .extracting("field")
                .contains(
                        "useEtrn", "viewType", "paymentForm",
                        "costRequest", "vatInclude"
                );
    }

    @Test
    @DisplayName("Должен вернуть ошибку, если нет точек маршрута")
    void shouldFail_WhenNoWaypoints() {
        // Given
        requestDto.setWaypoints(List.of());

        // When
        var result = validationService.validate(requestDto);

        // Then
        assertThat(result.getValidation().getIsValidForPublication()).isFalse();
        assertThat(result.getValidation().getErrors())
                .anyMatch(e -> e.getField().equals("waypoints")
                           && e.getMessage().contains("указана хотя бы одна точка маршрута"));
    }

    @Test
    @DisplayName("Должен вернуть ошибку, если нет точки погрузки")
    void shouldFail_WhenNoLoadPoint() {
        // Given
        requestDto.setWaypoints(
                List.of(
                        WaypointDto.builder()
                            .type(WaypointType.UNLOAD)
                            .contact(WaypointContactDto.builder()
                                    .contactPerson("Иван")
                                    .contactPhone("+7")
                                    .contactEmail("a@b.ru")
                                    .build())
                            .build()
                )
        );

        // When
        var result = validationService.validate(requestDto);

        // Then
        assertThat(result.getValidation().getIsValidForPublication()).isFalse();
        assertThat(result.getValidation().getErrors())
                .anyMatch(e -> e.getField().equals("waypoints")
                           && e.getMessage().contains("точка погрузки"));
    }

    @Test
    @DisplayName("Должен вернуть ошибку, если нет точки выгрузки")
    void shouldFail_WhenNoUnloadPoint() {
        // Given
        requestDto.setWaypoints(
                List.of(
                    WaypointDto.builder()
                            .type(WaypointType.LOAD)
                            .contact(WaypointContactDto.builder()
                                    .contactPerson("Иван")
                                    .contactPhone("+7")
                                    .contactEmail("a@b.ru")
                                    .build())
                            .build()
                )
        );

        // When
        var result = validationService.validate(requestDto);

        // Then
        assertThat(result.getValidation().getIsValidForPublication()).isFalse();
        assertThat(result.getValidation().getErrors())
                .anyMatch(e -> e.getField().equals("waypoints")
                           && e.getMessage().contains("точка выгрузки"));
    }

    @Test
    @DisplayName("Должен вернуть ошибки в контактах, если поля пусты")
    void shouldFail_WhenContactFieldsAreBlank() {
        requestDto.setUseEtrn(true);
        // Given
        requestDto.setWaypoints(
                List.of(
                    WaypointDto.builder()
                            .type(WaypointType.LOAD)
                            .contact(WaypointContactDto.builder()
                                    .contactPerson("")
                                    .contactPhone(" ")
                                    .contactEmail("")
                                    .build())
                            .build(),
                    WaypointDto.builder()
                            .type(WaypointType.UNLOAD)
                            .contact(WaypointContactDto.builder()
                                    .contactPerson("Петров")
                                    .contactPhone("")
                                    .contactEmail("valid@example.com")
                                    .build())
                            .build()
                )
        );

        // When
        var result = validationService.validate(requestDto);

        // Then
        assertThat(result.getValidation().getIsValidForPublication()).isFalse();
        assertThat(result.getValidation().getErrors())
                .extracting("field")
                .contains(
                        "waypoints[0].contact.contactPerson",
                        "waypoints[0].contact.contactPhone",
                        "waypoints[0].contact.contactEmail",
                        "waypoints[1].contact.contactPhone"
                );
    }

    @Test
    @DisplayName("Должен вернуть ошибки, если cargoDetails не указан или обязательные поля равны null")
    void shouldFail_WhenCargoDetailsInvalid() {
        // Given
        requestDto.setCargoDetails(CargoDetailsDto.builder()
                .weightKg(null)
                .volumeM3(null)
                .declaredValue(null)
                .length(null)
                .width(null)
                .height(null)
                .cargoType(null)
                .cargoPackage(null)
                .build());

        requestDto.setWaypoints(createValidWaypoints());
        requestDto.setVehicleRequirements(createValidVehicleRequirements());

        // When
        var result = validationService.validate(requestDto);

        // Then
        assertThat(result.getValidation().getIsValidForPublication()).isFalse();
        assertThat(result.getValidation().getErrors())
                .extracting("field")
                .containsExactlyInAnyOrder(
                        "cargoDetails.weightKg",
                        "cargoDetails.volumeM3",
                        "cargoDetails.declaredValue",
                        "cargoDetails.length",
                        "cargoDetails.width",
                        "cargoDetails.height",
                        "cargoDetails.cargoType",
                        "cargoDetails.cargoPackage"
                );
    }

    @Test
    @DisplayName("Должен вернуть ошибки, если vehicleRequirements невалидны")
    void shouldFail_WhenVehicleRequirementsInvalid() {
        // Given
        requestDto.setVehicleRequirements(VehicleRequirementsDto.builder()
                .loadType("")
                .unloadType("")
                .capacityM3(BigDecimal.valueOf(-5))
                .loadCapacity(BigDecimal.valueOf(0))
                .vehicleBodyType(List.of()) // Ошибка: пустой список
                .vehicleExtraFeatures(null)
                .build());

        requestDto.setWaypoints(createValidWaypoints());
        requestDto.setCargoDetails(createValidCargoDetails());

        // When
        var result = validationService.validate(requestDto);

        // Then
        assertThat(result.getValidation().getIsValidForPublication()).isFalse();
        assertThat(result.getValidation().getErrors())
                .extracting("field")
                .contains(
                        "vehicleRequirements.loadType",
                        "vehicleRequirements.unloadType",
                        "vehicleRequirements.capacityM3",
                        "vehicleRequirements.loadCapacity",
                        "vehicleRequirements.vehicleBodyType"
                );
    }

    @Test
    @DisplayName("Должен вернуть ошибку, если paymentTerms некорректен")
    void shouldFail_WhenPaymentTermsInvalid() {
        // Given
        requestDto.setPaymentTerms("invalid_term");

        setupValidRequest();

        // When
        var result = validationService.validate(requestDto);

        // Then
        assertThat(result.getValidation().getIsValidForPublication()).isFalse();
        assertThat(result.getValidation().getErrors())
                .anyMatch(e -> e.getField().equals("paymentTerms"));
    }

    @Test
    @DisplayName("Не должен требовать полей ЭТрН, если useEtrn = false")
    void shouldNotValidateEtrnFields_WhenUseEtrnFalse() {
        // Given
        requestDto.setUseEtrn(false);
        requestDto.setSenderFio(null);
        requestDto.setSenderPhone(null);
        requestDto.setRecipientFio(null);
        requestDto.setRecipientPhone(null);

        setupValidRequest();

        // When
        var result = validationService.validate(requestDto);

        // Then
        assertThat(result.getValidation().getIsValidForPublication()).isTrue();
    }

    // === Вспомогательные методы ===

    private void setupValidRequest() {
        requestDto.setCargoDetails(createValidCargoDetails());
        requestDto.setVehicleRequirements(createValidVehicleRequirements());
        requestDto.setWaypoints(createValidWaypoints());
    }

    private CargoDetailsDto createValidCargoDetails() {
        return CargoDetailsDto.builder()
                .weightKg(BigDecimal.valueOf(100.0))
                .volumeM3(BigDecimal.valueOf(5.0))
                .declaredValue(BigDecimal.valueOf(10000))
                .length(BigDecimal.valueOf(2.5))
                .width(BigDecimal.valueOf(1.8))
                .height(BigDecimal.valueOf(1.5))
                .cargoType(List.of("GENERAL"))
                .cargoPackage(List.of("BOX"))
                .build();
    }

    private VehicleRequirementsDto createValidVehicleRequirements() {
        return VehicleRequirementsDto.builder()
                .loadType("REAR")
                .unloadType("REAR")
                .capacityM3(BigDecimal.valueOf(20))
                .loadCapacity(BigDecimal.valueOf(5))
                .vehicleBodyType(List.of("REFRIGERATOR"))
                .vehicleExtraFeatures(List.of("heater"))
                .build();
    }

    private List<WaypointDto> createValidWaypoints() {
        return List.of(
            WaypointDto.builder()
                    .type(WaypointType.LOAD)
                    .contact(WaypointContactDto.builder()
                            .contactPerson("Иван Иванов")
                            .contactPhone("+7 916 123-45-67")
                            .contactEmail("ivan@example.com")
                            .organizationInn("44433243423")
                            .build())
                    .build(),
            WaypointDto.builder()
                    .type(WaypointType.UNLOAD)
                    .contact(WaypointContactDto.builder()
                            .contactPerson("Мария Петрова")
                            .contactPhone("+7 921 987-65-43")
                            .contactEmail("maria@example.com")
                            .organizationInn("342324432234")
                            .build())
                    .build()
        );
    }
    @Test
    @DisplayName("Должен вернуть ошибку, если methodDeterminingMass не указан при useEtrn = true")
    void shouldFail_WhenMethodDeterminingMassNotProvidedAndUseEtrnTrue() {
        // Given
        requestDto.setUseEtrn(true);
        requestDto.setCargoDetails(CargoDetailsDto.builder()
                .weightKg(BigDecimal.valueOf(100.0))
                .volumeM3(BigDecimal.valueOf(5.0))
                .declaredValue(BigDecimal.valueOf(10000))
                .length(BigDecimal.valueOf(2.5))
                .width(BigDecimal.valueOf(1.8))
                .height(BigDecimal.valueOf(1.5))
                .cargoType(List.of("GENERAL"))
                .cargoPackage(List.of("BOX"))
                .methodDeterminingMass(null) // Не указан
                .build());

        requestDto.setWaypoints(createValidWaypoints());
        requestDto.setVehicleRequirements(createValidVehicleRequirements());

        // When
        var result = validationService.validate(requestDto);

        // Then
        assertThat(result.getValidation().getIsValidForPublication()).isFalse();
        assertThat(result.getValidation().getErrors())
                .anyMatch(e -> e.getField().equals("cargoDetails.methodDeterminingMass")
                        && e.getMessage().contains("метод определения массы груза"));
    }

    @Test
    @DisplayName("Должен пройти валидацию, если methodDeterminingMass указан при useEtrn = true")
    void shouldPassValidation_WhenMethodDeterminingMassProvidedAndUseEtrnTrue() {
        // Given
        requestDto.setUseEtrn(true);
        requestDto.setCargoDetails(CargoDetailsDto.builder()
                .weightKg(BigDecimal.valueOf(100.0))
                .volumeM3(BigDecimal.valueOf(5.0))
                .declaredValue(BigDecimal.valueOf(10000))
                .length(BigDecimal.valueOf(2.5))
                .width(BigDecimal.valueOf(1.8))
                .height(BigDecimal.valueOf(1.5))
                .cargoType(List.of("GENERAL"))
                .cargoPackage(List.of("BOX"))
                .methodDeterminingMass(List.of("WEIGHBRIDGE")) // Указан
                .build());

        requestDto.setWaypoints(createValidWaypoints());
        requestDto.setVehicleRequirements(createValidVehicleRequirements());

        // When
        var result = validationService.validate(requestDto);

        // Then
        assertThat(result.getValidation().getIsValidForPublication()).isTrue();
        assertThat(result.getValidation().getErrors()).isEmpty();
    }

    @Test
    @DisplayName("Не должен требовать methodDeterminingMass, если useEtrn = false")
    void shouldNotRequireMethodDeterminingMass_WhenUseEtrnFalse() {
        // Given
        requestDto.setUseEtrn(false);
        requestDto.setCargoDetails(CargoDetailsDto.builder()
                .weightKg(BigDecimal.valueOf(100.0))
                .volumeM3(BigDecimal.valueOf(5.0))
                .declaredValue(BigDecimal.valueOf(10000))
                .length(BigDecimal.valueOf(2.5))
                .width(BigDecimal.valueOf(1.8))
                .height(BigDecimal.valueOf(1.5))
                .cargoType(List.of("GENERAL"))
                .cargoPackage(List.of("BOX"))
                .methodDeterminingMass(null) // Допустимо при useEtrn = false
                .build());

        requestDto.setWaypoints(createValidWaypoints());
        requestDto.setVehicleRequirements(createValidVehicleRequirements());

        // When
        var result = validationService.validate(requestDto);

        // Then
        assertThat(result.getValidation().getIsValidForPublication()).isTrue();
        assertThat(result.getValidation().getErrors()).isEmpty();
    }
}