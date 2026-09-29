package issues;

import io.qameta.allure.Feature;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.ConsentFunction;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.trip.TripApplication;
import ru.sber.transport.trip.business.dto.IntegrationType;
import ru.sber.transport.trip.database.trips.tables.records.ContractorsRecord;
import ru.sber.transport.trip.database.trips.tables.records.TripsRecord;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.trip.database.trips.Tables.CONTRACTORS;
import static ru.sber.transport.trip.database.trips.Tables.TRIPS_;

@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
@DisplayName("TRANSPORT-24020")
@SpringBootTest(classes = TripApplication.class)
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
class Transport24020Test extends KafkaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DSLContext context;

    @MockBean
    private AuthorizationManager<?> manager;

    @MockBean
    private ConsentFunction function;

    @BeforeEach
    void beforeEach() {
        AuthorizeUtils.authorize(manager);
        when(function.apply(any())).thenReturn(true);
    }

    @Test
    @DisplayName("Поиск информации")
    void test_search() throws Exception {
        var searchString = "137";
        var contractorId = UUID.randomUUID();

        context.insertInto(CONTRACTORS)
            .set(new ContractorsRecord(contractorId, BigInteger.ONE, true, IntegrationType.DISPATCHER.name()))
            .execute();

        var requests = JSON.json("""
            [
                {
                    "id":"ec458d9d-cbb4-4c64-b31a-44f59df8a1b9",
                    "authorId":"aeda1ad4-5ea5-4dc6-950e-2b8ca7200460",
                    "humanReadableId":"OT-0001-00013788",
                    "author": {
                        "id":null,
                        "lastName":"Незымаев",
                        "firstName":"Антон",
                        "patronymic":"Александрович",
                        "mobilePhone":"+79197634848"
                    },
                    "passengerId":"aeda1ad4-5ea5-4dc6-950e-2b8ca7200460",
                    "passenger": {
                        "id":null,
                        "lastName":"Незымаев",
                        "firstName":"Антон",
                        "patronymic":"Александрович",
                        "mobilePhone":"+79197634848"
                    },
                    "organizationId":"6e6e04b7-1912-422a-820a-1a6777dfde1b",
                    "taxiClass":"ECONOMY",
                    "passengerCount":1,
                    "expected": {
                        "cost":12200,
                        "distance":30.107999999999997,
                        "time":5559.000000000
                    },
                    "creationTime":"2024-02-06T14:13:46.000000135Z",
                    "desiredDate":"2024-02-06T16:00:00Z",
                    "rideId":"997b8764-0ed5-4061-aac5-f9469bd9c6a0",
                    "suburb":false,
                    "timeZone":"GMT+03:00",
                    "requestOptions":null,
                    "tariffId":"c7d24e8b-17fd-43aa-b027-51c14d103177",
                    "tariff": {
                        "transport_type":"TAXI",
                        "id":"c7d24e8b-17fd-43aa-b027-51c14d103177",
                        "humanReadableId":"TF-0001-00000707",
                        "serviceType":"EMPLOYEE_TRANSPORTATION",
                        "organizationId":"6e6e04b7-1912-422a-820a-1a6777dfde1b",
                        "regionId":"2c6f7c55-69bd-4efb-a4e9-167c4fc3c9f0",
                        "region":"Москва",
                        "transportType": ["TransportTypeEnum","TAXI"],
                        "active":true,
                        "contractId":"eddddd35-ff26-40f8-8080-321c0aa4dd45",
                        "contractorId":"1f00bfdb-32d7-4bce-9dcf-f68056f5ec93",
                        "taxiClass":"ECONOMY",
                        "rideCostPerKm":100,
                        "distanceIncluded":1.0,
                        "minRideDistanceCost":100,
                        "rideCostPerMin":100,
                        "timeIncluded":1,
                        "minRideTimeCost":100,
                        "waitCostPerMin":100,
                        "waitCostPerMinIntermediate":100,
                        "freeWaitingTime":1,
                        "carServiceCost":0,
                        "timedTariffParams": {
                            "coefWorkDayMorning":1.0,
                            "coefWorkDayNoon":1.0,
                            "coefWorkDayEvening":1.0,
                            "coefWorkDayNight":1.0,
                            "coefDayOff":1.0
                        },
                        "coopTariffParams": {
                            "savingsDeviationPct":5.0,
                            "distanceDeviationKm":0.9,
                            "timeDeviationMin":10,
                            "minCancelTimeMin":30
                        },
                        "suburbTariffParams": {
                            "costPerKmSuburb":100,
                            "costPerMinSuburb":100,
                            "suburbServiceCostPerKm":100,
                            "suburbServiceCostPerMin":100,
                            "costPerKmInterRegion":0,
                            "costPerMinInterRegion":0
                        },
                        "contractorDeviationParams": {
                            "maxDiffComputedDistancePercent":20,
                            "maxDiffFactDistancePercent":20,
                            "maxDiffComputedCostPercent":20,
                            "maxDiffContractorCostPercent":1,
                            "maxDiffComputedWaitingPercent":20
                        },
                        "coefTraffic":1.0,
                        "coefChildSeat":1.0,
                        "coefPetTransport":1.0,
                        "coefBicycle":1.0,
                        "coefOrg":1.0,
                        "workGroup":"000001_Тестовая_организация_01/транспорт/Москва/3108561415",
                        "triggerTime":120,
                        "isNightTariff":false
                    },
                    "contractorId":"%s",
                    "status":"WAITING_FOR_ASSIGNMENT",
                    "transportType":"TAXI",
                    "sharedRideOwner":true,
                    "coop":true
                }
            ]""".formatted(contractorId).replace("\n", "").replace(" ", ""));

        context.insertInto(TRIPS_)
            .set(new TripsRecord(UUID.randomUUID(), "WAITING_FOR_ASSIGNMENT", contractorId, null, BigInteger.ONE, requests, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, false, null, null, OffsetDateTime.now(), OffsetDateTime.now().plusDays(1), OffsetDateTime.now(), OffsetDateTime.now().plusDays(1), null, null))
            .execute();

        mockMvc
            .perform(get("/contractor/%s/?requestHumanReadableId=%s".formatted(contractorId, searchString))
                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).claim("data_master", true)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1));

    }

    @Test
    @DisplayName("Поиск информации. Красивый JSON")
    void test_search_pretty_json() throws Exception {
        var searchString = "137";
        var contractorId = UUID.randomUUID();

        context.insertInto(CONTRACTORS)
            .set(new ContractorsRecord(contractorId, BigInteger.ONE, true, IntegrationType.DISPATCHER.name()))
            .execute();

        var requests = JSON.json("""
            [
                {
                    "id":"ec458d9d-cbb4-4c64-b31a-44f59df8a1b9",
                    "authorId":"aeda1ad4-5ea5-4dc6-950e-2b8ca7200460",
                    "humanReadableId":"OT-0001-00013788",
                    "author": {
                        "id":null,
                        "lastName":"Незымаев",
                        "firstName":"Антон",
                        "patronymic":"Александрович",
                        "mobilePhone":"+79197634848"
                    },
                    "passengerId":"aeda1ad4-5ea5-4dc6-950e-2b8ca7200460",
                    "passenger": {
                        "id":null,
                        "lastName":"Незымаев",
                        "firstName":"Антон",
                        "patronymic":"Александрович",
                        "mobilePhone":"+79197634848"
                    },
                    "organizationId":"6e6e04b7-1912-422a-820a-1a6777dfde1b",
                    "taxiClass":"ECONOMY",
                    "passengerCount":1,
                    "expected": {
                        "cost":12200,
                        "distance":30.107999999999997,
                        "time":5559.000000000
                    },
                    "creationTime":"2024-02-06T14:13:46.000000135Z",
                    "desiredDate":"2024-02-06T16:00:00Z",
                    "rideId":"997b8764-0ed5-4061-aac5-f9469bd9c6a0",
                    "suburb":false,
                    "timeZone":"GMT+03:00",
                    "requestOptions":null,
                    "tariffId":"c7d24e8b-17fd-43aa-b027-51c14d103177",
                    "tariff": {
                        "transport_type":"TAXI",
                        "id":"c7d24e8b-17fd-43aa-b027-51c14d103177",
                        "humanReadableId":"TF-0001-00000707",
                        "serviceType":"EMPLOYEE_TRANSPORTATION",
                        "organizationId":"6e6e04b7-1912-422a-820a-1a6777dfde1b",
                        "regionId":"2c6f7c55-69bd-4efb-a4e9-167c4fc3c9f0",
                        "region":"Москва",
                        "transportType": ["TransportTypeEnum","TAXI"],
                        "active":true,
                        "contractId":"eddddd35-ff26-40f8-8080-321c0aa4dd45",
                        "contractorId":"1f00bfdb-32d7-4bce-9dcf-f68056f5ec93",
                        "taxiClass":"ECONOMY",
                        "rideCostPerKm":100,
                        "distanceIncluded":1.0,
                        "minRideDistanceCost":100,
                        "rideCostPerMin":100,
                        "timeIncluded":1,
                        "minRideTimeCost":100,
                        "waitCostPerMin":100,
                        "waitCostPerMinIntermediate":100,
                        "freeWaitingTime":1,
                        "carServiceCost":0,
                        "timedTariffParams": {
                            "coefWorkDayMorning":1.0,
                            "coefWorkDayNoon":1.0,
                            "coefWorkDayEvening":1.0,
                            "coefWorkDayNight":1.0,
                            "coefDayOff":1.0
                        },
                        "coopTariffParams": {
                            "savingsDeviationPct":5.0,
                            "distanceDeviationKm":0.9,
                            "timeDeviationMin":10,
                            "minCancelTimeMin":30
                        },
                        "suburbTariffParams": {
                            "costPerKmSuburb":100,
                            "costPerMinSuburb":100,
                            "suburbServiceCostPerKm":100,
                            "suburbServiceCostPerMin":100,
                            "costPerKmInterRegion":0,
                            "costPerMinInterRegion":0
                        },
                        "contractorDeviationParams": {
                            "maxDiffComputedDistancePercent":20,
                            "maxDiffFactDistancePercent":20,
                            "maxDiffComputedCostPercent":20,
                            "maxDiffContractorCostPercent":1,
                            "maxDiffComputedWaitingPercent":20
                        },
                        "coefTraffic":1.0,
                        "coefChildSeat":1.0,
                        "coefPetTransport":1.0,
                        "coefBicycle":1.0,
                        "coefOrg":1.0,
                        "workGroup":"000001_Тестовая_организация_01/транспорт/Москва/3108561415",
                        "triggerTime":120,
                        "isNightTariff":false
                    },
                    "contractorId":"%s",
                    "status":"WAITING_FOR_ASSIGNMENT",
                    "transportType":"TAXI",
                    "sharedRideOwner":true,
                    "coop":true
                }
            ]""".formatted(contractorId));

        context.insertInto(TRIPS_)
            .set(new TripsRecord(UUID.randomUUID(), "WAITING_FOR_ASSIGNMENT", contractorId, null, BigInteger.ONE, requests, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, false, null, null, OffsetDateTime.now(), OffsetDateTime.now().plusDays(1), OffsetDateTime.now(), OffsetDateTime.now().plusDays(1), null, null))
            .execute();

        mockMvc
            .perform(get("/contractor/%s/?requestHumanReadableId=%s".formatted(contractorId, searchString))
                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).claim("data_master", true)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("Поиск информации. Нецелевое поле")
    void test_search_no_target() throws Exception {
        var searchString = "WAITING_FOR_ASSIGNMENT";
        var contractorId = UUID.randomUUID();

        context.insertInto(CONTRACTORS)
            .set(new ContractorsRecord(contractorId, BigInteger.ONE, true, IntegrationType.DISPATCHER.name()))
            .execute();

        var requests = JSON.json("""
            [
                {
                    "id":"ec458d9d-cbb4-4c64-b31a-44f59df8a1b9",
                    "authorId":"aeda1ad4-5ea5-4dc6-950e-2b8ca7200460",
                    "humanReadableId":"OT-0001-00013788",
                    "author": {
                        "id":null,
                        "lastName":"Незымаев",
                        "firstName":"Антон",
                        "patronymic":"Александрович",
                        "mobilePhone":"+79197634848"
                    },
                    "passengerId":"aeda1ad4-5ea5-4dc6-950e-2b8ca7200460",
                    "passenger": {
                        "id":null,
                        "lastName":"Незымаев",
                        "firstName":"Антон",
                        "patronymic":"Александрович",
                        "mobilePhone":"+79197634848"
                    },
                    "organizationId":"6e6e04b7-1912-422a-820a-1a6777dfde1b",
                    "taxiClass":"ECONOMY",
                    "passengerCount":1,
                    "expected": {
                        "cost":12200,
                        "distance":30.107999999999997,
                        "time":5559.000000000
                    },
                    "creationTime":"2024-02-06T14:13:46.000000135Z",
                    "desiredDate":"2024-02-06T16:00:00Z",
                    "rideId":"997b8764-0ed5-4061-aac5-f9469bd9c6a0",
                    "suburb":false,
                    "timeZone":"GMT+03:00",
                    "requestOptions":null,
                    "tariffId":"c7d24e8b-17fd-43aa-b027-51c14d103177",
                    "tariff": {
                        "transport_type":"TAXI",
                        "id":"c7d24e8b-17fd-43aa-b027-51c14d103177",
                        "humanReadableId":"TF-0001-00000707",
                        "serviceType":"EMPLOYEE_TRANSPORTATION",
                        "organizationId":"6e6e04b7-1912-422a-820a-1a6777dfde1b",
                        "regionId":"2c6f7c55-69bd-4efb-a4e9-167c4fc3c9f0",
                        "region":"Москва",
                        "transportType": ["TransportTypeEnum","TAXI"],
                        "active":true,
                        "contractId":"eddddd35-ff26-40f8-8080-321c0aa4dd45",
                        "contractorId":"1f00bfdb-32d7-4bce-9dcf-f68056f5ec93",
                        "taxiClass":"ECONOMY",
                        "rideCostPerKm":100,
                        "distanceIncluded":1.0,
                        "minRideDistanceCost":100,
                        "rideCostPerMin":100,
                        "timeIncluded":1,
                        "minRideTimeCost":100,
                        "waitCostPerMin":100,
                        "waitCostPerMinIntermediate":100,
                        "freeWaitingTime":1,
                        "carServiceCost":0,
                        "timedTariffParams": {
                            "coefWorkDayMorning":1.0,
                            "coefWorkDayNoon":1.0,
                            "coefWorkDayEvening":1.0,
                            "coefWorkDayNight":1.0,
                            "coefDayOff":1.0
                        },
                        "coopTariffParams": {
                            "savingsDeviationPct":5.0,
                            "distanceDeviationKm":0.9,
                            "timeDeviationMin":10,
                            "minCancelTimeMin":30
                        },
                        "suburbTariffParams": {
                            "costPerKmSuburb":100,
                            "costPerMinSuburb":100,
                            "suburbServiceCostPerKm":100,
                            "suburbServiceCostPerMin":100,
                            "costPerKmInterRegion":0,
                            "costPerMinInterRegion":0
                        },
                        "contractorDeviationParams": {
                            "maxDiffComputedDistancePercent":20,
                            "maxDiffFactDistancePercent":20,
                            "maxDiffComputedCostPercent":20,
                            "maxDiffContractorCostPercent":1,
                            "maxDiffComputedWaitingPercent":20
                        },
                        "coefTraffic":1.0,
                        "coefChildSeat":1.0,
                        "coefPetTransport":1.0,
                        "coefBicycle":1.0,
                        "coefOrg":1.0,
                        "workGroup":"000001_Тестовая_организация_01/транспорт/Москва/3108561415",
                        "triggerTime":120,
                        "isNightTariff":false
                    },
                    "contractorId":"%s",
                    "status":"WAITING_FOR_ASSIGNMENT",
                    "transportType":"TAXI",
                    "sharedRideOwner":true,
                    "coop":true
                }
            ]""".formatted(contractorId).replace("\n", "").replace(" ", ""));

        context.insertInto(TRIPS_)
            .set(new TripsRecord(UUID.randomUUID(), "WAITING_FOR_ASSIGNMENT", contractorId, null, BigInteger.ONE, requests, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, false, null, null, OffsetDateTime.now(), OffsetDateTime.now().plusDays(1), OffsetDateTime.now(), OffsetDateTime.now().plusDays(1), null, null))
            .execute();

        mockMvc
            .perform(get("/contractor/%s/?requestHumanReadableId=%s".formatted(contractorId, searchString))
                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("scope", "DISPATCHER").claim("roles", List.of("ANY_ROLE")).claim("data_master", true)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(0));

    }
}
