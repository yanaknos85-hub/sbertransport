package ru.sberbank.ditsib.transport.tariff.controller;

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
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff.messaging.TariffMessage;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.dao.*;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Department;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;
import ru.sberbank.ditsib.transport.tariff.dto.CarSharingTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.NewCarSharingTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.TimedTariffParamsDTO;
import ru.sberbank.ditsib.transport.tariff.dto.mapper.EntityDTOMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SuppressWarnings({"OptionalGetWithoutIsPresent"})
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@Transactional
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка контроллера тарифов каршеринга")
@ActiveProfiles({"test", "kafka"})
class CarSharingTariffControllerTest extends KafkaTest {

    protected Organization organization1;
    protected Organization organization2;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private NewCarSharingTariffDTO testNewCarSharingTariff1;
    private static final String USER_ID = "d1c2bb8d-a1b2-480e-8d96-36e9017227c7";
    protected Employee employee1;
    protected Department department1;

    @Autowired
    private EntityDTOMapper mapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TariffRepository<BaseTariff> tariffRepository;
    @Autowired
    private CarSharingTariffRepository carSharingTariffRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @BeforeEach
    public void prepareData() {
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

        Contract contract = Contract.builder().organizations(Set.of(organization1)).contractNumber("1")
                .transportType(TransportTypeEnum.CARSHARING)
                .contractorId(UUID.randomUUID()).regionIds(Set.of(UUID.randomUUID())).includeVat(false)
                .creationTime(LocalDateTime.now()).startDate(LocalDate.of(2021, 4, 18))
                .endDate(LocalDate.now()).sum(1000L).userId(UUID.randomUUID())
                .contractType(ContractType.TRANSITIONAL)
                .restrictionType(RestrictionType.NONE)
                .driverLatePickupPenalty(BigDecimal.valueOf(0.01))
                .poorServiceQualityPenalty(BigDecimal.valueOf(0.01))
                .driverOrderCancellationPenalty(BigDecimal.valueOf(0.01))
                .build();
        contractRepository.save(contract);

        testNewCarSharingTariff1 = NewCarSharingTariffDTO.builder()
                .contractId(contract.getId())
                .regionId(Collections.singleton(UUID.randomUUID()))
                .organizationId(organization1.getId())
                .transportType(TransportTypeEnum.CARSHARING)
                .rideCostPerKm(100)
                .rideCostPerMin(200)
                .waitCostPerMin(300)
                .timedTariffParams(TimedTariffParamsDTO.builder()
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
    }

    @Test
    @DisplayName("Добавление и исключение при попытке продублировать запись")
    void add() throws Exception {
        var request = objectMapper.writeValueAsString(testNewCarSharingTariff1);

        var response = mockMvc.perform(
                post("/" + TransportTypeEnum.CARSHARING.getName().toLowerCase(Locale.ROOT))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)).andExpect(status().isOk());

        assertThat(carSharingTariffRepository.count()).isEqualTo(1);

        CarSharingTariffDTO
                actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                CarSharingTariffDTO.class);
        var expected = carSharingTariffRepository.findAll().getFirst();

