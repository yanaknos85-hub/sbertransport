package ru.sberbank.ditsib.transport.tariff.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.tariff.model.PublicTripDTO;
import ru.sber.transport.tariff.model.TripDto;
import ru.sber.transport.tariff.model.WaypointDTO;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.dto.tariff.CalculatedDto;
import ru.sberbank.ditsib.transport.tariff.database.dao.*;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;
import ru.sberbank.ditsib.transport.tariff.dto.RegionDto;
import ru.sberbank.ditsib.transport.tariff.service.ContractService;
import ru.sberbank.ditsib.transport.tariff.service.RegionDataResolver;

import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@Slf4j
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@Transactional
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка контроллера подсчета стоимости")
@ActiveProfiles({"test", "kafka"})
class CalculatingControllerTest extends KafkaTest {
    
    private static final int TAXI_TARIFF_COUNT = 5;
    private static final int TARIFF_PERSONAL_COUNT = 3;
    
    public static final String USER_ID = "f10bcc5b-51db-4e1c-a747-2a229604f974";
    
    private Organization organization1;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private PersonalTariffRepository personalTariffRepository;
    
    @Autowired
    private TaxiTariffRepository taxiTariffRepository;
    
    @Autowired
    private PublicTariffRepository publicTariffRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private ContractService contractService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private RegionDataResolver regionDataResolver;
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    private final UUID moscow = UUID.randomUUID();
    private final UUID spb = UUID.randomUUID();
    private final UUID samara = UUID.randomUUID();
    
