package ru.sber.transport.telemechanic.resolver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jooq.Record;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.file_works.database.dao.ExportTaskRepository;
import ru.sber.transport.file_works.database.migrations.DatabaseMigration;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.EwbRegistryDynamicRepository;
import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.enumerate.EwbRegistryField;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;
import ru.sber.transport.telemechanic.helper.EwbRegistryHelper;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.telemechanic.TestData.EMPLOYEE_1_ID;
import static ru.sber.transport.telemechanic.enumerate.EwbRegistryField.*;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_DISPATCHER_SUPPORT_SERVICE;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres
class EwbRegistryAllOrganizationsResolverImplTest {
    private static final String FILES_REGISTRY_URI = "/files/ewb-all-organizations";
    private static final String RESULT_URL_URI = "result_url";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AuthorizationManager<?> manager;
    @Autowired
    private DatabaseMigration databaseMigration;
    @Autowired
    private ExportTaskRepository exportTaskRepository;
    @Autowired
    private EwbRegistryDynamicRepository ewbDynamicRepositoryRepository;
    
    @Test
    @DisplayName("Экспорт")
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void export() throws Exception {
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var fieldSet = Set.of(EWB_HUMAN_READABLE_ID, EWB_STATUS, EWB_CREATION_TIME, EWB_MEDIC_SUCCESS, EWB_TELEMECH_SUCCESS, TRANSPORT_STATE_NUMBER,
                              TRANSPORT_TYPE_TITLE, TRANSPORT_SUBTYPE_TITLE, ATTORNEY_NUMBER, ATTORNEY_ISSUE_DATE, ATTORNEY_CREATION_SYSTEM,
                              DRIVING_LICENSE_SERIES, DRIVING_LICENSE_NUMBER, DRIVING_LICENSE_ISSUE_DATE, DRIVING_LICENSE_EXPIRY_DATE);
        var parameters = createParameters(fieldSet,
                                          null,
                                          null,
                                          null,
                                          Set.of(),
                                          null,
                                          null);
        var content = mockMvc.perform(get(FILES_REGISTRY_URI + parameters)
                                              .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                         .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name()))))
                             .andExpect(status().isOk())
                             .andReturn()
                             .getResponse().getContentAsString();
        var response = objectMapper.readValue(content, new TypeReference<Map<String, String>>() {
        });
        var file = response.get(RESULT_URL_URI);
        await().timeout(Duration.ofSeconds(15)).pollDelay(Duration.ofSeconds(3))
               .until(() -> {
                   var data = exportTaskRepository.findByName(file);
                   if (data != null) {
                       return data.getDone();
                   }
                   return false;
               });
        
        var bytes = mockMvc.perform(get(response.get(RESULT_URL_URI))
                                            .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                       .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name()))))
                           .andExpect(status().isOk())
                           .andReturn()
                           .getResponse().getContentAsByteArray();
        ZipSecureFile.setMinInflateRatio(0.001);
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet("Sheet1");
            assertThat(sheet).isNotNull();
            var row0 = sheet.getRow(0);
            assertThat(row0).isNotNull();
            assertThat(row0.getPhysicalNumberOfCells()).isEqualTo(1);
            assertThat(row0.getCell(0).getStringCellValue()).isEqualTo("КОНФИДЕНЦИАЛЬНО");
            var row1 = sheet.getRow(1);
            assertThat(row1).isNotNull();
            assertThat(row1.getPhysicalNumberOfCells()).isEqualTo(1);
            assertThat(row1.getCell(0).getStringCellValue()).isEqualTo("Реестр");
            checkHeaderRow(sheet.getRow(2));
            var expectedList = ewbDynamicRepositoryRepository.findEwbRegistryExcel(fieldSet, null, null, null, null, null);
            for (int rowIndex = 3, index = 0; index < expectedList.size(); rowIndex++, index++) {
                checkRow(rowIndex, index, sheet.getRow(rowIndex), expectedList.get(index));
            }
        }
    }
    
    @Test
    @SneakyThrows
    @DisplayName("Выгрузка с фильтрами")
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void exportWithFilters() {
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var fieldSet = Set.of(EWB_HUMAN_READABLE_ID, EWB_STATUS, EWB_CREATION_TIME, EWB_MEDIC_SUCCESS, EWB_TELEMECH_SUCCESS, TRANSPORT_STATE_NUMBER,
                              TRANSPORT_TYPE_TITLE, TRANSPORT_SUBTYPE_TITLE, ATTORNEY_NUMBER, ATTORNEY_ISSUE_DATE, ATTORNEY_CREATION_SYSTEM,
                              DRIVING_LICENSE_SERIES, DRIVING_LICENSE_NUMBER, DRIVING_LICENSE_ISSUE_DATE, DRIVING_LICENSE_EXPIRY_DATE);
        var parameters = createParameters(fieldSet,
                                          "EWB_ID",
                                          "EWB_ID",
                                          UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"),
                                          Set.of(UUID.fromString("d640b449-4482-4a48-bc05-2bbcfa66b465"),
                                                 UUID.fromString("20d4a338-e121-4a0b-9d80-a7b6b035484f")),
                                          "2020-01-01T00:00:00",
                                          "2030-01-01T00:00:00");
        var content = mockMvc.perform(get(FILES_REGISTRY_URI + parameters)
                                              .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                         .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name()))))
                             .andExpect(status().isOk())
                             .andReturn()
                             .getResponse().getContentAsString();
        var response = objectMapper.readValue(content, new TypeReference<Map<String, String>>() {
        });
        var file = response.get(RESULT_URL_URI);
        await().timeout(Duration.ofSeconds(15)).pollDelay(Duration.ofSeconds(3))
               .until(() -> {
                   var data = exportTaskRepository.findByName(file);
                   if (data != null) {
                       return data.getDone();
                   }
                   return false;
               });
        
        var bytes = mockMvc.perform(get(response.get(RESULT_URL_URI))
                                            .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                       .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name()))))
                           .andExpect(status().isOk())
                           .andReturn()
                           .getResponse().getContentAsByteArray();
        ZipSecureFile.setMinInflateRatio(0.001);
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet("Sheet1");
            assertThat(sheet).isNotNull();
            var row0 = sheet.getRow(0);
            assertThat(row0).isNotNull();
            assertThat(row0.getPhysicalNumberOfCells()).isEqualTo(1);
            assertThat(row0.getCell(0).getStringCellValue()).isEqualTo("КОНФИДЕНЦИАЛЬНО");
            var row1 = sheet.getRow(1);
            assertThat(row1).isNotNull();
            assertThat(row1.getPhysicalNumberOfCells()).isEqualTo(1);
            assertThat(row1.getCell(0).getStringCellValue()).isEqualTo("Реестр");
            checkHeaderRow(sheet.getRow(2));
            var expectedList = ewbDynamicRepositoryRepository.findEwbRegistryExcel(fieldSet,
                                                                                   "EWB_ID",
                                                                                   "EWB_ID",
                                                                                   UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"),
                                                                                   Set.of(UUID.fromString("d640b449-4482-4a48-bc05-2bbcfa66b465"),
                                                                                          UUID.fromString("20d4a338-e121-4a0b-9d80-a7b6b035484f")),
                                                                                   new DateRange(
                                                                                           LocalDateTime.parse("2020-01-01T00:00:00",
                                                                                                               DateTimeFormatter.ISO_DATE_TIME),
                                                                                           LocalDateTime.parse("2030-01-01T00:00:00",
                                                                                                               DateTimeFormatter.ISO_DATE_TIME)
                                                                                   ));
            for (int rowIndex = 3, index = 0; index < expectedList.size(); rowIndex++, index++) {
                checkRow(rowIndex, index, sheet.getRow(rowIndex), expectedList.get(index));
            }
        }
    }
    
    private static void checkRow(int rowIndex, int index, XSSFRow actualRow, Record expected) {
        assertAll("Checking row " + rowIndex + " equals with element " + index,
                  () -> assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(expected.get(TRANSPORT_TYPE_TITLE.getAlias())),
                  () -> assertThat(actualRow.getCell(1).getStringCellValue()).isEqualTo(expected.get(TRANSPORT_STATE_NUMBER.getAlias())),
                  () -> checkLocalDateCell(actualRow, 2, LocalDate.parse(
                          EwbRegistryHelper.getFieldValueForExcel(DRIVING_LICENSE_ISSUE_DATE, expected), DATE_FORMATTER
                                                                        )),
                  () -> checkLocalDateCell(actualRow, 3, LocalDate.parse(
                          EwbRegistryHelper.getFieldValueForExcel(ATTORNEY_ISSUE_DATE, expected), DATE_FORMATTER
                                                                                )),
                  () -> checkLocalDateTimeCell(actualRow, 4, LocalDateTime.parse(
                          EwbRegistryHelper.getFieldValueForExcel(EWB_CREATION_TIME, expected), TIME_FORMATTER
                                                                                )),
                  () -> checkLocalDateCell(actualRow, 5, LocalDate.parse(
                          EwbRegistryHelper.getFieldValueForExcel(DRIVING_LICENSE_EXPIRY_DATE, expected), DATE_FORMATTER
                                                                        )),
                  () -> assertThat(actualRow.getCell(6).getStringCellValue()).isEqualTo(
                          EwbRegistryHelper.getFieldValueForExcel(ATTORNEY_NUMBER, expected)),
                  () -> assertThat(actualRow.getCell(7).getStringCellValue()).isEqualTo(
                          EwbRegistryHelper.getFieldValueForExcel(DRIVING_LICENSE_NUMBER, expected)),
                  () -> assertThat(actualRow.getCell(8).getStringCellValue()).isEqualTo(expected.get(EWB_HUMAN_READABLE_ID.getAlias())),
                  () -> assertThat(actualRow.getCell(9).getStringCellValue()).isEqualTo(expected.get(TRANSPORT_SUBTYPE_TITLE.getAlias())),
                  () -> assertThat(actualRow.getCell(10).getStringCellValue()).isEqualTo(
                          EwbRegistryHelper.getFieldValueForExcel(EWB_MEDIC_SUCCESS, expected)
                                                                                        ),
                  () -> assertThat(actualRow.getCell(11).getStringCellValue()).isEqualTo(
                          EwbRegistryHelper.getFieldValueForExcel(EWB_TELEMECH_SUCCESS, expected)
                                                                                        ),
                  () -> assertThat(actualRow.getCell(12).getStringCellValue()).isEqualTo(expected.get(DRIVING_LICENSE_SERIES.getAlias())),
                  () -> assertThat(actualRow.getCell(13).getStringCellValue()).isEqualTo(expected.get(ATTORNEY_CREATION_SYSTEM.getAlias())),
                  () -> assertThat(actualRow.getCell(14).getStringCellValue()).isEqualTo(EwbStatus.valueOf(
                                                                                                 expected.get(EWB_STATUS.getAlias(), String.class)).getRusName()
                                                                                        )
                 );
    }
    
    private static void checkHeaderRow(XSSFRow row2) {
        assertAll("Checking header",
                  () -> assertThat(row2.getCell(0).getStringCellValue()).isEqualTo("Вид ТС"),
                  () -> assertThat(row2.getCell(1).getStringCellValue()).isEqualTo("Государственный номер"),
                  () -> assertThat(row2.getCell(2).getStringCellValue()).isEqualTo("Дата выдачи прав"),
                  () -> assertThat(row2.getCell(3).getStringCellValue()).isEqualTo("Дата доверенности"),
                  () -> assertThat(row2.getCell(4).getStringCellValue()).isEqualTo("Дата и время создания ЭПЛ"),
                  () -> assertThat(row2.getCell(5).getStringCellValue()).isEqualTo("Дата окончания прав"),
                  () -> assertThat(row2.getCell(6).getStringCellValue()).isEqualTo("Номер доверенности"),
                  () -> assertThat(row2.getCell(7).getStringCellValue()).isEqualTo("Номер прав"),
                  () -> assertThat(row2.getCell(8).getStringCellValue()).isEqualTo("Номер путевого листа"),
                  () -> assertThat(row2.getCell(9).getStringCellValue()).isEqualTo("Подвид ТС"),
                  () -> assertThat(row2.getCell(10).getStringCellValue()).isEqualTo("Результат медицинского контроля"),
                  () -> assertThat(row2.getCell(11).getStringCellValue()).isEqualTo("Результат технического контроля"),
                  () -> assertThat(row2.getCell(12).getStringCellValue()).isEqualTo("Серия прав"),
                  () -> assertThat(row2.getCell(13).getStringCellValue()).isEqualTo("Система хранения доверенности"),
                  () -> assertThat(row2.getCell(14).getStringCellValue()).isEqualTo("Статус Путевого листа")
                 );
    }
    
    private static String createParameters(
            Set<EwbRegistryField> fieldSet,
            String searchText,
            String humanReadableId,
            UUID organizationId,
            Set<UUID> departmentIds,
            String start,
            String end
                                          ) {
        var fieldSetByString = String.join(",", fieldSet.stream()
                                                        .map(EwbRegistryField::name)
                                                        .toList());
        var departmentIdsByString = String.join(",", departmentIds.stream()
                                                                  .map(UUID::toString)
                                                                  .toList());
        return "?fieldSet=" + fieldSetByString +
               (searchText != null ? "&searchText=" + searchText : "") +
               (humanReadableId != null ? "&humanReadableId=" + humanReadableId : "") +
               (organizationId != null ? "&organizationId=" + organizationId : "") +
               (!departmentIds.isEmpty() ? "&departmentIds=" + departmentIdsByString : "") +
               (start != null ? "&start=" + start : "") +
               (end != null ? "&end=" + end : "");
    }
    
    private static void checkLocalDateTimeCell(XSSFRow actualRow, int cellNum, LocalDateTime expected) {
        var cellValue = actualRow.getCell(cellNum).getStringCellValue();
        if (Objects.isNull(cellValue)) {
            assertThat(expected).isNull();
        } else {
            assertThat(LocalDateTime.parse(cellValue, TIME_FORMATTER)).isEqualTo(expected);
        }
    }
    
    private static void checkLocalDateCell(XSSFRow actualRow, int cellNum, LocalDate expected) {
        var cellValue = actualRow.getCell(cellNum).getStringCellValue();
        if (Objects.isNull(cellValue)) {
            assertThat(expected).isNull();
        } else {
            assertThat(LocalDate.parse(cellValue, DATE_FORMATTER)).isEqualTo(expected);
        }
    }
}
