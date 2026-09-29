package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import ru.sberbank.ditsib.transport.vehicle.database.model.Department;
import ru.sberbank.ditsib.transport.vehicle.database.model.FuelType;
import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;
import ru.sberbank.ditsib.transport.vehicle.database.model.Transport;
import ru.sberbank.ditsib.transport.vehicle.database.projection.TransportShortProjection;
import ru.sberbank.ditsib.transport.vehicle.dto.files.TransportReportDto;
import ru.sberbank.ditsib.transport.vehicle.dto.files.TransportReportProjection;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.TransportSelfSearchingRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.TransportSearchWithStructureResponseDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TransportMapperTest {

    private final TransportMapper transportMapper = new TransportMapperImpl(
            new OrganizationMapperImpl(), new DepartmentMapperImpl(), new FuelTypeMapperImpl()
    );

    @Test
    void transportSelfSearchingRequestDtoToTransportSearchingRequestDto() {
        var selfSearchingRequestDto = Instancio.of(TransportSelfSearchingRequestDto.class).create();
        var organizationId = UUID.randomUUID();

        var searchingRequestDto = transportMapper.transportSelfSearchingRequestDtoToTransportSearchingRequestDto(selfSearchingRequestDto, organizationId);

        assertThat(searchingRequestDto)
                .usingRecursiveComparison()
                .ignoringFields("organizationId")
                .isEqualTo(selfSearchingRequestDto);
        assertThat(searchingRequestDto.organizationId()).isEqualTo(organizationId);
    }

    @Test
    void transportShortProjectionToTransportSearchOrganizationResponseDto() {
        var id = UUID.randomUUID();
        var model = UUID.randomUUID().toString();
        var brand = UUID.randomUUID().toString();
        var stateNumber = "A123AA777";
        var factory = new SpelAwareProxyProjectionFactory();
        var transportShortProjection = factory.createProjection(TransportShortProjection.class);
        transportShortProjection.setId(id);
        transportShortProjection.setModel(model);
        transportShortProjection.setBrand(brand);
        transportShortProjection.setStateNumber(stateNumber);
        var expected = new TransportSearchWithStructureResponseDto(id, model, brand, stateNumber);
        assertEquals(expected, transportMapper.transportShortProjectionToTransportSearchWithStructureResponseDto(transportShortProjection));
    }

    @Test
    void listTransportShortProjectionToListTransportSearchOrganizationResponseDto() {
        var id1 = UUID.randomUUID();
        var model1 = UUID.randomUUID().toString();
        var brand1 = UUID.randomUUID().toString();
        var stateNumber1 = "A123AA777";
        var id2 = UUID.randomUUID();
        var model2 = UUID.randomUUID().toString();
        var brand2 = UUID.randomUUID().toString();
        var stateNumber2 = "A321AA777";
        var factory = new SpelAwareProxyProjectionFactory();
        var transportShortProjection1 = factory.createProjection(TransportShortProjection.class);
        var transportShortProjection2 = factory.createProjection(TransportShortProjection.class);
        transportShortProjection1.setId(id1);
        transportShortProjection1.setModel(model1);
        transportShortProjection1.setBrand(brand1);
        transportShortProjection1.setStateNumber(stateNumber1);
        transportShortProjection2.setId(id2);
        transportShortProjection2.setModel(model2);
        transportShortProjection2.setBrand(brand2);
        transportShortProjection2.setStateNumber(stateNumber2);
        var expected1 = new TransportSearchWithStructureResponseDto(id1, model1, brand1, stateNumber1);
        var expected2 = new TransportSearchWithStructureResponseDto(id2, model2, brand2, stateNumber2);
        var actual = transportMapper
                .listTransportShortProjectionToListTransportSearchWithStructureResponseDto(List.of(transportShortProjection1,
                        transportShortProjection2));
        assertEquals(2, actual.size());
        assertTrue(actual.contains(expected1));
        assertTrue(actual.contains(expected2));
    }

    @Test
    void listTransportReportProjectionToTransportReportDto() {
        var factory = new SpelAwareProxyProjectionFactory();
        var transportReportProjection = factory.createProjection(TransportReportProjection.class);
        transportReportProjection.setInventoryNumber("6");
        transportReportProjection.setAssetNumber("6");
        transportReportProjection.setOfficialName("Дальневосточный банк (ДВБ)");
        transportReportProjection.setDepartmentName("Отдел трансп обеспечения УРМ г. Магадан");
        transportReportProjection.setTypeTitle("Запасной");
        transportReportProjection.setSubtypeTitle("На всякий");
        transportReportProjection.setStateNumber("А111АА116");
        transportReportProjection.setVinCode("6111sssssssss");
        transportReportProjection.setChassisNumber("6");
        transportReportProjection.setBodyNumber("6");
        transportReportProjection.setCertificateNumber("6");
        transportReportProjection.setCertificateIssuedDate(LocalDate.parse("2023-03-20"));
        transportReportProjection.setPassportNumber("6");
        transportReportProjection.setPassportIssuedDate(LocalDate.parse("2024-03-05"));
        transportReportProjection.setBrandByPassport("6");
        transportReportProjection.setModelByPassport("6");
        transportReportProjection.setBodyColor("6");
        transportReportProjection.setTelematicsIMEI("3433435");
        transportReportProjection.setTelematicsTitle("Santel");
        transportReportProjection.setExploitationStart(LocalDate.parse("2023-03-01"));
        transportReportProjection.setExploitationEnd(null);
        transportReportProjection.setCurrentMileage(6);
        transportReportProjection.setStatus("IN_USE");
        transportReportProjection.setYear(2007);
        transportReportProjection.setVehicleType("Легковой");
        transportReportProjection.setModelTitle("2114");
        transportReportProjection.setBrandTitle("Лада");
        transportReportProjection.setCategoryTitle("Легковые автомобили, небольшие грузовики (до 3,5 тонн)");
        transportReportProjection.setManufacturer("ВАЗ");
        transportReportProjection.setEcologicalClass("4");
        transportReportProjection.setEnginePower(240);
        transportReportProjection.setEngineTypeTitle("БЕНЗИН");
        transportReportProjection.setEngineCapacity(1400);
        transportReportProjection.setFuelTankVolume(50);
        transportReportProjection.setFuelTypeTitle("АИ-98");
        transportReportProjection.setDriveTitle("Передний");
        transportReportProjection.setMudguardInstalled(true);
        transportReportProjection.setSpareWheelHolderInstalled(false);
        transportReportProjection.setWeight(700);
        transportReportProjection.setMaxWeight(900);
        transportReportProjection.setHeight(1400);
        transportReportProjection.setWidth(1650);
        transportReportProjection.setLength(4122);
        transportReportProjection.setServiceIntervalDays(10000);
        transportReportProjection.setServiceIntervalMileage(180);
        transportReportProjection.setServiceAuthorizationDays(10);
        transportReportProjection.setServiceAuthorizationMileage(2000);
        transportReportProjection.setBodyTypeTitle("Купе");
        transportReportProjection.setTransmissionTypeTitle("МКПП 7");
        transportReportProjection.setFrontWheelSizeTitle("185/55R15 81V");
        transportReportProjection.setRearWheelSizeTitle("185/55R15 81V");
        transportReportProjection.setYearManufactureBegin(1980);
        transportReportProjection.setYearManufactureEnd(null);
        transportReportProjection.setLocationAddress("Москва");
        transportReportProjection.setParkingAddress("Москва");
        transportReportProjection.setComment("Комментарий");
        transportReportProjection.setCityConsumptionRate(BigDecimal.TEN);
        transportReportProjection.setCountryConsumptionRate(BigDecimal.TEN);
        transportReportProjection.setHybridConsumptionRate(BigDecimal.TEN);
        transportReportProjection.setAccessiblePositionTitle("Главарь");
        transportReportProjection.setBalanceUnitNumber("0077");
        transportReportProjection.setFacility("0078");
        transportReportProjection.setEquipmentUnitSystemNumber("0079");
        var expected = new TransportReportDto("6", "6", "Дальневосточный банк (ДВБ)", "Отдел трансп обеспечения УРМ г. Магадан", "Запасной",
                "На всякий", "А111АА116", "6111sssssssss", "6", "6",
                "6", LocalDate.parse("2023-03-20"), "6", LocalDate.parse("2024-03-05"), "6",
                "6", "6", "3433435", "Santel", LocalDate.parse("2023-03-01"),
                null, 6, "IN_USE", 2007, "Легковой",
                "2114", "Лада", "Легковые автомобили, небольшие грузовики (до 3,5 тонн)", "ВАЗ", "4",
                240, "БЕНЗИН", 1400, 50, "АИ-98",
                BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN, "Передний", true, false, 700, 900, 1400, 1650, 4122, 10000, 180, 10,
                2000, "Купе", "МКПП 7", "185/55R15 81V", "185/55R15 81V", 1980, null,
                "Москва", "Москва", "Комментарий", "Главарь", "0077", "0078", "0079");
        var actual = transportMapper
                .listTransportReportProjectionToTransportReportDto(List.of(transportReportProjection));
        assertEquals(1, actual.size());
        assertTrue(actual.contains(expected));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void transportToTransportMessage(boolean deleted) {
        var transport = Instancio.of(Transport.class).create();
        var result = transportMapper.transportToTransportMessage(transport, deleted);

        assertThat(result.brand()).isEqualTo(transport.getBrandByPassport());
        assertThat(result.model()).isEqualTo(transport.getModelByPassport());
        assertThat(result.transportType()).isEqualTo(transport.getVehicleType());
        assertThat(result.vin()).isEqualTo(transport.getVinCode());
        assertThat(result.type()).isEqualTo(transport.getSubtype().getType().getTitle());
        assertThat(result.subtype()).isEqualTo(transport.getSubtype().getTitle());
        assertThat(result.organizationIds()).isEqualTo(transport.getOrganizations().stream().map(Organization::getId).collect(Collectors.toSet()));
        assertThat(result.deleted()).isEqualTo(deleted);
        assertThat(result.departmentIds()).isEqualTo(transport.getDepartments().stream().map(Department::getId).collect(Collectors.toSet()));
        assertThat(result.vehicleId()).isEqualTo(transport.getVehicle().getId());
        assertThat(result.modelId()).isEqualTo(transport.getVehicle().getModel().getId());
        assertThat(result.brandId()).isEqualTo(transport.getVehicle().getModel().getBrand().getId());
        assertThat(result.fuelTypeIds()).isEqualTo(transport.getVehicle().getFuelTypes().stream().map(FuelType::getId).collect(Collectors.toSet()));
        assertThat(result.engineTypeId()).isEqualTo(transport.getVehicle().getEngineType().getId());
        assertThat(result.fuelTankVolume()).isEqualTo(transport.getVehicle().getFuelTankVolume());
        assertThat(result.cityConsumptionRate()).isEqualTo(transport.getVehicle().getCityConsumptionRate());
        assertThat(result.countryConsumptionRate()).isEqualTo(transport.getVehicle().getCountryConsumptionRate());
        assertThat(result.hybridConsumptionRate()).isEqualTo(transport.getVehicle().getHybridConsumptionRate());
        assertThat(result.getId()).isEqualTo(transport.getId());
        assertThat(result.stateNumber()).isEqualTo(transport.getStateNumber());
        assertThat(result.year()).isEqualTo(transport.getYear());
        assertThat(result.currentMileage()).isEqualTo(transport.getCurrentMileage());
        assertThat(result.exploitationStart()).isEqualTo(transport.getExploitationStart());
        assertThat(result.exploitationEnd()).isEqualTo(transport.getExploitationEnd());
        assertThat(result.inventoryNumber()).isEqualTo(transport.getInventoryNumber());
        assertThat(result.contractorId()).isEqualTo(transport.getContractorId());
        assertThat(result.autoparkId()).isEqualTo(transport.getAutoparkId());
        assertThat(result.locationAddress()).isEqualTo(transport.getLocationAddress());
        assertThat(result.parkingAddress()).isEqualTo(transport.getParkingAddress());
        assertThat(result.balanceUnitNumber()).isEqualTo(transport.getBalanceUnitNumber());
        assertThat(result.facility()).isEqualTo(transport.getFacility());
        assertThat(result.equipmentUnitSystemNumber()).isEqualTo(transport.getEquipmentUnitSystemNumber());
        assertThat(result.bodyTypeTitle()).isEqualTo(transport.getVehicle().getBodyType().getTitle());
    }

    @Test
    void shouldReturnNullIfTransportIsNull() {
        var result = transportMapper.transportToTransportMessage(null, true);
        assertNull(result);
    }
}
