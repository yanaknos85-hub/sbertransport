package ru.sberbank.ditsib.transport.tariff.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.dao.*;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.ContractDTO;
import ru.sberbank.ditsib.transport.tariff.dto.ContractForKafkaMessage;
import ru.sberbank.ditsib.transport.tariff.dto.ContractSearchDTO;
import ru.sberbank.ditsib.transport.tariff.dto.GetContractDTO;
import ru.sberbank.ditsib.transport.tariff.dto.VatType;
import ru.sberbank.ditsib.transport.tariff.exceptions.PaymentOrganizationException;
import ru.sberbank.ditsib.transport.tariff.exceptions.VatValueException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@Transactional
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка договоров контрагентов")
@ActiveProfiles({"test", "kafka"})
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class ContractControllerTest extends KafkaTest {
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GeoZoneRepository geoZoneRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    protected Organization organization1;
    protected Organization organization2;

    public static final String USER1_ID = "f10bcc5b-51db-4e1c-a747-2a229604f974";

    @BeforeEach
    public void init() {
        organization1 = Instancio.of(Organization.class)
                .set(field(Organization::getDigitId), 1L)
                .create();
        organization2 = Instancio.of(Organization.class)
                .set(field(Organization::getDigitId), 2L)
                .create();
        organizationRepository.saveAndFlush(organization1);
        organizationRepository.saveAndFlush(organization2);

        employeeRepository.deleteAll();
    }

    @Test
    @DisplayName("CRUD договоров контрагентов")
    @WithMockUser(username = USER1_ID, roles = "GUEST")
    void test_CRUD() throws Exception {
        var now = Calendar.getInstance();
        var plannedDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        int plannedYear = plannedDate.getYear();

        var regionId = UUID.randomUUID();

        var contractor = new Contractor();
        contractor.setId(UUID.randomUUID());
        contractor.setIntegrationType(TaxiExternalIntegrationType.JSON_API_1_0.name());
        contractor.setRegionIds(new ArrayList<>(List.of(regionId)));

        contractorRepository.save(contractor);

        var user = UUID.randomUUID().toString();

        employeeRepository.saveAndFlush(Employee.builder()
                .id(UUID.fromString(user))
                .userId(UUID.fromString(user))
                .firstName("Иван")
                .lastName("Иванов")
                .build());

        var contractList1 = contractRepository.findAll();
        assertEquals(0, contractList1.size());

        var contractDTO = new ContractDTO();
        contractDTO.setContractorId(contractor.getId());
        contractDTO.setOrganizationIds(Collections.singletonList(organization1.getId()));
        contractDTO.setRegion(regionId.toString());
        contractDTO.setRegionIds(new HashSet<>(Set.of(regionId)));
        contractDTO.setTransportType(TransportTypeEnum.TAXI);
        contractDTO.setSum(1000L);
        contractDTO.setStartDate(LocalDate.of(plannedYear - 1, 1, 1));
        contractDTO.setEndDate(LocalDate.of(plannedYear + 1, 1, 1));
        contractDTO.setContractNumber("123456");
        contractDTO.setIncludeVat(true);
        contractDTO.setVatValue(VatType.BASE_22.getPercentValue());
        contractDTO.setUvhd("12312");
        contractDTO.setPaymentOrganizationId(organization1.getId());
        String contractStr = objectMapper.writeValueAsString(contractDTO);

        var geoZone = new GeoZone();
        geoZone.setId(contractDTO.getRegionIds().iterator().next());
        geoZone.setName("Москва");

        geoZoneRepository.save(geoZone);

        var result =
                mockMvc.perform(post("/contracts")
                                .with(jwt().jwt(builder -> builder.jti(user).claim("roles", "ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(contractStr))
                        .andExpect(status().isOk());

        String contentAsString = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        GetContractDTO getContractDTO = objectMapper.readValue(contentAsString, GetContractDTO.class);

        assertEquals(1000, getContractDTO.getSum());
        assertIterableEquals(contractDTO.getRegionIds(), getContractDTO.getRegionIds());
        assertEquals(geoZone.getName(), getContractDTO.getRegion());

        var message = consumeMessage("service.contract", ContractForKafkaMessage.class);
        assertEquals(message.contractorId(), getContractDTO.getContractorId());
        assertEquals(message.organizations().size(), getContractDTO.getOrganizationIds().size());

        var contractList2 = contractRepository.findAll();
        assertEquals(1, contractList2.size());

        var contractDTO2 = new ContractDTO();
        contractDTO2.setContractorId(contractor.getId());
        contractDTO2.setOrganizationIds(Collections.singletonList(organization1.getId()));
        contractDTO2.setRegion("MOSKVA");
        contractDTO2.setRegionIds(new HashSet<>(Set.of(regionId)));
        contractDTO2.setTransportType(TransportTypeEnum.TAXI);
        contractDTO2.setSum(2000L);
        contractDTO2.setStartDate(LocalDate.of(plannedYear - 1, 1, 1));
        contractDTO2.setEndDate(LocalDate.of(plannedYear + 1, 1, 1));
        contractDTO2.setContractNumber("654321");
        contractDTO2.setIncludeVat(true);
        contractDTO2.setVatValue(VatType.BASE_22.getPercentValue());
        contractDTO2.setUvhd("43564");
        contractDTO2.setResponsibleEmployeeId(UUID.fromString(user));
        contractDTO2.setPaymentOrganizationId(organization2.getId());
        String contractStr2 = objectMapper.writeValueAsString(contractDTO2);

        var url = "/contracts/" + getContractDTO.getId();

        //perform edit
        mockMvc.perform(put(url).with(jwt().jwt(builder -> builder.jti(user).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(contractStr2))
                .andExpect(status().isOk());

        //perform get
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value("2000"))
                .andExpect(jsonPath("$.regionIds.length()").value(1))
                .andExpect(jsonPath("$.regionIds[0]").value(contractDTO2.getRegionIds().iterator().next().toString()))
                .andExpect(jsonPath("$.id").value(getContractDTO.getId().toString()))
                .andExpect(jsonPath("$.startDate").value(getContractDTO.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(getContractDTO.getEndDate().toString()));

        //perform delete
        mockMvc.perform(delete(url)
                        .with(jwt().jwt(builder -> builder.jti(user).claim("roles", "ROLE_USER"))))
                .andExpect(status().isOk());

        var contractList3 = contractRepository.findAll();
        assertEquals(1, contractList3.size());
    }

    @Test
    @DisplayName("CRUD договоров контрагентов")
    @WithMockUser(username = USER1_ID, roles = "GUEST")
    void throwExceptionPaymentOrganizationPostAndPutMethodTest() throws Exception {
        var now = Calendar.getInstance();
        var plannedDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        var plannedYear = plannedDate.getYear();

        var regionId = UUID.randomUUID();

        var contractor = new Contractor();
        contractor.setId(UUID.randomUUID());
        contractor.setIntegrationType(TaxiExternalIntegrationType.JSON_API_1_0.name());
        contractor.setRegionIds(new ArrayList<>(List.of(regionId)));

        contractorRepository.save(contractor);

        var user = UUID.randomUUID().toString();

        employeeRepository.saveAndFlush(Employee.builder()
                .id(UUID.fromString(user))
                .userId(UUID.fromString(user))
                .firstName("Иван")
                .lastName("Иванов")
                .build());

        var contractList1 = contractRepository.findAll();
        assertEquals(0, contractList1.size());

        var contractDTO = createAndFillDataNewContract(contractor, regionId, plannedYear);
        contractDTO.setPaymentOrganizationId(null);
        var contractStr = objectMapper.writeValueAsString(contractDTO);

        var geoZone = new GeoZone();
        geoZone.setId(contractDTO.getRegionIds().iterator().next());
        geoZone.setName("Москва");

        geoZoneRepository.save(geoZone);

        mockMvc.perform(post("/contracts")
                        .with(jwt().jwt(builder -> builder.jti(user).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(contractStr))
                .andExpect(status().isConflict())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(PaymentOrganizationException.class)
                        .hasMessageContaining("Для договора с типом Такси необходимо указать организацию, ответственную за оплату"));

        contractDTO.setPaymentOrganizationId(organization1.getId());
        contractStr = objectMapper.writeValueAsString(contractDTO);
        var resultAddContract =
                mockMvc.perform(post("/contracts")
                                .with(jwt().jwt(builder -> builder.jti(user).claim("roles", "ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(contractStr))
                        .andExpect(status().isOk());

        var contentAsString = resultAddContract.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        var getContractDTO = objectMapper.readValue(contentAsString, GetContractDTO.class);

        contractDTO.setPaymentOrganizationId(null);
        contractStr = objectMapper.writeValueAsString(contractDTO);
        mockMvc.perform(put("/contracts/" + getContractDTO.getId())
                        .with(jwt().jwt(builder -> builder.jti(user).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(contractStr))
                .andExpect(status().isConflict())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(PaymentOrganizationException.class)
                        .hasMessageContaining("Для договора с типом Такси необходимо указать организацию, ответственную за оплату"));

    }

    @Test
    @DisplayName("CRUD договоров контрагентов")
    @WithMockUser(username = USER1_ID, roles = "GUEST")
    void noThrowExceptionPaymentOrganizationPostAndPutMethodIncomeOutcomeRegistryTypeTest() throws Exception {
        var now = Calendar.getInstance();
        var plannedDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        var plannedYear = plannedDate.getYear();

        var regionId = UUID.randomUUID();

        var contractor = new Contractor();
        contractor.setId(UUID.randomUUID());
        contractor.setIntegrationType(TaxiExternalIntegrationType.JSON_API_1_0.name());
        contractor.setRegionIds(new ArrayList<>(List.of(regionId)));

        contractorRepository.save(contractor);

        var user = UUID.randomUUID().toString();

        employeeRepository.saveAndFlush(Employee.builder()
                .id(UUID.fromString(user))
                .userId(UUID.fromString(user))
                .firstName("Иван")
                .lastName("Иванов")
                .build());

        var contractList1 = contractRepository.findAll();
        assertEquals(0, contractList1.size());

        var contractDTO = createAndFillDataNewContract(contractor, regionId, plannedYear);
        contractDTO.setPaymentOrganizationId(null);
        contractDTO.setContractorId(null);
        var contractStr = objectMapper.writeValueAsString(contractDTO);

        var geoZone = new GeoZone();
        geoZone.setId(contractDTO.getRegionIds().iterator().next());
        geoZone.setName("Москва");

        geoZoneRepository.save(geoZone);

        var resultAddContract =
                mockMvc.perform(post("/contracts")
                                .with(jwt().jwt(builder -> builder.jti(user).claim("roles", "ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(contractStr))
                        .andExpect(status().isOk());

        var contentAsString = resultAddContract.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        var getContractDTO = objectMapper.readValue(contentAsString, GetContractDTO.class);

        var url = "/contracts/" + getContractDTO.getId();

        contractDTO.setPaymentOrganizationId(null);
        contractStr = objectMapper.writeValueAsString(contractDTO);
        mockMvc.perform(put(url)
                        .with(jwt().jwt(builder -> builder.jti(user).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(contractStr))
                        .andExpect(status().isOk());

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value("1000"))
                .andExpect(jsonPath("$.regionIds.length()").value(1))
                .andExpect(jsonPath("$.regionIds[0]").value(contractDTO.getRegionIds().iterator().next().toString()))
                .andExpect(jsonPath("$.id").value(getContractDTO.getId().toString()))
                .andExpect(jsonPath("$.startDate").value(getContractDTO.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(getContractDTO.getEndDate().toString()));
    }

    @Test
    @DisplayName("CRUD договоров контрагентов")
    @WithMockUser(username = USER1_ID, roles = "GUEST")
    void throwExceptionVatValuePostAndPutMethodTest() throws Exception {
        var now = Calendar.getInstance();
        var plannedDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        var plannedYear = plannedDate.getYear();

        var regionId = UUID.randomUUID();

        var contractor = new Contractor();
        contractor.setId(UUID.randomUUID());
        contractor.setIntegrationType(TaxiExternalIntegrationType.JSON_API_1_0.name());
        contractor.setRegionIds(new ArrayList<>(List.of(regionId)));

        contractorRepository.save(contractor);

        var user = UUID.randomUUID().toString();

        employeeRepository.saveAndFlush(Employee.builder()
                .id(UUID.fromString(user))
                .userId(UUID.fromString(user))
                .firstName("Иван")
                .lastName("Иванов")
                .build());

        var contractList1 = contractRepository.findAll();
        assertEquals(0, contractList1.size());

        var contractDTO = createAndFillDataNewContract(contractor, regionId, plannedYear);
        contractDTO.setVatValue(3454667);
        var contractStr = objectMapper.writeValueAsString(contractDTO);

        mockMvc.perform(post("/contracts")
                        .with(jwt().jwt(builder -> builder.jti(user).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(contractStr))
                .andExpect(status().isConflict())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(VatValueException.class)
                        .hasMessageContaining("Wrong vat value"));

        mockMvc.perform(put("/contracts/" + UUID.randomUUID())
                        .with(jwt().jwt(builder -> builder.jti(user).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(contractStr))
                .andExpect(status().isConflict())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(VatValueException.class)
                        .hasMessageContaining("Wrong vat value"));
    }

    @Test
    @DisplayName("Получение всех договоров")
    @WithMockUser(value = USER1_ID, roles = "GUEST")
    void test_getAll() throws Exception {
        Calendar now = Calendar.getInstance();
        LocalDateTime plannedDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        int plannedYear = plannedDate.getYear();

        var employee = Employee.builder()
                .userId(UUID.fromString(USER1_ID))
                .id(UUID.fromString(USER1_ID))
                .departmentId(UUID.randomUUID())
                .firstName("Иван")
                .lastName("Иванов")
                .build();

        employeeRepository.save(employee);

        Contract contract = new Contract();
        contract.setUserId(UUID.fromString(USER1_ID));
        contract.setResponsibleEmployeeId(UUID.fromString(USER1_ID));
        contract.setCreationTime(LocalDateTime.now());
        contract.setContractorId(UUID.randomUUID());
        contract.setOrganizations(Collections.singleton(organization1));
        contract.setRegionIds(new HashSet<>(Set.of(UUID.randomUUID())));
        contract.setTransportType(TransportTypeEnum.TAXI);
        contract.setSum(1000L);
        contract.setStartDate(LocalDate.of(plannedYear - 1, 1, 1));
        contract.setEndDate(LocalDate.of(plannedYear + 1, 1, 1));
        contract.setContractNumber("123456");
        contract.setIncludeVat(true);
        contract.setVatValue(20);
        contract.setContractType(ContractType.TRANSITIONAL);
        contract.setRestrictionType(RestrictionType.NONE);
        contract.setDriverLatePickupPenalty(BigDecimal.valueOf(0.01));
        contract.setPoorServiceQualityPenalty(BigDecimal.valueOf(0.02));
        contract.setDriverOrderCancellationPenalty(BigDecimal.valueOf(0.03));
        contractRepository.saveAndFlush(contract);

        var response = mockMvc.perform(
                        get("/contracts")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();
        List<GetContractDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        assertEquals(contractRepository.findAll().size(), actual.size());
    }

    @Test
    @DisplayName("Поиск договоров")
    @WithMockUser(value = USER1_ID, roles = "GUEST")
    void test_search() throws Exception {
        Calendar now = Calendar.getInstance();
        LocalDateTime plannedDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        int plannedYear = plannedDate.getYear();

        employeeRepository.saveAndFlush(Employee.builder()
                .id(UUID.fromString(USER1_ID))
                .userId(UUID.fromString(USER1_ID))
                .firstName("Иван")
                .lastName("Иванов")
                .build());

        Contract contract = new Contract();
        contract.setUserId(UUID.fromString(USER1_ID));
        contract.setResponsibleEmployeeId(UUID.fromString(USER1_ID));
        contract.setCreationTime(LocalDateTime.now());
        contract.setContractorId(UUID.randomUUID());
        contract.setOrganizations(Collections.singleton(organization1));
        contract.setRegionIds(new HashSet<>(Set.of(UUID.randomUUID())));
        contract.setTransportType(TransportTypeEnum.TAXI);
        contract.setSum(1000L);
        contract.setStartDate(LocalDate.of(plannedYear - 1, 1, 1));
        contract.setEndDate(LocalDate.of(plannedYear + 1, 1, 1));
        contract.setContractNumber("123456");
        contract.setIncludeVat(true);
        contract.setVatValue(20);
        contract.setContractType(ContractType.TRANSITIONAL);
        contract.setRestrictionType(RestrictionType.NONE);
        contract = contractRepository.saveAndFlush(contract);

        Contract contract2 = new Contract();
        contract2.setUserId(UUID.fromString(USER1_ID));
        contract2.setResponsibleEmployeeId(UUID.fromString(USER1_ID));
        contract2.setCreationTime(LocalDateTime.now());
        contract2.setContractorId(UUID.randomUUID());
        contract2.setOrganizations(Collections.singleton(organization2));
        contract2.setRegionIds(new HashSet<>(Set.of(UUID.randomUUID())));
        contract2.setTransportType(TransportTypeEnum.TAXI);
        contract2.setSum(1000L);
        contract2.setStartDate(LocalDate.of(plannedYear - 3, 1, 1));
        contract2.setEndDate(LocalDate.of(plannedYear - 2, 1, 1));
        contract2.setContractNumber("123456");
        contract2.setIncludeVat(true);
        contract2.setVatValue(20);
        contract2.setContractType(ContractType.TRANSITIONAL);
        contract2.setRestrictionType(RestrictionType.NONE);
        contract2 = contractRepository.saveAndFlush(contract2);


        ContractSearchDTO contractSearchDTO = ContractSearchDTO.builder()
                .region(contract.getRegionIds().stream().findFirst().orElse(null))
                .startDate(plannedDate.toLocalDate())
                .endDate(plannedDate.toLocalDate())
                .build();

        String body = objectMapper.writeValueAsString(contractSearchDTO);

        var response = mockMvc.perform(
                        post("/contracts/search")
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn();
        Page<GetContractDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                        new TypeReference<>() {
                        });
        assertEquals(1, actual.getTotalElements());
        List<GetContractDTO> searchResults = actual.toList();
        GetContractDTO getContractDTO = searchResults.getFirst();
        assertEquals(contract.getId(), getContractDTO.getId());
        assertTrue(contract.getOrganizations().stream().map(Organization::getId).collect(Collectors.toSet())
                .containsAll(getContractDTO.getOrganizationIds()));
        assertEquals(contract.getContractNumber(), getContractDTO.getContractNumber());
        assertEquals(contract.getContractorId(), getContractDTO.getContractorId());
        assertTrue(contractSearchDTO.getStartDate().isBefore(getContractDTO.getEndDate()) ||
                contractSearchDTO.getStartDate().isEqual(getContractDTO.getEndDate()));
        assertTrue(contractSearchDTO.getEndDate().isAfter(getContractDTO.getStartDate()) ||
                contractSearchDTO.getEndDate().isEqual(getContractDTO.getStartDate()));
    }

    @Test
    @DisplayName("Поиск уникальных увхд")
    @WithMockUser(value = USER1_ID, roles = "GUEST")
    void test_getUniqueUvhd() throws Exception {
        var count = 100;
        var contractorId1 = UUID.randomUUID();
        var contractorId2 = UUID.randomUUID();
        for (int i = 0; i < count; i++) {
            var contractorId = i % 2 == 0 ? contractorId1 : contractorId2;

            var contract = new Contract();
            contract.setUserId(UUID.fromString(USER1_ID));
            contract.setCreationTime(LocalDateTime.now());
            contract.setContractorId(contractorId);
            contract.setOrganizations(Collections.singleton(organization1));
            contract.setRegionIds(new HashSet<>(Set.of(UUID.randomUUID())));
            contract.setTransportType(TransportTypeEnum.TAXI);
            contract.setSum(1000L);
            contract.setStartDate(LocalDate.now());
            contract.setEndDate(LocalDate.now());
            contract.setContractNumber("123456");
            contract.setIncludeVat(true);
            contract.setVatValue(20);
            contract.setUvhd(String.valueOf(i % 10));
            contract.setContractType(ContractType.TRANSITIONAL);
            contract.setRestrictionType(RestrictionType.NONE);
            contractRepository.saveAndFlush(contract);
        }

        var response = mockMvc.perform(get("/contracts/uvhd?contractorId=%s&page=0&size=3".formatted(contractorId1))
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();

        var content = response.getResponse().getContentAsString(StandardCharsets.UTF_8);
        var actual = objectMapper.readValue(content, new TypeReference<Page<String>>() {
        });

        assertEquals(5, actual.getTotalElements());
        assertEquals(2, actual.getTotalPages());
    }

    @Test
    @DisplayName("Поиск уникальных увхд с фильтрацией по типу транспорта")
    @WithMockUser(value = USER1_ID, roles = "GUEST")
    void test_getUniqueUvhdWithTransportTypeFilter() throws Exception {
        var contractorId = UUID.randomUUID();

        createTestContract(contractorId, TransportTypeEnum.COURIER, "UVHD-C-1");
        createTestContract(contractorId, TransportTypeEnum.COURIER, "UVHD-C-2");
        createTestContract(contractorId, TransportTypeEnum.INDIVIDUAL, "UVHD-I-1");
        createTestContract(contractorId, TransportTypeEnum.INDIVIDUAL, "UVHD-I-2");
        createTestContract(contractorId, TransportTypeEnum.DEDICATED, "UVHD-D-1");

        var response = mockMvc.perform(get("/contracts/uvhd?contractorId=%s&transportType=COURIER&page=0&size=10".formatted(contractorId))
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();

        var content = response.getResponse().getContentAsString(StandardCharsets.UTF_8);
        var actual = objectMapper.readValue(content, new TypeReference<Page<String>>() {
        });

        assertEquals(2, actual.getTotalElements());
        assertTrue(actual.getContent().contains("UVHD-C-1"));
        assertTrue(actual.getContent().contains("UVHD-C-2"));

        response = mockMvc.perform(get("/contracts/uvhd?contractorId=%s&transportType=INDIVIDUAL&page=0&size=10".formatted(contractorId)))
                .andExpect(status().isOk()).andReturn();

        content = response.getResponse().getContentAsString(StandardCharsets.UTF_8);
        actual = objectMapper.readValue(content, new TypeReference<Page<String>>() {
        });

        assertEquals(2, actual.getTotalElements());
        assertTrue(actual.getContent().contains("UVHD-I-1"));
        assertTrue(actual.getContent().contains("UVHD-I-2"));

        response = mockMvc.perform(get("/contracts/uvhd?contractorId=%s&page=0&size=10".formatted(contractorId)))
                .andExpect(status().isOk()).andReturn();

        content = response.getResponse().getContentAsString(StandardCharsets.UTF_8);
        actual = objectMapper.readValue(content, new TypeReference<Page<String>>() {
        });

        assertEquals(5, actual.getTotalElements());
    }

    private ContractDTO createAndFillDataNewContract(Contractor contractor, UUID regionId, int plannedYear) {
        var contractDTO = new ContractDTO();
        contractDTO.setContractorId(contractor.getId());
        contractDTO.setOrganizationIds(Collections.singletonList(organization1.getId()));
        contractDTO.setRegion(regionId.toString());
        contractDTO.setRegionIds(new HashSet<>(Set.of(regionId)));
        contractDTO.setTransportType(TransportTypeEnum.TAXI);
        contractDTO.setSum(1000L);
        contractDTO.setStartDate(LocalDate.of(plannedYear - 1, 1, 1));
        contractDTO.setEndDate(LocalDate.of(plannedYear + 1, 1, 1));
        contractDTO.setContractNumber("123456");
        contractDTO.setIncludeVat(true);
        contractDTO.setVatValue(VatType.BASE_22.getPercentValue());
        contractDTO.setUvhd("12312");
        contractDTO.setPaymentOrganizationId(null);
        contractDTO.setPaymentOrganizationId(organization2.getId());
        return contractDTO;
    }

    private void createTestContract(UUID contractorId, TransportTypeEnum transportType, String uvhd) {
        var contract = new Contract();
        contract.setUserId(UUID.fromString(USER1_ID));
        contract.setCreationTime(LocalDateTime.now());
        contract.setContractorId(contractorId);
        contract.setOrganizations(Collections.singleton(organization1));
        contract.setRegionIds(new HashSet<>(Set.of(UUID.randomUUID())));
        contract.setTransportType(transportType);
        contract.setServiceType(TransportServiceType.CARGO_TRANSPORTATION);
        contract.setSum(1000L);
        contract.setStartDate(LocalDate.now());
        contract.setEndDate(LocalDate.now());
        contract.setContractNumber("1234567");
        contract.setIncludeVat(true);
        contract.setVatValue(20);
        contract.setUvhd(uvhd);
        contract.setContractType(ContractType.TRANSITIONAL);
        contract.setRestrictionType(RestrictionType.NONE);
        contractRepository.saveAndFlush(contract);
    }
}
