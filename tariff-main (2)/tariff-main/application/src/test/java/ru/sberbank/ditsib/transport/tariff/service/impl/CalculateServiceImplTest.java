package ru.sberbank.ditsib.transport.tariff.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff.model.TripDto;
import ru.sber.transport.tariff.model.WaypointDTO;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.dao.*;
import ru.sberbank.ditsib.transport.tariff.database.model.Contract;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;
import ru.sberbank.ditsib.transport.tariff.database.model.PublicTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.TaxiTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;
import ru.sberbank.ditsib.transport.tariff.dto.RegionDto;
import ru.sberbank.ditsib.transport.tariff.service.RegionDataResolver;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@Transactional
@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка сервиса расчёта тарифов")
@Disabled("Требуется актуализация")
@ActiveProfiles({"test", "kafka"})
class CalculateServiceImplTest extends KafkaTest {
    
    @Autowired
    private CalculateServiceImpl calculateService;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @MockitoBean
    private RegionDataResolver regionDataResolver;
    
    @Autowired
    private TaxiTariffRepository taxiTariffRepository;
    
    @Autowired
    private PublicTariffRepository publicTariffRepository;
    
    @Autowired
    private ContractRepository contractRepository;
    
    private Employee employee;
    
    @BeforeEach
    public void init() {
        organizationRepository.saveAndFlush(Organization.builder().id(UUID.randomUUID()).digitId(1L).build());
        employee = employeeRepository.saveAndFlush(Employee.builder().id(UUID.randomUUID()).build());
        Organization organization2 = Organization.builder().id(UUID.randomUUID()).digitId(2L).build();
        organizationRepository.saveAndFlush(organization2);
    }
    
