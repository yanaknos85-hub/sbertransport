package ru.sberbank.ditsib.transport.request.controller.carsharing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff.model.CalculatedDto;
import ru.sberbank.ditsib.transport.constants.CarsharingJoinRequestStatus;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.dto.tariff.TransportTypeDto;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.dao.deadline.DeadlineSettingsRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.dto.carsharing.*;
import ru.sberbank.ditsib.transport.request.mappers.CarsharingJoinRequestMapper;
import ru.sberbank.ditsib.transport.request.mappers.DeadlineSettingsMapper;
import ru.sberbank.ditsib.transport.request.shared.DeadlineSettingsSharedData;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.ditsib.transport.constants.CarsharingJoinRequestStatus.*;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.CARSHARING;
import static ru.sberbank.ditsib.transport.request.database.model.carsharing.CorporateCarsharingJoinStatus.DECLINED;
import static ru.sberbank.ditsib.transport.request.database.model.carsharing.CorporateCarsharingJoinStatus.JOINED;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера заявок на подключение к корп.каршерингам")
@MockitoBean(types = JwtDecoder.class)
class CarsharingJoinRequestControllerTest extends KafkaTest {
    
    @Autowired
    private CarsharingJoinRequestMapper joinRequestMapper;
    @Autowired
    private DeadlineSettingsMapper deadlineSettingsMapper;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private CarsharingJoinRequestRepository joinRequestRepository;
    @Autowired
    private CarsharingJoinRequestTextRepository textRepository;
    @Autowired
    private CarsharingTariffRepository tariffRepository;
    @Autowired
    private CarsharingContractRepository contractRepository;
    @Autowired
    private CorporateCarsharingRepository carsharingRepository;
    @Autowired
    private ContractorRepository contractorRepository;
    @Autowired
    private CarsharingJoinRequestProcessingModeSettingRepository modeSettingRepository;
    @Autowired
    private ContractorAndJoinStatusRepository contractorAndJoinStatusRepository;
    @Autowired
    private DeadlineSettingsRepository deadlineSettingsRepository;
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    private final static String COMMON_URI = "/carsharing-join-request/";
    private final static String COMMON_PROCESS_URI = "/carsharing-join-request/process/";
    private final static String EMAIL = "email@ema.il";
    private final static String PHONE = "+7 (908) 123-45-01";
    private final static String EMAIL2 = "email2@ema.il";
    private final static String PHONE2 = "+7 (927) 123-45-02";
    private final static int DEADLINE_VALUE = 3;
    private final static ChronoUnit DEADLINE_UNIT = ChronoUnit.DAYS;
    private final static String DEADLINE_STRING = "3 дня";
    
    private final TransportTypeEnum CARSHARING_ENUM = TransportTypeEnum.CARSHARING;
    private final UUID CARSHARING_ID = TransportTypeEnum.CARSHARING.getId();
    private final String CARSHARING_NAME = TransportTypeEnum.CARSHARING.getName();
    private final UUID ORG1_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-fffffffffff1");
    private final UUID ORG2_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-fffffffffff2");
    private final String REGION1 = "Region1";
    private final String REGION2 = "Region2";
    private final UUID TARIFF1_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-aaaaaaaaaaa1");
    private final UUID TARIFF2_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-aaaaaaaaaaa2");
    private final UUID TARIFF3_ID = UUID.fromString("cc3eb25c-b3d0-436b-bf3e-aaaaaaaaaaa3");
    private final UUID CONTRACT1_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-bbbbbbbbbbb1");
    private final UUID CONTRACT2_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-bbbbbbbbbbb2");
    private final UUID CONTRACT3_ID = UUID.fromString("cc2eb24c-b3d0-424b-bf3e-bbbbbbbbbbb3");
    private final UUID CONTRACTOR1_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-ccccccccccc1");
    private final UUID CONTRACTOR3_ID = UUID.fromString("cc2eb24c-b3d0-424b-bf3e-ccccccccccc3");
    private final String USER1_ID_STR = "cc2eb25b-b5d0-429b-bf1e-aaaaaaccccc1";
    private final UUID USER1_ID = UUID.fromString(USER1_ID_STR);
    private final UUID USER2_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-aaaaaaccccc2");
    private final UUID USER3_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-aaaaaaccccc3");
    private final UUID USER4_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-aaaaaaccccc4");
    private final UUID USER5_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-aaaaaaccccc5");
    private final UUID EMPLOYEE1_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-ccccccbbbbb1");
    private final UUID EMPLOYEE2_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-ccccccbbbbb2");
    private final UUID EMPLOYEE3_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-ccccccbbbbb3");
    private final UUID EMPLOYEE4_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-ccccccbbbbb4");
    private final UUID EMPLOYEE5_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-ccccccbbbbb5");
    
    private final DeadlineSettingsSharedData deadlineSettingsSharedData = new DeadlineSettingsSharedData();
    private TransportTypeDto transportTypeDto;
    private Organization org1;
    private Organization org2;
    private Employee employee1;
    private Employee employee2;
    private Employee employee3;
    private Employee employee4;
    private Employee employee5;
    private CarsharingTariff tariff1;
    private CarsharingTariff tariff2;
    private CarsharingTariff tariff3;
    private CarsharingContract contract1;
    private CarsharingContract contract2;
    private CarsharingContract contract3;
    private Contractor contractor1;
    private Contractor contractor2;
    private Contractor contractor3;
    private CorporateCarsharing carsharing11;
    private CorporateCarsharing carsharing12;
    private CorporateCarsharing carsharing21;
    private CorporateCarsharing carsharing22;
    private CorporateCarsharing carsharing31;
    private CorporateCarsharing carsharing32;
    
    @BeforeEach
    void init() {
        transportTypeDto = TransportTypeDto.builder()
                                           .id(CARSHARING_ID)
                                           .name(CARSHARING_NAME)
                                           .build();
        var mode = CarsharingJoinRequestProcessingModeSetting.builder()
                                                             .processingMode(
                                                                     CarsharingJoinRequestProcessingMode.MANUALLY_BY_ENGINEER)
                                                             .build();
        modeSettingRepository.save(mode);
        
        // убедимся в том, что проставлен требуемый режим обработки заявки
        assertThat(modeSettingRepository.count()).isEqualTo(1);
        assertThat(modeSettingRepository.findAll().get(0).getProcessingMode()).isEqualTo(
                CarsharingJoinRequestProcessingMode.MANUALLY_BY_ENGINEER);
        
        org1 = organizationRepository.save(createOrganization(ORG1_ID, 1L));
        org2 = organizationRepository.save(createOrganization(ORG2_ID, 2L));
        
        var dep1 = departmentRepository.save(createDepartment(UUID.randomUUID(), org1));
        var dep2 = departmentRepository.save(createDepartment(UUID.randomUUID(), org2));
        
        employee1 = employeeRepository.save(createEmployee(EMPLOYEE1_ID, dep1, USER1_ID));
        employee2 = employeeRepository.save(createEmployee(EMPLOYEE2_ID, dep2, USER2_ID));
        employee3 = employeeRepository.save(createEmployee(EMPLOYEE3_ID, null, USER3_ID));
        employee4 = employeeRepository.save(createEmployee(EMPLOYEE4_ID, null, USER4_ID));
        employee5 = employeeRepository.save(createEmployee(EMPLOYEE5_ID, null, USER5_ID));
        employeeRepository.flush();
        contractor1 = contractorRepository.save(createContractor(CONTRACTOR1_ID, "Contractor1"));
        //contractor2 = contractorRepository.save(createContractor(CONTRACTOR2_ID, "Contractor2"));
        contractor3 = contractorRepository.save(createContractor(CONTRACTOR3_ID, "Contractor3"));
        
        
        contract1 = contractRepository.save(
                createContract(CONTRACT1_ID, CONTRACTOR1_ID, REGION1, Set.of(ORG1_ID, ORG2_ID)));
        contract2 = contractRepository.save(
                createContract(CONTRACT2_ID, CONTRACTOR1_ID, REGION2, Set.of(ORG1_ID, ORG2_ID)));
        contract3 = contractRepository.save(
                createContract(CONTRACT3_ID, CONTRACTOR3_ID, REGION1, Set.of(ORG1_ID, ORG2_ID)));
        
        tariff1 = tariffRepository.save(createTariff(TARIFF1_ID, REGION1, CONTRACT1_ID));
        tariff2 = tariffRepository.save(createTariff(TARIFF2_ID, REGION2, CONTRACT2_ID));
        tariff3 = tariffRepository.save(createTariff(TARIFF3_ID, REGION1, CONTRACT3_ID));
        tariffRepository.flush();
        
        carsharing11 = carsharingRepository.save(createCarsharing(contract1, ORG1_ID, null));
        carsharing12 = carsharingRepository.save(createCarsharing(contract1, ORG2_ID, null));
        carsharing21 = carsharingRepository.save(createCarsharing(contract2, ORG1_ID, null));
        carsharing22 = carsharingRepository.save(createCarsharing(contract2, ORG2_ID, null));
        carsharing31 = carsharingRepository.save(createCarsharing(contract3, ORG1_ID, null));
        carsharing32 = carsharingRepository.save(createCarsharing(contract3, ORG2_ID, null));
        carsharingRepository.flush();
        //сохраним настройки КС для всех корп.клиентов, чтобы далее проверить формирование текстового поля
        var deadlineSettingsMessage =
                deadlineSettingsSharedData.getTestDeadlineSettingsMessage(UUID.randomUUID());
        var deadlineSettings = deadlineSettingsMapper.fromMessage(deadlineSettingsMessage);
        deadlineSettings.getCarsharingJoinDeadline().setUnit(DEADLINE_UNIT);
        deadlineSettings.getCarsharingJoinDeadline().setValue(DEADLINE_VALUE);
        deadlineSettings.setOrganizationId(ORG1_ID);
        deadlineSettingsRepository.save(deadlineSettings);
        
        deadlineSettingsMessage =
                deadlineSettingsSharedData.getTestDeadlineSettingsMessage(UUID.randomUUID());
        deadlineSettings = deadlineSettingsMapper.fromMessage(deadlineSettingsMessage);
        deadlineSettings.getCarsharingJoinDeadline().setUnit(DEADLINE_UNIT);
        deadlineSettings.getCarsharingJoinDeadline().setValue(DEADLINE_VALUE);
        deadlineSettings.setId(UUID.randomUUID());
        deadlineSettings.setOrganizationId(ORG2_ID);
        deadlineSettingsRepository.save(deadlineSettings);
        deadlineSettingsRepository.flush();
    }
    
