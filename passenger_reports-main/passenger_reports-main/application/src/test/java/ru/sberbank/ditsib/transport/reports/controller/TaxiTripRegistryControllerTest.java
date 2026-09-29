package ru.sberbank.ditsib.transport.reports.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripType;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;
import ru.sberbank.ditsib.transport.reports.constants.UploadFileFormats;
import ru.sberbank.ditsib.transport.reports.dao.*;
import ru.sberbank.ditsib.transport.reports.dto.NewTaxiTripRegistryDTO;
import ru.sberbank.ditsib.transport.reports.dto.RegistryPerContractorDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryShortDTO;
import ru.sberbank.ditsib.transport.reports.exception.FileActionsFailsException;
import ru.sberbank.ditsib.transport.reports.exception.IllegalFileException;
import ru.sberbank.ditsib.transport.reports.exception.UnsupportedFileFormatException;
import ru.sberbank.ditsib.transport.reports.model.*;
import ru.sberbank.ditsib.transport.reports.model.excel.CalculatedTripStatus;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistry;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistryString;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRide;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRideKPI;
import ru.sberbank.ditsib.transport.reports.model.tariff.Contract;
import ru.sberbank.ditsib.transport.reports.model.tariff.ContractorDeviationsTariffParams;
import ru.sberbank.ditsib.transport.reports.model.tariff.TaxiTariff;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.SingleTaxiTrip;
import ru.sberbank.ditsib.transport.reports.service.ContractorService;

import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.ConstraintViolationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.ditsib.transport.reports.service.FileService.*;


@SuppressWarnings({ "SpringJavaInjectionPointsAutowiringInspection" })
@AutoConfigureMockMvc
@DisplayName("Тест контроллера импорта реестра поездок на такси от контрагента")
@SpringBootTest
@EmbeddedPostgres
@Transactional
@MockitoBean(types = JwtDecoder.class)
class TaxiTripRegistryControllerTest extends KafkaTest {
    
    private static final String MEDIA_TYPE = MediaType.MULTIPART_FORM_DATA_VALUE;
    
    @Autowired
    private TaxiTripRegistryRepository registryRepository;
    
    @Autowired
    private ContractorService contractorService;
    
    @Autowired
    private ContractorRepository contractorRepository;
    
    @Autowired
    private ContractRepository contractRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private TaxiTariffRepository tariffRepository;
    
    @Autowired
    private RequestRepository requestRepository;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private CoopTaxiTripRepository coopTripRepository;
    
    @Autowired
    private SingleTaxiTripRepository singleTripRepository;
    
    @Autowired
    private SharedRideRepository sharedRideRepository;
    
    @Autowired
    private SharedRequestKpiRepository sharedRequestKpiRepository;
    
    @Autowired
    private OrderKpiRepository orderKpiRepository;
    
    @Autowired
    private WaypointRepository waypointRepository;
    
    @Value("${file.test.source-dir}")
    private String filesSourceRelativePath;
    
    private Contractor contractor;
    
    private Organization org;
    
    private NewTaxiTripRegistryDTO newDto;
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    @BeforeEach
    void init() {
        contractor = Contractor.builder().id(UUID.randomUUID()).name("Contractor").build();
        contractorService.save(contractor);
        var contractorRep = contractorRepository.getReferenceById(contractor.getId());
        
        var contract = Contract.builder()
                               .id(UUID.randomUUID())
                               .contractor(contractorRep)
                               .uvhd("test")
                               .contractNumber("test")
                               .includeVat(true)
                               .sum(1000L)
                               .endDate(LocalDate.now())
                               .startDate(LocalDate.now().minusDays(2))
                               .transportType(TransportTypeEnum.TAXI)
                               .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                               .creationTime(LocalDateTime.now().minusDays(2))
                               .active(true)
                               .userId(UUID.randomUUID())
                               .vatValue(1000)
                               .build();
        contractRepository.save(contract);
        
        newDto = NewTaxiTripRegistryDTO.builder()
                                       .contractorId(contractor.getId())
                                       .date(LocalDate.of(2021, 3, 15))
                                       .build();
    }
    
    @AfterEach
    void clear() {
        registryRepository.deleteAll();
        singleTripRepository.deleteAll();
        coopTripRepository.deleteAll();
        requestRepository.deleteAll();
        waypointRepository.deleteAll();
        tariffRepository.deleteAll();
        organizationRepository.deleteAll();
        orderKpiRepository.deleteAll();
        sharedRideRepository.deleteAll();
        sharedRequestKpiRepository.deleteAll();
        contractorService.delete(contractor);
    }
    
    @AfterAll
    static void deleteTestDirectory() {
        //актуализировать путь при смене в properties
        String relativePath = "target/upload";
        Path folderPath = Paths.get(relativePath).toAbsolutePath().normalize();
        try {
            FileUtils.deleteDirectory(folderPath.toFile());
        } catch (IOException ex) {
            throw new FileActionsFailsException(FileActionsFailsException.FOLDER_DELETE_FORMAT, folderPath.toString());
        }
    }
    