    @BeforeEach
    void fillRepository() {
        organization1 = Instancio.of(Organization.class)
                .set(field(Organization::getDigitId), 1L)
                .create();
        var organization2 = Instancio.of(Organization.class)
                .set(field(Organization::getDigitId), 2L)
                .create();
        organizationRepository.saveAndFlush(organization1);
        organizationRepository.saveAndFlush(organization2);
    
        Employee employee = Employee.builder()
                                    .userId(UUID.fromString(USER_ID))
                                    .id(UUID.randomUUID())
                                    .departmentId(UUID.randomUUID())
                                    .build();
        employeeRepository.save(employee);
        
        Calendar now = Calendar.getInstance();
        LocalDateTime plannedDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        int plannedYear = plannedDate.getYear();
        
        UUID contractorId1 = UUID.randomUUID();
        UUID contractorId2 = UUID.randomUUID();
        UUID contractorId3 = UUID.randomUUID();
        
        Contract contract1 = new Contract();
        contract1.setUserId(UUID.randomUUID());
        contract1.setContractorId(contractorId1);
        contract1.getOrganizations().add(organization1);
        contract1.setContractNumber("Contract number 1");
        contract1.setTransportType(TransportTypeEnum.TAXI);
        contract1.getRegionIds().add(moscow);
        contract1.setSum(1000L);
        contract1.setStartDate(LocalDate.of(plannedYear - 1, 1, 1));
        contract1.setEndDate(LocalDate.of(plannedYear + 1, 1, 1));
        contract1.setCreationTime(LocalDateTime.now());
        contract1.setContractNumber("Contract 1");
        contract1.setContractType(ContractType.INCOME);
        contract1.setRestrictionType(RestrictionType.NONE);
        contractService.save(contract1);
        
        Contract contract2 = new Contract();
        contract2.setUserId(UUID.randomUUID());
        contract2.setContractorId(contractorId2);
        contract2.getOrganizations().add(organization1);
        contract2.setContractNumber("Contract number 2");
        contract2.getRegionIds().add(moscow);
        contract2.setTransportType(TransportTypeEnum.TAXI);
        contract2.setSum(2000L);
        contract2.setStartDate(LocalDate.of(plannedYear - 1, 1, 1));
        contract2.setEndDate(LocalDate.of(plannedYear + 1, 1, 1));
        contract2.setCreationTime(LocalDateTime.now());
        contract2.setContractNumber("Contract 2");
        contract2.setContractType(ContractType.INCOME);
        contract2.setRestrictionType(RestrictionType.NONE);
        contractService.save(contract2);
        
        Contract contract3 = new Contract();
        contract3.setUserId(UUID.randomUUID());
        contract3.setContractorId(contractorId3);
        contract3.getOrganizations().add(organization1);
        contract3.setContractNumber("Contract number 3");
        contract3.getRegionIds().add(spb);
        contract3.setTransportType(TransportTypeEnum.TAXI);
        contract3.setSum(4000L);
        contract3.setStartDate(LocalDate.of(plannedYear - 1, 1, 1));
        contract3.setEndDate(LocalDate.of(plannedYear + 1, 1, 1));
        contract3.setContractNumber("Contract 3");
        contract3.setCreationTime(LocalDateTime.now());
        contract3.setContractType(ContractType.INCOME);
        contract3.setRestrictionType(RestrictionType.NONE);
        contractService.save(contract3);
        
        Contract contract4 = new Contract();
        contract4.setUserId(UUID.randomUUID());
        contract4.setContractorId(contractorId3);
        contract4.setContractNumber("Contract 4");
        contract4.getOrganizations().add(organization2);
        contract4.setContractNumber("Contract number 4");
        contract4.getRegionIds().add(spb);
        contract4.setTransportType(TransportTypeEnum.TAXI);
        contract4.setSum(4000L);
        contract4.setStartDate(LocalDate.of(plannedYear - 1, 1, 1));
        contract4.setEndDate(LocalDate.of(plannedYear + 1, 1, 1));
        contract4.setCreationTime(LocalDateTime.now());
        contract4.setContractType(ContractType.INCOME);
        contract4.setRestrictionType(RestrictionType.NONE);
        contractService.save(contract4);
        
        for (int i = 0; i < TAXI_TARIFF_COUNT; i++) {
            var tariff = TaxiTariff.builder()
                                   .organization(organization1)
                                   .regionId(i % 2 == 0 ? moscow : spb)
                                   .taxiClass(TaxiClass.ECONOMY)
                                   .contract(contract1)
                                   .freeWaitingTime(0)
                                   .rideCostPerKm(i + 1)
                                   .rideCostPerMin(i * 2 + 1)
                                   .minRideDistanceCost(i * 3)
                                   .minRideTimeCost(i * 4)
                                   .waitCostPerMin(i)
                                   .transportType(TransportTypeEnum.TAXI)
                                   .build();
            taxiTariffRepository.save(tariff);
        }
        
        for (var i = 0; i < TARIFF_PERSONAL_COUNT; i++) {
            var tariff = PersonalTariff.builder()
                                       .rideCostPerKm(i + 1)
                                       .rideCostPerMin(i + 1)
                                       .organization(organization1)
                                       .regionId(moscow)
                                       .seasonStart(LocalDate.now(ZoneId.of(ZoneOffset.UTC.getId())))
                                       .seasonEnd(LocalDate.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(1))
                                       .seasonalCoefficient(i * 100. + .001)
                                       .transportType(TransportTypeEnum.PERSONAL)
                                       .build();
            
            personalTariffRepository.save(tariff);
        }
        
        var publicTariff1 = PublicTariff.builder()
                                        .regionId(samara)
                                        .organization(organization1)
                                        .transportType(TransportTypeEnum.PUBLIC)
                                        .metroAvailability(true)
                                        .tramAvailability(true)
                                        .trolleybusAvailability(true)
                                        .busAvailability(true)
                                        .metroTicketCost(50)
                                        .tramTicketCost(50)
                                        .trolleybusTicketCost(50)
                                        .busTicketCost(50)
                                        .build();
        publicTariffRepository.save(publicTariff1);
    }
    