    @AfterEach
    void clear() {
        deadlineSettingsRepository.deleteAll();
        joinRequestRepository.deleteAll();
        textRepository.deleteAll();
        carsharingRepository.deleteAll();
        contractorAndJoinStatusRepository.deleteAll();
        contractRepository.deleteAll();
        tariffRepository.deleteAll();
        contractorRepository.deleteAll();
        employeeRepository.deleteAll();
        organizationRepository.deleteAll();
    }
    

    @Test
    @DisplayName("Получение списка каршерингов - несуществующий сотрудник - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void getCarsharingJoinsForEmployee_incorrectEmployee() throws Exception {
        List<CalculatedDto> calculated = new ArrayList<>();
        calculated.add(createCalcDto(TARIFF1_ID, 1000L));
        var request = objectMapper.writeValueAsString(calculated);
        
        String uri = COMMON_URI + ORG1_ID + "/" + UUID.randomUUID() + "/" + "joins";
        
        Exception ex = incorrectPOST(uri, request, status().isNotFound());
        assertThat(ex).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Получение бланка заявки - успех")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void getCreatedOrBlank_createBlankTest() throws Exception {
        //убедимся, что табличка текстовых полей пуста
        assertThat(textRepository.count()).isZero();
        
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        GetCarsharingJoinRequestDTO actual = correctGET(uri);
        
        //убедимся, что табличка текстовых полей была заполнена по умолчанию
        assertThat(textRepository.count()).isEqualTo(1);
        Optional<CarsharingJoinRequestText> optionalText = textRepository.findByOrganizationId(ORG1_ID);
        assertThat(optionalText).isPresent();
        assertThat(optionalText.get().getBeforeFioSecondPart()).isEqualTo(DEADLINE_STRING);
        
        assertThat(actual).isNotNull();
        assertThat(actual.getContractors().size()).isEqualTo(3);
        // проверить, что для организации1 и сотрудника1 доступны для подключения: КА1 Р1, КА1 Р2, КА3 Р1
        checkSingleCarsharingFromBlankRequest(actual, CONTRACTOR1_ID, REGION1);
        checkSingleCarsharingFromBlankRequest(actual, CONTRACTOR1_ID, REGION2);
        checkSingleCarsharingFromBlankRequest(actual, CONTRACTOR3_ID, REGION1);
    }
    
    @Test
    @DisplayName("Получение заявки - несуществующий сотрудник - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void getCreatedOrBlank_incorrectEmployee() throws Exception {
        String uri = COMMON_URI + ORG1_ID + "/" + UUID.randomUUID();
        Exception ex = incorrectGET(uri, status().isNotFound());
        assertThat(ex).isInstanceOf(EntityNotFoundException.class);
    }
    
