package ru.sberbank.ditsib.transport.tariff.controller.importExport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.file_works.database.dao.ExportTaskRepository;
import ru.sber.transport.tariff.messaging.ContractMessage;
import ru.sber.transport.file_works.dto.PageInfo;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.file_works.services.UploadStates;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.tariff.controller.ContractController;
import ru.sberbank.ditsib.transport.tariff.database.dao.ContractRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.ContractorRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.GetContractDTO;

import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres
@Transactional
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка импорта/экспорта контрактов")
@ActiveProfiles({"test", "kafka"})
class ContractImportExportTest extends KafkaTest {

    public static final String USER_ID = "7a667c38-efa3-4376-b657-196a9e836827";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private ContractController contractController;

    @Autowired
    private UploadStates uploadStates;

    @Autowired
    private GeoZoneRepository geoZoneRepository;

    @Autowired
    private ExportTaskRepository exportTaskRepository;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @BeforeEach
    void setup() {
        contractRepository.deleteAllInBatch();
        for (var i = 0; i < 50; i++) {
            var contractor = new Contractor();
            var organization = new Organization();

            organization.setId(UUID.randomUUID());
            organization.setName(String.format("Организация %02d", i + 1));
            organization.setDigitId((long) i);

            contractor.setId(UUID.randomUUID());
            contractor.setIntegrationType(TaxiExternalIntegrationType.JSON_API_1_0.name());
            contractor.setName(String.format("Контрагент %02d", i + 1));

            organizationRepository.save(organization);
            contractorRepository.save(contractor);
        }
    }

    @AfterEach
    void after() {
        contractorRepository.deleteAll();
        contractRepository.deleteAll();
        geoZoneRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    @Transactional(propagation = Propagation.SUPPORTS)
    @DisplayName("Импорт")
    @Disabled("Требуется обработка новых полей")
    void test_import() throws Exception {
        var count = 50;
        for (int i = 0; i < count; i++) {
            geoZoneRepository.save(new GeoZone(UUID.randomUUID(), String.format("Регион %02d", i + 1), i + "", null));
        }
        var file = new MockMultipartFile("file",
                "file.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/contract.xlsx"));

        assertRequest(file);
        var actualList =
                contractController.getAll(null).stream().sorted(Comparator.comparing(GetContractDTO::getContractNumber)).toList();
        var actualMessages = consumeMessages("service.contract", UUID.class, ContractMessage.class).values().stream()
                .flatMap(List::stream)
                .sorted(Comparator.comparing(
                        ContractMessage::creationTime)).toList();
        var serviceTypes =
                List.of(TransportServiceType.EMPLOYEE_TRANSPORTATION).toArray(TransportServiceType[]::new);
        // в файле 50 записей
        assertAll(
                () -> assertThat(actualList).hasSize(25),
                () -> assertThat(actualMessages).hasSize(25)
        );

        for (var i = 0; i < actualList.size(); i++) {
            var actual = actualList.get(i);
            var message = actualMessages.get(i);
            var contractor = contractorRepository.findById(actual.getContractorId()).orElseThrow();

            var index = i;
            var days = (2 * i) + 1;
            var number = (2 * i) + 1;

            UUID geozoneId = actual.getRegionIds().stream().findFirst().orElseThrow();
            GeoZone foundGeozone = geoZoneRepository.findById(geozoneId).orElse(null);
            assertNotNull(foundGeozone);
            assertAll("Checking index " + index,
                    () -> assertThat(actual.getEndDate()).isEqualTo(LocalDate.of(2000, 1, 31).plusDays(days)),
                    () -> assertThat(actual.getStartDate()).isEqualTo(LocalDate.of(1999, 12, 31).plusDays(days)),
                    () -> assertThat(actual.getContractNumber()).isEqualTo(String.format("Договор %02d", number)),
                    () -> assertThat(actual.getSum()).isEqualTo(number * 100L),
                    () -> assertThat(actual.getVatValue()).isEqualTo(number),
                    () -> assertThat(actual.getContractorId()).isEqualTo(contractor.getId()),
                    () -> assertThat(actual.isActive()).isEqualTo(true),
                    () -> assertThat(actual.isIncludeVat()).isEqualTo(true),
                    () -> assertThat(message.endDate()).isEqualTo(LocalDate.of(2000, 1, 31).plusDays(days)),
                    () -> assertThat(message.startDate()).isEqualTo(LocalDate.of(1999, 12, 31).plusDays(days)),
                    () -> assertThat(message.sum()).isEqualTo(number * 100L),
                    () -> assertThat(message.serviceType()).isEqualTo(
                            serviceTypes[index % serviceTypes.length].name()),
                    () -> assertThat(message.contractorId()).isEqualTo(contractor.getId()),
                    () -> assertThat(message.uvhd()).isEqualTo(actual.getUvhd()),
                    () -> assertThat(message.active()).isEqualTo(true),
                    () -> assertThat(message.deleted()).isEqualTo(false)
            );
        }
    }