    @AfterEach
    void afterEach() {
        personalTariffRepository.deleteAll();
        taxiTariffRepository.deleteAll();
        publicTariffRepository.deleteAll();
        organizationRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Расчет всех тарифов")
    @WithMockUser(username = USER_ID, roles = "GUEST")
    @Disabled("Требуется актуализация")
    void calculateTrip() throws Exception {
        RegionDto regionDto = RegionDto.builder().id(moscow).name("MOSKVA").code("1").build();
        when(regionDataResolver.getRegionBranch(any())).thenReturn(Collections.singletonList(regionDto));
        
        personalTariffRepository.flush();
        taxiTariffRepository.flush();
        System.out.println(personalTariffRepository.count());
        
        PublicTripDTO publicTripData = PublicTripDTO.builder().metroTicketsQuantity(2).tramTicketsQuantity(2)
                                                    .trolleybusTicketsQuantity(2).busTicketsQuantity(2).build();
        
        var data = objectMapper.writeValueAsString(createDataWithPublic(
                LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                Duration.ofHours(5),
                publicTripData,
                organization1.getId()));
        
        var response = mockMvc.perform(post("/calculate").with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))
                                                         .content(data)
                                                         .contentType(MediaType.APPLICATION_JSON))
                              .andExpect(status().isOk()).andReturn();
        
        var actualList = objectMapper.readValue(response.getResponse().getContentAsString(),
                                                new TypeReference<List<CalculatedDto>>() {
                                                });
        //Проверка общего количества возвращенных тарифов - может разниться
        assertThat(actualList).hasSize(4);
        //Проверка количества такси тарифов - может разниться
        long taxiTariffCount = actualList.stream()
                                         .filter(dto -> TransportTypeEnum.TAXI.name().equals(dto.getTransportType()))
                                         .count();
        System.out.println("OUTPUT: found taxi tariffs " + taxiTariffCount);
        // 1 - количество тарифов по региону Москва.
        assertEquals(1, taxiTariffCount);
        