    @Test
    @DisplayName("Создание заявки - успех")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void create() throws Exception {
        //убедимся, что табличка текстовых полей пуста
        assertThat(textRepository.count()).isZero();
        
        //получим бланк заявки для сотрудника 1 и сформируем заявку. Подключаем в регионе 1: КА1 Р1, КА3 Р1
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, false, true, PHONE, EMAIL, true, true));
        var actual = createRequest(uri, json);
        
        //убедимся, что табличка текстовых полей была заполнена по умолчанию
        assertThat(textRepository.count()).isEqualTo(1);
        Optional<CarsharingJoinRequestText> optionalText = textRepository.findByOrganizationId(ORG1_ID);
        assertThat(optionalText).isPresent();
        assertThat(optionalText.get().getBeforeFioSecondPart()).isEqualTo(DEADLINE_STRING);
        
        //проверим заявку и подключаемы каршеринги
        assertThat(actual).isNotNull();
        assertThat(actual.getContractors().size()).isEqualTo(3);
        assertThat(actual.getEmail()).isEqualTo(EMAIL);
        assertThat(actual.getPhone()).isEqualTo(PHONE);
        assertThat(actual.getRequestStatus()).isEqualTo(UNDER_CONSIDERATION);
        // проверить, что для организации1 и сотрудника1 была составлена заявка для подключения: КА1 Р1, КА1 Р2, КА3 Р1
        checkSingleCarsharingFromSentRequest(actual, CONTRACTOR1_ID, REGION1, true);
        checkSingleCarsharingFromSentRequest(actual, CONTRACTOR1_ID, REGION2, false);
        checkSingleCarsharingFromSentRequest(actual, CONTRACTOR3_ID, REGION1, true);
    }
    
    @Test
    @DisplayName("Создание заявки - дубликат заявки - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void create_duplicate() throws Exception {
        //создадим заявку
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, false, true, PHONE, EMAIL, true, true));
        var first = createRequest(uri, json);
        assertThat(first).isNotNull();
        assertThat(joinRequestRepository.count()).isEqualTo(1);
        
        //попытаться продублировать заявку
        Exception ex = incorrectPOST(uri, json, status().isBadRequest());
        assertThat(ex).isInstanceOf(ResponseStatusException.class);
    }
    
    @Test
    @DisplayName("Создание заявки - несуществующий сотрудник - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void create_incorrectEmployee() throws Exception {
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, false, true, PHONE, EMAIL, true, true));
        
        String incorrectUri = COMMON_URI + ORG1_ID + "/" + UUID.randomUUID();
        Exception ex = incorrectPOST(incorrectUri, json, status().isNotFound());
        assertThat(ex).isInstanceOf(EntityNotFoundException.class);
    }
    
    @Test
    @DisplayName("Создание заявки - невалидные данные - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void create_invalidData() throws Exception {
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, false, true, "", null, true, true));
        
        Exception ex = incorrectPOST(uri, json, status().is4xxClientError());
        assertThat(ex).isInstanceOf(MethodArgumentNotValidException.class);
        assertThat(((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors().size()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Создание заявки - несогласие с П144 - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void create_disagreeWithP144() throws Exception {
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, false, true, PHONE, EMAIL, true, false));
        
        Exception ex = incorrectPOST(uri, json, status().isBadRequest());
        assertThat(ex).isInstanceOf(ResponseStatusException.class);
    }
    
    @Test
    @DisplayName("Создание заявки - несогласие с политикой обработки перс.данных - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void create_disagreeWithPrivatePolicy() throws Exception {
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, false, true, PHONE, EMAIL, false, true));
        
        Exception ex = incorrectPOST(uri, json, status().isBadRequest());
        assertThat(ex).isInstanceOf(ResponseStatusException.class);
    }
    
    @Test
    @DisplayName("Создание заявки - не совпадает количество каршерингов - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void create_incorrectCarsharings_qnt() throws Exception {
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        // получим dto и удалим один подключаемый каршеринг
        NewCarsharingJoinRequestDTO joinRequestDTO = getBlankAndCreateRequestDto(
                uri, true, false, true, PHONE, EMAIL, false, true);
        var itemC1R1 =
                filterContractorJoinDtoFromSet(joinRequestDTO.getContractors(), CONTRACTOR1_ID, REGION1);
        joinRequestDTO.getContractors().remove(itemC1R1);
        
        String json = objectMapper.writeValueAsString(joinRequestDTO);
        Exception ex = incorrectPOST(uri, json, status().isBadRequest());
        assertThat(ex).isInstanceOf(ResponseStatusException.class);
    }
    
    @Test
    @DisplayName("Создание заявки - некорректные каршеринги - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void create_incorrectCarsharings_id() throws Exception {
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        // получим dto и отредактируем один подключаемый каршеринг
        NewCarsharingJoinRequestDTO joinRequestDTO = getBlankAndCreateRequestDto(
                uri, true, false, true, PHONE, EMAIL, false, true);
        var itemC1R1 =
                filterContractorJoinDtoFromSet(joinRequestDTO.getContractors(), CONTRACTOR1_ID, REGION1);
        itemC1R1.setRegion(REGION2);
        itemC1R1.getContractor().setId(UUID.randomUUID());
        
        String json = objectMapper.writeValueAsString(joinRequestDTO);
        Exception ex = incorrectPOST(uri, json, status().isBadRequest());
        assertThat(ex).isInstanceOf(ResponseStatusException.class);
    }
    
    @Test
    @DisplayName("Создание заявки - не выбран ни отдин каршеринг - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void create_excludedAllCarsharings() throws Exception {
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, false, false, false, PHONE, EMAIL, true, true));
        
        Exception ex = incorrectPOST(uri, json, status().isBadRequest());
        assertThat(ex).isInstanceOf(ResponseStatusException.class);
    }
    
    @Test
    @DisplayName("Получение созданной заявки - успех")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void getCreatedOrBlank_getCreatedTest() throws Exception {
        //Создадим заявку. Подключаем в регионе 1: КА1 Р2
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, false, true, false, PHONE, EMAIL, true, true));
        var created = createRequest(uri, json);
        var actual = correctGET(uri);
        
        assertThat(actual.getId()).isEqualTo(created.getId());
        assertThat(actual.getPhone()).isEqualTo(created.getPhone());
        assertThat(actual.getEmail()).isEqualTo(created.getEmail());
        assertThat(actual.getEmployee().getId()).isEqualTo(created.getEmployee().getId());
        assertThat(actual.getHumanReadableId()).isEqualTo(created.getHumanReadableId());
        assertThat(actual.getContractors().size()).isEqualTo(created.getContractors().size());
        // сравнить данные по подключению
        checkReadRequestItemByCreated(actual, created, CONTRACTOR1_ID, REGION1);
        checkReadRequestItemByCreated(actual, created, CONTRACTOR1_ID, REGION2);
        checkReadRequestItemByCreated(actual, created, CONTRACTOR3_ID, REGION1);
    }
    
    @Test
    @DisplayName("Редактирование созданной заявки - успех")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void update_success() throws Exception {
        // создадим заявку. Подключаем в регионе 1: КА1 Р1, КА3 Р1
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, false, true, PHONE, EMAIL, true, true));
        var createdDto = createRequest(uri, json);
        UUID joinRequestId = createdDto.getId();
        
        // сформируем запрос и отредактируем заявку
        String updateUri = COMMON_URI + "/" + joinRequestId;
        var updatedDto = getUpdateRequestDtoFromCreated(
                createdDto, false, true, false,
                "Updated Phone", "Updated Email");
        var updateJson = objectMapper.writeValueAsString(updatedDto);
        
        mockMvc.perform(put(updateUri)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(updateJson)
                                .characterEncoding("UTF-8"))
               .andExpect(status().isOk());
        
        assertThat(joinRequestRepository.count()).isEqualTo(1);
        CarsharingJoinRequest actual = joinRequestRepository.findAll().get(0);
        
        assertThat(actual.getId()).isEqualTo(createdDto.getId());
        assertThat(actual.getPhone()).isNotEqualTo(createdDto.getPhone());
        assertThat(actual.getPhone()).isEqualTo(updatedDto.getPhone());
        assertThat(actual.getEmail()).isNotEqualTo(createdDto.getEmail());
        assertThat(actual.getEmail()).isEqualTo(updatedDto.getEmail());
        assertThat(actual.getEmployee().getId()).isEqualTo(createdDto.getEmployee().getId());
        assertThat(actual.getHumanReadableId()).isEqualTo(createdDto.getHumanReadableId());
        assertThat(actual.getContractors().size()).isEqualTo(updatedDto.getContractors().size());
        
        // сравнить данные по подключению
        checkRequestFromDbItemByStatus(actual.getContractors(), updatedDto.getContractors(),
                                       false, CONTRACTOR1_ID, REGION1);
        checkRequestFromDbItemByStatus(actual.getContractors(), updatedDto.getContractors(),
                                       true, CONTRACTOR1_ID, REGION2);
        checkRequestFromDbItemByStatus(actual.getContractors(), updatedDto.getContractors(),
                                       false, CONTRACTOR3_ID, REGION1);
    }
    
    @Test
    @DisplayName("Редактирование созданной заявки - несуществующий id заявки - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void update_incorrectJoinRequestId() throws Exception {
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, false, true, PHONE, EMAIL, true, true));
        var createdDto = createRequest(uri, json);
        
        // сформируем запрос с рандомным id заявки и попробуем отредактировать заявку
        String updateUri = COMMON_URI + "/" + UUID.randomUUID();
        var updatedDto = getUpdateRequestDtoFromCreated(
                createdDto, false, false, false, "Updated Phone", "Updated Email");
        var updateJson = objectMapper.writeValueAsString(updatedDto);
        
        var ex = incorrectPUT(updateUri, updateJson, status().isNotFound());
        assertThat(ex).isInstanceOf(EntityNotFoundException.class);
    }
    
    @Test
    @DisplayName("Редактирование заявки - не выбран ни отдин каршеринг - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void update_excludedAllCarsharings() throws Exception {
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, false, true, PHONE, EMAIL, true, true));
        var createdDto = createRequest(uri, json);
        UUID joinRequestId = createdDto.getId();
        
        // сформируем запрос, в котором не выбран ни отдин каршеринг, и попробуем отредактировать заявку
        String updateUri = COMMON_URI + "/" + joinRequestId;
        var updatedDto = getUpdateRequestDtoFromCreated(
                createdDto, false, false, false,
                "Updated Phone", "Updated Email");
        var updateJson = objectMapper.writeValueAsString(updatedDto);
        
        var ex = incorrectPUT(updateUri, updateJson, status().isBadRequest());
        assertThat(ex).isInstanceOf(ResponseStatusException.class);
    }
    
    @Test
    @DisplayName("Редактирование заявки - некорректный статус заявки - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void update_incorrectJoinRequestStatus() throws Exception {
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, false, true, PHONE, EMAIL, true, true));
        var createdDto = createRequest(uri, json);
        UUID joinRequestId = createdDto.getId();
        
        // заменим статус заявки в БД
        assertThat(joinRequestRepository.count()).isEqualTo(1);
        var joinRequest = joinRequestRepository.findAll().get(0);
        joinRequest.setRequestStatus(DONE);
        joinRequestRepository.save(joinRequest);
        
        // сформируем корректный запрос и попробуем отредактировать заявку
        String updateUri = COMMON_URI + "/" + joinRequestId;
        var updatedDto = getUpdateRequestDtoFromCreated(
                createdDto, true, false, true,
                "Updated Phone", "Updated Email");
        var updateJson = objectMapper.writeValueAsString(updatedDto);
        
        var ex = incorrectPUT(updateUri, updateJson, status().isBadRequest());
        assertThat(ex).isInstanceOf(ResponseStatusException.class);
    }
    
    @Test
    @DisplayName("Редактирование заявки - не совпадает количество каршерингов - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void update_incorrectCarsharings_qnt() throws Exception {
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, false, true, PHONE, EMAIL, true, true));
        var createdDto = createRequest(uri, json);
        UUID joinRequestId = createdDto.getId();
        
        // сформируем запрос, в котором не совпадает количество каршерингов, и попробуем отредактировать заявку
        String updateUri = COMMON_URI + "/" + joinRequestId;
        var updatedDto = getUpdateRequestDtoFromCreated(
                createdDto, false, false, false,
                "Updated Phone", "Updated Email");
        // удалим каршеринг КА1 Р1 из DTO
        Set<ContractorAndJoinStatusDTO> contractorAndJoinStatuses = updatedDto.getContractors();
        var itemC1R1 = filterContractorJoinDtoFromSet(contractorAndJoinStatuses, CONTRACTOR1_ID, REGION1);
        contractorAndJoinStatuses.remove(itemC1R1);
        var updateJson = objectMapper.writeValueAsString(updatedDto);
        
        var ex = incorrectPUT(updateUri, updateJson, status().isBadRequest());
        assertThat(ex).isInstanceOf(ResponseStatusException.class);
    }
    
    @Test
    @DisplayName("Редактирование заявки - некорректные каршеринги - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void update_incorrectCarsharings_id() throws Exception {
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, false, true, PHONE, EMAIL, true, true));
        var createdDto = createRequest(uri, json);
        UUID joinRequestId = createdDto.getId();
        
        // заменим каршеринг КА1 Р2 на КА3 Р2 в заявке в БД
        assertThat(joinRequestRepository.count()).isEqualTo(1);
        var joinRequest = joinRequestRepository.findAll().get(0);
        var contractorsAndStatuses = joinRequest.getContractors();
        var itemC1R1 = filterContractorJoinFromSet(contractorsAndStatuses, CONTRACTOR1_ID, REGION2);
        itemC1R1.setContractor(contractor3);
        joinRequestRepository.save(joinRequest);
        
        // сформируем корректный запрос и попробуем отредактировать заявку
        String updateUri = COMMON_URI + "/" + joinRequestId;
        var updatedDto = getUpdateRequestDtoFromCreated(
                createdDto, true, false, true,
                "Updated Phone", "Updated Email");
        var updateJson = objectMapper.writeValueAsString(updatedDto);
        
        var ex = incorrectPUT(updateUri, updateJson, status().isBadRequest());
        assertThat(ex).isInstanceOf(ResponseStatusException.class);
    }
    
    @Test
    @DisplayName("Редактирование заявки - невалидные данные - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void update_invalidData() throws Exception {
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, false, true, PHONE, EMAIL, true, true));
        var createdDto = createRequest(uri, json);
        UUID joinRequestId = createdDto.getId();
        
        // сформируем запрос c невалидным DTO и попробуем отредактировать заявку
        String updateUri = COMMON_URI + "/" + joinRequestId;
        var updatedDto = getUpdateRequestDtoFromCreated(
                createdDto, true, false, true,
                null, "");
        var updateJson = objectMapper.writeValueAsString(updatedDto);
        
        var ex = incorrectPUT(updateUri, updateJson, status().is4xxClientError());
        assertThat(ex).isInstanceOf(MethodArgumentNotValidException.class);
        assertThat(((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors().size()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Удаление заявки - успех")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void delete_success() throws Exception {
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, false, true, PHONE, EMAIL, true, true));
        var createdDto = createRequest(uri, json);
        UUID joinRequestId = createdDto.getId();
        assertThat(joinRequestRepository.count()).isEqualTo(1);
        
        String deleteUri = COMMON_URI + "/" + joinRequestId;
        mockMvc.perform(delete(deleteUri)).andExpect(status().isOk());
        
        assertThat(joinRequestRepository.count()).isZero();
    }
    
    @Test
    @DisplayName("Удаление созданной заявки - несуществующий id заявки - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void delete_incorrectJoinRequestId() throws Exception {
        // сформируем некорректный запрос и попробуем удалить заявку
        String deleteUri = COMMON_URI + "/" + UUID.randomUUID();
        var ex = mockMvc.perform(delete(deleteUri)).andExpect(status().isNotFound()).andReturn().getResolvedException();
        assertThat(ex).isInstanceOf(EntityNotFoundException.class);
    }
    
    @Test
    @DisplayName("Обработка заявки")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void process() throws Exception {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        authentication = new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(USER1_ID_STR).build());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // создать заявку на подключение с выбором каршерингов: O1 -> C1 R1, C3 R1
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, false, true, PHONE, EMAIL, true, true));
        var createdDto = createRequest(uri, json);
        UUID joinRequestId = createdDto.getId();
        
        String processUri = COMMON_PROCESS_URI + ORG1_ID + "/" + joinRequestId;
        // обработать заявку так, что инженер подключил каршерингам O1 так:
        // C1 R1 - выбран и подключен
        // C1 R2 - не выбран, но типа подключен. При обработке заявки эта ситуация должна быть установлена в DECLINE
        // C3 R1 - выбран, но не подключен
        String processJson = getJsonForProcessJoinRequest(createdDto, true, true, false);
        GetCarsharingJoinRequestDTO actual = createRequest(processUri, processJson);
        assertThat(actual).isNotNull();
        assertThat(actual.getRequestStatus()).isEqualTo(DONE);
        
        // убедимся, что был подключен только каршеринг O1 С1 R1 из шести возможных
        assertThat(carsharingRepository.count()).isEqualTo(6);
        checkJoinedEmployeesToCarsharing(CONTRACTOR1_ID, REGION1, ORG1_ID, Set.of(employee1));
        checkNonJoinedEmployeeToCarsharing(CONTRACTOR1_ID, REGION2, ORG1_ID, employee1);
        checkNonJoinedEmployeeToCarsharing(CONTRACTOR3_ID, REGION1, ORG1_ID, employee1);
        checkNonJoinedEmployeeToCarsharing(CONTRACTOR1_ID, REGION1, ORG2_ID, employee1);
        checkNonJoinedEmployeeToCarsharing(CONTRACTOR1_ID, REGION2, ORG2_ID, employee1);
        checkNonJoinedEmployeeToCarsharing(CONTRACTOR3_ID, REGION1, ORG2_ID, employee1);
        
        // подключим сотрудника 2 ко всем каршерингам 1-ого корп.клиента
        String uri2 = COMMON_URI + ORG1_ID + "/" + EMPLOYEE2_ID;
        String json2 = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri2, true, true, true, PHONE2, EMAIL2, true, true));
        var createdDto2 = createRequest(uri2, json2);
        UUID joinRequestId2 = createdDto2.getId();
        // обработать заявку так, что инженер подключил ко всем выбранным каршерингам O1
        String processUri2 = COMMON_PROCESS_URI + ORG1_ID + "/" + joinRequestId2;
        String processJson2 = getJsonForProcessJoinRequest(createdDto2, true, true, true);
        GetCarsharingJoinRequestDTO actual2 = createRequest(processUri2, processJson2);
        assertThat(actual2).isNotNull();
        assertThat(actual2.getRequestStatus()).isEqualTo(DONE);
        
        // подключим сотрудника 3 ко всем каршерингам 2-ого корп.клиента
        String uri3 = COMMON_URI + ORG2_ID + "/" + EMPLOYEE3_ID;
        String json3 = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri3, true, true, true, PHONE2, EMAIL2, true, true));
        var createdDto3 = createRequest(uri3, json3);
        UUID joinRequestId3 = createdDto3.getId();
        // обработать заявку так, что инженер подключил ко всем выбранным каршерингам O2
        String processUri3 = COMMON_PROCESS_URI + ORG2_ID + "/" + joinRequestId3;
        String processJson3 = getJsonForProcessJoinRequest(createdDto3, true, true, true);
        GetCarsharingJoinRequestDTO actual3 = createRequest(processUri3, processJson3);
        assertThat(actual3).isNotNull();
        assertThat(actual3.getRequestStatus()).isEqualTo(DONE);
        
        // убедимся, что были подключены все каршеринги для сотрудника 2
        assertThat(carsharingRepository.count()).isEqualTo(6);
        checkJoinedEmployeesToCarsharing(CONTRACTOR1_ID, REGION1, ORG1_ID, Set.of(employee1, employee2));
        checkJoinedEmployeesToCarsharing(CONTRACTOR1_ID, REGION2, ORG1_ID, Set.of(employee2));
        checkJoinedEmployeesToCarsharing(CONTRACTOR3_ID, REGION1, ORG1_ID, Set.of(employee2));
        checkJoinedEmployeesToCarsharing(CONTRACTOR1_ID, REGION1, ORG2_ID, Set.of(employee3));
        checkJoinedEmployeesToCarsharing(CONTRACTOR1_ID, REGION2, ORG2_ID, Set.of(employee3));
        checkJoinedEmployeesToCarsharing(CONTRACTOR3_ID, REGION1, ORG2_ID, Set.of(employee3));
    }
    
    @Test
    @DisplayName("Обработка заявки - несуществующий id заявки - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void process_incorrectJoinRequestId() throws Exception {
        // создать заявку на подключение
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, true, true, PHONE, EMAIL, true, true));
        var createdDto = createRequest(uri, json);
        
        // попытаться обработать несуществующую заявку
        String incorrectUri = COMMON_PROCESS_URI + ORG1_ID + "/" + UUID.randomUUID();
        String processJson = getJsonForProcessJoinRequest(createdDto, true, true, true);
        Exception ex = incorrectPOST(incorrectUri, processJson, status().isNotFound());
        assertThat(ex).isInstanceOf(EntityNotFoundException.class);
    }
    
    @Test
    @DisplayName("Обработка заявки - некорректный статус заявки - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void process_incorrectJoinRequestStatus() throws Exception {
        // создать заявку на подключение
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, true, true, PHONE, EMAIL, true, true));
        var createdDto = createRequest(uri, json);
        UUID joinRequestId = createdDto.getId();
        
        // заменим статус заявки в БД
        assertThat(joinRequestRepository.count()).isEqualTo(1);
        var joinRequest = joinRequestRepository.findAll().get(0);
        joinRequest.setRequestStatus(DONE);
        joinRequestRepository.save(joinRequest);
        
        // попытаться обработать заявку
        String processUri = COMMON_PROCESS_URI + ORG1_ID + "/" + joinRequestId;
        String processJson = getJsonForProcessJoinRequest(createdDto, true, true, true);
        Exception ex = incorrectPOST(processUri, processJson, status().isBadRequest());
        assertThat(ex).isInstanceOf(ResponseStatusException.class);
    }
    
    @Test
    @DisplayName("Обработка заявки - не совпадает количество каршерингов - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void process_incorrectCarsharings_qnt() throws Exception {
        // создать заявку на подключение
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, true, true, PHONE, EMAIL, true, true));
        var createdDto = createRequest(uri, json);
        UUID joinRequestId = createdDto.getId();
        
        // удалим каршеринг КА1 Р1 из DTO
        Set<GetContractorAndJoinStatusDTO> contractorAndJoinStatuses = createdDto.getContractors();
        var itemC1R1 = filterGetContractorJoinDtoFromSet(contractorAndJoinStatuses, CONTRACTOR1_ID, REGION1);
        contractorAndJoinStatuses.remove(itemC1R1);
        
        // попытаться обработать заявку
        String processUri = COMMON_PROCESS_URI + ORG1_ID + "/" + joinRequestId;
        String processJson = getJsonForProcessJoinRequest(createdDto, null, true, true);
        Exception ex = incorrectPOST(processUri, processJson, status().isBadRequest());
        assertThat(ex).isInstanceOf(ResponseStatusException.class);
    }
    
    @Test
    @DisplayName("Обработка заявки - некорректные каршеринги - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void process_incorrectCarsharings_id() throws Exception {
        // создать заявку на подключение
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, true, true, PHONE, EMAIL, true, true));
        var createdDto = createRequest(uri, json);
        UUID joinRequestId = createdDto.getId();
        
        // заменим каршеринг КА1 Р2 на КА3 Р2 в заявке в БД
        assertThat(joinRequestRepository.count()).isEqualTo(1);
        var joinRequest = joinRequestRepository.findAll().get(0);
        var contractorsAndStatuses = joinRequest.getContractors();
        var itemC1R1 = filterContractorJoinFromSet(contractorsAndStatuses, CONTRACTOR1_ID, REGION2);
        itemC1R1.setContractor(contractor3);
        joinRequestRepository.save(joinRequest);
        
        // попытаться обработать заявку
        String processUri = COMMON_PROCESS_URI + ORG1_ID + "/" + joinRequestId;
        String processJson = getJsonForProcessJoinRequest(createdDto, true, true, true);
        Exception ex = incorrectPOST(processUri, processJson, status().isBadRequest());
        assertThat(ex).isInstanceOf(ResponseStatusException.class);
    }
    
    @Test
    @DisplayName("Обработка заявки - невалидные данные - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void process_invalidJson() throws Exception {
        // создать заявку на подключение
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, true, true, PHONE, EMAIL, true, true));
        var createdDto = createRequest(uri, json);
        UUID joinRequestId = createdDto.getId();
        
        // создать невалидный json и попытаться обработать заявку
        String processUri = COMMON_PROCESS_URI + ORG1_ID + "/" + joinRequestId;
        Set<ProcessedContractorAndJoinStatusDTO> invalidDtos = new HashSet<>(3);
        for (int i = 0; i < 2; i++) {
            invalidDtos.add(new ProcessedContractorAndJoinStatusDTO());
        }
        invalidDtos.add(ProcessedContractorAndJoinStatusDTO.builder()
                                                           .joinStatus(JOINED)
                                                           .region("")
                                                           .contractor(ContractorDTO.builder()
                                                                                    .name("Contractor")
                                                                                    .build())
                                                           .build());
        String processJson = objectMapper.writeValueAsString(invalidDtos);
        Exception ex = incorrectPOST(processUri, processJson, status().is4xxClientError());
        assertThat(ex).isInstanceOf(ConstraintViolationException.class);
        assertThat(((ConstraintViolationException) ex).getConstraintViolations().size()).isEqualTo(8);
    }
    
    @Test
    @DisplayName("Получение заявки по id - успех")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void getById() throws Exception {
        // создать заявку на подключение
        String uri = COMMON_URI + ORG1_ID + "/" + EMPLOYEE1_ID;
        String json = objectMapper.writeValueAsString(getBlankAndCreateRequestDto(
                uri, true, true, true, PHONE, EMAIL, true, true));
        var createdDto = createRequest(uri, json);
        UUID joinRequestId = createdDto.getId();
        
        // получить ее по id
        String getUri = COMMON_PROCESS_URI + joinRequestId;
        GetCarsharingJoinRequestDTO actual = correctGET(getUri);
        assertThat(actual).isNotNull();
        assertThat(actual.getEmail()).isEqualTo(EMAIL);
        assertThat(actual.getPhone()).isEqualTo(PHONE);
        assertThat(actual.getRequestStatus()).isEqualTo(UNDER_CONSIDERATION);
        assertThat(actual.getId()).isEqualTo(createdDto.getId());
        assertThat(actual.getEmployee().getId()).isEqualTo(createdDto.getEmployee().getId());
        assertThat(actual.getHumanReadableId()).isEqualTo(createdDto.getHumanReadableId());
        checkReadRequestItemByCreated(actual, createdDto, CONTRACTOR1_ID, REGION1);
        checkReadRequestItemByCreated(actual, createdDto, CONTRACTOR1_ID, REGION2);
        checkReadRequestItemByCreated(actual, createdDto, CONTRACTOR3_ID, REGION1);
    }
    
    @Test
    @DisplayName("Получение заявки по id - несуществующая заявка - исключение")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void getById_incorrectId() throws Exception {
        // попытаться получить несуществующую заявку
        String incorrectUri = COMMON_PROCESS_URI + UUID.randomUUID();
        Exception ex = incorrectGET(incorrectUri, status().isNotFound());
        assertThat(ex).isInstanceOf(EntityNotFoundException.class);
    }
    
    @Test
    @DisplayName("Получение несогласованных заявок")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void getAllAwaitingRequests() throws Exception {
        List<CarsharingJoinRequest> awaitingRequests = createGetAllRequestsByStatusTestData()
                .stream()
                .filter(r -> r.getRequestStatus().equals(UNDER_CONSIDERATION))
                .collect(Collectors.toList());
        
        String uri = COMMON_PROCESS_URI + "awaiting";
        List<GetCarsharingJoinRequestShortDTO> actual = correctAllGET(uri);
        
        assertThat(actual.size()).isEqualTo(awaitingRequests.size());
    }
    
    @Test
    @DisplayName("Получение обработанных заявок")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void getAllDoneRequests() throws Exception {
        List<CarsharingJoinRequest> doneRequests = createGetAllRequestsByStatusTestData()
                .stream()
                .filter(r -> r.getRequestStatus().equals(DONE))
                .toList();
        
        String uri = COMMON_PROCESS_URI + "done";
        List<GetCarsharingJoinRequestShortDTO> actual = correctAllGET(uri);
        
        assertThat(actual.size()).isEqualTo(doneRequests.size());
    }
    
    @Test
    @DisplayName("Получение отклоненных заявок")
    @WithMockUser(username = USER1_ID_STR, roles = "GUEST")
    void getAllDeclinedRequests() throws Exception {
        List<CarsharingJoinRequest> cancelledRequests = createGetAllRequestsByStatusTestData()
                .stream()
                .filter(r -> r.getRequestStatus().equals(CANCELLED))
                .toList();
        
        String uri = COMMON_PROCESS_URI + "cancelled";
        List<GetCarsharingJoinRequestShortDTO> actual = correctAllGET(uri);
        
        assertThat(actual.size()).isEqualTo(cancelledRequests.size());
    }
    
    //todo тесты на получение по статусу
    
    /**
     * Получить конкретный GetContractorAndJoinStatusDTO из набора
     *
     * @param set Set<GetContractorAndJoinStatusDTO>
     * @param contractorId ID контрагента
     * @param region регион
     *
     * @return конкретный GetContractorAndJoinStatusDTO
     */
    private GetContractorAndJoinStatusDTO filterGetContractorJoinDtoFromSet(
            Set<GetContractorAndJoinStatusDTO> set,
            UUID contractorId, String region
                                                                           ) {
        var filtered = set.stream()
                          .filter(c -> c.getContractor().getId().equals(contractorId) &&
                                       c.getRegion().equals(region)).toList();
        assertThat(filtered.size()).isEqualTo(1);
        return filtered.get(0);
    }
    
    /**
     * Получить конкретный ContractorAndJoinStatusDTO из набора
     *
     * @param set Set<ContractorAndJoinStatusDTO>
     * @param contractorId ID контрагента
     * @param region регион
     *
     * @return конкретный ContractorAndJoinStatusDTO
     */
    private ContractorAndJoinStatusDTO filterContractorJoinDtoFromSet(
            Set<ContractorAndJoinStatusDTO> set, UUID contractorId,
            String region
                                                                     ) {
        var filtered = set.stream()
                          .filter(c -> c.getContractor().getId().equals(contractorId) &&
                                       c.getRegion().equals(region)).collect(Collectors.toList());
        assertThat(filtered.size()).isEqualTo(1);
        return filtered.get(0);
    }
    
    /**
     * Получить конкретный ContractorAndJoinStatus из набора
     *
     * @param set Set<ContractorAndJoinStatus>
     * @param contractorId ID контрагента
     * @param region регион
     *
     * @return конкретный ContractorAndJoinStatus
     */
    private ContractorAndJoinStatus filterContractorJoinFromSet(
            Set<ContractorAndJoinStatus> set, UUID contractorId,
            String region
                                                               ) {
        var filtered = set.stream()
                          .filter(c -> c.getContractor().getId().equals(contractorId) &&
                                       c.getRegion().equals(region)).collect(Collectors.toList());
        assertThat(filtered.size()).isEqualTo(1);
        return filtered.get(0);
    }
    
    /**
     * Получить конкретный ProcessedContractorAndJoinStatusDTO из набора
     *
     * @param dtos Set<ProcessedContractorAndJoinStatusDTO>
     * @param contractorId ID контрагента
     * @param region регион
     *
     * @return конкретный ProcessedContractorAndJoinStatusDTO
     */
    private ProcessedContractorAndJoinStatusDTO filterProcessedContractorJoinFromSet(
            Set<ProcessedContractorAndJoinStatusDTO> dtos, UUID contractorId, String region
                                                                                    ) {
        var filtered = dtos.stream()
                           .filter(c -> c.getContractor().getId().equals(contractorId) &&
                                        c.getRegion().equals(region)).collect(Collectors.toList());
        assertThat(filtered.size()).isEqualTo(1);
        return filtered.get(0);
    }
    
    /**
     * Проверить ответ get-запроса
     *
     * @param actual List<CarsharingJoinAndCalculatedDTO> - ответ контроллера на запрос
     * @param contractorId ID контрагента
     * @param region регион
     * @param isJoined признак подключения сотрудника, указанного в uri запроса
     * @param calculatedDto CalculatedDto - запрос
     */
    private void checkActualByCalcDto(
            List<CarsharingJoinAndCalculatedDTO> actual, UUID contractorId, String region,
            CalculatedDto calculatedDto, boolean isJoined
                                     ) {
        List<CarsharingJoinAndCalculatedDTO> singleDtoList =
                actual.stream()
                      .filter(dto -> dto.getContractor().getId().equals(contractorId) &&
                                     dto.getRegion().equals(region))
                      .collect(Collectors.toList());
        assertThat(singleDtoList.size()).isEqualTo(1);
        assertThat(singleDtoList.get(0).getCalculatedData().getCost()).isEqualTo(calculatedDto.getCost());
        assertThat(singleDtoList.get(0).isJoined()).isEqualTo(isJoined);
    }
    
    /**
     * Проверить один элемент из списка каршерингов, полученных в бланке по умолчанию
     *
     * @param actual GetCarsharingJoinRequestDTO
     * @param contractorId ID контрагента
     * @param region регион
     */
    private void checkSingleCarsharingFromBlankRequest(
            GetCarsharingJoinRequestDTO actual, UUID contractorId,
            String region
                                                      ) {
        assertThat(actual.getContractors().stream().filter(c -> c.getContractor().getId().equals(contractorId) &&
                                                                c.getRegion().equals(region)).count()).isEqualTo(1);
    }
    
    /**
     * Проверить один элемент из списка каршерингов, полученных после отправки заявки
     *
     * @param actual GetCarsharingJoinRequestDTO
     * @param contractorId ID контрагента
     * @param region регион
     * @param employeeChoise признак выбора сотрудником корп.каршеринга для подключения
     */
    private void checkSingleCarsharingFromSentRequest(
            GetCarsharingJoinRequestDTO actual, UUID contractorId,
            String region, boolean employeeChoise
                                                     ) {
        var filtered = actual.getContractors()
                             .stream()
                             .filter(c -> c.getContractor().getId().equals(contractorId) &&
                                          c.getRegion().equals(region))
                             .collect(Collectors.toList());
        assertThat(filtered.size()).isEqualTo(1);
        var actualItem = filtered.get(0);
        assertThat(actualItem.isEmployeeChoice()).isEqualTo(employeeChoise);
    }
    
    /**
     * Получить пустой бланк заявки для сотрудника, или заявки GET-запросом
     *
     * @param uri URI
     *
     * @return GetCarsharingJoinRequestDTO
     *
     * @throws Exception ошибка
     */
    private GetCarsharingJoinRequestDTO correctGET(String uri)
            throws Exception {
        var result = mockMvc.perform(get(uri)
                                             .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER")
                                                                               .claim("data_master", true))))
                            .andExpect(status().isOk()).andReturn().getResponse();
        return objectMapper.readValue(
                result.getContentAsString(StandardCharsets.UTF_8), GetCarsharingJoinRequestDTO.class);
    }
    
    /**
     * Получить список заявок со конекретным статусом
     *
     * @param uri URI
     *
     * @return GetCarsharingJoinRequestDTO
     *
     * @throws Exception ошибка
     */
    private List<GetCarsharingJoinRequestShortDTO> correctAllGET(String uri) throws Exception {
        var result = mockMvc.perform(get(uri))
                            .andExpect(status().isOk()).andReturn().getResponse();
        return objectMapper.readValue(result.getContentAsString(StandardCharsets.UTF_8),
                                      new TypeReference<>() {
                                      });
    }
    
    /**
     * Создать полезную нагрузку для редактирования заявки с данными: КА1 Р1, КА1 Р2, КА3 Р1
     *
     * @param createdDto GetCarsharingJoinRequestDTO, полученный в результате создания заявки
     * @param choiceC1R1 выбрать или игнорировать КА1 Р1
     * @param choiceC1R2 выбрать или игнорировать КА1 Р2
     * @param choiceC3R1 выбрать или игнорировать КА3 Р1
     * @param phone номер телефона
     * @param email почта
     *
     * @return UpdateCarsharingJoinRequestDTO - полезная загрузка для редактирования заявки
     */
    private UpdateCarsharingJoinRequestDTO getUpdateRequestDtoFromCreated(
            GetCarsharingJoinRequestDTO createdDto, boolean choiceC1R1, boolean choiceC1R2, boolean choiceC3R1,
            String phone, String email
                                                                         ) {
        var joinStatusDtos = joinRequestMapper.getJoinStatusDtosToDtoSet(createdDto.getContractors());
        var contractorJoinDto1 = filterContractorJoinDtoFromSet(joinStatusDtos, CONTRACTOR1_ID, REGION1);
        contractorJoinDto1.setEmployeeChoice(choiceC1R1);
        var contractorJoinDto2 = filterContractorJoinDtoFromSet(joinStatusDtos, CONTRACTOR1_ID, REGION2);
        contractorJoinDto2.setEmployeeChoice(choiceC1R2);
        var contractorJoinDto3 = filterContractorJoinDtoFromSet(joinStatusDtos, CONTRACTOR3_ID, REGION1);
        contractorJoinDto3.setEmployeeChoice(choiceC3R1);
        
        return UpdateCarsharingJoinRequestDTO.builder()
                                             .contractors(joinStatusDtos)
                                             .email(email)
                                             .phone(phone)
                                             .build();
    }
    
    /**
     * Создать полезную нагрузку для создания заявки с данными: КА1 Р1, КА1 Р2, КА3 Р1
     *
     * @param uri URI
     * @param choiceC1R1 выбрать или игнорировать КА1 Р1
     * @param choiceC1R2 выбрать или игнорировать КА1 Р2
     * @param choiceC3R1 выбрать или игнорировать КА3 Р1
     * @param phone номер телефона
     * @param email почта
     * @param personalDataAgree согласие на обраб.перс.данных
     * @param rulesP144Agree согласие с памяткой П-144
     *
     * @return NewCarsharingJoinRequestDTO - полезная загрузка для создания заявки
     */
    private NewCarsharingJoinRequestDTO getBlankAndCreateRequestDto(
            String uri, boolean choiceC1R1, boolean choiceC1R2, boolean choiceC3R1, String phone, String email,
            boolean personalDataAgree, boolean rulesP144Agree
                                                                   ) throws Exception {
        GetCarsharingJoinRequestDTO blankJoinRequestDto = correctGET(uri);
        Set<GetContractorAndJoinStatusDTO> joinStatusGetDtos = blankJoinRequestDto.getContractors();
        Set<ContractorAndJoinStatusDTO> joinStatusDtos = joinRequestMapper.getJoinStatusDtosToDtoSet(joinStatusGetDtos);
        var contractorJoinDto1 = filterContractorJoinDtoFromSet(joinStatusDtos, CONTRACTOR1_ID, REGION1);
        contractorJoinDto1.setEmployeeChoice(choiceC1R1);
        var contractorJoinDto2 = filterContractorJoinDtoFromSet(joinStatusDtos, CONTRACTOR1_ID, REGION2);
        contractorJoinDto2.setEmployeeChoice(choiceC1R2);
        var contractorJoinDto3 = filterContractorJoinDtoFromSet(joinStatusDtos, CONTRACTOR3_ID, REGION1);
        contractorJoinDto3.setEmployeeChoice(choiceC3R1);
        
        return NewCarsharingJoinRequestDTO.builder()
                                          .phone(phone)
                                          .email(email)
                                          .personalDataAgree(personalDataAgree)
                                          .rulesP144Agree(rulesP144Agree)
                                          .contractors(joinStatusDtos)
                                          .build();
    }
    
    /**
     * Получить json для обработки заявки от лица инженера
     *
     * @param createdDto GetCarsharingJoinRequestDTO из ответа при создании заявки
     * @param joinC1R1 подключать ли каршеринг КА1 Р1
     * @param joinC1R2 подключать ли каршеринг КА1 Р2
     * @param joinC3R1 подключать ли каршеринг КА3 Р1
     *
     * @return JSON
     *
     * @throws Exception ошибка
     */
    private String getJsonForProcessJoinRequest(
            GetCarsharingJoinRequestDTO createdDto, Boolean joinC1R1, Boolean joinC1R2, Boolean joinC3R1
                                               )
            throws Exception {
        // создадим DTO подключений, обработанных инженером
        Set<ProcessedContractorAndJoinStatusDTO> processedDtos =
                createdDto.getContractors().stream()
                          .map(j -> ProcessedContractorAndJoinStatusDTO.builder()
                                                                       .contractor(j.getContractor())
                                                                       .region(j.getRegion())
                                                                       .joinStatus(DECLINED)
                                                                       .build())
                          .collect(Collectors.toSet());
        // откорректируем в зависимости от выбора в сигнатуре метода
        if (joinC1R1 != null) {
            var processC1R1 = filterProcessedContractorJoinFromSet(processedDtos, CONTRACTOR1_ID, REGION1);
            processC1R1.setJoinStatus(joinC1R1 ? JOINED : DECLINED);
        }
        if (joinC1R2 != null) {
            var processC1R2 = filterProcessedContractorJoinFromSet(processedDtos, CONTRACTOR1_ID, REGION2);
            processC1R2.setJoinStatus(joinC1R2 ? JOINED : DECLINED);
        }
        if (joinC3R1 != null) {
            var processC3R1 = filterProcessedContractorJoinFromSet(processedDtos, CONTRACTOR3_ID, REGION1);
            processC3R1.setJoinStatus(joinC3R1 ? JOINED : DECLINED);
        }
        return objectMapper.writeValueAsString(processedDtos);
    }
    
    /**
     * Создать заявку
     *
     * @param uri URI
     * @param json objectMapper.writeValueAsString(newJoinRequestDto)
     *
     * @return GetCarsharingJoinRequestDTO
     *
     * @throws Exception ошибка
     */
    private GetCarsharingJoinRequestDTO createRequest(String uri, String json) throws Exception {
        var result = mockMvc.perform(post(uri)
                                             .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER").claim("data_master",
                                                                                                                                    true)))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE)
                                             .content(json)
                                             .characterEncoding("UTF-8"))
                            .andExpect(status().isOk()).andReturn().getResponse();
        
        return objectMapper.readValue(
                result.getContentAsString(StandardCharsets.UTF_8), GetCarsharingJoinRequestDTO.class);
    }
    
    /**
     * Проверить совпадение возвращаемой при создании заявки с заявкой из get-запроса
     *
     * @param read заявка из get-запроса
     * @param created возвращаемая при создании заявка
     * @param contractorId ID контрагента
     * @param region регион
     */
    private void checkReadRequestItemByCreated(
            GetCarsharingJoinRequestDTO read, GetCarsharingJoinRequestDTO created,
            UUID contractorId, String region
                                              ) {
        var readItem = filterGetContractorJoinDtoFromSet(read.getContractors(), contractorId, region);
        var createdItem = filterGetContractorJoinDtoFromSet(created.getContractors(), contractorId, region);
        assertThat(readItem.getContractor().getId()).isEqualTo(createdItem.getContractor().getId());
        assertThat(readItem.getRegion()).isEqualTo(createdItem.getRegion());
        assertThat(readItem.getJoinStatus()).isEqualTo(createdItem.getJoinStatus());
    }
    
    /**
     * Проверить совпадение данных заявки из БД с данными put-запроса
     *
     * @param contractorsFromDb Set<ContractorAndJoinStatusDTO> из БД
     * @param employeeChoice выбран ли каршеринг для подключения
     * @param contractorId ID контрагента
     * @param region регион
     */
    private void checkRequestFromDbItemByStatus(
            Set<ContractorAndJoinStatus> contractorsFromDb, Set<ContractorAndJoinStatusDTO> updatedDto,
            boolean employeeChoice, UUID contractorId, String region
                                               ) {
        var fromDb = filterContractorJoinFromSet(contractorsFromDb, contractorId, region);
        var updated = filterContractorJoinDtoFromSet(updatedDto, contractorId, region);
        assertThat(fromDb.getContractor().getId()).isEqualTo(updated.getContractor().getId());
        assertThat(fromDb.getRegion()).isEqualTo(updated.getRegion());
        assertThat(fromDb.isEmployeeChoice()).isEqualTo(employeeChoice);
    }
    
    /**
     * Проверить, что список сотрудников подключен к выбранному корп.каршерингу
     *
     * @param contractorId ID контрагента
     * @param region реигон
     * @param organizationID ID корп.клиента
     * @param employees список сотрудников
     */
    private void checkJoinedEmployeesToCarsharing(
            UUID contractorId, String region, UUID organizationID,
            Set<Employee> employees
                                                 ) {
        CorporateCarsharing carsharing =
                carsharingRepository.findByContractContractorIdAndContractRegionAndOrganizationIdAndActive(
                        contractorId, region, organizationID, true).orElseThrow();
        assertThat(carsharing.getJoinedEmployees().containsAll(employees)).isTrue();
    }
    
    /**
     * Проверить, что сотрудник <b>не подключен</b> к выбранному корп.каршерингу
     *
     * @param contractorId ID контрагента
     * @param region реигон
     * @param organizationID ID корп.клиента
     * @param employee сотрудник
     */
    private void checkNonJoinedEmployeeToCarsharing(
            UUID contractorId, String region, UUID organizationID,
            Employee employee
                                                   ) {
        CorporateCarsharing carsharing =
                carsharingRepository.findByContractContractorIdAndContractRegionAndOrganizationIdAndActive(
                        contractorId, region, organizationID, true).orElseThrow();
        assertThat(carsharing.getJoinedEmployees().contains(employee)).isFalse();
    }
    
    /**
     * Получить исключение при некорректном GET-запросе
     *
     * @param uri URI
     * @param status ResultMatcher
     *
     * @return Exception
     *
     * @throws Exception ошибка
     */
    private Exception incorrectGET(String uri, ResultMatcher status) throws Exception {
        return mockMvc.perform(get(uri)
                                       .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER")
                                                                         .claim("data_master", true))))
                      .andExpect(status).andReturn().getResolvedException();
    }
    
    /**
     * Поймать исключение при некорректном POST-запросе
     *
     * @param uri URI
     * @param json JSON
     * @param status ResultMatcher
     *
     * @return Exception ошибка
     *
     * @throws Exception ошибка
     */
    private Exception incorrectPOST(String uri, String json, ResultMatcher status) throws Exception {
        return mockMvc.perform(post(uri).contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER")
                                                                          .claim("data_master", true)))
                                        .content(json)
                                        .characterEncoding("UTF-8"))
                      .andExpect(status).andReturn().getResolvedException();
    }
    
    /**
     * Поймать исключение при некорректном PUT-запросе
     *
     * @param uri URI
     * @param json JSON
     * @param status ResultMatcher
     *
     * @return Exception
     *
     * @throws Exception ошибка
     */
    private Exception incorrectPUT(String uri, String json, ResultMatcher status) throws Exception {
        return mockMvc.perform(put(uri).contentType(MediaType.APPLICATION_JSON_VALUE)
                                       .content(json)
                                       .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER")
                                                                         .claim("data_master", true)))
                                       .characterEncoding("UTF-8"))
                      .andExpect(status).andReturn().getResolvedException();
    }
    
    private Organization createOrganization(UUID id, Long digitId) {
        return Organization.builder()
                           .id(id)
                           .digitId(digitId)
                           .active(true)
                           .build();
    }
    
    private Employee createEmployee(UUID id, Department department, UUID userId) {
        return Employee.builder()
                       .id(id)
                       .firstName("FirstName")
                       .lastName("LastName")
                       .department(department)
                       .userId(userId)
                       .personnelNumber((int) (Math.random() * 100) + "-" + (int) (Math.random() * 100))
                       .active(true)
                       .build();
    }
    
    private Contractor createContractor(UUID id, String name) {
        return Contractor.builder()
                         .id(id)
                         .name(name)
                         .msrn("58161615")
                         .tin("566516")
                         .deleted(false)
                         .build();
    }
    
    private CarsharingContract createContract(UUID id, UUID contractorId, String region, Set<UUID> orgIds) {
        return CarsharingContract.builder()
                                 .id(id)
                                 .contractorId(contractorId)
                                 .region(region)
                                 .organizations(orgIds)
                                 .transportType(CARSHARING)
                                 .active(true)
                                 .deleted(false)
                                 .build();
    }
    
    private CorporateCarsharing createCarsharing(CarsharingContract contract, UUID orgId, Set<Employee> employees) {
        return CorporateCarsharing.builder()
                                  .contract(contract)
                                  .organizationId(orgId)
                                  .joinedEmployees(employees)
                                  .active(true)
                                  .build();
    }
    
    private CarsharingTariff createTariff(UUID id, String region, UUID contractId) {
        return CarsharingTariff.builder()
                               .id(id)
                               .transportType(CARSHARING_ENUM)
                               .humanReadableId(
                                       "TF-000" + (int) (Math.random() * 10) + "-" + (int) (Math.random() * 1000))
                               .region(region)
                               .contractId(contractId)
                               .rideCostPerKm(1)
                               .rideCostPerMin(2)
                               .waitCostPerMin(3)
                               .departmentId(UUID.randomUUID())
                               .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                               .transportType(TransportTypeEnum.TAXI)
                               .build();
    }
    
    private CalculatedDto createCalcDto(@NotNull UUID id, @Positive long cost) {
        return CalculatedDto.builder()
                            .id(id)
                            .transportType(transportTypeDto)
                            .cost(cost)
                            .build();
    }
    
    /**
     * Метод для создания выборки для тестирования гет-запросов на получение списков
     *
     * @return List<CarsharingJoinRequest>
     */
    private List<CarsharingJoinRequest> createGetAllRequestsByStatusTestData() {
        CarsharingJoinRequestText text =
                textRepository.save(CarsharingJoinRequestText.builder().organizationId(ORG1_ID).build());
        
        List<CarsharingJoinRequest> result = new ArrayList<>(9);
        ContractorAndJoinStatus joinC1R1 = createJoin(contractor1, REGION1);
        ContractorAndJoinStatus joinC1R2 = createJoin(contractor1, REGION2);
        ContractorAndJoinStatus joinC3R1 = createJoin(contractor3, REGION1);
        
        // создать по 3 заявки с каждым статусом
        result.add(createJoinRequest(CANCELLED, Set.of(joinC1R1), employee1, text));
        result.add(createJoinRequest(CANCELLED, Set.of(joinC1R2), employee2, text));
        result.add(createJoinRequest(CANCELLED, Set.of(joinC3R1), employee3, text));
        result.add(createJoinRequest(UNDER_CONSIDERATION, Set.of(joinC1R1), employee1, text));
        result.add(createJoinRequest(UNDER_CONSIDERATION, Set.of(joinC1R2), employee4, text));
        result.add(createJoinRequest(UNDER_CONSIDERATION, Set.of(joinC3R1), employee5, text));
        result.add(createJoinRequest(DONE, Set.of(joinC1R2, joinC1R1), employee2, text));
        result.add(createJoinRequest(DONE, Set.of(joinC3R1), employee5, text));
        result.add(createJoinRequest(DONE, Set.of(joinC1R1, joinC3R1), employee3, text));
        
        return joinRequestRepository.saveAll(result);
    }
    
    /**
     * Метод для тестирования гет-запросов на получение списков
     *
     * @param contractor контрагент
     * @param region регион
     *
     * @return ContractorAndJoinStatus
     */
    private ContractorAndJoinStatus createJoin(Contractor contractor, String region) {
        return ContractorAndJoinStatus.builder()
                                      .contractor(contractor)
                                      .region(region)
                                      .joinStatus(JOINED)
                                      .employeeChoice(true)
                                      .build();
    }
    
    /**
     * Метод для тестирования гет-запросов на получение списков
     *
     * @param status статус
     * @param joins контрагенты и статусы их подключения
     * @param employee пользователь
     *
     * @return CarsharingJoinRequest
     */
    private CarsharingJoinRequest createJoinRequest(
            CarsharingJoinRequestStatus status, Set<ContractorAndJoinStatus> joins, Employee employee,
            CarsharingJoinRequestText text
                                                   ) {
        return CarsharingJoinRequest.builder()
                                    .humanReadableId(
                                            "CR-000" + (int) (Math.random() * 10) + "-" + (int) (Math.random() * 100))
                                    .requestStatus(status)
                                    .phone("+7 (917) 324 21 " + (int) (Math.random() * 100))
                                    .email("mail." + (int) (Math.random() * 10000) + "@g.ru")
                                    .contractors(joins)
                                    .creationTime(LocalDateTime.now())
                                    .employee(employee)
                                    .text(text)
                                    .rulesP144Agree(true)
                                    .personalDataAgree(true).build();
    }
    
    private Department createDepartment(UUID id, Organization organization) {
        return Department.builder()
                         .id(id)
                         .organization(organization)
                         .build();
    }
}