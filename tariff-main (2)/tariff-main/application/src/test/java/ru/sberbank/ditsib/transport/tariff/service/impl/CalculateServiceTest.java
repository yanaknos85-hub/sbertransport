package ru.sberbank.ditsib.transport.tariff.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.tariff.model.SuburbTripDataDTO;
import ru.sber.transport.tariff.model.TripDto;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.RequestOptions;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.tariff.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.service.ContractService;

import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.constraints.NotNull;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Transactional
@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка сервиса расчёта тарифов")
@Disabled("Требуется актуализация")
@ActiveProfiles({"test", "kafka"})
class CalculateServiceTest extends KafkaTest {
    @Value("${traffic.threshold:7}")
    private Integer trafficThreshold;
    
    @Autowired
    private CalculateServiceImpl calculateService;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private ContractService contractService;
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    
    private TaxiTariff taxiTariff1;
    private PersonalTariff personalTariff1;
    private CarSharingTariff carSharingTariff1;
    private BicycleTariff bicycleTariff1;
    private ScooterTariff scooterTariff;
    private final static String REGION_WORD = "Region";
    private final UUID regionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        Organization organization = Organization.builder().id(UUID.randomUUID()).digitId(1L).build();
        organizationRepository.saveAndFlush(organization);

        Calendar now = Calendar.getInstance();
        LocalDateTime plannedDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        int plannedYear = plannedDate.getYear();
        
        Contract contract1 = new Contract();
        contract1.setUserId(UUID.randomUUID());
        contract1.setContractorId(UUID.randomUUID());
        contract1.setOrganizations(Collections.singleton(organization));
        contract1.setRegion(REGION_WORD);
        contract1.setTransportType(TransportTypeEnum.TAXI);
        contract1.setSum(1000L);
        contract1.setStartDate(LocalDate.of(plannedYear - 1, 1, 1));
        contract1.setEndDate(LocalDate.of(plannedYear + 1, 1, 1));
        contract1.setCreationTime(LocalDateTime.now());
        contractService.save(contract1);
        
        taxiTariff1 = TaxiTariff.builder()
                                .region(REGION_WORD)
                                .regionId(regionId)
                                .organization(organization)
                                .transportType(TransportTypeEnum.TAXI)
                                .contract(contract1)
                                .taxiClass(TaxiClass.ECONOMY)
                                .rideCostPerKm(3000)
                                .distanceIncluded(10d)
                                .minRideDistanceCost(10)
                                .rideCostPerMin(5)
                                .timeIncluded(20)
                                .minRideTimeCost(10)
                                .waitCostPerMin(5)
                                .waitCostPerMinIntermediate(6)
                                .freeWaitingTime(1)
                                .coopTariffParams(
                                        CoopTariffParams.builder()
                                                        .minCancelTimeMin(45)
                                                        .distanceDeviationKm(5d)
                                                        .savingsDeviationPct(15d)
                                                        .timeDeviationMin(5).build())
                                .suburbTariffParams(SuburbTariffParams.builder()
                                                                      .costPerMinInterRegion(100)
                                                                      .costPerKmInterRegion(200)
                                                                      .costPerMinSuburb(400)
                                                                      .costPerKmSuburb(500)
                                                                      .suburbServiceCostPerKm(600)
                                                                      .suburbServiceCostPerMin(700)
                                                                      .build())
                                .timedTariffParams(TimedTariffParams.builder()
                                                                    .coefWorkDayMorning(1.)
                                                                    .coefWorkDayNoon(2.)
                                                                    .coefWorkDayEvening(3.)
                                                                    .coefWorkDayNight(4.1)
                                                                    .coefDayOff(5.1)
                                                                    .build())
                                .coefTraffic(.1)
                                .coefOrg(2.)
                                .coefPetTransport(3.)
                                .coefChildSeat(4.)
                                .coefBicycle(5.)
                                .build();
        