        for (var actual : actualList) {
            var tariffType = actual.getTransportType();
            switch (tariffType) {
                case TransportTypeEnum.Constants.TAXI_STRING -> assertThat(actual.getCost()).as("Check tariff TAXI cost")
                                                                                            .isIn(400L, 1814L, 3228L);
                case TransportTypeEnum.Constants.PERSONAL_STRING -> assertThat(actual.getCost()).as("Check tariff TAXI cost")
                                                                                                .isIn(0L, 400L, 800L, 1200L, 80000L, 240001L, 20000L);
                case TransportTypeEnum.Constants.PUBLIC_STRING -> assertThat(actual.getCost()).isSameAs(400L);
            }
        }
    }
    
    @Test
    @DisplayName("Расчёт тарифа такси")
    @WithMockUser(username = USER_ID, roles = "GUEST")
    @Disabled("Требуется актуализация")
    void testCalculateTrip_taxi() throws Exception {
        RegionDto regionDto = RegionDto.builder().id(moscow).name("MOSKVA").code("1").build();
        when(regionDataResolver.getRegion(any())).thenReturn(regionDto);
        TripDto tripdDto = createTripDto(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                                         Duration.ofHours(5),
                                         organization1.getId());
        var data = objectMapper.writeValueAsString(tripdDto);
        
        var response = mockMvc.perform(post("/calculate/" + TransportTypeEnum.TAXI.getId()).content(data)
                                                                                           .contentType(
                                                                                                   MediaType.APPLICATION_JSON)
                                                                                           .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
                              .andExpect(status().isOk()).andReturn();
        
        var actualList = objectMapper.readValue(response.getResponse().getContentAsString(),
                                                new TypeReference<List<CalculatedDto>>() {
                                                });
        
        //3 в соответствии с правилами выбора тарифа по организации, региону и контракту
        assertThat(actualList).hasSize(3);
        for (var actual : actualList) {
            assertThat(actual.getTransportType()).isEqualTo(TransportTypeEnum.Constants.TAXI_STRING);
            assertThat(actual.getCost()).as("Check tariff TAXI cost")
                                        .isIn(400L, 1814L, 3228L);
        }
    }
    
    @Test
    @DisplayName("Расчет тарифа несуществующего типа")
    @WithMockUser(roles = "GUEST")
    void testCalculateTrip_unknownTransportType() throws Exception {
        var data = createTripDto(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())), Duration.ofHours(5),
                                 organization1.getId());
        
        mockMvc.perform(post("/calculate/7f18ce71-99a7-47b5-b285-335058c6715d")
                                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))
                                .content(objectMapper.writeValueAsString(data))
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Расчёт тарифа несуществующего типа")
    @WithMockUser(roles = "GUEST")
    void testCalculateTrip_unknown() throws Exception {
        var data = createTripDto(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())), Duration.ofHours(5));
        
        mockMvc.perform(post("/calculate/unknown").content(objectMapper.writeValueAsString(data))
                                                  .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
               .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Расчет тарифа личного ТС")
    @WithMockUser(username = USER_ID, roles = "GUEST")
    @Disabled("Требуется актуализация")
    void testCalculateTrip_personal() throws Exception {
        RegionDto regionDto = RegionDto.builder().id(moscow).name("MOSKVA").code("1").build();
        when(regionDataResolver.getRegion(any())).thenReturn(regionDto);
        var data = createTripDto(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())), Duration.ofHours(5),
                                 organization1.getId());
        
        var response = mockMvc.perform(post("/calculate/1a33601d-4db4-4720-8d09-95f015770fe0")
                                               .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))
                                               .content(objectMapper.writeValueAsString(data))
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
                              .andExpect(status().isOk()).andReturn();
        
        var actualList = objectMapper.readValue(response.getResponse().getContentAsString(),
                                                new TypeReference<List<CalculatedDto>>() {
                                                });
        
        assertThat(actualList).hasSize(TARIFF_PERSONAL_COUNT);
        for (var actual : actualList) {
            assertThat(actual.getTransportType()).isEqualTo(TransportTypeEnum.Constants.PERSONAL_STRING);
            assertThat(actual.getCost()).as("Check tariff PERSONAL cost")
                                        .isIn(0L, 400L, 800L, 1200L, 80000L, 240001L);
        }
    }
    
    @Test
    @DisplayName("Расчёт одного тарифа класса Такси")
    @WithMockUser(username = USER_ID, roles = "GUEST")
    @Disabled("Требуется актуализация")
    void testCalculateTrip_oneTransportKind() throws Exception {
        RegionDto regionDto = RegionDto.builder().id(moscow).name("MOSKVA").code("1").build();
        when(regionDataResolver.getRegion(any())).thenReturn(regionDto);
        var data = createTripDto(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())), Duration.ofHours(5),
                                 organization1.getId());
        
        var id = taxiTariffRepository.findAll().getFirst().getId();
        
        var response = mockMvc.perform(post("/calculate/7f18ce71-99a7-47b5-b285-335058c6715c/" + id)
                                               .content(objectMapper.writeValueAsString(data))
                                               .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
                              .andExpect(status().isOk()).andReturn();
        
        var actual = objectMapper.readValue(response.getResponse().getContentAsString(),
                                            new TypeReference<CalculatedDto>() {
                                            });
        
        assertThat(actual.getTransportType()).isEqualTo(TransportTypeEnum.Constants.TAXI_STRING);
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getCost()).as("Check tariff TAXI cost")
                                    .isIn(400L, 528L, 1814L);
    }
    
    @Test
    @DisplayName("Расчет одного тарифа класса Личного ТС")
    @WithMockUser(username = USER_ID, roles = "GUEST")
    @Disabled("Требуется актуализация")
    void testCalculateTrip_oneTariff_personal() throws Exception {
        var data = createTripDto(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())), Duration.ofHours(5),
                                 organization1.getId());
        
        var id = personalTariffRepository.findAll().getFirst().getId();
        
        var response = mockMvc.perform(post("/calculate/1a33601d-4db4-4720-8d09-95f015770fe0/" + id)
                                               .content(objectMapper.writeValueAsString(data))
                                               .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
                              .andExpect(status().isOk()).andReturn();
        
        var actual = objectMapper.readValue(response.getResponse().getContentAsString(),
                                            new TypeReference<CalculatedDto>() {
                                            });
        
        assertThat(actual.getTransportType()).isEqualTo(TransportTypeEnum.Constants.PERSONAL_STRING);
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getCost()).as("Check tariff PERSONAL cost")
                                    .isIn(0L, 400L);
    }
    
    @Test
    @DisplayName("Расчет одного тарифа класса Личного ТС. Несоответствие типа тарифа тарифу")
    @WithMockUser(username = USER_ID, roles = "GUEST")
    void testCalculateTrip_oneTariff_personal_wrongType() throws Exception {
        RegionDto regionDto = RegionDto.builder().id(moscow).name("MOSKVA").code("1").build();
        when(regionDataResolver.getRegion(any())).thenReturn(regionDto);
        var data = createTripDto(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())), Duration.ofHours(5),
                                 organization1.getId());
        
        var id = taxiTariffRepository.findAll().getFirst().getId();
        
        mockMvc.perform(post("/calculate/1a33601d-4db4-4720-8d09-95f015770fe0/" + id)
                                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))
                                .content(objectMapper.writeValueAsString(data))
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Расчет несуществующего тарифа класса")
    @WithMockUser(roles = "GUEST")
    void testCalculateTrip_oneUnknownTariff() throws Exception {
        var data = createTripDto(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())), Duration.ofHours(5),
                                 organization1.getId());
        
        var id = UUID.randomUUID();
        
        mockMvc.perform(post("/calculate/7f18ce71-99a7-47b5-b285-335058c6715d/" + id)
                                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))
                                .content(objectMapper.writeValueAsString(data))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
               .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Расчёт одного тарифа неизвестного класса")
    @WithMockUser(roles = "GUEST")
    void testCalculateTrip_oneTariff_unknown() throws Exception {
        var data = createTripDto(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())), Duration.ofHours(5));
        
        var id = personalTariffRepository.findAll().getFirst().getId();
        
        mockMvc.perform(post("/calculate/unknown/" + id)
                                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))
                                .content(objectMapper.writeValueAsString(data))
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Расчет для всех активных тарифов общественного транспорта - успех")
    @WithMockUser(username = USER_ID, roles = "GUEST")
    @Disabled("Требуется актуализация")
    void testCalculateTrip_allPublic_success() throws Exception {
        PublicTripDTO publicTripData = PublicTripDTO.builder().metroTicketsQuantity(2).tramTicketsQuantity(2)
                                                    .trolleybusTicketsQuantity(2).busTicketsQuantity(2).build();
        
        RegionDto regionDto = RegionDto.builder().id(samara).name("SAMARA").code("1").build();
        when(regionDataResolver.getRegion(any())).thenReturn(regionDto);
        var data = objectMapper.writeValueAsString(createDataWithPublic(
                LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                Duration.ofHours(5),
                publicTripData,
                organization1.getId()));
        
        var response = mockMvc.perform(post("/calculate/" + TransportTypeEnum.PUBLIC.getId())
                                               .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))
                                               .content(data).contentType(MediaType.APPLICATION_JSON)
                                               .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))).andExpect(status().isOk()).andReturn();
        
        var actualList = objectMapper.readValue(
                response.getResponse().getContentAsString(),
                new TypeReference<List<CalculatedDto>>() {
                }
                                               );
        
        assertThat(actualList).hasSize(1);
        CalculatedDto calculatedDto = actualList.getFirst();
        assertThat(calculatedDto.getTransportType()).isEqualTo(TransportTypeEnum.PUBLIC.getName());
        assertThat(calculatedDto.getCost()).isEqualTo(400);
    }
    
    @Test
    @DisplayName("Расчет для конкретного тарифа общественного транспорта - успех")
    @WithMockUser(username = USER_ID, roles = "GUEST")
    @Disabled("Требуется актуализация")
    void testCalculateTrip_concretePublicTariff_success() throws Exception {
        RegionDto regionDto = RegionDto.builder().id(moscow).name("MOSKVA").code("1").build();
        when(regionDataResolver.getRegion(any())).thenReturn(regionDto);
        List<? extends BaseTariff> publicTariff = publicTariffRepository.findAll();
        assertThat(publicTariff.size()).isSameAs(1);
        assertThat(publicTariff.getFirst()).isInstanceOf(PublicTariff.class);
        UUID id = publicTariff.getFirst().getId();
        
        PublicTripDTO publicTripData = PublicTripDTO.builder().metroTicketsQuantity(2).tramTicketsQuantity(2)
                                                    .trolleybusTicketsQuantity(2).busTicketsQuantity(2).build();
        
        var data = objectMapper.writeValueAsString(createDataWithPublic(
                LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                Duration.ofHours(5),
                publicTripData, organization1.getId()));
        
        var response = mockMvc.perform(post("/calculate/" + TransportTypeEnum.PUBLIC.getId() + "/" + id)
                                               .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))
                                               .content(data).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
        
        var calculatedDto = objectMapper.readValue(
                response.getResponse().getContentAsString(),
                new TypeReference<CalculatedDto>() {
                }
                                                  );
        
        assertThat(calculatedDto.getTransportType()).isEqualTo(TransportTypeEnum.PUBLIC.getName());
        assertThat(calculatedDto.getCost()).isEqualTo(400);
    }
    
    @Test
    @DisplayName("Расчёт для одного тарифа на общественном транспорте - ошибки валидации")
    @WithMockUser(roles = "GUEST")
    void testCalculateTrip_publicTariff_violations() throws Exception {
        PublicTripDTO publicTripData = PublicTripDTO.builder().metroTicketsQuantity(-2).tramTicketsQuantity(0)
                                                    .trolleybusTicketsQuantity(-10).busTicketsQuantity(10).build();
        
        var data = objectMapper.writeValueAsString(createDataWithPublic(
                LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                Duration.ofHours(5),
                publicTripData
                                                                       ));
        
        mockMvc.perform(post("/calculate/").header("Authorization", "Basic login")
                                           .content(objectMapper.writeValueAsString(data))
                                           .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))
                                           .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().is4xxClientError());
    }
    
    private TripDto createTripDto(LocalDateTime date, Duration duration) {
        return createTripDto(date, duration, null);
    }
    
    private TripDto createTripDto(LocalDateTime date, Duration duration, UUID organizationId) {
        var waypointDto = WaypointDTO.builder().city("moscow").latitude(1.1).longitude(2.2).build();
        return TripDto.builder()
                      .organizationId(organizationId)
                      .distance(100).time(duration).tripDate(date).startPoint(waypointDto).build();
    }
    
    private TripDto createDataWithPublic(
            LocalDateTime date, Duration duration,
            PublicTripDTO publicTripData
                                        ) {
        return createDataWithPublic(date, duration, publicTripData, null);
    }
    
    private TripDto createDataWithPublic(
            LocalDateTime date, Duration duration,
            PublicTripDTO publicTripData, UUID organizationId
                                        ) {
        var waypointDto = WaypointDTO.builder().city("moscow").latitude(1.1).longitude(2.2).build();
        return TripDto.builder()
                      .organizationId(organizationId)
                      .distance(100).time(duration).tripDate(date).startPoint(waypointDto)
                      .publicTripData(publicTripData).build();
    }
    
}