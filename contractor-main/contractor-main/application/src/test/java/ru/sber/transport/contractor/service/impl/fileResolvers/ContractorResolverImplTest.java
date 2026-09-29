package ru.sber.transport.contractor.service.impl.fileResolvers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.contractor.database.dao.ContractorRepository;
import ru.sber.transport.contractor.database.dao.EmployeeRepository;
import ru.sber.transport.contractor.database.model.*;
import ru.sber.transport.contractor.messages.ContractorMessage;
import ru.sber.transport.file_works.dto.PageInfo;
import ru.sber.transport.file_works.services.UploadStates;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.contractor.testutils.TestContractors.createTestContractor;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@UnitTest
@IsolatedTest
@Feature("app_platform_contractor")
@SpringBootTest(properties = {"export.tempDir=target/test/files"})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@DisplayName("Проверка импортёра файлов")
@ActiveProfiles("test")
class ContractorResolverImplTest {

    public static final String USER_ID = "00000000-0000-0000-0000-000000000000";

    @Value("${export.tempDir}")
    private String tempDir;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UploadStates uploadStates;

    @MockBean(name = "contractorOutput")
    private OutputBridge contractorOutput;

    @MockBean
    private AuthorizationManager<?> manager;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(manager, "ROLE_GUEST");
    }

    @AfterEach
    void afterEach() {
        contractorRepository.findAll().forEach(c -> {
            contractorRepository.save(c);
        });
        contractorRepository.flush();
        contractorRepository.deleteAll();
    }

    @SneakyThrows
    @Test
    @DisplayName("Импорт")
    @Disabled
    void importData() {
        var file = new MockMultipartFile("file", "file.xlsx", "application/vnd.openxmlformats-officedocument" +
                ".spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/contractor.xlsx"));

        assertThat(contractorRepository.count()).isZero();

        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(UUID.randomUUID()).build();
        employeeRepository.save(employee);

        mockMvc.perform(multipart("/files/contractor/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk());

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1)).until(() -> uploadStates.getResults("contractor", USER_ID).size(), equalTo(1));
        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1)).until(() -> uploadStates.getResults("contractor", USER_ID).get(0).getFinished(), equalTo(true));
        assertThat(uploadStates.getResults("contractor", USER_ID).get(0).getPages().stream().allMatch(page -> page.getExceptionStrings().isEmpty()))
                .isTrue();
        assertThat(uploadStates.getResults("contractor", USER_ID).get(0).getPages().stream().map(PageInfo::getRow)
                .flatMap(Collection::stream).allMatch(page -> page.getExceptionStrings().isEmpty()))
                .isTrue();
        assertThat(uploadStates.getResults("contractor", USER_ID).get(0).getPages().stream().map(PageInfo::getRow)
                .flatMap(Collection::stream).allMatch(page -> page.getViolations().isEmpty()))
                .isTrue();
        var count = 10;
        assertThat(contractorRepository.count()).isEqualTo(count);

        var actualList = contractorRepository.findAll();
        actualList.sort(Comparator.comparing(Contractor::getName));

        var messageCaptor = ArgumentCaptor.forClass(ContractorMessage.class);
        verify(contractorOutput, times(count)).send(messageCaptor.capture());
        var messages = messageCaptor.getAllValues().stream()
                .sorted(Comparator.comparing(ContractorMessage::name)).toList();

        for (var i = 0; i < count; i++) {
            var actual = actualList.get(i);
            var actualMessage = messages.get(i);
            assertThat(actual.getName()).isEqualTo(actualMessage.name());
            assertThat(actual.getTin()).isEqualTo(actualMessage.tin());
            assertThat(actual.getMsrn()).isEqualTo(actualMessage.msrn());
            assertThat(actual.getContactPersonInfo()).isEqualTo(actualMessage.contactPersonInfo());
            assertThat(actual.getContactPersonPhone()).isEqualTo(actualMessage.contactPersonPhone());
            assertThat(actual.getRating()).isEqualTo(actualMessage.rating());
        }
    }

    @SneakyThrows
    @Test
    @DisplayName("Импорт с неправильным названием листа")
    void importDataWrongSheetName() {
        var file = new MockMultipartFile("file", "file.xlsx", "application/vnd.openxmlformats-officedocument" +
                ".spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/contractorWrongSheetName.xlsx"));

        assertThat(contractorRepository.count()).isZero();

        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(UUID.randomUUID()).build();
        employeeRepository.save(employee);

        mockMvc.perform(multipart("/files/contractor/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk());

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1)).until(() -> uploadStates.getResults("contractor", USER_ID).size(), equalTo(1));
        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1)).until(() -> uploadStates.getResults("contractor", USER_ID).get(0).getFinished(), equalTo(true));
        assertThat(uploadStates.getResults("contractor", USER_ID).get(0).getPages().stream().allMatch(page -> page.getExceptionStrings().size() == 1))
                .isTrue();
        assertThat(uploadStates.getResults("contractor", USER_ID).get(0).getPages().stream().map(PageInfo::getRow)
                .flatMap(Collection::stream).allMatch(page -> page.getViolations().isEmpty()))
                .isTrue();
        var count = 0;
        assertThat(contractorRepository.count()).isEqualTo(count);
    }

    @SneakyThrows
    @Test
    @DisplayName("Экспорт")
    void exportData() {
        var count = 100;
        for (var i = 0; i < count; i++) {
            contractorRepository.save(createTestContractor(i, ContractorType.DISPATCHER_INTERNAL, ServiceType.EMPLOYEE_TRANSPORTATION, IntegrationType.EMAIL_XML_API));
        }

        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(UUID.randomUUID()).build();
        employeeRepository.save(employee);

        var content = mockMvc.perform(get("/files/contractor/")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        var response = new ObjectMapper().readValue(content, new TypeReference<Map<String, String>>() {
        });

        byte[] bytes = null;

        for (var i = 0; bytes == null; i++) {
            var result = mockMvc.perform(get(response.get("result_url") + "/")
                            .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse();

            if (result.getContentAsString().contains("in_progress\": true")) {
                if (i > 10) {
                    fail("Не удалось получить файл");
                }

                Thread.sleep(3000);
                continue;
            }

            bytes = result.getContentAsByteArray();
        }

        try (var byteArrayInputStream = new ByteArrayInputStream(bytes);
             var excel = new XSSFWorkbook(byteArrayInputStream)) {
            var sheet = excel.getSheet("Контрагенты");

            assertThat(sheet).isNotNull();

            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(count + 1); // 1 - заголовок

            var row = sheet.getRow(0);
            assertThat(row.getCell(0).getStringCellValue()).isEqualTo("Имя контрагента");
            assertThat(row.getCell(1).getStringCellValue()).isEqualTo("Инн");
            assertThat(row.getCell(2).getStringCellValue()).isEqualTo("ОГРН контрагента");
            assertThat(row.getCell(3).getStringCellValue()).isEqualTo("Фио контактного лица");
            assertThat(row.getCell(4).getStringCellValue()).isEqualTo("Номер контактного лица");

            var expectedList = contractorRepository.findAll();
            expectedList.sort(Comparator.comparing(Contractor::getName));

            for (int rowIndex = 1, index = 0; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++, index++) {
                var actualRow = sheet.getRow(rowIndex);
                var expected = expectedList.get(index);

                assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(expected.getName());
                assertThat(actualRow.getCell(1).getStringCellValue()).isEqualTo(expected.getTin());
                assertThat(actualRow.getCell(2).getStringCellValue()).isEqualTo(expected.getMsrn());
                //assertThat(actualRow.getCell(3).getStringCellValue()).isEqualTo(expected.getContactPersonInfo());
                assertThat(actualRow.getCell(4).getStringCellValue()).isEqualTo(expected.getContactPersonPhone());
            }
        }
    }
}