    @DisplayName("Выбор тарифа такси для вышестоящей гео-зоны")
    @Test
    public void calculate_doesntHaveTariffForCityButHaveTariffForRegion_taxi() {
        
        // Дано:
        //       нет тарифа такси для Подольска
        //       есть тариф такси для Московской области
        //       есть тариф общественного транспорта для Подольска
        Organization organization =
                organizationRepository.saveAndFlush(
                        Organization.builder()
                                    .id(UUID.fromString("0040f12f-57a2-48a3-b153-15e418e625f9"))
                                    .digitId(22L)
                                    .active(true)
                                    .name("Девелоперская")
                                    .build());
        
        WaypointDTO startPoint = WaypointDTO.builder()
                                            .city("Подольск")
                                            .country("Россия")
                                            .house("1")
                                            .latitude(55.35905885566531)
                                            .longitude(37.52185857332711)
                                            .region("Московская область")
                                            .street("Ихтиманская улица")
                                            .waitTime(Duration.ofSeconds(0))
                                            .build();
        
        TripDto trip = TripDto.builder()
                              .employeeId(employee.getId())
                              .distance(0.506)
                              .intermediateWaitingTime(Duration.ofSeconds(0))
                              .organizationId(organization.getId())
                              .time(Duration.ofSeconds(137000))
                              .startPoint(startPoint)
                              .tripDate(LocalDateTime.ofEpochSecond(1655820436, 0, ZoneOffset.UTC))
                              .waitingTime(Duration.ofSeconds(0))
                              .build();
        
        List<RegionDto> regions = new ArrayList<>();
        regions.add(RegionDto.builder().id(UUID.fromString("20a03e7d-efaa-4ddf-b6ec-46049129fcbb")).name("Подольск").code("1066").build());
        regions.add(RegionDto.builder().id(UUID.fromString("210dabd3-cb09-4224-975f-0bc0a63933ee")).name("Московская область").code("50").build());
        regions.add(
                RegionDto.builder().id(UUID.fromString("55059daa-2f61-4d5a-9210-263b4a10e32c")).name("Москва и Московская область").code("1").build());
        
        Set<Organization> organizations = new HashSet<>();
        organizations.add(organization);
        
        Set<UUID> regionMO = new HashSet<>();
        regionMO.add(UUID.fromString("210dabd3-cb09-4224-975f-0bc0a63933ee"));
        
        Contract contractTaxiMO =
                contractRepository.saveAndFlush(
                        Contract.builder()
                                .id(UUID.fromString("d6ab1d68-56ab-427b-8297-ad9165e86f3f"))
                                .contractorId(UUID.fromString("21a5ff1f-3731-4b40-a204-5ab749c4f5a0"))
                                .transportType(TransportTypeEnum.TAXI)
                                .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                                .sum(10000000L)
                                .userId(UUID.fromString("2dcda9c6-2050-4d90-ae55-37a23b56b9f2"))
                                .startDate(LocalDate.of(2022, 4, 1))
                                .endDate(LocalDate.of(2023, 5, 15))
                                .active(true)
                                .contractNumber("666666")
                                .includeVat(false)
                                .regionIds(regionMO)
                                .creationTime(LocalDateTime.now())
                                .organizations(organizations)
                                .build());
        
        PublicTariff publicTariffForPodolsk =
                publicTariffRepository.saveAndFlush(
                        PublicTariff.builder()
                                    .id(UUID.fromString("fbfe8d23-5b83-4812-a379-7ca1c60190d9"))
                                    .organization(organization)
                                    .transportType(TransportTypeEnum.PUBLIC)
                                    .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                                    .active(true)
                                    .regionId(UUID.fromString("20a03e7d-efaa-4ddf-b6ec-46049129fcbb"))
                                    .build());
        
        TaxiTariff tariffForRegion =
                taxiTariffRepository.saveAndFlush(
                        TaxiTariff.builder()
                                  .id(UUID.fromString("fbfe8d23-5b83-4812-a379-7ca1c60190da"))
                                  .organization(organization)
                                  .transportType(TransportTypeEnum.TAXI)
                                  .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                                  .active(true)
                                  .waitCostPerMin(100)
                                  .rideCostPerKm(11100)
                                  .taxiClass(TaxiClass.ECONOMY)
                                  .rideCostPerMin(6600)
                                  .regionId(UUID.fromString("210dabd3-cb09-4224-975f-0bc0a63933ee"))
                                  .contract(contractTaxiMO)
                                  .build());
        
        when(regionDataResolver.getRegionBranch(any())).thenReturn(regions);
        
        // Производим расчет тарифов по адресу в Подольске
        var result = calculateService.calculate(trip, employee);
        
        // Ожидаемый результат:
        //       найден тариф общественного транспорта для Подольска
        //       найден тариф такси для Московской области
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(e -> e.getTransportType().getName().equals(TransportTypeEnum.PUBLIC.getName())));
        assertEquals(publicTariffForPodolsk.getId(),
                     result.stream().filter(e -> e.getTransportType().getName().equals(TransportTypeEnum.PUBLIC.getName())).findFirst().orElseThrow()
                           .getId());
        assertTrue(result.stream().anyMatch(e -> e.getTransportType().getName().equals(TransportTypeEnum.TAXI.getName())));
        assertEquals(tariffForRegion.getId(),
                     result.stream().filter(e -> e.getTransportType().getName().equals(TransportTypeEnum.TAXI.getName())).findFirst().orElseThrow().getId());
    }
    
    @DisplayName("Выбор тарифа общественного транспорта для вышестоящей гео-зоны")
    @Test
    public void calculate_doesntHaveTariffForCityButHaveTariffForRegion_public() {
        
        // Дано:
        //       нет тарифа общественного транспорта для Подольска
        //       есть тариф общественного транспорта для Московской области
        //       есть тариф такси для Подольска
        Organization organization =
                organizationRepository.saveAndFlush(
                        Organization.builder()
                                    .id(UUID.fromString("0040f12f-57a2-48a3-b153-15e418e625f9"))
                                    .digitId(22L)
                                    .active(true)
                                    .name("Девелоперская")
                                    .build());
        
        WaypointDTO startPoint = WaypointDTO.builder()
                                            .city("Подольск")
                                            .country("Россия")
                                            .house("1")
                                            .latitude(55.35905885566531)
                                            .longitude(37.52185857332711)
                                            .region("Московская область")
                                            .street("Ихтиманская улица")
                                            .waitTime(Duration.ofSeconds(0))
                                            .build();
        
        TripDto trip = TripDto.builder()
                .employeeId(employee.getId())
                              .distance(0.506)
                              .intermediateWaitingTime(Duration.ofSeconds(0))
                              .organizationId(organization.getId())
                              .time(Duration.ofSeconds(137000))
                              .startPoint(startPoint)
                              .tripDate(LocalDateTime.ofEpochSecond(1655820436, 0, ZoneOffset.UTC))
                              .waitingTime(Duration.ofSeconds(0))
                              .build();
        
        List<RegionDto> regions = new ArrayList<>();
        regions.add(RegionDto.builder().id(UUID.fromString("20a03e7d-efaa-4ddf-b6ec-46049129fcbb")).name("Подольск").code("1066").build());
        regions.add(RegionDto.builder().id(UUID.fromString("210dabd3-cb09-4224-975f-0bc0a63933ee")).name("Московская область").code("50").build());
        regions.add(
                RegionDto.builder().id(UUID.fromString("55059daa-2f61-4d5a-9210-263b4a10e32c")).name("Москва и Московская область").code("1").build());
        
        Set<Organization> organizations = new HashSet<>();
        organizations.add(organization);
        
        Set<UUID> regionPodolsk = new HashSet<>();
        regionPodolsk.add(UUID.fromString("20a03e7d-efaa-4ddf-b6ec-46049129fcbb"));
        
        Contract contractTaxiPodolsk =
                contractRepository.saveAndFlush(
                        Contract.builder()
                                .id(UUID.fromString("d6ab1d68-56ab-427b-8297-ad9165e86f3f"))
                                .contractorId(UUID.fromString("21a5ff1f-3731-4b40-a204-5ab749c4f5a0"))
                                .transportType(TransportTypeEnum.TAXI)
                                .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                                .sum(10000000L)
                                .userId(UUID.fromString("2dcda9c6-2050-4d90-ae55-37a23b56b9f2"))
                                .startDate(LocalDate.of(2022, 4, 1))
                                .endDate(LocalDate.of(2023, 5, 15))
                                .active(true)
                                .contractNumber("666666")
                                .includeVat(false)
                                .regionIds(regionPodolsk)
                                .creationTime(LocalDateTime.now())
                                .organizations(organizations)
                                .build());
        
        PublicTariff publicTariffForMO =
                publicTariffRepository.saveAndFlush(
                        PublicTariff.builder()
                                    .id(UUID.fromString("fbfe8d23-5b83-4812-a379-7ca1c60190d9"))
                                    .organization(organization)
                                    .transportType(TransportTypeEnum.PUBLIC)
                                    .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                                    .active(true)
                                    .regionId(UUID.fromString("210dabd3-cb09-4224-975f-0bc0a63933ee"))
                                    .build());
        
        TaxiTariff tariffForPodolskEconomy =
                taxiTariffRepository.saveAndFlush(
                        TaxiTariff.builder()
                                  .id(UUID.fromString("fbfe8d23-5b83-4812-a379-7ca1c60190da"))
                                  .organization(organization)
                                  .transportType(TransportTypeEnum.TAXI)
                                  .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                                  .active(true)
                                  .waitCostPerMin(100)
                                  .rideCostPerKm(11100)
                                  .taxiClass(TaxiClass.ECONOMY)
                                  .rideCostPerMin(6600)
                                  .regionId(UUID.fromString("20a03e7d-efaa-4ddf-b6ec-46049129fcbb"))
                                  .contract(contractTaxiPodolsk)
                                  .build());
        
        TaxiTariff tariffForPodolskComfort =
                taxiTariffRepository.saveAndFlush(
                        TaxiTariff.builder()
                                  .id(UUID.fromString("fbfe8d23-5b83-4812-a379-7ca1c60190db"))
                                  .organization(organization)
                                  .transportType(TransportTypeEnum.TAXI)
                                  .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                                  .active(true)
                                  .waitCostPerMin(100)
                                  .rideCostPerKm(11100)
                                  .taxiClass(TaxiClass.COMFORT_PLUS)
                                  .rideCostPerMin(6600)
                                  .regionId(UUID.fromString("20a03e7d-efaa-4ddf-b6ec-46049129fcbb"))
                                  .contract(contractTaxiPodolsk)
                                  .build());
        
        TaxiTariff tariffForPodolskBusiness =
                taxiTariffRepository.saveAndFlush(
                        TaxiTariff.builder()
                                  .id(UUID.fromString("fbfe8d23-5b83-4812-a379-7ca1c60190dc"))
                                  .organization(organization)
                                  .transportType(TransportTypeEnum.TAXI)
                                  .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                                  .active(true)
                                  .waitCostPerMin(100)
                                  .rideCostPerKm(11100)
                                  .taxiClass(TaxiClass.BUSINESS)
                                  .rideCostPerMin(6600)
                                  .regionId(UUID.fromString("210dabd3-cb09-4224-975f-0bc0a63933ee"))
                                  .contract(contractTaxiPodolsk)
                                  .build());
        
        when(regionDataResolver.getRegionBranch(any())).thenReturn(regions);
        
        // Производим расчет тарифов по адресу в Подольске
        var result = calculateService.calculate(trip, employee);
        
        // Ожидаемый результат:
        //       найден тариф общественного транспорта для МО
        //       найден тариф такси для Подольска
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(e -> e.getTransportType().getName().equals(TransportTypeEnum.TAXI.getName())));
        assertEquals(tariffForPodolskEconomy.getId(),
                     result.stream().filter(e -> e.getTransportType().getName().equals(TransportTypeEnum.TAXI.getName())
                                                 && e.getTaxiClass().equals(TaxiClass.ECONOMY)).findFirst().orElseThrow().getId());
        assertEquals(tariffForPodolskComfort.getId(),
                     result.stream().filter(e -> e.getTransportType().getName().equals(TransportTypeEnum.TAXI.getName())
                                                 && e.getTaxiClass().equals(TaxiClass.COMFORT_PLUS)).findFirst().orElseThrow().getId());
        assertTrue(result.stream().anyMatch(e -> e.getTransportType().getName().equals(TransportTypeEnum.PUBLIC.getName())));
        assertEquals(publicTariffForMO.getId(),
                     result.stream().filter(e -> e.getTransportType().getName().equals(TransportTypeEnum.PUBLIC.getName())).findFirst().orElseThrow()
                           .getId());
    }
}