        personalTariff1 = PersonalTariff.builder()
                                        .region("Region")
                                        .regionId(UUID.randomUUID())
                                        .organization(organization)
                                        .rideCostPerKm(500)
                                        .rideCostPerMin(100)
                                        .transportType(TransportTypeEnum.PERSONAL)
                                        .seasonalCoefficient(1.5)
                                        .seasonStart(LocalDate.now())
                                        .seasonEnd(LocalDate.now().plusMonths(1))
                                        .coopTariffParams(
                                                CoopTariffParams.builder()
                                                                .minCancelTimeMin(45)
                                                                .distanceDeviationKm(5d)
                                                                .savingsDeviationPct(15d)
                                                                .timeDeviationMin(5).build())
                                        .engineTariffParams(EngineTariffParams.builder()
                                                                              .coefEngine1_6(1.)
                                                                              .coefEngine1_6_to_2_0(1.1)
                                                                              .coefEngine2_0_to_2_5(1.2)
                                                                              .build())
                                        .timedTariffParams(TimedTariffParams.builder()
                                                                            .coefDayOff(1.5)
                                                                            .coefWorkDayMorning(1.5)
                                                                            .coefWorkDayNoon(1.)
                                                                            .coefWorkDayEvening(1.5)
                                                                            .coefWorkDayNight(1.)
                                                                            .build())
                                        .suburbTariffParams(SuburbTariffParams.builder()
                                                                              .costPerKmInterRegion(5000)
                                                                              .costPerMinSuburb(2000)
                                                                              .costPerMinInterRegion(7000)
                                                                              .costPerMinInterRegion(1000)
                                                                              .build())
                                        .build();
        
        carSharingTariff1 = CarSharingTariff.builder()
                                            .region("Region")
                                            .regionId(UUID.randomUUID())
                                            .organization(organization)
                                            .transportType(TransportTypeEnum.CARSHARING)
                                            .rideCostPerKm(100)
                                            .rideCostPerMin(200)
                                            .waitCostPerMin(300)
                                            .timedTariffParams(TimedTariffParams.builder()
                                                                                .coefWorkDayMorning(1.)
                                                                                .coefWorkDayNoon(2.)
                                                                                .coefWorkDayEvening(3.)
                                                                                .coefWorkDayNight(4.)
                                                                                .coefDayOff(5.)
                                                                                .build())
                                            .coefCasko(1.)
                                            .coefChildSeat(2.)
                                            .coefTraffic(.1)
                                            .coefPetTransport(3.)
                                            .build();
        
        bicycleTariff1 = BicycleTariff.builder()
                                      .region("Region")
                                      .regionId(UUID.randomUUID())
                                      .organization(organization)
                                      .transportType(TransportTypeEnum.BICYCLE)
                                      .rideCostPerKm(100)
                                      .rideCostPerMin(200)
                                      .bookingCost(300)
                                      .timedTariffParams(TimedTariffParams.builder()
                                                                          .coefWorkDayMorning(1.)
                                                                          .coefWorkDayNoon(2.)
                                                                          .coefWorkDayEvening(3.)
                                                                          .coefWorkDayNight(4.1)
                                                                          .coefDayOff(5.1)
                                                                          .build())
                                      .coefInsurance(1.)
                                      .build();
        
        scooterTariff = ScooterTariff.builder()
                                     .region("Region")
                                     .regionId(UUID.randomUUID())
                                     .organization(organization)
                                     .transportType(TransportTypeEnum.SCOOTER)
        