    @DisplayName("Загрузка файла - успех")
    @Test
    @Transactional
    void downloadFile_success() throws Exception {
        importExcelRegistry(newDto, FILE_NAME1, filesSourceRelativePath, mockMvc, objectMapper);
        UUID contractorId = newDto.getContractorId();
        String url = COMMON_URI + "/download/" + contractorId + "/2021" + "/3";
        var response = mockMvc.perform(get(url)
                                               .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))
                                               .contentType(UploadFileFormats.XLSX.getMediaType()))
                              .andExpect(status().isOk())
                              .andReturn().getResponse();
        
        assertThat(response.getHeader("Content-Disposition")).matches("attachment; filename=.*" + FILE_NAME1 + ".*");
        assertThat(response.getContentType())
                .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
    
    @DisplayName("Загрузка файла - не валидные данные")
    @Test
    @WithMockUser(roles = "GUEST")
    @Transactional
    void downloadFile_noValid() throws Exception {
        importExcelRegistry(newDto, FILE_NAME1, filesSourceRelativePath, mockMvc, objectMapper);
        UUID contractorId = newDto.getContractorId();
        String url = COMMON_URI + "/download/" + contractorId + "/2999" + "/13";
        var ex = mockMvc.perform(get(url)
                                         .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))
                                         .contentType(UploadFileFormats.XLSX.getMediaType()))
                        .andExpect(status().isBadRequest()).andReturn().getResolvedException();
        assertThat(ex).isNotNull().isInstanceOf(ConstraintViolationException.class);
        assertThat(((ConstraintViolationException) ex).getConstraintViolations().size()).isEqualTo(2);
    }
    
    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Выгрузка файла - неверная дата - исключение")
    void downloadFileAsResource_incorrectDate_exception() throws Exception {
        importExcelRegistry(newDto, FILE_NAME1, filesSourceRelativePath, mockMvc, objectMapper);
        UUID contractorId = newDto.getContractorId();
        String url = COMMON_URI + "/download/" + contractorId + "/2021" + "/2";
        var exception = mockMvc.perform(get(url)
                                                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))
                                       )
                               .andExpect(status().isNotFound())
                               .andReturn().getResolvedException();
        assertThat(exception).isInstanceOf(EntityNotFoundException.class);
    }
    
    @Test
    @DisplayName("Выгрузка файла - неверный контрагент - исключение")
    void downloadFileAsResource_incorrectContractor_exception() throws Exception {
        importExcelRegistry(newDto, FILE_NAME1, filesSourceRelativePath, mockMvc, objectMapper);
        var contractor_1 = Contractor.builder().id(UUID.randomUUID()).name("Contractor").build();
        contractorService.save(contractor_1);
        UUID contractorId = contractor_1.getId();
        String url = COMMON_URI + "/download/" + contractorId + "/2021" + "/3";
        var exception = mockMvc.perform(get(url)
                                                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
                               .andExpect(status().isNotFound())
                               .andReturn().getResolvedException();
        assertThat(exception).isInstanceOf(EntityNotFoundException.class);
    }
    
    @DisplayName("Загрузка файла - нет файла - исключение")
    @Test
    @Transactional
    void downloadFile_notFile() throws Exception {
        UUID contractorId = newDto.getContractorId();
        String url = COMMON_URI + "/download/" + contractorId + "/2021" + "/3";
        var exception = mockMvc.perform(get(url)
                                                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
                               .andExpect(status().isNotFound())
                               .andReturn().getResolvedException();
        assertThat(exception).isInstanceOf(EntityNotFoundException.class);
    }
    
    @DisplayName("Получение реестра - успех")
    @Test
    @WithMockUser(roles = "GUEST")
    void findRegistry_success() throws Exception {
        Contractor contractor = Contractor.builder().id(UUID.randomUUID()).name("Contractor2").build();
        contractorService.save(contractor);
        var contractorRep = contractorRepository.getReferenceById(contractor.getId());
        
        TaxiTripRegistry registry = TaxiTripRegistry.builder().date(LocalDate.of(2021, 2, 1)).contractor(contractorRep).build();
        registry = registryRepository.save(registry);
        
        String uri = COMMON_URI + "/" + contractor.getId() + "/" + LocalDate.of(2021, 2, 15);
        
        var result = mockMvc.perform(get(uri)
                                             .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
                            .andExpect(status().isOk()).andReturn();
        var actual = objectMapper.readValue(result.getResponse().getContentAsString(), TaxiTripRegistryDTO.class);
        
        assertThat(actual.getId()).isEqualTo(registry.getId());
        assertThat(actual.getContractor().getId()).isEqualTo(registry.getContractor().getId());
        assertThat(actual.getDate().getDayOfMonth()).isEqualTo(1);
        assertThat(actual.getDate().getYear()).isEqualTo(registry.getDate().getYear());
        assertThat(actual.getDate().getMonth()).isEqualTo(registry.getDate().getMonth());
    }
    
    @DisplayName("Получение реестра - реестр не найден - исключение")
    @Test
    void findRegistry_notFound_ex() throws Exception {
        String uri = COMMON_URI + "/" + contractor.getId() + "/" + LocalDate.of(2021, 2, 15);
        Exception ex = getExceptionAfterFindExcelRegistry(uri, status().isNotFound());
        
        assertThat(ex).isInstanceOf(EntityNotFoundException.class);
    }
    
    @DisplayName("Получение реестра - некорректный uri - исключение")
    @Test
    void findRegistry_incorrectUri_ex() throws Exception {
        String incorrectUri = COMMON_URI + "/" + contractor.getId() + "/" + "21-02-15";
        Exception ex = getExceptionAfterFindExcelRegistry(incorrectUri, status().isConflict());
        
        assertThat(ex).isInstanceOf(IllegalStateResponseException.class);
    }
    
    @DisplayName("Получение всех реестров по id контрагента - успех")
    @Test
    void findAllRegistriesById_success() throws Exception {
        
        //для достоверности создадим реестры у другого контрагента
        Contractor contractor4 = Contractor.builder().id(UUID.randomUUID()).name("Contractor4").build();
        contractorService.save(contractor4);
        var contractor = contractorRepository.getReferenceById(contractor4.getId());
        
        String uri = COMMON_URI + "/" + contractor.getId();
        //запишем в порядке убывания даты, чтобы далее проверить сортировку по возрастанию
        TaxiTripRegistry registry1 = registryRepository.save(
                TaxiTripRegistry.builder().date(LocalDate.of(2021, 2, 1)).contractor(contractor).build());
        TaxiTripRegistry registry2 = registryRepository.save(
                TaxiTripRegistry.builder().date(LocalDate.of(2021, 1, 1)).contractor(contractor).build());
        
        assertThat(registryRepository.count()).isEqualTo(2);
        
        var result = mockMvc.perform(get(uri)
                                             .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
                            .andExpect(status().isOk()).andReturn();
        List<TaxiTripRegistryShortDTO> actualList =
                objectMapper.readValue(result.getResponse().getContentAsString(),
                                       new TypeReference<>() {
                                       });
        assertThat(actualList.size()).isEqualTo(2);
        //проверим порядок сортировки по возрастанию даты
        assertThat(actualList.get(1).getDate()).isAfter(actualList.get(0).getDate());
        checkDtoByRegistry(actualList, registry1);
        checkDtoByRegistry(actualList, registry2);
    }
    
    @DisplayName("Получение всех реестров по id контрагента - контрагент не найден - исключение")
    @Test
    void findAllRegistriesById_contractorIsNotFound_exception() throws Exception {
        //ссылка на несуществующего контрагента
        String uri = COMMON_URI + "/" + UUID.randomUUID();
        var exception = mockMvc.perform(get(uri)
                                                .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
                               .andExpect(status().isNotFound()).andReturn().getResolvedException();
        
        assertThat(exception).isInstanceOf(EntityNotFoundException.class);
    }
    
    @DisplayName("Получение всех реестров всех контрагентов - успех")
    @Test
    void findAllRegistries_success() throws Exception {
        //для достоверности создадим реестры у другого контрагента
        Contractor contractor2 = Contractor.builder().id(UUID.randomUUID()).name("Contractor2").build();
        contractorService.save(contractor2);
        var contractor = contractorRepository.getReferenceById(contractor2.getId());
        
        Contractor contractor3 = Contractor.builder().id(UUID.randomUUID()).name("Contractor3").build();
        contractorService.save(contractor3);
        var contractorRep = contractorRepository.getReferenceById(contractor3.getId());
        
        // сохраняем договор
        var contract = Contract.builder()
                               .id(UUID.randomUUID())
                               .contractor(contractor)
                               .uvhd("test")
                               .contractNumber("test")
                               .includeVat(true)
                               .sum(1000L)
                               .endDate(LocalDate.now())
                               .startDate(LocalDate.now().minusDays(2))
                               .transportType(TransportTypeEnum.TAXI)
                               .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                               .creationTime(LocalDateTime.now().minusDays(2))
                               .active(true)
                               .userId(UUID.randomUUID())
                               .vatValue(1000)
                               .build();
        contractRepository.save(contract);
        
        String uri = COMMON_URI + "/all";
        //запишем в порядке убывания даты, чтобы далее проверить сортировку по возрастанию
        TaxiTripRegistry registry1 = registryRepository.save(
                TaxiTripRegistry.builder().date(LocalDate.of(2021, 2, 1)).contractor(contractor).build());
        TaxiTripRegistry registry2 = registryRepository.save(
                TaxiTripRegistry.builder().date(LocalDate.of(2021, 1, 1)).contractor(contractorRep).build());
        
        assertThat(registryRepository.count()).isEqualTo(2);
        
        var result = mockMvc.perform(get(uri)
                                             .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
                            .andExpect(status().isOk()).andReturn();
        Set<RegistryPerContractorDTO> actualSet = objectMapper.readValue(result.getResponse().getContentAsString(),
                                                                         new TypeReference<>() {
                                                                         });
        assertThat(actualSet.size()).isEqualTo(2);
        List<RegistryPerContractorDTO> actualList = new ArrayList<>(actualSet);
        
        //проверка что реестры отсортированы по датам
        for (RegistryPerContractorDTO registry : actualList) {
            if (registry.getTaxiTripRegisters().size() > 1) {
                assertThat(registry.getTaxiTripRegisters().get(1).getDate()).isAfter(registry.getTaxiTripRegisters().get(0).getDate());
            }
        }
    }
    
    
    @DisplayName("Импорт реестра xlsx - успех")
    @Test
    @WithMockUser(roles = "GUEST")
    @Transactional
    void importRegistry_success() throws Exception {
        var actual = importExcelRegistry(newDto, FILE_NAME1, filesSourceRelativePath, mockMvc, objectMapper);
        
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getContractor().getId()).isEqualTo(newDto.getContractorId());
        assertThat(actual.getDate()).isEqualTo(newDto.getDate().withDayOfMonth(1));
        assertThat(actual.getStringsQnt()).isEqualTo(689);
        assertThat(actual.getBlankCells().size()).isEqualTo(631);
        assertThat(actual.getIncorrectTypeCells().size()).isEqualTo(13);
        //проверка БД
        checkActualFromDbIsEqualToActualDto(actual);
    }
    
    @DisplayName("Импорт реестра xlsx - дубликат реестра - исключение")
    @Test
    @WithMockUser(roles = "GUEST")
    @Transactional
    void importRegistry_duplicate_exception() throws Exception {
        var actual = importExcelRegistry(newDto, FILE_NAME1, filesSourceRelativePath, mockMvc, objectMapper);
        Exception ex = getExceptionAfterImportExcelRegistry(newDto, FILE_NAME1, status().isConflict());
        
        assertThat(ex).isInstanceOf(DuplicateDataException.class);
    }
    
    @DisplayName("Импорт реестра xlsx - невалидные данные - исключение")
    @Test
    @WithMockUser(roles = "GUEST")
    @Transactional
    void importRegistry_validationTest_exception() throws Exception {
        var invalidDto = NewTaxiTripRegistryDTO.builder().build();
        var errorCode = status().isBadRequest();
        Exception ex = getExceptionAfterImportExcelRegistry(invalidDto, FILE_NAME1, errorCode);
        
        assertThat(ex).isInstanceOf(MethodArgumentNotValidException.class);
        assertThat(((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors().size()).isEqualTo(2);
    }
    
    @DisplayName("Импорт реестра xlsx - некорректное расширение файла - исключение")
    @Test
    @WithMockUser(roles = "GUEST")
    @Transactional
    void importRegistry_incorrectFileFormat_exception() throws Exception {
        String incorrectFileFormat = FILE_NAME1.replace(".xlsx", "");
        var errorCode = status().isBadRequest();
        Exception ex = getExceptionAfterImportExcelRegistry(newDto, incorrectFileFormat, errorCode);
        
        assertThat(ex).isInstanceOf(IllegalFileException.class);
    }
    
    @DisplayName("Замена реестра xlsx - успех")
    @Test
    @WithMockUser(roles = "GUEST")
    @Transactional
    void replaceRegistry_success() throws Exception {
        var registry = importExcelRegistry(newDto, FILE_NAME1, filesSourceRelativePath, mockMvc, objectMapper);
        String uri = COMMON_URI + "/" + registry.getId();
        MockMultipartFile file = new MockMultipartFile("file", FILE_NAME2, MEDIA_TYPE,
                                                       getFileFromResource(filesSourceRelativePath, FILE_NAME2));
        
        var result = mockMvc.perform(multipart(uri)
                                             .file(file)
                                             .accept(MediaType.APPLICATION_JSON_VALUE))
                            .andExpect(status().isOk()).andReturn();
        
        var actual =
                objectMapper.readValue(result.getResponse().getContentAsString(), TaxiTripRegistryDTO.class);
        
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getContractor().getId()).isEqualTo(newDto.getContractorId());
        assertThat(actual.getDate()).isEqualTo(newDto.getDate().withDayOfMonth(1));
        assertThat(actual.getStringsQnt()).isEqualTo(9);
        assertThat(actual.getBlankCells().size()).isEqualTo(8);
        assertThat(actual.getIncorrectTypeCells().size()).isZero();
        //проверка БД
        checkActualFromDbIsEqualToActualDto(actual);
    }
    
    @DisplayName("Замена реестра xlsx - некорректный ID реестра - исключение")
    @Test
    @WithMockUser(roles = "GUEST")
    @Transactional
    void replaceRegistry_incorrectRegistryId_exception() throws Exception {
        importExcelRegistry(newDto, FILE_NAME1, filesSourceRelativePath, mockMvc, objectMapper);
        String incorrectRegistryIdUri = COMMON_URI + "/" + UUID.randomUUID();
        var errorCode = status().isNotFound();
        var ex = getExceptionAfterReplaceExcelRegistry(incorrectRegistryIdUri, FILE_NAME2, errorCode);
        
        assertThat(ex).isInstanceOf(EntityNotFoundException.class);
    }
    
    @DisplayName("Замена реестра xlsx - некорректное расширение файла - исключение")
    @Test
    @WithMockUser(roles = "GUEST")
    @Transactional
    void replaceRegistry_incorrectFileFormat_exception() throws Exception {
        var registry = importExcelRegistry(newDto, FILE_NAME1, filesSourceRelativePath, mockMvc, objectMapper);
        String uri = COMMON_URI + "/" + registry.getId();
        String incorrectFilePath = FILE_NAME2.replace("xlsx", "pdf");
        var errorCode = status().isUnsupportedMediaType();
        var ex = getExceptionAfterReplaceExcelRegistry(uri, incorrectFilePath, errorCode);
        
        assertThat(ex).isInstanceOf(UnsupportedFileFormatException.class);
    }
    
    @DisplayName("Проверка работы Checker-а строк реестра")
    @Test
    @Transactional
    void checkerTest() throws Exception {
        org = organizationRepository.save(Organization.builder().id(UUID.randomUUID()).officialName("Сбер").build());
        LocalDate commonDate = LocalDate.of(2021, 2, 1);
        
        //сначала необходимо заполнить несколько поездок - индивид. и совместн.
        ContractorDeviationsTariffParams deviations =
                ContractorDeviationsTariffParams.builder()
                                                .maxDiffComputedDistancePercent(10)
                                                .maxDiffFactDistancePercent(11)
                                                .maxDiffComputedCostPercent(12)
                                                .maxDiffContractorCostPercent(13)
                                                .maxDiffComputedWaitingPercent(14)
                                                .build();
        
        TaxiTariff tariff = TaxiTariff.builder()
                                      .id(UUID.randomUUID())
                                      .taxiClass(TaxiClass.ECONOMY)
                                      .rideCostPerKm(2000) //коп
                                      .rideCostPerMin(800)
                                      .waitCostPerMin(500)
                                      .carServiceCost(10000)
                                      .minRideDistanceCost(5000)
                                      .contractorDeviationParams(deviations)
                                      .build();
        tariff = tariffRepository.save(tariff);
        
        //поездка 1 - одиночная, параметры корректные
        ExpectedData expectedData1 = ExpectedData.builder()
                                                 .time(Duration.ofMinutes(15))    //min
                                                 .distance(4.63)    //km
                                                 .cost(150.50)    //rub
                                                 .build();
        Request request1 = Request.builder()
                                  .id(UUID.randomUUID())
                                  .humanReadableId("REQ-0001")
                                  .creationTime(LocalDateTime.now())
                                  .transportType(TransportTypeEnum.TAXI.name())
                                  .tariff(tariff)
                                  .expected(expectedData1)
                                  .desiredDate(LocalDateTime.of(commonDate, LocalTime.of(11, 30)))
                                  .coopTrip(false)
                                  .finishedTime(LocalDateTime.of(commonDate, LocalTime.of(11, 55)))
                                  .transportClass(TaxiClass.ECONOMY.name())
                                  .build();
        request1 = requestRepository.save(request1);
        
        SingleTaxiTrip singleTrip1 = SingleTaxiTrip.builder()
                                                   .id(UUID.randomUUID())
                                                   .request(request1)
                                                   .tripType(TripType.SINGLE)
                                                   .taxiId(1001 + "")
                                                   .organizationId(org.getId())
                                                   .tariff(tariff)
                                                   //заменить время, если появятся проверки
                                                   .dateTimeRegistered(LocalDateTime.now())
                                                   .tripStartTime(LocalDateTime.now())
                                                   .tripFinishTime(LocalDateTime.now())
                                                   .status(InboundTaxiTripStatus.ORDER_FINISHED.name())
                                                   .tripFactDistance(4.70)  //km
                                                   .tripFactDuration(Duration.ofMinutes(15))
                                                   .tripFactPrice(15600)    //kop
                                                   .tripFactWaitTime(Duration.ofMinutes(5))
                                                   .build();
        singleTripRepository.save(singleTrip1);
        
        //поездка 2 - кооперативная, параметры корректные
        SharedRideKPI kpi2 = SharedRideKPI.builder()
                                          .id(UUID.randomUUID())
                                          .totalCost(160.00)
                                          .totalDistanceKm(4.97)
                                          .totalTimeMin(16)
                                          .build();
        kpi2 = sharedRequestKpiRepository.save(kpi2);
        
        var sharedRequest2 = SharedRide.builder()
                                       .id(UUID.randomUUID())
                                       .passengers(2)
                                       .tariffId(tariff.getId())
                                       .kpi(kpi2)
                                       .build();
        sharedRequest2 = sharedRideRepository.save(sharedRequest2);
        
        List<Waypoint> waypoints = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            waypoints.add(i, Waypoint.builder()
                                     .id(UUID.randomUUID()).orderingIndex(i).waitTime(Duration.ofMinutes(i + 1))
                                     .build());
        }
        waypoints = waypointRepository.saveAll(waypoints);
        
        Request request2 = Request.builder()
                                  .id(UUID.randomUUID())
                                  .humanReadableId("REQ-0002")
                                  .creationTime(LocalDateTime.now())
                                  .transportType(TransportTypeEnum.TAXI.name())
                                  .tariff(tariff)
                                  .expected(expectedData1)
                                  .desiredDate(LocalDateTime.of(commonDate, LocalTime.of(11, 30)))
                                  .coopTrip(true)
                                  .finishedTime(LocalDateTime.of(commonDate, LocalTime.of(11, 55)))
                                  .transportType(TaxiClass.ECONOMY.name())
                                  .waypoints(waypoints)
                                  .sharedRide(sharedRequest2)
                                  .rideId(sharedRequest2.getId())
                                  .build();
        request2 = requestRepository.save(request2);
        
        for (Waypoint w : waypoints) {
            w.setRequest(request2);
        }
        waypointRepository.saveAll(waypoints);
        
        CoopTaxiTrip coopTrip2 = CoopTaxiTrip.builder()
                                             .id(UUID.randomUUID())
                                             .sharedRide(sharedRequest2)
                                             .rideId(sharedRequest2.getId())
                                             .tripType(TripType.COOP)
                                             .taxiId(1002 + "")
                                             .organizationId(org.getId())
                                             .tariff(tariff)
                                             //заменить время, если появятся проверки
                                             .dateTimeRegistered(LocalDateTime.now())
                                             .tripStartTime(LocalDateTime.now())
                                             .tripFinishTime(LocalDateTime.now())
                                             .status(InboundTaxiTripStatus.ORDER_FINISHED.name())
                                             .tripFactDistance(4.70)  //km
                                             .tripFactDuration(Duration.ofMinutes(15))
                                             .tripFactPrice(15600)    //kop
                                             .tripFactWaitTime(Duration.ofMinutes(5))
                                             .build();
        coopTripRepository.save(coopTrip2);
        
        //поездка 3 - одиночная, отменена, корректно
        ExpectedData expectedData3 = ExpectedData.builder()
                                                 .time(Duration.ofMinutes(0))    //min
                                                 .distance(0.0)    //km
                                                 .cost(0.0)    //rub
                                                 .build();
        Request request3 = Request.builder()
                                  .id(UUID.randomUUID())
                                  .humanReadableId("REQ-0003")
                                  .creationTime(LocalDateTime.now())
                                  .transportType(TransportTypeEnum.TAXI.name())
                                  .tariff(tariff)
                                  .expected(expectedData3)
                                  .desiredDate(LocalDateTime.of(commonDate, LocalTime.of(11, 30)))
                                  .coopTrip(false)
                                  .finishedTime(LocalDateTime.of(commonDate, LocalTime.of(11, 55)))
                                  .transportType(TaxiClass.ECONOMY.name())
                                  .build();
        requestRepository.save(request3);
        
        SingleTaxiTrip singleTrip3 = SingleTaxiTrip.builder()
                                                   .id(UUID.randomUUID())
                                                   .request(request3)
                                                   .tripType(TripType.SINGLE)
                                                   .taxiId(1003 + "")
                                                   .organizationId(org.getId())
                                                   .tariff(tariff)
                                                   //заменить время, если появятся проверки
                                                   .dateTimeRegistered(LocalDateTime.now())
                                                   .tripStartTime(LocalDateTime.now())
                                                   .tripFinishTime(LocalDateTime.now())
                                                   .status(InboundTaxiTripStatus.ORDER_CANCELLED_BY_CLIENT.name())
                                                   .tripFactDistance(0.0)  //km
                                                   .tripFactDuration(Duration.ofMinutes(0))
                                                   .tripFactPrice(0)    //kop
                                                   .tripFactWaitTime(Duration.ofMinutes(0))
                                                   .build();
        singleTripRepository.save(singleTrip3);
        
        //итог - реестр корректный
        TaxiTripRegistryDTO registryDto = importExcelRegistry(newDto, FILE_NAME3, filesSourceRelativePath, mockMvc, objectMapper);
        
        assertThat(registryDto.getValid()).isTrue();
        assertThat(registryDto.getStringsQnt()).isEqualTo(3);
        
        TaxiTripRegistry registry = registryRepository.findAll().get(0);
        assertThat(registry.getRegistryStrings().size()).isEqualTo(3);
        
        TaxiTripRegistryString actual1 = registry.getRegistryStrings().get(0);
        TaxiTripRegistryString actual2 = registry.getRegistryStrings().get(1);
        TaxiTripRegistryString actual3 = registry.getRegistryStrings().get(2);
        
        //выборочные проверки
        assertThat(actual1.getCalculatedTripType()).isEqualTo(TripType.SINGLE);
        assertThat(actual1.getCalculatedTripStatus()).isEqualTo(CalculatedTripStatus.DONE);
        assertThat(actual1.getId()).isNotNull();
        assertThat(actual1.getValidTaxiId0()).isTrue();
        assertThat(actual1.getValidCalcDistance4()).isTrue();
        assertThat(actual1.getValidWaitTime8()).isTrue();
        
        assertThat(actual2.getCalculatedTripType()).isEqualTo(TripType.COOP);
        assertThat(actual2.getCalculatedTripStatus()).isEqualTo(CalculatedTripStatus.DONE);
        assertThat(actual2.getId()).isNotNull();
        assertThat(actual2.getValidTripStatus1()).isTrue();
        assertThat(actual2.getValidTariff5()).isTrue();
        assertThat(actual2.getValidFactCost7()).isTrue();
        
        assertThat(actual3.getCalculatedTripType()).isEqualTo(TripType.SINGLE);
        assertThat(actual3.getCalculatedTripStatus()).isEqualTo(CalculatedTripStatus.CANCELED);
        assertThat(actual3.getId()).isNotNull();
        assertThat(actual3.getValidTripStatus1()).isTrue();
        assertThat(actual3.getValidTripDate2()).isTrue();
        assertThat(actual3.getValidCancelledTripCost3()).isTrue();
        assertThat(actual3.getValidFactDistance4a()).isTrue();
        assertThat(actual3.getValidWaitTime8()).isTrue();
        
        //поездка 10 (на основе 1ой) - не заполнено ожидание в реестре
        createTaxiTrip(tariff, "REQ-0010", expectedData1, 1010);
        
        //поездка 11 (на основе 1ой) - не заполнено расстояние в реестре
        createTaxiTrip(tariff, "REQ-0011", expectedData1, 1011);
        
        //поездка 12 (на основе 1ой) - ожидание и расстояние = 0, цена != 0 в реестре
        createTaxiTrip(tariff, "REQ-0012", expectedData1, 1012);
        
        //поездка 13 (на основе 1ой) - taxi_id в реестре левый
        createTaxiTrip(tariff, "REQ-0013", expectedData1, 1013);
        
        //поездка 14 (на основе 1ой) - все частные случаи в проверках (некорректные данные в реестре и в сущностях)
        ExpectedData expectedData14 = ExpectedData.builder()
                                                  .time(Duration.ofMinutes(15))    //min
                                                  .distance(7.26)    //km  - некорр., >10% разница
                                                  .cost(100.76)    //rub   - отклонение >10%
                                                  .build();
        Request request14 = Request.builder()
                                   .id(UUID.randomUUID())
                                   .humanReadableId("REQ-0014")
                                   .creationTime(LocalDateTime.now())
                                   .transportType(TransportTypeEnum.TAXI.name())
                                   .tariff(tariff)
                                   .expected(expectedData14)
                                   .desiredDate(LocalDateTime.of(commonDate, LocalTime.of(11, 30)))
                                   .coopTrip(false)
                                   .finishedTime(LocalDateTime.of(commonDate, LocalTime.of(11, 55)))
                                   .transportType(TaxiClass.ECONOMY.name())
                                   .build();
        request14 = requestRepository.save(request14);
        
        SingleTaxiTrip singleTrip14 = SingleTaxiTrip.builder()
                                                    .id(UUID.randomUUID())
                                                    .request(request14)
                                                    .tripType(TripType.SINGLE)
                                                    .taxiId(1014 + "")
                                                    .organizationId(org.getId())
                                                    .tariff(tariff)
                                                    //заменить время, если появятся проверки
                                                    .dateTimeRegistered(LocalDateTime.now())
                                                    .tripStartTime(LocalDateTime.now())
                                                    .tripFinishTime(LocalDateTime.now())
                                                    .status(InboundTaxiTripStatus.ORDER_FINISHED.name())
                                                    .tripFactDistance(2.17)  //km   - отклонение >10%
                                                    .tripFactDuration(Duration.ofMinutes(15))
                                                    .tripFactPrice(0)    //kop   - 0 в АС, в реестре != 0
                                                    .tripFactWaitTime(Duration.ofMinutes(0))   //0 и в реестре
                                                    .build();
        singleTrip14 = singleTripRepository.save(singleTrip14);
        
        newDto.setDate(newDto.getDate().withMonth(1));
        TaxiTripRegistryDTO registryDto2 = importExcelRegistry(newDto, FILE_NAME4, filesSourceRelativePath, mockMvc, objectMapper);
        
        assertThat(registryDto2.getValid()).isFalse();
        assertThat(registryDto2.getStringsQnt()).isEqualTo(5);
        
        assertThat(registryRepository.count()).isEqualTo(2);
        TaxiTripRegistry registry2 = registryRepository.findAll().get(1);
        assertThat(registry2.getRegistryStrings().size()).isEqualTo(5);
        
        TaxiTripRegistryString actual10 = registry2.getRegistryStrings().get(0);
        TaxiTripRegistryString actual11 = registry2.getRegistryStrings().get(1);
        TaxiTripRegistryString actual12 = registry2.getRegistryStrings().get(2);
        TaxiTripRegistryString actual13 = registry2.getRegistryStrings().get(3);
        TaxiTripRegistryString actual14 = registry2.getRegistryStrings().get(4);
        
        //выборочные проверки
        assertThat(actual10.getCalculatedTripStatus()).isEqualTo(CalculatedTripStatus.ERROR_STRING);
        assertThat(actual10.getCalculatedTripType()).isEqualTo(TripType.SINGLE);
        assertThat(actual10.getValidTaxiId0()).isTrue();
        assertThat(actual10.getValidTripStatus1()).isFalse();
        assertThat(actual10.getValidCalcDistance4()).isFalse();
        assertThat(actual10.getValidWaitTime8()).isFalse();
        
        assertThat(actual11.getCalculatedTripStatus()).isEqualTo(CalculatedTripStatus.ERROR_STRING);
        assertThat(actual11.getCalculatedTripType()).isEqualTo(TripType.SINGLE);
        assertThat(actual11.getValidTaxiId0()).isTrue();
        assertThat(actual11.getValidTripStatus1()).isFalse();
        assertThat(actual11.getValidFactDistance4a()).isFalse();
        assertThat(actual11.getValidCalcCost6()).isFalse();
        
        assertThat(actual12.getCalculatedTripStatus()).isEqualTo(CalculatedTripStatus.ERROR_STRING);
        assertThat(actual12.getCalculatedTripType()).isEqualTo(TripType.SINGLE);
        assertThat(actual12.getValidTaxiId0()).isTrue();
        assertThat(actual12.getValidTripStatus1()).isFalse();
        assertThat(actual12.getValidTripDate2()).isTrue();
        assertThat(actual12.getValidTariff5()).isFalse();
        assertThat(actual12.getValidFactCost7()).isFalse();
        
        assertThat(actual13.getCalculatedTripStatus()).isNull();
        assertThat(actual13.getCalculatedTripType()).isNull();
        assertThat(actual13.getValidTaxiId0()).isFalse();
        assertThat(actual13.getValidTripStatus1()).isFalse();
        assertThat(actual13.getValidTripDate2()).isFalse();
        assertThat(actual13.getValidCalcDistance4()).isFalse();
        assertThat(actual13.getValidFactDistance4a()).isFalse();
        assertThat(actual13.getValidTariff5()).isFalse();
        assertThat(actual13.getValidWaitTime8()).isFalse();
        
        assertThat(actual14.getCalculatedTripStatus()).isEqualTo(CalculatedTripStatus.DONE);
        assertThat(actual14.getCalculatedTripType()).isEqualTo(TripType.SINGLE);
        assertThat(actual14.getValidTaxiId0()).isTrue();
        assertThat(actual14.getValidTripStatus1()).isTrue();
        assertThat(actual14.getValidTripDate2()).isTrue();
        assertThat(actual14.getValidCancelledTripCost3()).isTrue();
        assertThat(actual14.getValidCalcDistance4()).isFalse();
        assertThat(actual14.getValidFactDistance4a()).isFalse();
        assertThat(actual14.getValidTariff5()).isFalse();
        assertThat(actual14.getValidCalcCost6()).isFalse();
        assertThat(actual14.getValidFactCost7()).isFalse();
        assertThat(actual14.getValidWaitTime8()).isTrue();
    }
    
    /**
     * Проверка соответствия Реестра, записанного в БД, и возвращаемого DTO
     *
     * @param actualDto TaxiTripRegistryDTO
     */
    private void checkActualFromDbIsEqualToActualDto(TaxiTripRegistryDTO actualDto) throws EntityNotFoundException {
        assertThat(registryRepository.count()).isEqualTo(1);
        
        TaxiTripRegistry actualFromDb = registryRepository.findById(actualDto.getId()).orElseThrow(
                () -> new EntityNotFoundException(TaxiTripRegistry.class, actualDto.getId()));
        
        assertThat(actualFromDb.getContractor().getId()).isEqualTo(actualDto.getContractor().getId());
        assertThat(actualFromDb.getContractor().getName()).isEqualTo(actualDto.getContractor().getName());
        assertThat(actualFromDb.getBlankCells().size()).isEqualTo(actualDto.getBlankCells().size());
        assertThat(actualFromDb.getIncorrectTypeCells().size()).isEqualTo(actualDto.getIncorrectTypeCells().size());
        assertThat(actualFromDb.getDate()).isEqualTo(actualDto.getDate());
        assertThat(actualFromDb.getRegistryStrings().size()).isEqualTo(actualDto.getStringsQnt());
    }
    
    /**
     * Получение исключения при поиске реестра
     *
     * @param uri для endpoint
     * @param errorCode ResultMatcher - статус ошибки
     *
     * @return Exception
     */
    private Exception getExceptionAfterFindExcelRegistry(String uri, ResultMatcher errorCode) throws Exception {
        return mockMvc.perform(get(uri)
                                       .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
                      .andExpect(errorCode).andReturn().getResolvedException();
    }
    
    /**
     * Получение исключения при импорте реестра
     *
     * @param newDto NewTaxiTripRegistryDTO
     * @param fileName имя файла реестра
     * @param errorCode ResultMatcher - статус ошибки
     *
     * @return Exception
     */
    private Exception getExceptionAfterImportExcelRegistry(
            NewTaxiTripRegistryDTO newDto, String fileName, ResultMatcher errorCode
                                                          )
            throws Exception {
        
        MockMultipartFile file = new MockMultipartFile("file", fileName, MEDIA_TYPE,
                                                       getFileFromResource(filesSourceRelativePath, fileName));
        MockMultipartFile request =
                new MockMultipartFile("request", "request", MediaType.APPLICATION_JSON_VALUE,
                                      objectMapper.writeValueAsString(newDto).getBytes(StandardCharsets.UTF_8));
        
        return mockMvc.perform(multipart(ru.sberbank.ditsib.transport.reports.service.FileService.COMMON_URI)
                                       .file(file)
                                       .file(request)
                                       .accept(MediaType.APPLICATION_JSON_VALUE))
                      .andExpect(errorCode).andReturn().getResolvedException();
    }
    
    /**
     * Получение исключения при замене реестра
     *
     * @param uri для endpoint
     * @param fileName имя файла реестра
     * @param errorCode ResultMatcher - статус ошибки
     *
     * @return Exception
     */
    private Exception getExceptionAfterReplaceExcelRegistry(String uri, String fileName, ResultMatcher errorCode)
            throws Exception {
        String request = objectMapper.writeValueAsString(newDto);
        MockMultipartFile file = new MockMultipartFile("file", fileName, MEDIA_TYPE,
                                                       getFileFromResource(filesSourceRelativePath, fileName));
        
        return mockMvc.perform(multipart(uri)
                                       .file(file)
                                       .accept(MediaType.APPLICATION_JSON_VALUE))
                      .andExpect(errorCode).andReturn().getResolvedException();
    }
    
    /**
     * Создать SingleTaxiTrip c данными как у первой поездки
     */
    private void createTaxiTrip(
            TaxiTariff tariff, String humanReadable, ExpectedData expectedData,
            Integer taxiId
                               ) {
        LocalDate commonDate = LocalDate.of(2021, 2, 1);
        Request request = Request.builder()
                                 .id(UUID.randomUUID())
                                 .humanReadableId(humanReadable)
                                 .creationTime(LocalDateTime.now())
                                 .transportType(TransportTypeEnum.TAXI.name())
                                 .tariff(tariff)
                                 .expected(expectedData)
                                 .desiredDate(LocalDateTime.of(commonDate, LocalTime.of(11, 30)))
                                 .coopTrip(false)
                                 .finishedTime(LocalDateTime.of(commonDate, LocalTime.of(11, 55)))
                                 .transportType(TaxiClass.ECONOMY.name())
                                 .build();
        request = requestRepository.save(request);
        
        SingleTaxiTrip singleTrip = SingleTaxiTrip.builder()
                                                  .id(UUID.randomUUID())
                                                  .request(request)
                                                  .tripType(TripType.SINGLE)
                                                  .taxiId(taxiId + "")
                                                  .organizationId(org.getId())
                                                  .tariff(tariff)
                                                  //заменить время, если появятся проверки
                                                  .dateTimeRegistered(LocalDateTime.now())
                                                  .tripStartTime(LocalDateTime.now())
                                                  .tripFinishTime(LocalDateTime.now())
                                                  .status(InboundTaxiTripStatus.ORDER_FINISHED.name())
                                                  .tripFactDistance(4.70)  //km
                                                  .tripFactDuration(Duration.ofMinutes(15))
                                                  .tripFactPrice(15600)    //kop
                                                  .tripFactWaitTime(Duration.ofMinutes(5))
                                                  .build();
        singleTripRepository.save(singleTrip);
    }
    
    /**
     * Сравнить ДТО с реестром, предварительно найдя ДТО в списке
     *
     * @param actualList список ДТО из реестров
     * @param registry реестр
     */
    private void checkDtoByRegistry(
            List<TaxiTripRegistryShortDTO> actualList, TaxiTripRegistry registry
                                   ) {
        var actual = actualList.stream().filter(dto -> dto.getId().equals(registry.getId())).findFirst().orElseThrow();
        assertThat(actual.getContractor().getId()).isEqualTo(registry.getContractor().getId());
        assertThat(actual.getContractor().getName()).isEqualTo(registry.getContractor().getName());
        assertThat(actual.getDate().getYear()).isEqualTo(registry.getDate().getYear());
        assertThat(actual.getDate().getMonth()).isEqualTo(registry.getDate().getMonth());
    }
}