package ru.sberbank.ditsib.transport.tariff.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.tariff.messaging.TariffMessage;
import ru.sber.transport.tariff.messaging.TaxiTariffMessage;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.tariff.database.dao.*;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Department;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.*;
import ru.sberbank.ditsib.transport.tariff.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.tariff.exceptions.TariffAlreadyExistsException;

import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SuppressWarnings({ "OptionalGetWithoutIsPresent", "SpringJavaInjectionPointsAutowiringInspection" })
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка контроллера тарифов такси")
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles({"test", "kafka"})
class TaxiTariffControllerTest extends KafkaTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private EntityDTOMapper mapper;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private TaxiTariffRepository taxiTariffRepository;
    
    @Autowired
    ContractRepository contractRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private GeoZoneRepository geoZoneRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @MockitoBean
    private RestTemplate restTemplate;
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    protected NewTaxiTariffDTO newTaxiTariff1;
    protected Organization organization1;
    protected Organization organization2;
    protected String region = "Region";
    protected Contract contract1;
    protected UUID regionId2 = UUID.randomUUID();
    
    private static final String USER_ID = "d1c2bb8d-a1b2-480e-8d96-36e9017227c7";
    
    private static final UUID contractorTariffUUID=UUID.randomUUID();
    protected Employee employee1;
    protected Department department1;
    
    @BeforeEach
    public void prepareData() {
        organization1 = Instancio.of(Organization.class)
                .set(field(Organization::getName), "Организация")
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
        
        Calendar now = Calendar.getInstance();
        LocalDateTime plannedDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        int plannedYear = plannedDate.getYear();
        
        UUID contractorId = UUID.randomUUID();
        UUID regionId = UUID.randomUUID();
        
        geoZoneRepository.save(new GeoZone(regionId, region, 1 + "", null));
        geoZoneRepository.save(new GeoZone(regionId2, region, 2 + "", null));
        
        contract1 = new Contract();
        contract1.setUserId(UUID.randomUUID());
        contract1.setContractorId(contractorId);
        contract1.setContractNumber("Contract number");
        contract1.setOrganizations(Collections.singleton(organization1));
        contract1.setRegionIds(Set.of(regionId));
        contract1.setTransportType(TransportTypeEnum.TAXI);
        contract1.setServiceType(TransportServiceType.EMPLOYEE_TRANSPORTATION);
        contract1.setContractNumber("Contract");
        contract1.setSum(1000L);
        contract1.setStartDate(LocalDate.of(plannedYear - 1, 1, 1));
        contract1.setEndDate(LocalDate.of(plannedYear + 1, 1, 1));
        contract1.setCreationTime(LocalDateTime.now());
        contract1.setContractType(ContractType.INCOME);
        contract1.setRestrictionType(RestrictionType.NONE);
        contract1 = contractRepository.saveAndFlush(contract1);
        
        newTaxiTariff1 = NewTaxiTariffDTO.builder()
                                         .contractorTariffId(" "+contractorTariffUUID+" ")
                                         .regionId(Collections.singleton(regionId))
                                         .organizationId(organization1.getId())
                                         .transportType(TransportTypeEnum.TAXI)
                                         .contractId(contract1.getId())
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
                                                 CoopTariffParamsDTO.builder()
                                                                    .minCancelTimeMin(45)
                                                                    .distanceDeviationKm(5d)
                                                                    .savingsDeviationPct(15d)
                                                                    .timeDeviationMin(5).build())
                                         .suburbTariffParams(SuburbTariffParamsDTO.builder()
                                                                                  .costPerMinInterRegion(100)
                                                                                  .costPerKmInterRegion(200)
                                                                                  .costPerMinSuburb(400)
                                                                                  .costPerKmSuburb(500)
                                                                                  .suburbServiceCostPerKm(600)
                                                                                  .suburbServiceCostPerMin(700)
                                                                                  .build())
                                         .contractorDeviationParams(
                                                 ContractorDeviationsTariffDTO.builder()
                                                                              .maxDiffComputedCostPercent(1)
                                                                              .maxDiffComputedWaitingPercent(2)
                                                                              .maxDiffComputedDistancePercent(3)
                                                                              .maxDiffContractorCostPercent(4)
                                                                              .maxDiffFactDistancePercent(5)
                                                                              .build())
                                         .timedTariffParams(TimedTariffParamsDTO.builder()
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
        when(restTemplate.postForEntity(any(), any(),any())).thenReturn(null);
    }
    
    
    @Test
    @DisplayName("Добавление")
    @WithMockUser(roles = "GUEST", value = USER_ID)
    @Disabled("Требуется актуализация")
    void add() throws Exception {
        var newItem = newTaxiTariff1;
        
        var request = objectMapper.writeValueAsString(newItem);
        
        var response = mockMvc.perform(
                                      post("/taxi").contentType(MediaType.APPLICATION_JSON).content(request))
                              .andExpect(status().isOk())
                              .andExpect(jsonPath("$.humanReadableId").isNotEmpty());
        
        assertThat(taxiTariffRepository.count()).isEqualTo(1);
        
        TaxiTariffDTO
                actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                TaxiTariffDTO.class);
        
        var expected = taxiTariffRepository.findAll().getFirst();
        
        assertNotNull(expected.getId());
        assertEquals(expected.getId(), actual.getId());
        assertNotNull((actual.getContractorTariffId()));
        assertEquals(contractorTariffUUID.toString(), actual.getContractorTariffId());
        assertEquals(expected.getId(), UUID.fromString(actual.getTariffId()));
        assertNotNull(expected.getHumanReadableId());
        assertEquals(expected.getHumanReadableId(), actual.getHumanReadableId());
        assertNotNull(expected.getRegionId());
        assertTrue(actual.getRegionId().contains(expected.getRegionId()));
        assertNotNull(expected.getOrganization().getId());
        assertEquals(expected.getOrganization().getId(), actual.getOrganizationId());
        assertNotNull(expected.getContract());
        assertEquals(expected.getContract().getId(), actual.getContractId());
        assertEquals(TransportTypeEnum.TAXI, expected.getTransportType());
        assertEquals(expected.getTransportType(), actual.getTransportType());
        assertNotNull(expected.getTaxiClass());
        assertEquals(expected.getTaxiClass(), actual.getTaxiClass());
        assertEquals(expected.getRideCostPerKm(), actual.getRideCostPerKm());
        assertEquals(expected.getDistanceIncluded(), actual.getDistanceIncluded());
        assertEquals(expected.getMinRideDistanceCost(), actual.getMinRideDistanceCost());
        assertEquals(expected.getRideCostPerMin(), actual.getRideCostPerMin());
        assertEquals(expected.getTimeIncluded(), actual.getTimeIncluded());
        assertEquals(expected.getMinRideTimeCost(), actual.getMinRideTimeCost());
        assertEquals(expected.getWaitCostPerMin(), actual.getWaitCostPerMin());
        assertEquals(expected.getWaitCostPerMinIntermediate(), actual.getWaitCostPerMinIntermediate());
        assertEquals(expected.getFreeWaitingTime(), actual.getFreeWaitingTime());
        assertNotNull(expected.getCoopTariffParams());
        assertEquals(expected.getCoopTariffParams().getMinCancelTimeMin(),
                     actual.getCoopTariffParams().getMinCancelTimeMin());
        assertEquals(expected.getCoopTariffParams().getSavingsDeviationPct(),
                     actual.getCoopTariffParams().getSavingsDeviationPct());
        assertEquals(expected.getCoopTariffParams().getDistanceDeviationKm(),
                     actual.getCoopTariffParams().getDistanceDeviationKm());
        assertEquals(expected.getCoopTariffParams().getTimeDeviationMin(),
                     actual.getCoopTariffParams().getTimeDeviationMin());
        
        assertNotNull(expected.getSuburbTariffParams());
        assertEquals(expected.getSuburbTariffParams().getCostPerKmInterRegion(),
                     actual.getSuburbTariffParams().getCostPerKmInterRegion());
        assertEquals(expected.getSuburbTariffParams().getCostPerKmSuburb(),
                     actual.getSuburbTariffParams().getCostPerKmSuburb());
        assertEquals(expected.getSuburbTariffParams().getCostPerMinInterRegion(),
                     actual.getSuburbTariffParams().getCostPerMinInterRegion());
        assertEquals(expected.getSuburbTariffParams().getCostPerMinSuburb(),
                     actual.getSuburbTariffParams().getCostPerMinSuburb());
        assertEquals(expected.getSuburbTariffParams().getSuburbServiceCostPerKm(),
                     actual.getSuburbTariffParams().getSuburbServiceCostPerKm());
        assertEquals(expected.getSuburbTariffParams().getSuburbServiceCostPerMin(),
                     actual.getSuburbTariffParams().getSuburbServiceCostPerMin());
        
        assertNotNull(expected.getTimedTariffParams());
        assertEquals(expected.getTimedTariffParams().getCoefWorkDayMorning(),
                     actual.getTimedTariffParams().getCoefWorkDayMorning());
        assertEquals(expected.getTimedTariffParams().getCoefWorkDayNoon(),
                     actual.getTimedTariffParams().getCoefWorkDayNoon());
        assertEquals(expected.getTimedTariffParams().getCoefWorkDayEvening(),
                     actual.getTimedTariffParams().getCoefWorkDayEvening());
        assertEquals(expected.getTimedTariffParams().getCoefWorkDayNight(),
                     actual.getTimedTariffParams().getCoefWorkDayNight());
        assertEquals(expected.getTimedTariffParams().getCoefDayOff(),
                     actual.getTimedTariffParams().getCoefDayOff());
        
        assertEquals(expected.getCoefBicycle(), actual.getCoefBicycle());
        assertEquals(expected.getCoefChildSeat(), actual.getCoefChildSeat());
        assertEquals(expected.getCoefOrg(), actual.getCoefOrg());
        assertEquals(expected.getCoefPetTransport(), actual.getCoefPetTransport());
        assertEquals(expected.getCoefTraffic(), actual.getCoefTraffic());
        assertEquals(expected.getCoefChildSeat(), actual.getCoefChildSeat());
        
        assertEquals(expected.getContractorDeviationParams().getMaxDiffComputedCostPercent(),
                     actual.getContractorDeviationParams().getMaxDiffComputedCostPercent());
        assertEquals(expected.getContractorDeviationParams().getMaxDiffComputedDistancePercent(),
                     actual.getContractorDeviationParams().getMaxDiffComputedDistancePercent());
        assertEquals(expected.getContractorDeviationParams().getMaxDiffContractorCostPercent(),
                     actual.getContractorDeviationParams().getMaxDiffContractorCostPercent());
        assertEquals(expected.getContractorDeviationParams().getMaxDiffComputedWaitingPercent(),
                     actual.getContractorDeviationParams().getMaxDiffComputedWaitingPercent());
        assertEquals(expected.getContractorDeviationParams().getMaxDiffFactDistancePercent(),
                     actual.getContractorDeviationParams().getMaxDiffFactDistancePercent());
        
        var message = consumeMessage("service.tariff", TariffMessage.class);
        BaseTariff foundTariff = taxiTariffRepository.findById(actual.getId()).get();
        assertEquals(foundTariff.getHumanReadableId(), actual.getHumanReadableId());
        assertEquals(message.humanReadableId(), actual.getHumanReadableId());
        assertTrue(message.humanReadableId().contains("TF-"));
        
        var taxiTariffMessage = consumeMessage("service.tariff", TaxiTariffMessage.class);
        assertNotNull(taxiTariffMessage.contractorId());
        assertEquals(WorkGroup.builder().organizationName(organization1.getName())
                              .regionName(region)
                              .contractNumber(contract1.getContractNumber())
                              .build().getFullName(),
                     taxiTariffMessage.workGroup());
        
        
    }
    
    
    @Test
    @DisplayName("Изменение")
    @WithMockUser(roles = "GUEST")
    @Disabled("Требуется актуализация")
    void edit() throws Exception {
        BaseTariff saved = taxiTariffRepository.save(mapper.newDtoToTaxiTariff(newTaxiTariff1));
        
        var expected = newTaxiTariff1.toBuilder()
                                     .contractorTariffId("12345").rideCostPerMin(15).coopTariffParams(
                        CoopTariffParamsDTO.builder().minCancelTimeMin(6).build())
                                     .build();
        
        var request = objectMapper.writeValueAsString(expected);
        
        mockMvc.perform(
                       put("/taxi/" + saved.getId()).contentType(MediaType.APPLICATION_JSON).content(request))
               .andExpect(status().isOk());
        
        TaxiTariff actual = taxiTariffRepository.findById(saved.getId()).get();
        
        assertEquals(expected.getRideCostPerMin(), actual.getRideCostPerMin());
        assertEquals(expected.getCoopTariffParams().getMinCancelTimeMin(),
                     actual.getCoopTariffParams().getMinCancelTimeMin());
        assertNotNull(actual.getTransportType());
        assertEquals(saved.getHumanReadableId(), actual.getHumanReadableId());
        
        var message = consumeMessage("service.tariff", TariffMessage.class);
        assertEquals(saved.getHumanReadableId(), message.humanReadableId());
        assertTrue(message.humanReadableId().contains("TF-"));
        
        var taxiMessage = consumeMessage("service.tariff.taxi", TaxiTariffMessage.class);
        assertNotNull((taxiMessage.integrationType()));
        assertNotNull((taxiMessage.contractorTariffId()));
        assertEquals(expected.getContractorTariffId(), taxiMessage.contractorTariffId());
    }
    
    @Test
    @DisplayName("Получение")
    @WithMockUser(roles = "GUEST")
    void getTaxi() throws Exception {
        
        var expected = newTaxiTariff1;
        TaxiTariff saved = taxiTariffRepository.save(mapper.newDtoToTaxiTariff(newTaxiTariff1));
        var request = objectMapper.writeValueAsString(expected);
        
        var response = mockMvc.perform(
                                      get("/taxi/" + saved.getId()).contentType(MediaType.APPLICATION_JSON).content(request))
                              .andExpect(status().isOk());
        
        TaxiTariffDTO
                actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                TaxiTariffDTO.class);
        
        assertEquals(saved.getId(), actual.getId());
        assertEquals(saved.getId(), UUID.fromString(actual.getTariffId()));
        assertEquals(expected.getOrganizationId(), actual.getOrganizationId());
        assertEquals(expected.getRideCostPerKm(), actual.getRideCostPerKm());
        assertEquals(expected.getRideCostPerMin(), actual.getRideCostPerMin());
        assertEquals(expected.getWaitCostPerMin(), actual.getWaitCostPerMin());
        assertEquals(expected.getFreeWaitingTime(), actual.getFreeWaitingTime());
        assertEquals(expected.getTransportType(), actual.getTransportType());
        assertEquals(expected.getTaxiClass(), actual.getTaxiClass());
        assertEquals(expected.getTaxiClass(), actual.getTaxiClass());
        assertEquals(expected.getMinRideDistanceCost(), actual.getMinRideDistanceCost());
        assertEquals(expected.getDistanceIncluded(), actual.getDistanceIncluded());
        assertEquals(expected.getWaitCostPerMinIntermediate(), actual.getWaitCostPerMinIntermediate());
        assertEquals(expected.getTimeIncluded(), actual.getTimeIncluded());
        assertEquals(expected.getCoopTariffParams().getMinCancelTimeMin(),
                     actual.getCoopTariffParams().getMinCancelTimeMin());
        assertEquals(expected.getCoopTariffParams().getSavingsDeviationPct(),
                     actual.getCoopTariffParams().getSavingsDeviationPct());
        assertEquals(expected.getCoopTariffParams().getDistanceDeviationKm(),
                     actual.getCoopTariffParams().getDistanceDeviationKm());
        assertEquals(expected.getCoopTariffParams().getTimeDeviationMin(),
                     actual.getCoopTariffParams().getTimeDeviationMin());
    }
    
    @Test
    @DisplayName("Удаление")
    @WithMockUser(roles = "GUEST")
    void deleteItem() throws Exception {
        var id = taxiTariffRepository.saveAndFlush(mapper.newDtoToTaxiTariff(newTaxiTariff1)).getId();
        
        mockMvc.perform(delete("/" + TransportTypeEnum.TAXI.getName().toLowerCase(Locale.ROOT) + "/" + id.toString()))
               .andExpect(status().isOk());
        
        assertThat(taxiTariffRepository.findAll().stream().filter(BaseTariff::isActive).collect(
                Collectors.toList())).size().isEqualTo(0);
    }
    
    @Test
    @DisplayName("Удаление несуществующего")
    @WithMockUser(roles = "GUEST")
    void delete_noExists() throws Exception {
        mockMvc.perform(
                       delete("/" + TransportTypeEnum.TAXI.getName().toLowerCase(Locale.ROOT) + "/" + UUID.randomUUID().toString()))
               .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Просмотр всех")
    @WithMockUser(roles = "GUEST")
    void getAll() throws Exception {
        for (var i = 0; i < 10; i++) {
            taxiTariffRepository.save(mapper.newDtoToTaxiTariff(newTaxiTariff1.toBuilder()
                                                                              .regionId(Collections.singleton(UUID.randomUUID()))
                                                                              .rideCostPerKm(200)
                                                                              .freeWaitingTime((3 + i) * 36)
                                                                              .rideCostPerMin(6 + i)
                                                                              .build()));
        }
        
        var response =
                mockMvc.perform(get("/" + TransportTypeEnum.TAXI.getName().toLowerCase(Locale.ROOT))).andExpect(status().isOk());
        
        response.andExpect(jsonPath("$.length()").value(taxiTariffRepository.count()));
        
        for (var i = 0; i < taxiTariffRepository.count(); i++) {
            var expected = (BaseTariff) taxiTariffRepository.findAll().get(i);
            
            response.andExpect(jsonPath("$[" + i + "].id").value(expected.getId().toString()))
                    .andExpect(jsonPath("$[" + i + "].regionId").value(expected.getRegionId().toString()))
                    .andExpect(jsonPath("$[" + i + "].transportType")
                                       .value(expected.getTransportType().toString()));
        }
    }
    
    @Test
    @DisplayName("Добавление неуникального тарифа")
    @WithMockUser(roles = "GUEST")
    @Disabled("Требуется актуализация")
    void addnonUnique() throws Exception {
        taxiTariffRepository.saveAndFlush(mapper.newDtoToTaxiTariff(newTaxiTariff1));
        
        var request = objectMapper.writeValueAsString(newTaxiTariff1);
        
        var exception = mockMvc.perform(
                                       post("/taxi").contentType(MediaType.APPLICATION_JSON).content(request))
                               .andExpect(status().isConflict())
                               .andReturn().getResolvedException();
        //При попытке сохранить тариф с неуникальной комбинацией организации, региона, контрагента и класса такси
        // получаем исключение
        assertInstanceOf(TariffAlreadyExistsException.class, exception);
        
        NewTaxiTariffDTO unique = newTaxiTariff1.toBuilder().regionId(Collections.singleton(regionId2)).build();
        
        //после изменения региона исключение пропадает
        mockMvc.perform(
                       post("/taxi").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(unique)))
               .andExpect(status().isOk())
               .andReturn();
    }
    
}