                                     .rideCostPerKm(100)
                                     .rideCostPerMin(200)
                                     .bookingCost(300)
                                     .timedTariffParams(TimedTariffParams.builder()
                                                                         .coefWorkDayMorning(1.)
                                                                         .coefWorkDayNoon(2.)
                                                                         .coefWorkDayEvening(3.)
                                                                         .coefWorkDayNight(4.1)
                                                                         .coefDayOff(5.1)
                                                                         .build())
                                     .coefInsurance(1.)
                                     .build();
    }
    
    
    @Test
    @DisplayName("Тест временных коэффициентов")
    void testTimedTariff() {
        TimedTariffParams timedTariffParams = TimedTariffParams.builder()
                                                               .coefWorkDayMorning(1.)
                                                               .coefWorkDayNoon(2.)
                                                               .coefWorkDayEvening(3.)
                                                               .coefWorkDayNight(4.)
                                                               .coefDayOff(5.)
                                                               .build();
        LocalDateTime time = LocalDate.parse("2020-11-15", dateFormatter).atStartOfDay();
        Double timeCoefficient = calculateService.getTimeCoefficient(timedTariffParams, time);
        assertEquals(timedTariffParams.getCoefDayOff(), timeCoefficient);
        time = LocalDate.parse("2020-11-16", dateFormatter).atStartOfDay().plusHours(8);
        timeCoefficient = calculateService.getTimeCoefficient(timedTariffParams, time);
        assertEquals(timedTariffParams.getCoefWorkDayMorning(), timeCoefficient);
        time = LocalDate.parse("2020-11-16", dateFormatter).atStartOfDay().plusHours(11);
        timeCoefficient = calculateService.getTimeCoefficient(timedTariffParams, time);
        assertEquals(timedTariffParams.getCoefWorkDayNoon(), timeCoefficient);
        time = LocalDate.parse("2020-11-16", dateFormatter).atStartOfDay().plusHours(19);
        timeCoefficient = calculateService.getTimeCoefficient(timedTariffParams, time);
        assertEquals(timedTariffParams.getCoefWorkDayEvening(), timeCoefficient);
        time = LocalDate.parse("2020-11-16", dateFormatter).atStartOfDay().plusHours(23);
        timeCoefficient = calculateService.getTimeCoefficient(timedTariffParams, time);
        assertEquals(timedTariffParams.getCoefWorkDayNight(), timeCoefficient);
    }
    
    @Test
    @DisplayName("Тест базового расчёта такси")
    void test_calc_taxi_basic_cost() {
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDateTime.now())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(500).build();
        
        taxiTariff1 =
                taxiTariff1.toBuilder()
                           .distanceIncluded(0d)
                           .minRideDistanceCost(0)
                           .timeIncluded(0)
                           .minRideTimeCost(0)
                           .build();
        
        long actual = calculateService.calculateTaxiBasicCost(taxiTariff1, tripDto);
        assertEquals(1500070.0, actual);
    }
    
    @Test
    @DisplayName("Тест базового расчёта такси с минимальной стоимостью и временем")
    void test_calc_taxi_basic_cost_with_min_cost() {
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDateTime.now())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(500).build();
        
        taxiTariff1 =
                taxiTariff1.toBuilder()
                           .build();
        
        long actual = calculateService.calculateTaxiBasicCost(taxiTariff1, tripDto);
        double expected =
                calcBasicTaxiCost(tripDto);
        assertEquals(1470040.0, actual);
    }
    
    
    @Test
    @DisplayName("Тест пригородного расчёта такси")
    void test_calc_suburb_cost() {
        SuburbTariffParams suburbTariffParams = SuburbTariffParams.builder()
                                                                  .costPerMinInterRegion(100)
                                                                  .costPerKmInterRegion(200)
                                                                  .costPerMinSuburb(400)
                                                                  .costPerKmSuburb(500)
                                                                  .suburbServiceCostPerKm(600)
                                                                  .suburbServiceCostPerMin(700)
                                                                  .build();
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDateTime.now())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(50)
                                 .suburbTripData(SuburbTripDataDTO.builder()
                                                                  .interRegionDistance(10)
                                                                  .interRegionTime(20)
                                                                  .suburbDistance(30)
                                                                  .suburbTime(40)
                                                                  .suburbServiceDistance(50)
                                                                  .suburbServiceTime(60)
                                                                  .build())
                                 .options(Set.of(RequestOptions.values()))
                                 .build();
        
        
        SuburbTripDataDTO suburbTripData = tripDto.getSuburbTripData();
        long actual = calculateService.calculateTaxiSuburbCost(suburbTariffParams, suburbTripData);
        
        long expected = calcSuburbTaxiCost(suburbTariffParams, suburbTripData);
        assertEquals(expected, actual);
    }
    
    
    @Test
    @DisplayName("Тест расчёта общего коэффициента такси")
    void test_calc_total_coefficient() {
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDate.parse("2020-11-15").atStartOfDay())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(50)
                                 .trafficJamScore(8)
                                 .options(Set.of(RequestOptions.values()))
                                 .build();
        
        double actual = calculateService.calculateTotalTaxiCoefficient(taxiTariff1, tripDto);
        double expected = calcTaxiCoefficient(tripDto);
        
        assertEquals(expected, actual);
    }
    
    
    @Test
    @DisplayName("Тест полного расчёта такси")
    void test_calcTotalCost() {
        taxiTariff1 = taxiTariff1.toBuilder().suburbTariffParams(SuburbTariffParams.builder()
                                                                                   .costPerMinInterRegion(100)
                                                                                   .costPerKmInterRegion(200)
                                                                                   .costPerMinSuburb(400)
                                                                                   .costPerKmSuburb(500)
                                                                                   .suburbServiceCostPerKm(600)
                                                                                   .suburbServiceCostPerMin(700)
                                                                                   .build())
                                 .build();
        
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDate.parse("2020-11-15").atStartOfDay())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(50)
                                 .suburbTripData(SuburbTripDataDTO.builder()
                                                                  .interRegionDistance(10)
                                                                  .interRegionTime(20)
                                                                  .suburbDistance(30)
                                                                  .suburbTime(40)
                                                                  .suburbServiceDistance(50)
                                                                  .suburbServiceTime(60)
                                                                  .build())
                                 .options(Set.of(RequestOptions.values()))
                                 .build();
    
        final var actual = calculateService.calculateByTransportType(taxiTariff1, tripDto);
        
        assertEquals(138948480D, actual);
    }
    
    @Test
    @DisplayName("Тест выбора сезонного коэффициента личного авто")
    void test_engineCoefficients() {
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDateTime.now())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(500).build();
        
        personalTariff1 = personalTariff1.toBuilder()
                                         .seasonalCoefficient(5.)
                                         .seasonStart(LocalDate.parse("2020-11-01"))
                                         .seasonEnd(LocalDate.parse("2021-03-15"))
                                         .build();
        tripDto = tripDto.toBuilder().tripDate(LocalDate.parse("2020-11-01").atStartOfDay()).build();
        double actualCoefficient = calculateService.getSeasonalCoefficient(personalTariff1, tripDto.getTripDate());
        double expectedCoefficient = personalTariff1.getSeasonalCoefficient();
        assertEquals(expectedCoefficient, actualCoefficient);
        
        tripDto = tripDto.toBuilder().tripDate(tripDto.getTripDate().minusMonths(1)).build();
        actualCoefficient = calculateService.getSeasonalCoefficient(personalTariff1, tripDto.getTripDate());
        expectedCoefficient = 1.;
        assertEquals(expectedCoefficient, actualCoefficient);
        
        tripDto = tripDto.toBuilder().tripDate(LocalDate.parse("2021-01-01").atStartOfDay()).build();
        actualCoefficient = calculateService.getSeasonalCoefficient(personalTariff1, tripDto.getTripDate());
        expectedCoefficient = personalTariff1.getSeasonalCoefficient();
        assertEquals(expectedCoefficient, actualCoefficient);
        
        tripDto = tripDto.toBuilder().tripDate(LocalDate.parse("2030-01-01").atStartOfDay()).build();
        actualCoefficient = calculateService.getSeasonalCoefficient(personalTariff1, tripDto.getTripDate());
        expectedCoefficient = personalTariff1.getSeasonalCoefficient();
        assertEquals(expectedCoefficient, actualCoefficient);
    }
    
    @Test
    @DisplayName("Тест выбора коэффициента двигателя личного авто")
    void test_seasonalCoefficient() {
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDateTime.now())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(500).build();
        
        EngineTariffParams engineTariffParams = EngineTariffParams.builder()
                                                                  .coefEngine1_6(2.)
                                                                  .coefEngine1_6_to_2_0(3.)
                                                                  .coefEngine2_0_to_2_5(4.)
                                                                  .build();
        personalTariff1 = personalTariff1.toBuilder().engineTariffParams(engineTariffParams).build();
        tripDto = tripDto.toBuilder().engineVolume(1000).build();
        double actualCoefficient = calculateService.getEngineCoefficient(engineTariffParams, tripDto);
        double expectedCoefficient = engineTariffParams.getCoefEngine1_6();
        assertEquals(expectedCoefficient, actualCoefficient);
        tripDto = tripDto.toBuilder().engineVolume(1650).build();
        actualCoefficient = calculateService.getEngineCoefficient(engineTariffParams, tripDto);
        expectedCoefficient = engineTariffParams.getCoefEngine1_6_to_2_0();
        assertEquals(expectedCoefficient, actualCoefficient);
        tripDto = tripDto.toBuilder().engineVolume(2100).build();
        actualCoefficient = calculateService.getEngineCoefficient(engineTariffParams, tripDto);
        expectedCoefficient = engineTariffParams.getCoefEngine2_0_to_2_5();
        assertEquals(expectedCoefficient, actualCoefficient);
        tripDto = tripDto.toBuilder().engineVolume(3000).build();
        actualCoefficient = calculateService.getEngineCoefficient(engineTariffParams, tripDto);
        expectedCoefficient = engineTariffParams.getCoefEngine2_0_to_2_5();
        assertEquals(expectedCoefficient, actualCoefficient);
    }
    
    @Test
    @DisplayName("Тест базового расчёта личного авто")
    void test_calc_personal_basic_cost() {
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDateTime.now())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(500).build();
        
        personalTariff1 =
                personalTariff1.toBuilder()
                               .distanceIncluded(0d)
                               .minRideDistanceCost(0)
                               .timeIncluded(0)
                               .minRideTimeCost(0)
                               .build();
        
        long actual = calculateService.calculatePersonalBasicCost(personalTariff1, tripDto);
        assertEquals(251000.0, actual);
    }
    
    @Test
    @DisplayName("Тест базового расчёта личного авто с минимальной стоимостью и временем")
    void test_calc_personal_basic_cost_with_min_cost() {
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDateTime.now())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(500).build();
        
        long actual = calculateService.calculatePersonalBasicCost(personalTariff1, tripDto);
        assertEquals(251000.0, actual);
    }
    
    @Test
    @DisplayName("Базовый расчёт по личному транспорту не зависит от минимальной стоимости")
    void test_calc_personal_basic_cost_min_cost_not_affected() {
        
        personalTariff1.setMinRideDistanceCost(1000_00);
        personalTariff1.setMinRideTimeCost(2000_00);
        
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDateTime.now())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(500).build();
        
        long actual = calculateService.calculatePersonalBasicCost(personalTariff1, tripDto);
        assertEquals(251000.0, actual);
    }
    
    @Test
    @DisplayName("Раcсчитанная стоимость по личному транспорту не меньше минимальной стоимости")
    void test_calc_personal_basic_cost_with_min_cost_3() {
        
        personalTariff1.setMinRideDistanceCost(1000_00);
        personalTariff1.setMinRideTimeCost(2000_00);
        
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDateTime.now())
                                 .time(Duration.ofMinutes(0))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(0).build();
        
        long actual = calculateService.calculatePersonal(personalTariff1, tripDto);
        assertEquals(200000.0, actual);
    }
    
    @Test
    @DisplayName("Тест пригородного расчёта личного авто")
    void test_calc_suburb_cost_personal() {
        SuburbTariffParams suburbTariffParams = SuburbTariffParams.builder()
                                                                  .costPerMinInterRegion(100)
                                                                  .costPerKmInterRegion(200)
                                                                  .costPerMinSuburb(400)
                                                                  .costPerKmSuburb(500)
                                                                  .suburbServiceCostPerKm(600)
                                                                  .suburbServiceCostPerMin(700)
                                                                  .build();
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDateTime.now())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(50)
                                 .suburbTripData(SuburbTripDataDTO.builder()
                                                                  .interRegionDistance(10)
                                                                  .interRegionTime(20)
                                                                  .suburbDistance(30)
                                                                  .suburbTime(40)
                                                                  .suburbServiceDistance(50)
                                                                  .suburbServiceTime(60)
                                                                  .build())
                                 .options(Set.of(RequestOptions.values()))
                                 .build();
        
        
        SuburbTripDataDTO suburbTripData = tripDto.getSuburbTripData();
        long actual = calculateService.calculatePersonalSuburbCost(suburbTariffParams, suburbTripData);
        
        long expected = calcSuburbPersonalCost(suburbTariffParams, suburbTripData);
        assertEquals(expected, actual);
    }
    
    @Test
    @DisplayName("Тест расчёта общего коэффициента личного авто")
    void test_calcTotalCoefPersonalCar() {
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDate.parse("2020-11-15").atStartOfDay())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(50)
                                 .trafficJamScore(8)
                                 .engineVolume(3000)
                                 .options(Set.of(RequestOptions.values()))
                                 .build();
        
        double actual = calculateService.calculateTotalPersonalCoefficient(personalTariff1, tripDto);
        double expected = calculateTotalPersonalCoefficient(personalTariff1, tripDto);
        
        assertEquals(expected, actual);
    }
    
    @Test
    @DisplayName("Тест полного расчёта личного транспорта")
    void test_calcTotalCostPersonal() {
        personalTariff1 = personalTariff1.toBuilder().suburbTariffParams(SuburbTariffParams.builder()
                                                                                           .costPerMinInterRegion(100)
                                                                                           .costPerKmInterRegion(200)
                                                                                           .costPerMinSuburb(400)
                                                                                           .costPerKmSuburb(500)
                                                                                           .suburbServiceCostPerKm(600)
                                                                                           .suburbServiceCostPerMin(700)
                                                                                           .build())
                                         .build();
        
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDate.parse("2020-11-15").atStartOfDay())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(50)
                                 .suburbTripData(SuburbTripDataDTO.builder()
                                                                  .interRegionDistance(10)
                                                                  .interRegionTime(20)
                                                                  .suburbDistance(30)
                                                                  .suburbTime(40)
                                                                  .suburbServiceDistance(50)
                                                                  .suburbServiceTime(60)
                                                                  .build())
                                 .options(Set.of(RequestOptions.values()))
                                 .build();
    
        final var actual = calculateService.calculateByTransportType(personalTariff1, tripDto);
        assertEquals(91500D, actual);
    }
    
    
    @Test
    @DisplayName("Тест базового расчёта каршеринга")
    void test_calcCarSharingBasicCost() {
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDateTime.now())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(500).build();
        
        long actual = calculateService.calculateCarSharingBasicCost(carSharingTariff1, tripDto);
        assertEquals(52000L, actual);
    }
    
    @Test
    @DisplayName("Тест расчёта общего коэффициента каршеринга")
    void test_calcTotalCoefCarSharing() {
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDate.parse("2020-11-15").atStartOfDay())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(50)
                                 .trafficJamScore(8)
                                 .engineVolume(3000)
                                 .options(Set.of(RequestOptions.values()))
                                 .build();
        
        double actual = calculateService.calculateCarSharingCoefficient(carSharingTariff1, tripDto);
        double expected = calculateCarSharingCoefficient(carSharingTariff1, tripDto);
        
        assertEquals(expected, actual);
    }
    
    @Test
    @DisplayName("Тест полного расчёта каршеринга")
    void test_calcTotalCostCarSharing() {
        
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDate.parse("2020-11-15").atStartOfDay())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(50)
                                 .options(Set.of(RequestOptions.values()))
                                 .build();
        
        final var actual = calculateService.calculateByTransportType(carSharingTariff1, tripDto);
        
        long expected =
                (long) (calcBasicCarSharingCost(tripDto)
                        * calculateCarSharingCoefficient(carSharingTariff1, tripDto));
        
        assertEquals(210000D, actual);
    }
    
    @Test
    @DisplayName("Тест базового расчёта велосипеда")
    void test_calcBicycleBasicCost() {
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDateTime.now())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(500).build();
        
        long actual = calculateService.calculateBicycleBasicCost(bicycleTariff1, tripDto);
        assertEquals(52300, actual);
    }
    
    @Test
    @DisplayName("Тест расчёта общего коэффициента велосипеда")
    void test_calcTotalCoefCarBicycle() {
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDate.parse("2020-11-15").atStartOfDay())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(50)
                                 .trafficJamScore(8)
                                 .engineVolume(3000)
                                 .options(Set.of(RequestOptions.values()))
                                 .build();
        
        double actual = calculateService.calculateBicycleCoefficient(bicycleTariff1, tripDto);
        double expected = calculateBicycleCoefficient(bicycleTariff1, tripDto);
        
        assertEquals(expected, actual);
    }
    
    @Test
    @DisplayName("Тест полного расчёта велосипеда")
    void test_calcTotalCostBicycle() {
        
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDate.parse("2020-11-15").atStartOfDay())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(50)
                                 .options(Set.of(RequestOptions.values()))
                                 .build();
        
        final var actual = calculateService.calculateByTransportType(bicycleTariff1, tripDto);
        
        assertEquals(37230D, actual);
    }
    
    @Test
    @DisplayName("Тест базового расчёта самоката")
    void test_calcScooterBasicCost() {
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDateTime.now())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(500).build();
        
        long actual = calculateService.calculateScooterBasicCost(scooterTariff, tripDto);
        assertEquals(52300L, actual);
    }
    
    @Test
    @DisplayName("Тест расчёта общего коэффициента самоката")
    void test_calcTotalCoefCarScooter() {
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDate.parse("2020-11-15").atStartOfDay())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(50)
                                 .trafficJamScore(8)
                                 .engineVolume(3000)
                                 .options(Set.of(RequestOptions.values()))
                                 .build();
        
        double actual = calculateService.calculateScooterCoefficient(scooterTariff, tripDto);
        double expected = calculateScooterCoefficient(scooterTariff, tripDto);
        
        assertEquals(expected, actual);
    }
    
    @Test
    @DisplayName("Тест полного расчёта самоката")
    void test_calcTotalCostScooter() {
        
        TripDto tripDto = TripDto.builder()
                                 .tripDate(LocalDate.parse("2020-11-15").atStartOfDay())
                                 .time(Duration.ofMinutes(10))
                                 .waitingTime(Duration.ofMinutes(5))
                                 .distance(50)
                                 .options(Set.of(RequestOptions.values()))
                                 .build();
        
        final var actual = calculateService.calculateByTransportType(scooterTariff, tripDto);

        assertEquals(37230D, actual);
    }
    
    private long calcSuburbTaxiCost(SuburbTariffParams suburbTariffParams, SuburbTripDataDTO suburbTripData) {
        long result = (long) (suburbTripData.getInterRegionDistance() * suburbTariffParams.getCostPerKmInterRegion()
                              + (long) suburbTripData.getInterRegionTime() * suburbTariffParams.getCostPerMinInterRegion()
                              + suburbTripData.getSuburbDistance() * suburbTariffParams.getCostPerKmSuburb()
                              + (long) suburbTripData.getSuburbTime() * suburbTariffParams.getCostPerMinSuburb()
                              + suburbTripData.getSuburbServiceDistance() *
                                suburbTariffParams.getSuburbServiceCostPerKm()
                              + (long) suburbTripData.getSuburbServiceTime() *
                                suburbTariffParams.getSuburbServiceCostPerMin());
        System.out.println("Calced coef was: " + result);
        return result;
    }
    
    private long calcSuburbPersonalCost(SuburbTariffParams suburbTariffParams, SuburbTripDataDTO suburbTripData) {
        long result = (long) (suburbTripData.getInterRegionDistance() * suburbTariffParams.getCostPerKmInterRegion()
                              + (long) suburbTripData.getInterRegionTime() * suburbTariffParams.getCostPerMinInterRegion()
                              + suburbTripData.getSuburbDistance() * suburbTariffParams.getCostPerKmSuburb()
                              + (long) suburbTripData.getSuburbTime() * suburbTariffParams.getCostPerMinSuburb());
        System.out.println("Calced coef was: " + result);
        return result;
    }
    
    private double calcTaxiCoefficient(TripDto tripDto) {
        
        double result = taxiTariff1.getTimedTariffParams().getCoefDayOff()
                        * (1 + taxiTariff1.getCoefTraffic() * Math.max(0, (tripDto.getTrafficJamScore() -
                                                                           trafficThreshold)))
                        * taxiTariff1.getCoefBicycle()
                        * taxiTariff1.getCoefChildSeat()
                        * taxiTariff1.getCoefOrg()
                        * taxiTariff1.getCoefPetTransport();
        System.out.println("Calced coef was: " + result);
        return result;
    }
    
    private double calcBasicTaxiCost(TripDto tripDto) {
        double result = taxiTariff1.getMinRideDistanceCost() + Math.max(0,
                                                                                tripDto.getDistance() -
                                                                                taxiTariff1.getDistanceIncluded()) *
                                                                       taxiTariff1.getRideCostPerKm()
                        + taxiTariff1.getMinRideTimeCost()+ Math.max(0,
                                                                              (tripDto.getTime().toMinutes() -
                                                                               taxiTariff1.getTimeIncluded()) *
                                                                              taxiTariff1.getRideCostPerMin())
                        + taxiTariff1.getWaitCostPerMin() * Math.max(0,
                                                                     tripDto.getWaitingTime().toMinutes() -
                                                                     taxiTariff1.getFreeWaitingTime()) +
                        taxiTariff1.getWaitCostPerMinIntermediate() * tripDto.getIntermediateWaitingTime().toMinutes();
        System.out.println("Calced basic cost was: " + result);
        return result;
    }
    
    private double calcBasicPersonalCost(TripDto tripDto) {
        return Math.max(personalTariff1.getMinRideDistanceCost(), Math.max(0,
                                                                           tripDto.getDistance() -
                                                                           personalTariff1
                                                                                   .getDistanceIncluded()) *
                                                                  personalTariff1.getRideCostPerKm())
               + Math.max(personalTariff1.getMinRideTimeCost(), Math.max(0,
                                                                         (tripDto.getTime().toMinutes() -
                                                                          personalTariff1.getTimeIncluded()) *
                                                                         personalTariff1.getRideCostPerMin()))
               + personalTariff1.getWaitCostPerMin() * tripDto.getWaitingTime().toMinutes();
    }
    
    private long calcBasicCarSharingCost(TripDto tripDto) {
        return (long) (carSharingTariff1.getRideCostPerMin() * tripDto.getTime().toMinutes() +
                       carSharingTariff1.getRideCostPerKm() * tripDto.getDistance() +
                       carSharingTariff1.getWaitCostPerMin() * (tripDto.getIntermediateWaitingTime()
                                                                         .toMinutes()));
    }
    
    private double calculateTotalPersonalCoefficient(@NotNull PersonalTariff tariff, @NotNull TripDto tripDto) {
        double coefficient = 1d;
        LocalDateTime tripTime = getTripTime(tripDto);
        coefficient *= calculateService.getSeasonalCoefficient(tariff, tripTime);
        coefficient *= calculateService.getTimeCoefficient(tariff.getTimedTariffParams(), tripTime);
        if (tripDto.getTrafficJamScore() >= 7) {
            coefficient *= (1 + tariff.getCoefTraffic() * (tripDto.getTrafficJamScore() - trafficThreshold));
        }
        Set<RequestOptions> options = tripDto.getOptions();
        if (options.contains(RequestOptions.MATERIAL_ASSETS_TRANSPORTATION)) {
            coefficient *= tariff.getCoefMaterialAssets();
        }
        coefficient *= calculateService.getEngineCoefficient(tariff.getEngineTariffParams(), tripDto);
        return coefficient;
    }
    
    
    protected double calculateCarSharingCoefficient(@NotNull CarSharingTariff tariff, @NotNull TripDto tripData) {
        double coefficient = 1d;
        Set<RequestOptions> options = tripData.getOptions();
        if (options.contains(RequestOptions.CHILD_SEAT)) {
            coefficient *= tariff.getCoefChildSeat();
        }
        if (options.contains(RequestOptions.PET_TRANSPORTATION)) {
            coefficient *= tariff.getCoefPetTransport();
        }
        if (tripData.getTrafficJamScore() >= 7) {
            coefficient *= (1 + tariff.getCoefTraffic() * (tripData.getTrafficJamScore() - trafficThreshold));
        }
        LocalDateTime tripTime = getTripTime(tripData);
        coefficient *= calculateService.getTimeCoefficient(tariff.getTimedTariffParams(), tripTime);
        return coefficient;
    }
    
    protected double calculateBicycleCoefficient(BicycleTariff tariff, TripDto tripData) {
        double coefficient = 1d;
        LocalDateTime tripTime = getTripTime(tripData);
        coefficient *= calculateService.getTimeCoefficient(tariff.getTimedTariffParams(), tripTime);
        coefficient *= tariff.getCoefInsurance();
        return coefficient;
    }
    
    protected double calculateScooterCoefficient(ScooterTariff tariff, TripDto tripData) {
        double coefficient = 1d;
        LocalDateTime tripTime = getTripTime(tripData);
        coefficient *= calculateService.getTimeCoefficient(tariff.getTimedTariffParams(), tripTime);
        coefficient *= tariff.getCoefInsurance();
        return coefficient;
    }
    
    private LocalDateTime getTripTime(TripDto tripData) {
        if (tripData == null || tripData.getTripDate() == null) {
            return null;
        }
        String timeZone = tripData.getTimeZone();
        if (timeZone == null) {
            timeZone = "GMT+03";
        }
        return ZonedDateTime.of(tripData.getTripDate(), TimeZone.getTimeZone("UTC").toZoneId())
                            .withZoneSameInstant(TimeZone.getTimeZone(timeZone).toZoneId())
                            .toLocalDateTime();
    }
}
