package ru.sber.transport.cargo.exchange.request.mapper;

import org.junit.jupiter.api.Test;
import ru.sber.transport.cargo.exchange.request.database.model.*;
import ru.sber.transport.cargo.exchange.request.dto.AddressInfoDto;
import ru.sber.transport.cargo.exchange.request.dto.RequestDto;
import ru.sber.transport.cargo.exchange.request.dto.SpecialConditionsDto;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static ru.sber.transport.cargo.exchange.request.enums.RequestStatus.*;
import static ru.sber.transport.cargo.exchange.request.enums.WaypointType.LOAD;
import static ru.sber.transport.cargo.exchange.request.enums.WaypointType.UNLOAD;

class RequestMapperTest {

    private final RequestMapper requestMapper = new RequestMapperImpl();

    @Test
    void shouldMap_Request_To_RequestDto_WithAllNestedFields() {
        // Given
        var requestId = UUID.randomUUID();
        var ownerId = UUID.randomUUID();

        var specialConditions = SpecialConditions.builder()
                .id(UUID.randomUUID())
                .isDangerous(true)
                .dangerousClass("3")
                .hasTemperature(true)
                .tempMin((short) 2)
                .tempMax((short) 8)
                .isOversized(true)
                .otherConditions("Требуется сопровождение ГИБДД при перевозке")
                .build();

        var request = Request.builder()
                .id(requestId)
                .ownerId(ownerId)
                .status(CANCELLED_BY_CUSTOMER)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now().plusDays(2))
                .specialConditions(specialConditions)
                .build();

        specialConditions.setRequest(request);

        // When
        var dto = requestMapper.toDto(request, UUID.randomUUID());

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(request.getId());
        assertThat(dto.getOwnerId()).isEqualTo(request.getOwnerId());
        assertThat(dto.getStatus()).isEqualTo(request.getStatus().name());
        assertThat(dto.getCreatedAt()).isEqualTo(request.getCreatedAt());
        assertThat(dto.getUpdatedAt()).isEqualTo(request.getUpdatedAt());
        assertThat(dto.getCompletedAt()).isEqualTo(request.getCompletedAt());

