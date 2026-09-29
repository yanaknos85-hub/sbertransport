package ru.sber.transport.telemechanic.resolver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jooq.Record;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import ru.sber.transport.telemechanic.database.dao.MedicRequestRegistryDynamicRepository;
import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestSearchDto;
import ru.sber.transport.telemechanic.enumerate.MedicRequestField;
import ru.sber.transport.telemechanic.helper.MedicRequestRegistryHelper;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.telemechanic.enumerate.MedicRequestField.*;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_ADMIN_CORP_CLIENT;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres
@Sql(scripts = {
        "/scripts/basic_corp_structure.sql",
        "/scripts/ewb_integration_test.sql"
})
@Sql(value = "/scripts/cleanup_database.sql", executionPhase = AFTER_TEST_METHOD)
class MedicRequestRegistrySelfOrganizationResolverImplTest {
    
    public static final String FILES_REGISTRY_URI = "/files/medic-request-self-organization";
    public static final String RESULT_URL_URI = "result_url";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    private static final Set<MedicRequestField> FIELD_SET =
            Set.of(EWB_UUID, EWB_HUMAN_READABLE_ID, EWB_MEDIC_DECISION_TIME, MEDIC_REQUEST_HUMAN_READABLE_ID, MEDIC_REQUEST_SYSTOLIC_PRESSURE,
                   MEDIC_REQUEST_DIASTOLIC_PRESSURE, MEDIC_REQUEST_PULSE, MEDIC_REQUEST_TEMPERATURE, MEDIC_REQUEST_BREATH_ALCOHOL_TEST_RESULT,
                   MEDIC_REQUEST_STATUS, MEDIC_FULL_NAME, MEDIC_PERSONNEL_NUMBER, MEDIC_ORGANIZATION_NAME, MEDIC_DEPARTMENT_NAME,
                   MEDIC_LICENSE_SERIES, MEDIC_LICENSE_NUMBER, MEDIC_LICENSE_ISSUE_DATE, MEDIC_LICENSE_EXPIRY_DATE, DRIVER_FULL_NAME,
                   DRIVER_PERSONNEL_NUMBER, DRIVER_ORGANIZATION_NAME, DRIVER_DEPARTMENT_NAME, DRIVER_TIN, DRIVER_LICENSE_SERIES,
                   DRIVER_LICENSE_NUMBER, DRIVER_LICENSE_ISSUE_DATE, DRIVER_LICENSE_EXPIRY_DATE);
    
    @MockitoBean
    private AuthorizationManager<?> manager;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DatabaseMigration databaseMigration;
    @Autowired
    private ExportTaskRepository exportTaskRepository;
    @Autowired
    private MedicRequestRegistryDynamicRepository medicRequestRegistryDynamicRepository;
    
    @ParameterizedTest
    @MethodSource("parameters")
    @SneakyThrows
    void export(
            Set<MedicRequestField> fieldSet, String searchText, String personnelNumber,
            String humanReadableId, UUID organizationId, Set<UUID> departmentIds, DateRange dateRange
               ) {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var parameters = createParameters(fieldSet,
                                          searchText,
                                          personnelNumber,
                                          humanReadableId,
                                          organizationId,
                                          departmentIds,
                                          dateRange);
        var content = mockMvc.perform(get(FILES_REGISTRY_URI + parameters)
                                              .with(jwt().jwt(builder -> builder.jti("95c2bd4f-3a79-4b30-8d7e-9308e751b40a"))
                                                         .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
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
                                            .with(jwt().jwt(builder -> builder.jti("95c2bd4f-3a79-4b30-8d7e-9308e751b40a"))
                                                       .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
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
            var expectedList = medicRequestRegistryDynamicRepository.findMedicRequestExcel(
                    new MedicRequestSearchDto(fieldSet,
                                              searchText,
                                              personnelNumber,
                                              humanReadableId,
                                              organizationId,
                                              departmentIds,
                                              dateRange)
                                                                                          );
            for (int rowIndex = 3, index = 0; index < expectedList.size(); rowIndex++, index++) {
                checkRow(rowIndex, index, sheet.getRow(rowIndex), expectedList.get(index));
            }
        }
    }
    
    static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of(FIELD_SET, null, null, null, null, Set.of(), null),
                Arguments.of(FIELD_SET,
                             "0000",
                             "1913586",
                             "0002",
                             UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"),
                             Set.of(UUID.fromString("d640b449-4482-4a48-bc05-2bbcfa66b465")),
                             new DateRange(
                                     LocalDateTime.of(2023, 1, 1, 0, 0, 0),
                                     LocalDateTime.of(2025, 1, 1, 0, 0, 0)
                             )
                            )
                        );
    }
    