    private void assertRequest(MockMultipartFile file) throws Exception {
        mockMvc.perform(multipart("/files/contract").file(file)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER"))))
                .andExpect(status().isOk());

        await().atMost(Duration.ofSeconds(60)).until(() -> uploadStates.getResults("contract", USER_ID).size(),
                equalTo(1));
        await().atMost(Duration.ofSeconds(60)).until(() -> uploadStates.getResults("contract", USER_ID).getFirst().getFinished(), equalTo(true));
        assertThat(uploadStates.getResults("contract", USER_ID).getFirst().getPages().stream().allMatch(page -> page.getExceptionStrings().isEmpty()))
                .isTrue();
        assertThat(uploadStates.getResults("contract", USER_ID).getFirst().getPages().stream().map(PageInfo::getRow)
                .flatMap(Collection::stream).allMatch(page -> page.getExceptionStrings().isEmpty()))
                .isTrue();
    }

    @Test
    @DisplayName("Экспорт")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void test_export() throws Exception {
        var count = 50;
        List<GeoZone> geoZoneList = new ArrayList<>();
        for (var i = 0; i < count; i++) {
            var contract = new Contract();
            GeoZone savedGeozone =
                    geoZoneRepository.save(new GeoZone(UUID.randomUUID(), String.format("Регион %02d", i), i + "", null));
            geoZoneList.add(savedGeozone);
            contract.setContractNumber(String.format("Договор %02d", i));
            contract.setContractorId(contractorRepository.findAll(Sort.by(Contractor_.NAME)).get(i).getId());
            contract.setActive(i % 2 == 0);
            contract.setEndDate(LocalDate.of(2001, 1, 1).plusDays(i));
            contract.setIncludeVat(i % 2 == 1);
            contract.setRegionIds(Set.of(savedGeozone.getId()));
            contract.setServiceType(TransportServiceType.values()[i % TransportServiceType.values().length]);
            contract.setStartDate(LocalDate.of(2002, 1, 1).plusDays(i));
            contract.setSum(100L * i);
            contract.setTransportType(TransportTypeEnum.values()[i % TransportTypeEnum.values().length]);
            contract.setVatValue(i);
            contract.setUserId(UUID.randomUUID());
            contract.setCreationTime(LocalDateTime.now());
            contract.setUvhd(String.valueOf(i));
            contract.setContractType(ContractType.TRANSITIONAL);
            contract.setRestrictionType(RestrictionType.NONE);

            contractRepository.save(contract);
        }

        var content = mockMvc.perform(get("/files/contract")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        var response = new ObjectMapper().readValue(content, new TypeReference<Map<String, String>>() {
        });

        var file = response.get("result_url");
        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(3))
                .until(() -> {
                    var data = exportTaskRepository.findByName(file);
                    if (data != null) {
                        return data.getDone();
                    }
                    return false;
                });

        var bytes = mockMvc.perform(get(response.get("result_url"))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsByteArray();

        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet("Договоры");

            assertThat(sheet).isNotNull();
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(count + 1); // 1 - количество строк заголовков.

            var header = sheet.getRow(0);
            assertHeader(header);

            var expectedList = contractRepository.findAll();

            for (int rowIndex = 1, index = 0; index < count; rowIndex++, index++) {
                assertRow(geoZoneList, sheet, expectedList, rowIndex);
            }
        }
    }

    private void assertRow(List<GeoZone> geoZoneList, XSSFSheet sheet, List<Contract> expectedList, int rowIndex) {
        var row = sheet.getRow(rowIndex);

        var expectedItemOptional = expectedList.stream()
                .filter(contract -> contract.getContractNumber().equals(row.getCell(2).getStringCellValue()))
                .findFirst();
        assertThat(expectedItemOptional).isPresent();
        var expectedItem = expectedItemOptional.get();
        var contractor = contractorRepository.findById(expectedItem.getContractorId());
        assertThat(contractor).isPresent();
        assertAll(
                () -> assertThat(row.getCell(1).getBooleanCellValue()).isEqualTo(expectedItem.isActive()),
                () -> assertThat(row.getCell(2).getStringCellValue()).isEqualTo(expectedItem.getContractNumber()),
                () -> assertThat(row.getCell(3).getLocalDateTimeCellValue().toLocalDate()).isEqualTo(expectedItem.getEndDate()),
                () -> assertThat(row.getCell(4).getBooleanCellValue()).isEqualTo(expectedItem.isIncludeVat()),
                () -> assertThat(row.getCell(6).getStringCellValue()).isEqualTo(expectedItem.getServiceType().getDescription()),
                () -> assertThat(row.getCell(7).getLocalDateTimeCellValue().toLocalDate()).isEqualTo(expectedItem.getStartDate()),
                () -> assertThat(row.getCell(8).getNumericCellValue()).isEqualTo(expectedItem.getSum().doubleValue()),
                () -> assertThat(row.getCell(9).getStringCellValue()).isEqualTo(expectedItem.getTransportType().getRusName()),
                () -> assertThat(row.getCell(10).getNumericCellValue()).isEqualTo(expectedItem.getVatValue().doubleValue()),
                () -> assertThat(row.getCell(11).getStringCellValue()).isEqualTo(contractor.get().getName()),
                () -> assertThat(row.getCell(12).getStringCellValue()).isEqualTo(expectedItem.getUvhd())
        );
    }

    private void assertHeader(XSSFRow header) {
        assertThat((Iterable<? extends Cell>) header).isNotNull();
        assertThat(header.getPhysicalNumberOfCells()).isEqualTo(13); // количество столбцов в файле

        // порядок столбцов в экспорте соответствует порядку в yml
        assertAll(
                () -> assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Организация"),
                () -> assertThat(header.getCell(1).getStringCellValue()).isEqualTo("Активность"),
                () -> assertThat(header.getCell(2).getStringCellValue()).isEqualTo("Номер договора"),
                () -> assertThat(header.getCell(3).getStringCellValue()).isEqualTo("Дата окончания"),
                () -> assertThat(header.getCell(4).getStringCellValue()).isEqualTo("НДС включены"),
                () -> assertThat(header.getCell(5).getStringCellValue()).isEqualTo("Регион"),
                () -> assertThat(header.getCell(6).getStringCellValue()).isEqualTo("Тип услуги"),
                () -> assertThat(header.getCell(7).getStringCellValue()).isEqualTo("Дата начала"),
                () -> assertThat(header.getCell(8).getStringCellValue()).isEqualTo("Сумма"),
                () -> assertThat(header.getCell(9).getStringCellValue()).isEqualTo("Тип транспорта"),
                () -> assertThat(header.getCell(10).getStringCellValue()).isEqualTo("НДС (%)"),
                () -> assertThat(header.getCell(11).getStringCellValue()).isEqualTo("Контрагент"),
                () -> assertThat(header.getCell(12).getStringCellValue()).isEqualTo("Номер договора УВХД")
        );
    }
}