        assertNotNull(expected.getId());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getContract().getId(), actual.getContractId());
        assertNotNull(expected.getHumanReadableId());
        assertEquals(expected.getHumanReadableId(), actual.getHumanReadableId());
        assertNotNull(expected.getRegionId());
        assertTrue(actual.getRegionId().contains(expected.getRegionId()));
        assertNotNull(expected.getOrganization().getId());
        assertEquals(expected.getOrganization().getId(), actual.getOrganizationId());
        assertEquals(expected.getRideCostPerKm(), actual.getRideCostPerKm());
        assertEquals(expected.getRideCostPerMin(), actual.getRideCostPerMin());
        assertEquals(expected.getWaitCostPerMin(), actual.getWaitCostPerMin());

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

        assertEquals(expected.getCoefChildSeat(), actual.getCoefChildSeat());
        assertEquals(expected.getCoefPetTransport(), actual.getCoefPetTransport());
        assertEquals(expected.getCoefTraffic(), actual.getCoefTraffic());
        assertEquals(expected.getCoefChildSeat(), actual.getCoefChildSeat());
        assertEquals(expected.getCoefCasko(), actual.getCoefCasko());

        var message = consumeMessage("service.tariff", TariffMessage.class);
        CarSharingTariff foundTariff = carSharingTariffRepository.findById(actual.getId()).get();
        assertEquals(foundTariff.getHumanReadableId(), actual.getHumanReadableId());
        assertEquals(message.humanReadableId(), actual.getHumanReadableId());
        assertEquals(message.organizationId(), actual.getOrganizationId());
        assertTrue(message.humanReadableId().contains("TF-"));
        assertEquals(message.contractId(), foundTariff.getContract().getId());

        //проверка создания тарифа, дублирующего добавленный
        var ex = mockMvc.perform(
                post("/" + TransportTypeEnum.CARSHARING.getName().toLowerCase(Locale.ROOT))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)).andExpect(status().isConflict()).andReturn().getResolvedException();
        assertInstanceOf(DuplicateDataException.class, ex);
    }


    @Test
    @DisplayName("Изменение")
    @WithMockUser(roles = "GUEST")
    void edit() throws Exception {
        CarSharingTariff saved = carSharingTariffRepository.save(mapper.newDtoToCarSharingTariff(testNewCarSharingTariff1));

        var expected = testNewCarSharingTariff1.toBuilder()
                .rideCostPerKm(101)
                .rideCostPerMin(201)
                .waitCostPerMin(301)
                .timedTariffParams(TimedTariffParamsDTO.builder()
                        .coefWorkDayMorning(1.1)
                        .coefWorkDayNoon(2.1)
                        .coefWorkDayEvening(3.1)
                        .coefWorkDayNight(4.1)
                        .coefDayOff(5.1)
                        .build())
                .coefCasko(1.1)
                .coefChildSeat(2.1)
                .coefTraffic(.11)
                .coefPetTransport(3.1)
                .build();

        var request = objectMapper.writeValueAsString(expected);

        mockMvc.perform(
                        put("/" + TransportTypeEnum.CARSHARING.getName().toLowerCase(Locale.ROOT) + "/" + saved.getId())
                                .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk());

        CarSharingTariff actual = carSharingTariffRepository.findById(saved.getId()).get();

        assertNotNull(actual.getId());
        assertNotNull(actual.getHumanReadableId());
        assertNotNull(expected.getRegionId());
        assertTrue(expected.getRegionId().contains(actual.getRegionId()));
        assertNotNull(expected.getOrganizationId());
        assertEquals(expected.getOrganizationId(), actual.getOrganization().getId());
        assertNotNull(expected.getRideCostPerKm());
        assertEquals(expected.getRideCostPerKm(), actual.getRideCostPerKm());
        assertNotNull(expected.getRideCostPerMin());
        assertEquals(expected.getRideCostPerMin(), actual.getRideCostPerMin());
        assertNotNull(expected.getWaitCostPerMin());
        assertEquals(expected.getWaitCostPerMin(), actual.getWaitCostPerMin());

        assertNotNull(expected.getTimedTariffParams());
        assertNotNull(expected.getTimedTariffParams().getCoefWorkDayMorning());
        assertNotNull(expected.getTimedTariffParams().getCoefWorkDayNoon());
        assertNotNull(expected.getTimedTariffParams().getCoefWorkDayEvening());
        assertNotNull(expected.getTimedTariffParams().getCoefWorkDayNight());
        assertNotNull(expected.getTimedTariffParams().getCoefDayOff());
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

        assertNotNull(expected.getCoefChildSeat());
        assertEquals(expected.getCoefChildSeat(), actual.getCoefChildSeat());
        assertNotNull(expected.getCoefPetTransport());
        assertEquals(expected.getCoefPetTransport(), actual.getCoefPetTransport());
        assertNotNull(expected.getCoefTraffic());
        assertEquals(expected.getCoefTraffic(), actual.getCoefTraffic());
        assertNotNull(expected.getCoefChildSeat());
        assertEquals(expected.getCoefChildSeat(), actual.getCoefChildSeat());
        assertNotNull(expected.getCoefCasko());
        assertEquals(expected.getCoefCasko(), actual.getCoefCasko());

        var message = consumeMessage("service.tariff", TariffMessage.class);
        assertEquals(saved.getHumanReadableId(), message.humanReadableId());
        assertEquals(saved.getOrganization().getId(), message.organizationId());
        assertTrue(message.humanReadableId().contains("TF-"));
    }

    @Test
    @DisplayName("Получение")
    @WithMockUser(roles = "GUEST")
    void getTaxi() throws Exception {

        var expected = testNewCarSharingTariff1;
        CarSharingTariff saved = carSharingTariffRepository.save(mapper.newDtoToCarSharingTariff(testNewCarSharingTariff1));
        var request = objectMapper.writeValueAsString(expected);

        var response = mockMvc.perform(
                get("/" + TransportTypeEnum.CARSHARING.getName().toLowerCase(Locale.ROOT) + "/" +
                        saved.getId()).contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isOk());

        CarSharingTariffDTO
                actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                CarSharingTariffDTO.class);

        assertNotNull(actual.getId());
        assertNotNull(expected.getOrganizationId());
        assertEquals(expected.getOrganizationId(), actual.getOrganizationId());
        assertNotNull(expected.getRideCostPerKm());
        assertEquals(expected.getRideCostPerKm(), actual.getRideCostPerKm());
        assertNotNull(expected.getRideCostPerMin());
        assertEquals(expected.getRideCostPerMin(), actual.getRideCostPerMin());
        assertNotNull(expected.getWaitCostPerMin());
        assertEquals(expected.getWaitCostPerMin(), actual.getWaitCostPerMin());

        assertNotNull(expected.getTimedTariffParams());
        assertNotNull(expected.getTimedTariffParams().getCoefWorkDayMorning());
        assertNotNull(expected.getTimedTariffParams().getCoefWorkDayNoon());
        assertNotNull(expected.getTimedTariffParams().getCoefWorkDayEvening());
        assertNotNull(expected.getTimedTariffParams().getCoefWorkDayNight());
        assertNotNull(expected.getTimedTariffParams().getCoefDayOff());
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


        assertNotNull(expected.getCoefChildSeat());
        assertEquals(expected.getCoefChildSeat(), actual.getCoefChildSeat());
        assertNotNull(expected.getCoefPetTransport());
        assertEquals(expected.getCoefPetTransport(), actual.getCoefPetTransport());
        assertNotNull(expected.getCoefTraffic());
        assertEquals(expected.getCoefTraffic(), actual.getCoefTraffic());
        assertNotNull(expected.getCoefChildSeat());
        assertEquals(expected.getCoefChildSeat(), actual.getCoefChildSeat());
        assertNotNull(expected.getCoefCasko());
        assertEquals(expected.getCoefCasko(), actual.getCoefCasko());
    }

    @Test
    @DisplayName("Удаление")
    @WithMockUser(roles = "GUEST")
    void deleteItem() throws Exception {
        var id = carSharingTariffRepository.saveAndFlush(mapper.newDtoToCarSharingTariff(testNewCarSharingTariff1)).getId();
        assertThat(carSharingTariffRepository.count()).isEqualTo(1);

        mockMvc.perform(delete("/" + TransportTypeEnum.CARSHARING.getName().toLowerCase(Locale.ROOT) + "/" + id.toString()))
                .andExpect(status().isOk());

        assertThat(carSharingTariffRepository.findAll().stream().filter(BaseTariff::isActive).collect(
                Collectors.toList())).size().isEqualTo(0);
    }

    @Test
    @DisplayName("Удаление несуществующего")
    @WithMockUser(roles = "GUEST")
    void delete_noExists() throws Exception {
        mockMvc.perform(
                        delete("/" + TransportTypeEnum.CARSHARING.getName().toLowerCase(Locale.ROOT) + "/" + UUID.randomUUID().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Просмотр всех")
    @WithMockUser(roles = "GUEST")
    void getAll() throws Exception {
        for (var i = 0; i < 10; i++) {
            tariffRepository.save(mapper.newDtoToCarSharingTariff(
                    testNewCarSharingTariff1.toBuilder()
                            .rideCostPerKm(200 * i)
                            .rideCostPerMin(300 * i)
                            .waitCostPerMin(400 * i)
                            .build()));
        }

        var response =
                mockMvc.perform(get("/" + TransportTypeEnum.CARSHARING.getName().toLowerCase(Locale.ROOT)))
                        .andExpect(status().isOk());

        response.andExpect(jsonPath("$.length()").value(tariffRepository.count()));

        for (var i = 0; i < carSharingTariffRepository.count(); i++) {
            var expected = carSharingTariffRepository.findAll().get(i);

            response.andExpect(jsonPath("$[" + i + "].id").value(expected.getId().toString()))
                    .andExpect(jsonPath("$[" + i + "].regionId").value(expected.getRegionId().toString()))
                    .andExpect(jsonPath("$[" + i + "].transportType")
                            .value(expected.getTransportType().toString()))
                    .andExpect(jsonPath("$[" + i + "].rideCostPerKm").value(expected.getRideCostPerKm()))
                    .andExpect(jsonPath("$[" + i + "].rideCostPerMin")
                            .value(expected.getRideCostPerMin()))
                    .andExpect(jsonPath("$[" + i + "].waitCostPerMin").value(expected.getWaitCostPerMin()));
        }
    }
}
