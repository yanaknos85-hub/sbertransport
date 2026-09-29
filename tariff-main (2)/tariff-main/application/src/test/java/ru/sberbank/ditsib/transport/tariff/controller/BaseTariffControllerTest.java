package ru.sberbank.ditsib.transport.tariff.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.dao.*;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Department;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.*;
import ru.sberbank.ditsib.transport.tariff.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.tariff.humanReadableId.dao.CompanySQRepositoryRequest;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка контроллера тарифов")
@ActiveProfiles({"test", "kafka"})
class BaseTariffControllerTest extends KafkaTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String USER_ID = "d1c2bb8d-a1b2-480e-8d96-36e9017227c7";
    
    @Autowired
    private EntityDTOMapper mapper;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private TariffService tariffService;
    
    @Autowired
    private TaxiTariffRepository taxiTariffRepository;
    
    @Autowired
    private PersonalTariffRepository personalTariffRepository;
    
    @Autowired
    private CarSharingTariffRepository carSharingTariffRepository;
    
    @Autowired
    private BicycleTariffRepository bicycleTariffRepository;
    
    @Autowired
    private ScooterTariffRepository scooterTariffRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private ContractRepository contractRepository;
    
    @Autowired
    CompanySQRepositoryRequest companySQRepositoryRequest;
    
    @Autowired
    private GeoZoneRepository geoZoneRepository;
    
    @MockitoBean
    RestTemplate restTemplate;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private ContractorRepository contractorRepository;
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    protected NewTaxiTariffDTO testNewTaxiTariff1;
    protected NewPersonalTariffDTO testNewPersonalTariff1;
    protected NewTaxiTariffDTO testNewWalkTariff;
    protected Organization organization1;
    protected Organization organization2;
    protected Contract contract;
    protected UUID regionId = UUID.randomUUID();
    protected Employee employee1;
    protected Department department1;
    
    @BeforeEach
    public void prepareData() {
        geoZoneRepository.save(new GeoZone(regionId, "region", 1 + "", null));
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(
                ResponseEntity.ok("Response"));
        organization1 = Instancio.of(Organization.class)
                .set(field(Organization::getDigitId), 1L)
                .create();
        organization2 = Instancio.of(Organization.class)
                .set(field(Organization::getDigitId), 2L)
                .create();
        organizationRepository.saveAndFlush(organization1);
        organizationRepository.saveAndFlush(organization2);
        
        department1 =
                Department.builder().departmentName("Департамент").id(UUID.randomUUID()).organizationId(organization1.getId()).build();
        departmentRepository.save(department1);
        
        employee1 =
                Employee.builder().id(UUID.randomUUID()).userId(UUID.fromString(USER_ID)).departmentId(department1.getId()).build();
        employeeRepository.saveAndFlush(employee1);
        
        Contractor contractor = new Contractor();
        contractor.setId(UUID.randomUUID());
        contractor.setName("name");
        contractor.setActive(true);
        contractor.setTin("1213123123");
        contractor.setMsrn("4343322323");
        contractor.setRegionIds(Set.of(regionId).stream().toList());
        contractor.setIntegrationType("EMAIL");
        contractor = contractorRepository.save(contractor);
        
        UUID userId = UUID.randomUUID();
        
        contract = Contract.builder()
                           .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                           .transportType(TransportTypeEnum.TAXI)
                           .regionIds(Set.of(regionId))
                           .contractorId(contractor.getId())
                           .contractNumber("Number")
                           .sum(1000L)
                           .startDate(LocalDate.now())
                           .endDate(LocalDate.now())
                           .creationTime(LocalDateTime.now())
                           .userId(userId)
                           .contractType(ContractType.TRANSITIONAL)
                           .restrictionType(RestrictionType.NONE)
                           .build();
        contract.getOrganizations().add(organization1);
        contract = contractRepository.saveAndFlush(contract);
        
        testNewTaxiTariff1 = NewTaxiTariffDTO.builder()
                                             .regionId(Collections.singleton(regionId))
                                             .rideCostPerKm(300)
                                             .organizationId(organization1.getId())
                                             .rideCostPerMin(5)
                                             .waitCostPerMin(5)
                                             .freeWaitingTime(1)
                                             .contractId(contract.getId())
                                             .coopTariffParams(
                                                     CoopTariffParamsDTO.builder()
                                                                        .minCancelTimeMin(45)
                                                                        .distanceDeviationKm(5d)
                                                                        .savingsDeviationPct(15d)
                                                                        .timeDeviationMin(5).build())
                                             .timedTariffParams(TimedTariffParamsDTO.builder()
                                                                                    .coefDayOff(1.5)
                                                                                    .coefWorkDayMorning(1.5)
                                                                                    .coefWorkDayNoon(1.)
                                                                                    .coefWorkDayEvening(1.5)
                                                                                    .coefWorkDayNight(1.)
                                                                                    .build())
                                             .suburbTariffParams(SuburbTariffParamsDTO.builder()
                                                                                      .costPerKmInterRegion(5000)
                                                                                      .costPerMinSuburb(2000)
                                                                                      .costPerMinInterRegion(7000)
                                                                                      .costPerMinInterRegion(1000)
                                                                                      .build())
                                             .coopTariffParams(
                                                     CoopTariffParamsDTO.builder()
                                                                        .minCancelTimeMin(45)
                                                                        .distanceDeviationKm(5d)
                                                                        .savingsDeviationPct(15d)
                                                                        .timeDeviationMin(5).build())
                                             .minRideDistanceCost(10)
                                             .minRideTimeCost(10)
                                             .transportType(TransportTypeEnum.TAXI)
                                             .taxiClass(TaxiClass.ECONOMY)
                                             .priceDetail("freeWaitingTime", 3600 * 1000)
                                             .priceDetail("minDistance", 3)
                                             .priceDetail("minTime", 30 * 60 * 1000)
                                             .priceDetail("minutePrice", 10)
                                             .priceDetail("submission", 11)
                                             .priceDetail("taxiClass", TaxiClass.ECONOMY).build();
        
        testNewPersonalTariff1 = NewPersonalTariffDTO.builder()
                                                     .regionId(Collections.singleton(regionId))
                                                     .organizationId(organization1.getId())
                                                     .rideCostPerKm(500)
                                                     .transportType(TransportTypeEnum.PERSONAL)
                                                     .seasonalCoefficient(.5)
                                                     .seasonStart(LocalDate.now())
                                                     .seasonEnd(LocalDate.now().plusMonths(1))
                                                     .build();
        
        
    }
    
    @AfterEach
    public void clean() {
        companySQRepositoryRequest.deleteAllInBatch();
        taxiTariffRepository.deleteAllInBatch();
        personalTariffRepository.deleteAllInBatch();
        carSharingTariffRepository.deleteAllInBatch();
        bicycleTariffRepository.deleteAllInBatch();
        scooterTariffRepository.deleteAllInBatch();
        List<Contract> contractList = contractRepository.findAll();
        contractList.forEach(e -> {
            e.getOrganizations().clear();
            contractRepository.saveAndFlush(e);
        });
        contractRepository.deleteAll();
        organizationRepository.deleteAllInBatch();
        employeeRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
    }
    
    @Test
    @DisplayName("Удаление")
    @WithMockUser(roles = "GUEST")
    void deleteItem() throws Exception {
        var id = taxiTariffRepository.saveAndFlush(TaxiTariff.builder()
                                                             .regionId(regionId)
                                                             .organization(organization1)
                                                             .transportType(TransportTypeEnum.TAXI)
                                                             .taxiClass(TaxiClass.ECONOMY)
                                                             .contract(contract)
                                                             .rideCostPerMin(10)
                                                             .rideCostPerKm(200)
                                                             .waitCostPerMin(10)
                                                             .build()).getId();
        
        mockMvc.perform(delete("/" + TransportTypeEnum.TAXI.getId() + "/" + id.toString()))
               .andExpect(status().isOk());
        
        assertThat(taxiTariffRepository.findAll().stream().filter(BaseTariff::isActive).collect(
                Collectors.toList())).size().isEqualTo(0);
    }
    
    @Test
    @DisplayName("Удаление несуществующего")
    @WithMockUser(roles = "GUEST")
    void delete_noExists() throws Exception {
        mockMvc.perform(
                       delete("/" + TransportTypeEnum.TAXI.getId() + "/" + UUID.randomUUID().toString()))
               .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Просмотр одного")
    @WithMockUser(roles = "GUEST")
    void getOne() throws Exception {
        var id = taxiTariffRepository.save(TaxiTariff.builder()
                                                     .regionId(regionId)
                                                     .organization(organization1)
                                                     .taxiClass(TaxiClass.ECONOMY)
                                                     .rideCostPerMin(10)
                                                     .rideCostPerKm(200)
                                                     .waitCostPerMin(10)
                                                     .contract(contract)
                                                     .workGroup("testWorkgroup")
                                                     .transportType(TransportTypeEnum.TAXI)
                                                     .rideCostPerKm(200).build()).getId();
        
        var response =
                mockMvc.perform(get("/" + TransportTypeEnum.TAXI.getId() + "/" + id.toString()))
                       .andExpect(status().isOk());
        
        var expected = taxiTariffRepository.findAll().getFirst();
        
        response.andExpect(jsonPath("$.id").value(expected.getId().toString()))
                .andExpect(jsonPath("$.regionId").value(expected.getRegionId().toString()))
                .andExpect(jsonPath("$.transportType").value(expected.getTransportType().toString()))
                .andExpect(jsonPath("$.workgroup").value(expected.getWorkGroup()))
                .andExpect(jsonPath("$.contractId").value(expected.getContract().getId().toString()))
                .andExpect(jsonPath("$.contractorId").value(expected.getContract().getContractorId().toString()));
    }
    
    @Test
    @DisplayName("Просмотр несуществующего")
    @WithMockUser(roles = "GUEST")
    void getOne_nonExists() throws Exception {
        mockMvc.perform(
                       get("/" + TransportTypeEnum.TAXI.getId() + "/" + UUID.randomUUID().toString()))
               .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Просмотр всех")
    @WithMockUser(roles = "GUEST")
    void getAll() throws Exception {
        for (var i = 0; i < 10; i++) {
            var newRegionId = UUID.randomUUID();
            geoZoneRepository.save(new GeoZone(newRegionId, "Region", i + "", null));
            TaxiTariff taxiTariff = mapper.newDtoToTaxiTariff(testNewTaxiTariff1.toBuilder()
                                                                                .regionId(Collections.singleton(newRegionId))
                                                                                .rideCostPerKm(200)
                                                                                .freeWaitingTime((3 + i) * 36)
                                                                                .rideCostPerMin(6 + i)
                                                                                .waitCostPerMin(10 * i)
                                                                                .minRideTimeCost(7 + i)
                                                                                .minRideDistanceCost(8 + i)
                                                                                .build());
            taxiTariff.setContract(contract);
            tariffService.save(taxiTariff);
        }
        
        var response = mockMvc.perform(get("/")
                                               .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))).andExpect(status().isOk());
        
        response.andExpect(jsonPath("$.length()").value(taxiTariffRepository.count()));
        var expectedList = taxiTariffRepository.findAll().stream().sorted(Comparator.comparing(TaxiTariff::getHumanReadableId)).toList();
        for (var i = 0; i < taxiTariffRepository.count(); i++) {
            var expected = expectedList.get(i);
            response.andExpect(jsonPath("$[" + i + "].id").value(expected.getId().toString()))
                    .andExpect(jsonPath("$[" + i + "].humanReadableId").value(expected.getHumanReadableId()))
                    .andExpect(jsonPath("$[" + i + "].regionId").value(expected.getRegionId().toString()))
                    .andExpect(jsonPath("$[" + i + "].organizationId")
                                       .value(expected.getOrganization().getId().toString()))
                    .andExpect(jsonPath("$[" + i + "].transportType")
                                       .value(expected.getTransportType().toString()))
                    .andExpect(jsonPath("$[" + i + "].serviceType")
                                       .value(expected.getServiceType().toString()))
                    .andExpect(jsonPath("$[" + i + "].contractId")
                                       .value(expected.getContract().getId().toString()));
        }
    }
    
    @Test
    @DisplayName("Просмотр всех по виду транспорта")
    @WithMockUser(roles = "GUEST")
    void getAll_ofTransportType() throws Exception {
        for (var i = 0; i < 10; i++) {
            var taxiTariff = mapper.newDtoToTaxiTariff(testNewTaxiTariff1.toBuilder()
                                                                         .regionId(Collections.singleton(regionId))
                                                                         .rideCostPerKm(200)
                                                                         .transportType(TransportTypeEnum.TAXI)
                                                                         .freeWaitingTime((3 + i) * 36)
                                                                         .rideCostPerMin(6 + i)
                                                                         .waitCostPerMin(10 * i)
                                                                         .minRideTimeCost(7 + i)
                                                                         .minRideDistanceCost(8 + i)
                                                                         .build());
            taxiTariffRepository.save(taxiTariff);
            
            personalTariffRepository.save(mapper.newDtoToPersonalTariff(
                    PersonalTariffDTO.builder()
                                     .organizationId(organization1.getId())
                                     .regionId(Collections.singleton(regionId))
                                     .transportType(TransportTypeEnum.PERSONAL)
                                     .rideCostPerKm(200 * i)
                                     .rideCostPerMin(100 * i)
                                     .seasonalCoefficient(0.1 * i + .001)
                                     .seasonStart(LocalDate.now().plusDays(i))
                                     .seasonEnd(LocalDate.now().plusDays(i).plusMonths(1))
                                     .transportType(TransportTypeEnum.PERSONAL).build()));
        }
        
        var response = mockMvc.perform(get("/" + TransportTypeEnum.PERSONAL.getId())
                                               .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
                              .andExpect(status().isOk());
        
        response.andExpect(jsonPath("$.length()").value(personalTariffRepository.count()));
        
        for (var i = 0; i < personalTariffRepository.count(); i++) {
            var expected =
                    personalTariffRepository.findAll().get(i);
            
            response.andExpect(jsonPath("$[" + i + "].id").value(expected.getId().toString()))
                    .andExpect(jsonPath("$[" + i + "].regionId").value(expected.getRegionId().toString()))
                    .andExpect(jsonPath("$[" + i + "].transportType")
                                       .value(expected.getTransportType().toString()));
        }
    }
    
    @Test
    @DisplayName("Поиск  тарифов")
    void search() throws Exception {
        int correctSize = 50;
        int pageSize = 50;
        for (var i = 0; i < correctSize; i++) {
            var newRegionId = UUID.randomUUID();
            geoZoneRepository.save(new GeoZone(newRegionId, "Region", i + "", null));
            TaxiTariff taxiTariff = mapper.newDtoToTaxiTariff(testNewTaxiTariff1.toBuilder()
                                                                                .regionId(Collections.singleton(newRegionId))
                                                                                .rideCostPerKm(200)
                                                                                .freeWaitingTime((3 + i) * 36)
                                                                                .rideCostPerMin(6 + i)
                                                                                .waitCostPerMin(10 * i)
                                                                                .minRideTimeCost(7 + i)
                                                                                .minRideDistanceCost(8 + i)
                                                                                .build());
            taxiTariff.setContract(contract);
            tariffService.save(taxiTariff);
        }
        assertEquals(50, taxiTariffRepository.count());
        List<TaxiTariff> all = taxiTariffRepository.findAll();
        for (int i = 0; i < 45; i++) {
            tariffService.delete(all.get(i));
        }
        var response = mockMvc.perform(post("/search?size=" + pageSize + "&sort=humanReadableId,desc")
                                               .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                                               .contentType(MediaType.APPLICATION_JSON))
                              .andExpect(status().isOk());
        
        
        response.andExpect(jsonPath("$.size").value(pageSize));
        all.sort(Comparator.comparing(BaseTariff::getHumanReadableId));
        Collections.reverse(all);
        for (var i = 0; i < pageSize; i++) {
            TaxiTariff expected = all.get(i);
            
            response.andExpect(jsonPath("$.content.[" + i + "].id").value(expected.getId().toString()))
                    .andExpect(jsonPath("$.content.[" + i + "].humanReadableId").value(expected.getHumanReadableId()))
                    .andExpect(jsonPath("$.content.[" + i + "].regionId").value(expected.getRegionId().toString()))
                    .andExpect(jsonPath("$.content.[" + i + "].organizationId")
                                       .value(expected.getOrganization().getId().toString()))
                    .andExpect(jsonPath("$.content.[" + i + "].transportType")
                                       .value(expected.getTransportType().toString()))
                    .andExpect(jsonPath("$.content.[" + i + "].serviceType")
                                       .value(expected.getServiceType().toString()))
                    .andExpect(jsonPath("$.content.[" + i + "].contractId")
                                       .value(expected.getContract().getId().toString()));
        }
    }
    
    @Test
    @DisplayName("Поиск  тарифов по параметрам")
    void searchWithParams() throws Exception {
        int correctSize = 30;
        UUID contractorId1 = UUID.randomUUID();
        UUID contractorId2 = UUID.randomUUID();
        for (var i = 0; i < correctSize; i++) {
            var newRegionId = UUID.randomUUID();
            geoZoneRepository.save(new GeoZone(newRegionId, "Region", i + "", null));
            TaxiTariff taxiTariff = mapper.newDtoToTaxiTariff(testNewTaxiTariff1.toBuilder()
                                                                                .regionId(Collections.singleton(newRegionId))
                                                                                .organizationId(organization1.getId())
                                                                                .rideCostPerKm(200)
                                                                                .freeWaitingTime((3 + i) * 36)
                                                                                .rideCostPerMin(6 + i)
                                                                                .waitCostPerMin(10 * i)
                                                                                .minRideTimeCost(7 + i)
                                                                                .minRideDistanceCost(8 + i)
                                                                                .build());
            taxiTariff.setContract(contract);
            tariffService.save(taxiTariff);
        }
        
        for (var i = 0; i < correctSize; i++) {
            var newRegionId = UUID.randomUUID();
            geoZoneRepository.save(new GeoZone(newRegionId, "Region", i + "", null));
            WalkTariff walkTariff = WalkTariff.builder()
                                              .regionId(newRegionId)
                                              .transportType(TransportTypeEnum.WALK)
                                              .organization(organization2)
                                              .build();
            tariffService.save(walkTariff);
        }
        
        for (var i = 0; i < correctSize; i++) {
            var newRegionId = UUID.randomUUID();
            geoZoneRepository.save(new GeoZone(newRegionId, "Region", i + "", null));
            TaxiTariff taxiTariff = mapper.newDtoToTaxiTariff(testNewTaxiTariff1.toBuilder()
                                                                                .regionId(Collections.singleton(newRegionId))
                                                                                .organizationId(organization2.getId())
                                                                                .rideCostPerKm(200)
                                                                                .freeWaitingTime((3 + i) * 36)
                                                                                .rideCostPerMin(6 + i)
                                                                                .waitCostPerMin(10 * i)
                                                                                .minRideTimeCost(7 + i)
                                                                                .minRideDistanceCost(8 + i)
                                                                                .build());
            taxiTariff.setContract(contract);
            tariffService.save(taxiTariff);
        }
        
        assertEquals(2 * correctSize, taxiTariffRepository.count());
        List<TaxiTariff> all = taxiTariffRepository.findAll();
        
        TariffSearchDTO searchDTO = TariffSearchDTO.builder()
                                                   .contractId(contract.getId())
                                                   .organizationId(organization1.getId())
                                                   .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                                                   .humanReadableId("11")
                                                   .build();
        
        var response =
                mockMvc.perform(post("/search?size=" + 100 + "&sort=humanReadableId")
                                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                                        .content(objectMapper.writeValueAsString(searchDTO))
                                        .contentType(MediaType.APPLICATION_JSON))
                       .andExpect(status().isOk());
        
        
        response.andExpect(jsonPath("$.size").value(100));
        
        response.andExpect(jsonPath("$.content.[0].organizationId")
                                   .value(organization1.getId().toString()))
                .andExpect(jsonPath("$.content.[0].transportType")
                                   .value(TransportTypeEnum.TAXI.toString()))
                .andExpect(jsonPath("$.content.[0].serviceType")
                                   .value(TransportServiceType.EMPLOYEE_TRANSPORTATION.toString()))
                .andExpect(jsonPath("$.content.[0].contractId")
                                   .value(contract.getId().toString()))
                .andExpect(jsonPath("$.content.[0].active")
                                   .value(true));
    }
    
}