        assertThat(dto.getSpecialConditions()).isNotNull();
        assertThat(dto.getSpecialConditions().getId()).isEqualTo(specialConditions.getId());
        assertThat(dto.getSpecialConditions().getIsDangerous()).isTrue();
        assertThat(dto.getSpecialConditions().getDangerousClass()).isEqualTo("3");
        assertThat(dto.getSpecialConditions().getHasTemperature()).isTrue();
        assertThat(dto.getSpecialConditions().getTempMin()).isEqualTo((short) 2);
        assertThat(dto.getSpecialConditions().getTempMax()).isEqualTo((short) 8);
        assertThat(dto.getSpecialConditions().getIsOversized()).isTrue();
        assertThat(dto.getSpecialConditions().getOtherConditions())
                .isEqualTo("Требуется сопровождение ГИБДД при перевозке");
    }

    @Test
    void shouldMap_RequestDto_To_Request_WithSpecialConditions() {
        // Given
        var dtoId = UUID.randomUUID();

        var specialConditionDto = SpecialConditionsDto.builder()
                .id(UUID.randomUUID())
                .isDangerous(false)
                .hasTemperature(true)
                .tempMin((short) -18)
                .tempMax((short) -15)
                .isOversized(false)
                .otherConditions("Хранить в морозильной камере")
                .build();

        var dto = RequestDto.builder()
                .id(dtoId)
                .ownerId(UUID.randomUUID())
                .status(RequestStatus.PUBLISHED.name())
                .createdAt(LocalDateTime.now().minusHours(2))
                .updatedAt(LocalDateTime.now())
                .specialConditions(specialConditionDto)
                .vatInclude(true)
                .costRequest(1000.)
                .build();

        // When
        var request = requestMapper.toEntity(dto);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.getId()).isEqualTo(dto.getId());
        assertThat(request.getStatus()).isEqualTo(RequestStatus.PUBLISHED);
        assertThat(request.getSpecialConditions()).isNotNull();

        var sc = request.getSpecialConditions();
        assertThat(sc.getIsDangerous()).isEqualTo(dto.getSpecialConditions().getIsDangerous());
        assertThat(sc.getHasTemperature()).isEqualTo(dto.getSpecialConditions().getHasTemperature());
        assertThat(sc.getTempMin()).isEqualTo(dto.getSpecialConditions().getTempMin());
        assertThat(sc.getTempMax()).isEqualTo(dto.getSpecialConditions().getTempMax());
        assertThat(sc.getOtherConditions()).isEqualTo(dto.getSpecialConditions().getOtherConditions());
        assertEquals(request.getCostRequest(), dto.getCostRequest());
        assertEquals(request.getVatInclude(), dto.getVatInclude());
    }

    @Test
    void shouldMap_Request_To_RequestSummaryDto_WithAllRequiredFields() {
        // Given
        var requestId = UUID.randomUUID();

        var loadingWaypoint = Waypoint.builder()
                .id(UUID.randomUUID())
                .type(LOAD)
                .orderingIndex(0)
                .date(LocalDate.of(2025, 9, 12))
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("ул. Ленина, д. 1, Москва")
                        .build())
                .build();

        var unloadingWaypoint = Waypoint.builder()
                .id(UUID.randomUUID())
                .type(UNLOAD)
                .orderingIndex(1)
                .date(LocalDate.of(2025, 9, 15))
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("ул. Пушкина, д. 2, Санкт-Петербург")
                        .build())
                .build();

        var cargoDetails = CargoDetails.builder()
                .id(UUID.randomUUID())
                .weightKg(BigDecimal.valueOf(5000))
                .volumeM3(BigDecimal.valueOf(20.5))
                .build();

        var vehicleRequirements = VehicleRequirements.builder()
                .id(UUID.randomUUID())
                .vehicleBodyType(List.of("tent"))
                .build();

        var request = Request.builder()
                .id(requestId)
                .humanReadableId("ОП-202601-0000001")
                .waypoints(List.of(loadingWaypoint, unloadingWaypoint))
                .cargoDetails(cargoDetails)
                .vehicleRequirements(vehicleRequirements)
                .createdAt(LocalDateTime.now())
                .costRequest(133.)
                .vatInclude(false)
                .build();

        loadingWaypoint.setRequest(request);
        unloadingWaypoint.setRequest(request);
        cargoDetails.setRequest(request);
        vehicleRequirements.setRequest(request);

        // When
        var summaryDto = requestMapper.toSummaryDto(request, UUID.randomUUID());

        // Then
        assertThat(summaryDto).isNotNull();
        assertThat(summaryDto.humanReadableId()).isEqualTo(request.getHumanReadableId());

        var firstWaypoint = request.getWaypoints().getFirst();
        var lastWaypoint = request.getWaypoints().getLast();
        assertThat(summaryDto.addressFrom()).isEqualTo(firstWaypoint.getAddressInfo().getAddressStringRepresentation());
        assertThat(summaryDto.addressTo()).isEqualTo(lastWaypoint.getAddressInfo().getAddressStringRepresentation());

        assertThat(summaryDto.vehicleBodyType()).isEqualTo(
                request.getVehicleRequirements() != null ? request.getVehicleRequirements().getVehicleBodyType() : null);
        assertThat(summaryDto.loadingDate()).isEqualTo(request.getWaypoints().getFirst()
                .getDate());
        assertThat(summaryDto.deliveryDate()).isEqualTo(request.getWaypoints().getLast()
                .getDate());

        var weightInTons = request.getCargoDetails() != null ?
                request.getCargoDetails().getWeightKg().divide(BigDecimal.valueOf(1000), RoundingMode.HALF_UP) : null;
        assertThat(summaryDto.weight()).isEqualByComparingTo(weightInTons);
        assertThat(summaryDto.volume()).isEqualTo(request.getCargoDetails() != null ? request.getCargoDetails().getVolumeM3() : null);

        assertEquals(request.getCostRequest(), summaryDto.costRequest());
        assertEquals(request.getVatInclude(), summaryDto.vatInclude());
        assertThat(summaryDto.selfReplied()).isFalse();
    }

    @Test
    void shouldHandleNullCargoDetails_AndVehicleRequirements() {
        // Given
        var loadingWaypoint = Waypoint.builder()
                .type(LOAD)
                .orderingIndex(0)
                .date(LocalDate.of(2025, 10, 1))
                .addressInfo(AddressInfoDto.builder().addressStringRepresentation("Казань, ул. Баумана").build())
                .build();

        var unloadingWaypoint = Waypoint.builder()
                .type(UNLOAD)
                .orderingIndex(1)
                .date(LocalDate.of(2025, 10, 15))
                .addressInfo(AddressInfoDto.builder().addressStringRepresentation("Екатеринбург, ул. Мира").build())
                .build();

        var request = Request.builder()
                .humanReadableId("ОП-202601-0000003")
                .waypoints(List.of(loadingWaypoint, unloadingWaypoint))
                .cargoDetails(null)
                .vehicleRequirements(null)
                .costRequest(144.)
                .vatInclude(true)
                .build();

        loadingWaypoint.setRequest(request);
        unloadingWaypoint.setRequest(request);

        // When
        var summaryDto = requestMapper.toSummaryDto(request, UUID.randomUUID());

        // Then
        var firstWaypoint = request.getWaypoints().getFirst();
        var lastWaypoint = request.getWaypoints().getLast();
        assertThat(summaryDto.addressFrom()).isEqualTo(firstWaypoint.getAddressInfo().getAddressStringRepresentation());
        assertThat(summaryDto.addressTo()).isEqualTo(lastWaypoint.getAddressInfo().getAddressStringRepresentation());
        assertThat(summaryDto.loadingDate()).isEqualTo(firstWaypoint.getDate() != null ? firstWaypoint.getDate() : null);
        assertThat(summaryDto.deliveryDate()).isEqualTo(lastWaypoint.getDate() != null ? lastWaypoint.getDate() : null);

        assertThat(summaryDto.weight()).isNull();
        assertThat(summaryDto.volume()).isNull();
        assertThat(summaryDto.vehicleBodyType()).isNull();

        assertEquals(request.getCostRequest(), summaryDto.costRequest());
        assertEquals(request.getVatInclude(), summaryDto.vatInclude());
        assertThat(summaryDto.selfReplied()).isFalse();
    }

    @Test
    void shouldReturnNullAddressAndDates_WhenWaypointsAreEmpty() {
        // Given
        var request = Request.builder()
                .waypoints(List.of())
                .cargoDetails(null)
                .vehicleRequirements(null)
                .build();

        // When
        var summaryDto = requestMapper.toSummaryDto(request, UUID.randomUUID());

        // Then
        assertThat(summaryDto).isNotNull();
        assertThat(summaryDto.addressFrom()).isNull();
        assertThat(summaryDto.addressTo()).isNull();
        assertThat(summaryDto.loadingDate()).isNull();
        assertThat(summaryDto.deliveryDate()).isNull();
        assertThat(summaryDto.weight()).isNull();
        assertThat(summaryDto.volume()).isNull();
        assertThat(summaryDto.vehicleBodyType()).isNull();
        assertThat(summaryDto.selfReplied()).isFalse();
    }

    @Test
    void shouldReturnNullAddressAndDates_WhenWaypointsAreNull() {
        // Given
        var request = Request.builder()
                .waypoints(null)
                .cargoDetails(null)
                .vehicleRequirements(null)
                .build();

        // When
        var summaryDto = requestMapper.toSummaryDto(request, UUID.randomUUID());

        // Then
        assertThat(summaryDto).isNotNull();
        assertThat(summaryDto.addressFrom()).isNull();
        assertThat(summaryDto.addressTo()).isNull();
        assertThat(summaryDto.loadingDate()).isNull();
        assertThat(summaryDto.deliveryDate()).isNull();
        assertThat(summaryDto.weight()).isNull();
        assertThat(summaryDto.volume()).isNull();
        assertThat(summaryDto.vehicleBodyType()).isNull();
        assertThat(summaryDto.selfReplied()).isFalse();
    }

    @Test
    void shouldHandleMissingDateFromInFirstWaypoint() {
        // Given
        var loadingWaypoint = Waypoint.builder()
                .type(LOAD)
                .orderingIndex(0)
                .date(null)
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("Москва, Садовая")
                        .build())
                .build();

        var unloadingWaypoint = Waypoint.builder()
                .type(UNLOAD)
                .orderingIndex(1)
                .date(LocalDate.of(2025, 12, 20))
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("Сочи, Морская")
                        .build())
                .build();

        var request = Request.builder()
                .waypoints(List.of(loadingWaypoint, unloadingWaypoint))
                .build();

        loadingWaypoint.setRequest(request);
        unloadingWaypoint.setRequest(request);

        // When
        var summaryDto = requestMapper.toSummaryDto(request, UUID.randomUUID());

        // Then
        var firstWaypoint = request.getWaypoints().getFirst();
        var lastWaypoint = request.getWaypoints().getLast();
        assertThat(summaryDto.addressFrom()).isEqualTo(firstWaypoint.getAddressInfo().getAddressStringRepresentation());
        assertThat(summaryDto.loadingDate()).isNull();
        assertThat(summaryDto.addressTo()).isEqualTo(lastWaypoint.getAddressInfo().getAddressStringRepresentation());
        assertThat(summaryDto.deliveryDate()).isEqualTo(lastWaypoint.getDate());
        assertThat(summaryDto.selfReplied()).isFalse();
    }

    @Test
    void shouldHandleMissingDateToInLastWaypoint() {
        // Given
        var loadingWaypoint = Waypoint.builder()
                .type(LOAD)
                .orderingIndex(0)
                .date(LocalDate.of(2025, 12, 1))
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("Владивосток, Ленина")
                        .build())
                .build();

        var unloadingWaypoint = Waypoint.builder()
                .type(UNLOAD)
                .orderingIndex(1)
                .date(null)
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("Хабаровск, Кирова")
                        .build())
                .build();

        var request = Request.builder()
                .waypoints(List.of(loadingWaypoint, unloadingWaypoint))
                .build();

        loadingWaypoint.setRequest(request);
        unloadingWaypoint.setRequest(request);

        // When
        var summaryDto = requestMapper.toSummaryDto(request, UUID.randomUUID());

        // Then
        var firstWaypoint = request.getWaypoints().getFirst();
        var lastWaypoint = request.getWaypoints().getLast();
        assertThat(summaryDto.addressFrom()).isEqualTo(firstWaypoint.getAddressInfo().getAddressStringRepresentation());
        assertThat(summaryDto.loadingDate()).isEqualTo(firstWaypoint.getDate());
        assertThat(summaryDto.addressTo()).isEqualTo(lastWaypoint.getAddressInfo().getAddressStringRepresentation());
        assertThat(summaryDto.deliveryDate()).isNull();
        assertThat(summaryDto.selfReplied()).isFalse();
    }

    @Test
    void shouldCorrectlySortWaypointsByOrderingIndex_ForSummaryMapping() {
        var unloadingWaypoint = Waypoint.builder()
                .type(UNLOAD)
                .orderingIndex(1)
                .date(LocalDate.of(2025, 11, 5))
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("СПб, Невский проспект")
                        .build())
                .build();

        var loadingWaypoint = Waypoint.builder()
                .type(LOAD)
                .orderingIndex(0)
                .date(LocalDate.of(2025, 11, 1))
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("Москва, Тверская")
                        .build())
                .build();

        var request = Request.builder()
                .waypoints(List.of(unloadingWaypoint, loadingWaypoint))
                .build();

        loadingWaypoint.setRequest(request);
        unloadingWaypoint.setRequest(request);

        var summaryDto = requestMapper.toSummaryDto(request, UUID.randomUUID());

        var sortedWaypoints = request.getWaypoints().stream()
                .sorted(Comparator.comparingInt(Waypoint::getOrderingIndex))
                .toList();
        var first = sortedWaypoints.getFirst();
        var last = sortedWaypoints.getLast();

        assertThat(summaryDto.addressFrom()).isEqualTo(first.getAddressInfo().getAddressStringRepresentation());
        assertThat(summaryDto.addressTo()).isEqualTo(last.getAddressInfo().getAddressStringRepresentation());
        assertThat(summaryDto.selfReplied()).isFalse();
    }

    @Test
    void shouldMap_Request_To_ShipperDto_WithAllFields() {
        // Given
        var requestId = UUID.randomUUID();

        var loadingWaypoint = Waypoint.builder()
                .id(UUID.randomUUID())
                .type(LOAD)
                .orderingIndex(0)
                .date(LocalDate.of(2025, 9, 12))
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("ул. Ленина, д. 1, Москва")
                        .build())
                .build();

        var unloadingWaypoint = Waypoint.builder()
                .id(UUID.randomUUID())
                .type(UNLOAD)
                .orderingIndex(1)
                .date(LocalDate.of(2025, 9, 15))
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("ул. Пушкина, д. 2, Санкт-Петербург")
                        .build())
                .build();

        var request = Request.builder()
                .id(requestId)
                .humanReadableId("ОП-202601-0000001")
                .waypoints(List.of(loadingWaypoint, unloadingWaypoint))
                .status(RequestStatus.PUBLISHED)
                .internalId("INT-001")
                .costRequest(500000.0)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        loadingWaypoint.setRequest(request);
        unloadingWaypoint.setRequest(request);

        // When
        var dto = requestMapper.toShipperDto(request);

        // Then
        var firstWaypoint = request.getWaypoints().getFirst();
        var lastWaypoint = request.getWaypoints().getLast();

        assertThat(dto).isNotNull();
        assertThat(dto.humanReadableId()).isEqualTo(request.getHumanReadableId());
        assertThat(dto.addressFrom()).isEqualTo(firstWaypoint.getAddressInfo().getAddressStringRepresentation());
        assertThat(dto.addressTo()).isEqualTo(lastWaypoint.getAddressInfo().getAddressStringRepresentation());
        assertThat(dto.loadingDate()).isEqualTo(firstWaypoint.getDate() != null ? firstWaypoint.getDate() : null);
        assertThat(dto.deliveryDate()).isEqualTo(lastWaypoint.getDate() != null ? lastWaypoint.getDate() : null);
        assertThat(dto.costRequest()).isEqualTo(request.getCostRequest());
        assertThat(dto.status()).isEqualTo(request.getStatus().getDisplayName());
        assertThat(dto.internalId()).isEqualTo(request.getInternalId());
        assertThat(dto.id()).isEqualTo(request.getId());
    }

    @Test
    void shouldReturnNullAddressAndDates_WhenWaypointsAreNull_Shipper() {
        // Given
        var request = Request.builder()
                .waypoints(null)
                .humanReadableId("ОП-202601-0000002")
                .status(RequestStatus.DRAFT)
                .costRequest(100000.0)
                .build();

        // When
        var dto = requestMapper.toShipperDto(request);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.humanReadableId()).isEqualTo(request.getHumanReadableId());
        assertThat(dto.addressFrom()).isNull();
        assertThat(dto.addressTo()).isNull();
        assertThat(dto.loadingDate()).isNull();
        assertThat(dto.deliveryDate()).isNull();
        assertThat(dto.status()).isEqualTo(request.getStatus().getDisplayName());
        assertThat(dto.costRequest()).isEqualTo(request.getCostRequest());
    }

    @Test
    void shouldReturnNullAddressAndDates_WhenWaypointsAreEmpty_Shipper() {
        // Given
        var request = Request.builder()
                .waypoints(List.of())
                .humanReadableId("ОП-202601-0000003")
                .status(RequestStatus.ARRIVED_AT_LOADING)
                .costRequest(75000.0)
                .build();

        // When
        var dto = requestMapper.toShipperDto(request);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.humanReadableId()).isEqualTo(request.getHumanReadableId());
        assertThat(dto.addressFrom()).isNull();
        assertThat(dto.addressTo()).isNull();
        assertThat(dto.loadingDate()).isNull();
        assertThat(dto.deliveryDate()).isNull();
        assertThat(dto.status()).isEqualTo(request.getStatus().getDisplayName());
        assertThat(dto.costRequest()).isEqualTo(request.getCostRequest());
        assertThat(dto.id()).isEqualTo(request.getId());
    }

    @Test
    void shouldHandleMissingDateFromInFirstWaypoint_Shipper() {
        // Given
        var loadingWaypoint = Waypoint.builder()
                .type(LOAD)
                .orderingIndex(0)
                .from(null)
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("Москва, Садовая")
                        .build())
                .build();

        var unloadingWaypoint = Waypoint.builder()
                .type(UNLOAD)
                .orderingIndex(1)
                .date(LocalDate.of(2025, 12, 20))
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("Сочи, Морская")
                        .build())
                .build();

        var request = Request.builder()
                .waypoints(List.of(loadingWaypoint, unloadingWaypoint))
                .humanReadableId("ОП-202601-0000004")
                .costRequest(200000.0)
                .status(RequestStatus.PUBLISHED)
                .build();

        loadingWaypoint.setRequest(request);
        unloadingWaypoint.setRequest(request);

        // When
        var dto = requestMapper.toShipperDto(request);

        // Then
        var firstWaypoint = request.getWaypoints().getFirst();
        var lastWaypoint = request.getWaypoints().getLast();
        assertThat(dto.addressFrom()).isEqualTo(firstWaypoint.getAddressInfo().getAddressStringRepresentation());
        assertThat(dto.loadingDate()).isNull();
        assertThat(dto.addressTo()).isEqualTo(lastWaypoint.getAddressInfo().getAddressStringRepresentation());
        assertThat(dto.deliveryDate()).isEqualTo(lastWaypoint.getDate());
        assertThat(dto.id()).isEqualTo(request.getId());
    }

    @Test
    void shouldHandleMissingDateToInLastWaypoint_Shipper() {
        // Given
        var loadingWaypoint = Waypoint.builder()
                .type(LOAD)
                .orderingIndex(0)
                .date(LocalDate.of(2025, 12, 1))
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("Владивосток, Ленина")
                        .build())
                .build();

        var unloadingWaypoint = Waypoint.builder()
                .type(UNLOAD)
                .orderingIndex(1)
                .date(null)
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("Хабаровск, Кирова")
                        .build())
                .build();

        var request = Request.builder()
                .waypoints(List.of(loadingWaypoint, unloadingWaypoint))
                .humanReadableId("ОП-202601-0000005")
                .costRequest(150000.0)
                .status(RequestStatus.DRAFT)
                .build();

        loadingWaypoint.setRequest(request);
        unloadingWaypoint.setRequest(request);

        // When
        var dto = requestMapper.toShipperDto(request);

        // Then
        var firstWaypoint = request.getWaypoints().getFirst();
        var lastWaypoint = request.getWaypoints().getLast();
        assertThat(dto.addressFrom()).isEqualTo(firstWaypoint.getAddressInfo().getAddressStringRepresentation());
        assertThat(dto.loadingDate()).isEqualTo(firstWaypoint.getDate());
        assertThat(dto.addressTo()).isEqualTo(lastWaypoint.getAddressInfo().getAddressStringRepresentation());
        assertThat(dto.deliveryDate()).isNull();
    }

    @Test
    void shouldCorrectlySortWaypointsByOrderingIndex_ForShipperMapping() {
        // Given — waypoints в неправильном порядке
        var unloadingWaypoint = Waypoint.builder()
                .type(UNLOAD)
                .orderingIndex(1)
                .date(LocalDate.of(2025, 11, 5))
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("СПб, Невский проспект")
                        .build())
                .build();

        var loadingWaypoint = Waypoint.builder()
                .type(LOAD)
                .orderingIndex(0)
                .date(LocalDate.of(2025, 11, 1))
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("Москва, Тверская")
                        .build())
                .build();

        var request = Request.builder()
                .waypoints(List.of(unloadingWaypoint, loadingWaypoint))
                .humanReadableId("ОП-202601-0000006")
                .costRequest(300000.0)
                .status(RequestStatus.PUBLISHED)
                .build();

        loadingWaypoint.setRequest(request);
        unloadingWaypoint.setRequest(request);

        // When
        var dto = requestMapper.toShipperDto(request);

        // Then
        var sortedWaypoints = request.getWaypoints().stream()
                .sorted(Comparator.comparingInt(Waypoint::getOrderingIndex))
                .toList();
        var first = sortedWaypoints.getFirst();
        var last = sortedWaypoints.getLast();

        assertThat(dto.addressFrom()).isEqualTo(first.getAddressInfo().getAddressStringRepresentation());
        assertThat(dto.addressTo()).isEqualTo(last.getAddressInfo().getAddressStringRepresentation());
        assertThat(dto.loadingDate()).isEqualTo(first.getDate() != null ? first.getDate() : null);
        assertThat(dto.deliveryDate()).isEqualTo(last.getDate() != null ? last.getDate() : null);
        assertThat(dto.id()).isEqualTo(request.getId());
    }

    @Test
    void shouldMapOnlyRequiredFields_AndIgnoreExtra() {
        // Given
        var request = Request.builder()
                .humanReadableId("ОП-202601-0000007")
                .waypoints(List.of(
                        Waypoint.builder()
                                .type(LOAD)
                                .orderingIndex(0)
                                .date(LocalDate.of(2025, 10, 1))
                                .addressInfo(AddressInfoDto.builder()
                                        .addressStringRepresentation("Казань")
                                        .build())
                                .build(),
                        Waypoint.builder()
                                .type(UNLOAD)
                                .orderingIndex(1)
                                .date(LocalDate.of(2025, 10, 10))
                                .addressInfo(AddressInfoDto.builder()
                                        .addressStringRepresentation("Екатеринбург")
                                        .build())
                                .build()
                ))
                .status(RequestStatus.PUBLISHED)
                .internalId("INT-007")
                .costRequest(400000.0)
                .build();

        request.getWaypoints().forEach(wp -> wp.setRequest(request));

        // When
        var dto = requestMapper.toShipperDto(request);

        // Then
        var firstWaypoint = request.getWaypoints().getFirst();
        var lastWaypoint = request.getWaypoints().getLast();
        assertThat(dto.humanReadableId()).isEqualTo(request.getHumanReadableId());
        assertThat(dto.addressFrom()).isEqualTo(firstWaypoint.getAddressInfo().getAddressStringRepresentation());
        assertThat(dto.addressTo()).isEqualTo(lastWaypoint.getAddressInfo().getAddressStringRepresentation());
        assertThat(dto.loadingDate()).isEqualTo(firstWaypoint.getDate());
        assertThat(dto.deliveryDate()).isEqualTo(lastWaypoint.getDate());
        assertThat(dto.costRequest()).isEqualTo(request.getCostRequest());
        assertThat(dto.status()).isEqualTo(request.getStatus().getDisplayName());
        assertThat(dto.internalId()).isEqualTo(request.getInternalId());
        assertThat(dto.id()).isEqualTo(request.getId());
    }

    @Test
    void shouldMap_Request_To_ShipperRequestDto_WhenStatusIsDraft() {
        // Given
        var requestId = UUID.randomUUID();
        var draftInfo = RequestDto.builder()
                .humanReadableId("ОП-202601-0000008")
                .status(DRAFT.name())
                .costRequest(350000.0)
                .build();

        var request = Request.builder()
                .id(requestId)
                .draftInfo(draftInfo)
                .build();

        // When
        var dto = requestMapper.toShipperRequestDto(request);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(request.getId());
        assertThat(dto.humanReadableId()).isEqualTo(draftInfo.getHumanReadableId());
        assertThat(dto.costRequest()).isEqualTo(draftInfo.getCostRequest());
        assertThat(dto.status()).isEqualTo(DRAFT.getDisplayName());
    }

    @Test
    void shouldMap_Request_To_ShipperRequestDto_WhenStatusIsNotDraft() {
        // Given
        var requestId = UUID.randomUUID();

        var loadingWaypoint = Waypoint.builder()
                .id(UUID.randomUUID())
                .type(LOAD)
                .orderingIndex(0)
                .date(LocalDate.of(2025, 9, 12))
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("ул. Ленина, д. 1, Москва")
                        .build())
                .build();

        var unloadingWaypoint = Waypoint.builder()
                .id(UUID.randomUUID())
                .type(UNLOAD)
                .orderingIndex(1)
                .date(LocalDate.of(2025, 9, 15))
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("ул. Пушкина, д. 2, Санкт-Петербург")
                        .build())
                .build();

        var request = Request.builder()
                .id(requestId)
                .humanReadableId("ОП-202601-0000009")
                .waypoints(List.of(loadingWaypoint, unloadingWaypoint))
                .status(PUBLISHED)
                .internalId("INT-009")
                .costRequest(600000.0)
                .build();

        loadingWaypoint.setRequest(request);
        unloadingWaypoint.setRequest(request);

        // When
        var dto = requestMapper.toShipperRequestDto(request);

        // Then
        var firstWaypoint = request.getWaypoints().getFirst();
        var lastWaypoint = request.getWaypoints().getLast();

        assertThat(dto).isNotNull();
        assertThat(dto.humanReadableId()).isEqualTo(request.getHumanReadableId());
        assertThat(dto.addressFrom()).isEqualTo(firstWaypoint.getAddressInfo().getAddressStringRepresentation());
        assertThat(dto.addressTo()).isEqualTo(lastWaypoint.getAddressInfo().getAddressStringRepresentation());
        assertThat(dto.loadingDate()).isEqualTo(firstWaypoint.getDate());
        assertThat(dto.deliveryDate()).isEqualTo(lastWaypoint.getDate());
        assertThat(dto.costRequest()).isEqualTo(request.getCostRequest());
        assertThat(dto.status()).isEqualTo(request.getStatus().getDisplayName());
        assertThat(dto.internalId()).isEqualTo(request.getInternalId());
    }

    @Test
    void shouldReturnNullForShipperRequestDto_WhenRequestIsNull() {
        // Given
        Request request = null;

        // When
        var dto = requestMapper.toShipperRequestDto(request);

        // Then
        assertThat(dto).isNull();
    }

    @Test
    void shouldReturnFalseForIsSelfReplied_WhenRequestIsNull() {
        // Given
        Request request = null;
        var userOrganizationId = UUID.randomUUID();

        // When
        var result = requestMapper.isSelfReplied(request, userOrganizationId);

        // Then
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseForIsSelfReplied_WhenCarrierReplyOrganizationIdsIsNull() {
        // Given
        var request = Request.builder()
                .carrierReplyOrganizationIds(null)
                .build();
        var userOrganizationId = UUID.randomUUID();

        // When
        var result = requestMapper.isSelfReplied(request, userOrganizationId);

        // Then
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseForIsSelfReplied_WhenUserOrganizationIdIsNull() {
        // Given
        var userOrganizationId = UUID.randomUUID();
        var request = Request.builder()
                .carrierReplyOrganizationIds(Set.of(UUID.randomUUID(), userOrganizationId))
                .build();

        // When
        var result = requestMapper.isSelfReplied(request, null);

        // Then
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseForIsSelfReplied_WhenUserOrganizationIdNotInList() {
        // Given
        var userOrganizationId = UUID.randomUUID();
        var otherOrganizationId = UUID.randomUUID();
        var request = Request.builder()
                .carrierReplyOrganizationIds(Set.of(otherOrganizationId))
                .build();

        // When
        var result = requestMapper.isSelfReplied(request, userOrganizationId);

        // Then
        assertFalse(result);
    }

    @Test
    void shouldReturnTrueForIsSelfReplied_WhenUserOrganizationIdInList() {
        // Given
        var userOrganizationId = UUID.randomUUID();
        var otherOrganizationId = UUID.randomUUID();
        var request = Request.builder()
                .carrierReplyOrganizationIds(Set.of(otherOrganizationId, userOrganizationId))
                .build();

        // When
        var result = requestMapper.isSelfReplied(request, userOrganizationId);

        // Then
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseForIsSelectedReplyOwner_WhenRequestIsNull() {
        // Given
        Request request = null;
        var userOrganizationId = UUID.randomUUID();

        // When
        var result = requestMapper.isSelectedReplyOwner(request, userOrganizationId);

        // Then
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseForIsSelectedReplyOwner_WhenCarrierOrganizationIdIsNull() {
        // Given
        var request = Request.builder()
                .carrierOrganizationId(null)
                .status(CARRIER_SELECTED)
                .build();
        var userOrganizationId = UUID.randomUUID();

        // When
        var result = requestMapper.isSelectedReplyOwner(request, userOrganizationId);

        // Then
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseForIsSelectedReplyOwner_WhenUserOrganizationIdIsNull() {
        // Given
        var request = Request.builder()
                .carrierOrganizationId(UUID.randomUUID())
                .status(CARRIER_SELECTED)
                .build();

        // When
        var result = requestMapper.isSelectedReplyOwner(request, null);

        // Then
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseForIsSelectedReplyOwner_WhenStatusIsNotCarrierSelected() {
        // Given
        var userOrganizationId = UUID.randomUUID();
        var carrierOrganizationId = UUID.randomUUID();
        var request = Request.builder()
                .carrierOrganizationId(carrierOrganizationId)
                .status(PUBLISHED)
                .build();

        // When
        var result = requestMapper.isSelectedReplyOwner(request, userOrganizationId);

        // Then
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseForIsSelectedReplyOwner_WhenCarrierOrganizationIdNotEqualsUserOrganizationId() {
        // Given
        var userOrganizationId = UUID.randomUUID();
        var carrierOrganizationId = UUID.randomUUID();
        var request = Request.builder()
                .carrierOrganizationId(carrierOrganizationId)
                .status(CARRIER_SELECTED)
                .build();

        // When
        var result = requestMapper.isSelectedReplyOwner(request, userOrganizationId);

        // Then
        assertFalse(result);
    }

    @Test
    void shouldReturnTrueForIsSelectedReplyOwner_WhenAllConditionsMet() {
        // Given
        var userOrganizationId = UUID.randomUUID();
        var request = Request.builder()
                .carrierOrganizationId(userOrganizationId)
                .status(CARRIER_SELECTED)
                .build();

        // When
        var result = requestMapper.isSelectedReplyOwner(request, userOrganizationId);

        // Then
        assertTrue(result);
    }

    @Test
    void shouldMap_Request_To_SummaryDto_WithSelfRepliedFlag() {
        // Given
        var requestId = UUID.randomUUID();
        var userOrganizationId = UUID.randomUUID();
        var otherOrganizationId = UUID.randomUUID();

        var loadingWaypoint = Waypoint.builder()
                .id(UUID.randomUUID())
                .type(LOAD)
                .orderingIndex(0)
                .date(LocalDate.of(2025, 9, 12))
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("ул. Ленина, д. 1, Москва")
                        .build())
                .build();

        var unloadingWaypoint = Waypoint.builder()
                .id(UUID.randomUUID())
                .type(UNLOAD)
                .orderingIndex(1)
                .date(LocalDate.of(2025, 9, 15))
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("ул. Пушкина, д. 2, Санкт-Петербург")
                        .build())
                .build();

        var request = Request.builder()
                .id(requestId)
                .humanReadableId("ОП-202601-0000010")
                .waypoints(List.of(loadingWaypoint, unloadingWaypoint))
                .carrierReplyOrganizationIds(Set.of(otherOrganizationId, userOrganizationId))
                .createdAt(LocalDateTime.now())
                .costRequest(150.)
                .vatInclude(false)
                .build();

        loadingWaypoint.setRequest(request);
        unloadingWaypoint.setRequest(request);

        // When
        var summaryDto = requestMapper.toSummaryDto(request, userOrganizationId);

        // Then
        assertThat(summaryDto).isNotNull();
        assertThat(summaryDto.humanReadableId()).isEqualTo(request.getHumanReadableId());
        assertThat(summaryDto.selfReplied()).isTrue();
    }

    @Test
    void shouldMap_Request_To_SummaryDto_WithoutSelfRepliedFlag() {
        // Given
        var requestId = UUID.randomUUID();
        var userOrganizationId = UUID.randomUUID();
        var otherOrganizationId = UUID.randomUUID();

        var loadingWaypoint = Waypoint.builder()
                .id(UUID.randomUUID())
                .type(LOAD)
                .orderingIndex(0)
                .date(LocalDate.of(2025, 9, 12))
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("ул. Ленина, д. 1, Москва")
                        .build())
                .build();

        var unloadingWaypoint = Waypoint.builder()
                .id(UUID.randomUUID())
                .type(UNLOAD)
                .orderingIndex(1)
                .date(LocalDate.of(2025, 9, 15))
                .addressInfo(AddressInfoDto.builder()
                        .addressStringRepresentation("ул. Пушкина, д. 2, Санкт-Петербург")
                        .build())
                .build();

        var request = Request.builder()
                .id(requestId)
                .humanReadableId("ОП-202601-0000011")
                .waypoints(List.of(loadingWaypoint, unloadingWaypoint))
                .carrierReplyOrganizationIds(Set.of(otherOrganizationId))
                .createdAt(LocalDateTime.now())
                .costRequest(160.)
                .vatInclude(true)
                .build();

        loadingWaypoint.setRequest(request);
        unloadingWaypoint.setRequest(request);

        // When
        var summaryDto = requestMapper.toSummaryDto(request, userOrganizationId);

        // Then
        assertThat(summaryDto).isNotNull();
        assertThat(summaryDto.humanReadableId()).isEqualTo(request.getHumanReadableId());
        assertThat(summaryDto.selfReplied()).isFalse();
    }

    @Test
    void shouldMap_Request_To_RequestDto_WithSelectedReplyOwnerFlag() {
        // Given
        var requestId = UUID.randomUUID();
        var userOrganizationId = UUID.randomUUID();

        var request = Request.builder()
                .id(requestId)
                .ownerId(UUID.randomUUID())
                .status(CARRIER_SELECTED)
                .carrierOrganizationId(userOrganizationId)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();

        // When
        var dto = requestMapper.toDto(request, userOrganizationId);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(request.getId());
        assertThat(dto.getSelectedReplyOwner()).isTrue();
    }

    @Test
    void shouldMap_Request_To_RequestDto_WithoutSelectedReplyOwnerFlag() {
        // Given
        var requestId = UUID.randomUUID();
        var userOrganizationId = UUID.randomUUID();
        var otherOrganizationId = UUID.randomUUID();

        var request = Request.builder()
                .id(requestId)
                .ownerId(UUID.randomUUID())
                .status(CARRIER_SELECTED)
                .carrierOrganizationId(otherOrganizationId)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();

        // When
        var dto = requestMapper.toDto(request, userOrganizationId);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(request.getId());
        assertThat(dto.getSelectedReplyOwner()).isFalse();
    }
}