    private static void checkRow(int rowIndex, int index, XSSFRow actualRow, Record expected) {
        assertAll("Checking row " + rowIndex + " equals with element " + index,
                  () -> assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(MEDIC_REQUEST_DIASTOLIC_PRESSURE, expected)
                                                                                       ),
                  () -> assertThat(actualRow.getCell(1).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(MEDIC_REQUEST_SYSTOLIC_PRESSURE, expected)
                                                                                       ),
                  () -> checkLocalDateCell(actualRow, 2, LocalDate.parse(
                          MedicRequestRegistryHelper.getFieldValueForExcel(DRIVER_LICENSE_ISSUE_DATE, expected), DATE_FORMATTER
                                                                        )),
                  () -> checkLocalDateCell(actualRow, 3, LocalDate.parse(
                          MedicRequestRegistryHelper.getFieldValueForExcel(MEDIC_LICENSE_ISSUE_DATE, expected), DATE_FORMATTER
                                                                        )),
                  () -> checkLocalDateTimeCell(actualRow, LocalDateTime.parse(
                          MedicRequestRegistryHelper.getFieldValueForExcel(EWB_MEDIC_DECISION_TIME, expected), DATE_TIME_FORMATTER
                                                                             )),
                  () -> checkLocalDateCell(actualRow, 5, LocalDate.parse(
                          MedicRequestRegistryHelper.getFieldValueForExcel(DRIVER_LICENSE_EXPIRY_DATE, expected), DATE_FORMATTER
                                                                        )),
                  () -> checkLocalDateCell(actualRow, 6, LocalDate.parse(
                          MedicRequestRegistryHelper.getFieldValueForExcel(MEDIC_LICENSE_EXPIRY_DATE, expected), DATE_FORMATTER
                                                                        )),
                  () -> assertThat(actualRow.getCell(7).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(DRIVER_TIN, expected)
                                                                                       ),
                  () -> assertThat(actualRow.getCell(8).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(EWB_HUMAN_READABLE_ID, expected)
                                                                                       ),
                  () -> assertThat(actualRow.getCell(9).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(EWB_UUID, expected)
                                                                                       ),
                  () -> assertThat(actualRow.getCell(10).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(DRIVER_LICENSE_NUMBER, expected)
                                                                                        ),
                  () -> assertThat(actualRow.getCell(11).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(MEDIC_LICENSE_NUMBER, expected)
                                                                                        ),
                  () -> assertThat(actualRow.getCell(12).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(MEDIC_REQUEST_HUMAN_READABLE_ID, expected)
                                                                                        ),
                  () -> assertThat(actualRow.getCell(13).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(DRIVER_ORGANIZATION_NAME, expected)
                                                                                        ),
                  () -> assertThat(actualRow.getCell(14).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(MEDIC_ORGANIZATION_NAME, expected)
                                                                                        ),
                  () -> assertThat(actualRow.getCell(15).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(MEDIC_REQUEST_STATUS, expected)
                                                                                        ),
                  () -> assertThat(actualRow.getCell(16).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(DRIVER_DEPARTMENT_NAME, expected)
                                                                                        ),
                  () -> assertThat(actualRow.getCell(17).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(MEDIC_DEPARTMENT_NAME, expected)
                                                                                        ),
                  () -> assertThat(actualRow.getCell(18).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(MEDIC_REQUEST_BREATH_ALCOHOL_TEST_RESULT, expected)
                                                                                        ),
                  () -> assertThat(actualRow.getCell(19).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(MEDIC_REQUEST_PULSE, expected)
                                                                                        ),
                  () -> assertThat(actualRow.getCell(20).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(DRIVER_LICENSE_SERIES, expected)
                                                                                        ),
                  () -> assertThat(actualRow.getCell(21).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(MEDIC_LICENSE_SERIES, expected)
                                                                                        ),
                  () -> assertThat(actualRow.getCell(22).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(MEDIC_PERSONNEL_NUMBER, expected)
                                                                                        ),
                  () -> assertThat(actualRow.getCell(23).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(DRIVER_PERSONNEL_NUMBER, expected)
                                                                                        ),
                  () -> assertThat(actualRow.getCell(24).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(MEDIC_REQUEST_TEMPERATURE, expected)
                                                                                        ),
                  () -> assertThat(actualRow.getCell(25).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(MEDIC_FULL_NAME, expected)
                                                                                        ),
                  () -> assertThat(actualRow.getCell(26).getStringCellValue()).isEqualTo(
                          MedicRequestRegistryHelper.getFieldValueForExcel(DRIVER_FULL_NAME, expected)
                                                                                        )
                 
                 );
    }
    
    private static void checkHeaderRow(XSSFRow row2) {
        assertAll("Checking header",
                  () -> assertThat(row2.getCell(0).getStringCellValue()).isEqualTo("Давление артериальное Диастолическое"),
                  () -> assertThat(row2.getCell(1).getStringCellValue()).isEqualTo("Давление артериальное Систолическое"),
                  () -> assertThat(row2.getCell(2).getStringCellValue()).isEqualTo("Дата выдачи водительских прав"),
                  () -> assertThat(row2.getCell(3).getStringCellValue()).isEqualTo("Дата выдачи лицензии"),
                  () -> assertThat(row2.getCell(4).getStringCellValue()).isEqualTo(
                          "Дата и время проведения предсменного, предрейсового медицинского осмотра"),
                  () -> assertThat(row2.getCell(5).getStringCellValue()).isEqualTo("Дата истечения водительских прав"),
                  () -> assertThat(row2.getCell(6).getStringCellValue()).isEqualTo("Дата окончания срока действия лицензии"),
                  () -> assertThat(row2.getCell(7).getStringCellValue()).isEqualTo("ИНН водителя"),
                  () -> assertThat(row2.getCell(8).getStringCellValue()).isEqualTo("Номер ЭПЛ"),
                  () -> assertThat(row2.getCell(9).getStringCellValue()).isEqualTo("Номер ЭПЛ ГИС ЭПД"),
                  () -> assertThat(row2.getCell(10).getStringCellValue()).isEqualTo("Номер водительских прав"),
                  () -> assertThat(row2.getCell(11).getStringCellValue()).isEqualTo("Номер лицензии"),
                  () -> assertThat(row2.getCell(12).getStringCellValue()).isEqualTo("Номер медицинского осмотра"),
                  () -> assertThat(row2.getCell(13).getStringCellValue()).isEqualTo("Организация водителя"),
                  () -> assertThat(row2.getCell(14).getStringCellValue()).isEqualTo("Организация мед работника"),
                  () -> assertThat(row2.getCell(15).getStringCellValue()).isEqualTo("Отметка о результате проведения предсменного, предрейсового " +
                                                                                    "медицинского осмотра"),
                  () -> assertThat(row2.getCell(16).getStringCellValue()).isEqualTo("Подразделение водителя"),
                  () -> assertThat(row2.getCell(17).getStringCellValue()).isEqualTo("Подразделение мед работника"),
                  () -> assertThat(row2.getCell(18).getStringCellValue()).isEqualTo("Показание алкоголя"),
                  () -> assertThat(row2.getCell(19).getStringCellValue()).isEqualTo("Пульс"),
                  () -> assertThat(row2.getCell(20).getStringCellValue()).isEqualTo("Серия водительских прав"),
                  () -> assertThat(row2.getCell(21).getStringCellValue()).isEqualTo("Серия лицензии"),
                  () -> assertThat(row2.getCell(22).getStringCellValue()).isEqualTo("Табельный  номер мед работника"),
                  () -> assertThat(row2.getCell(23).getStringCellValue()).isEqualTo("Табельный номер водителя"),
                  () -> assertThat(row2.getCell(24).getStringCellValue()).isEqualTo("Температура"),
                  () -> assertThat(row2.getCell(25).getStringCellValue()).isEqualTo("ФИО Медицинского работника"),
                  () -> assertThat(row2.getCell(26).getStringCellValue()).isEqualTo("ФИО водителя")
                 );
    }
    
    private static void checkLocalDateTimeCell(XSSFRow actualRow, LocalDateTime expected) {
        var cellValue = actualRow.getCell(4).getStringCellValue();
        if (Objects.isNull(cellValue)) {
            assertThat(expected).isNull();
        } else {
            assertThat(LocalDateTime.parse(cellValue, DATE_TIME_FORMATTER)).isEqualTo(expected);
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
    
    private static String createParameters(
            Set<MedicRequestField> fieldSet,
            String searchText,
            String personnelNumber,
            String humanReadableId,
            UUID organizationId,
            Set<UUID> departmentIds,
            DateRange dateRange
                                          ) {
        var fieldSetByString = String.join(",", fieldSet.stream()
                                                        .map(Enum::name)
                                                        .toList());
        var departmentIdsByString = String.join(",", departmentIds.stream()
                                                                  .map(UUID::toString)
                                                                  .toList());
        return "?fieldSet=" + fieldSetByString +
               (searchText != null ? "&searchText=" + searchText : "") +
               (personnelNumber != null ? "&personnelNumber=" + personnelNumber : "") +
               (humanReadableId != null ? "&humanReadableId=" + humanReadableId : "") +
               (organizationId != null ? "&organizationId=" + organizationId : "") +
               (!departmentIds.isEmpty() ? "&departmentIds=" + departmentIdsByString : "") +
               (dateRange != null
                ? "&start=" + dateRange.start() + "&end=" + dateRange.end()
                : "");
    }
}