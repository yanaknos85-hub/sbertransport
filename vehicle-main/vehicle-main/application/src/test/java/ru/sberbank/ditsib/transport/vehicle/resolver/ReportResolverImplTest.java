package ru.sberbank.ditsib.transport.vehicle.resolver;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.file_works.database.dao.ExportTaskRepository;
import ru.sber.transport.file_works.database.migrations.DatabaseMigration;
import ru.sber.transport.file_works.exporter.impl.XlsxExporter;
import ru.sberbank.ditsib.transport.vehicle.BaseIntegrationTest;
import ru.sberbank.ditsib.transport.vehicle.LoggingExtension;
import ru.sberbank.ditsib.transport.vehicle.constants.ReportType;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.fromString;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.ditsib.transport.vehicle.constants.Role.ROLE_ADMIN_CORP_CLIENT;

@Sql("/scripts/vehicle_integration_test.sql")
@Sql("/scripts/transport_integration_test.sql")
@Sql("/scripts/indicators_integration_test.sql")
class ReportResolverImplTest extends BaseIntegrationTest {
    public static final String FILES_REGISTRY_URI = "/files/indicators/";
    public static final String RESULT_URL_URI = "result_url";
    private static final UUID EMPLOYEE_1 = fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8");
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION_1 = new LoggingExtension(ReportResolverImpl.class);
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION_2 = new LoggingExtension(XlsxExporter.class);
    private static final String STATE_NUMBER_1 = "А111АА111";
    private static final String STATE_NUMBER_2 = "А111АА116";
    private static final String STATE_NUMBER_3 = "А777АА78";
    public static final String SHEET_NAME = "Отчет по показателям";
    public static final String REPORT_YEAR_PARAMETER_NAME = "year";
    public static final String REPORT_TYPE_PARAMETER_NAME = "reportType";
    
    @Autowired
    private DatabaseMigration databaseMigration;
    @Autowired
    private ExportTaskRepository exportTaskRepository;
    
    @BeforeEach
    void setup() {
        Mockito.reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
    }
    
    @AfterEach
    void deleteTestFolder() {
        attorneyRepository.deleteAll();
        fuelConsumptionRepository.deleteAll();
        odometerValueRepository.deleteAll();
        transportRepository.deleteAll();
        vehicleRepository.deleteAll();
        fuelTypeRepository.deleteAll();
        modelRepository.deleteAll();
        subtypeRepository.deleteAll();
        typeRepository.deleteAll();
        brandRepository.deleteAll();
        categoryRepository.deleteAll();
        engineTypeRepository.deleteAll();
        driveRepository.deleteAll();
        telematicsRepository.deleteAll();
        employeeRepository.deleteAll();
        positionRepository.deleteAll();
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Экспорт отчета, некорректный запрос - нет параметров")
    void exportNoParameters() throws Exception {
        
        var content = mockMvc.perform(get(FILES_REGISTRY_URI)
                                              .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                                         .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                             .andExpect(status().isOk())
                             .andReturn()
                             .getResponse().getContentAsString();
        checkLogs(content, "Не заполнен параметр фильтрации year");
    }
    
    @Test
    @DisplayName("Экспорт отчета, некорректный запрос - нет параметра year")
    void exportNoYearParameter() throws Exception {
        var content = mockMvc.perform(get(FILES_REGISTRY_URI)
                                              .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                                         .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                              .param(REPORT_TYPE_PARAMETER_NAME, "ODOMETER"))
                             .andExpect(status().isOk())
                             .andReturn()
                             .getResponse().getContentAsString();
        checkLogs(content, "Не заполнен параметр фильтрации year");
    }
    
    @Test
    @DisplayName("Экспорт отчета, некорректный запрос - нет параметра reportType")
    void exportNoReportTypeParameter() throws Exception {
        var content = mockMvc.perform(get(FILES_REGISTRY_URI)
                                              .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                                         .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                              .param(REPORT_YEAR_PARAMETER_NAME, "2024"))
                             .andExpect(status().isOk())
                             .andReturn()
                             .getResponse().getContentAsString();
        checkLogs(content, "Не заполнен параметр фильтрации reportType");
    }
    
    @Test
    @DisplayName("Экспорт, тип отчета - одометр")
    void exportOdometer() throws Exception {
        var content = mockMvc.perform(get(FILES_REGISTRY_URI)
                                              .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                                         .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                              .param(REPORT_YEAR_PARAMETER_NAME, "2024")
                                              .param(REPORT_TYPE_PARAMETER_NAME, ReportType.ODOMETER.name())
                                     )
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
                                            .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                                       .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                           .andExpect(status().isOk())
                           .andReturn()
                           .getResponse().getContentAsByteArray();
        ZipSecureFile.setMinInflateRatio(0.001);
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet(SHEET_NAME);
            assertThat(sheet).isNotNull();
            checkHeaderRow(sheet.getRow(0));
            var expectedRow1 = detectRow(sheet, STATE_NUMBER_1);
            var expectedRow2 = detectRow(sheet, STATE_NUMBER_2);
            var expectedRow3 = detectRow(sheet, STATE_NUMBER_3);
            checkOdometerRow(expectedRow1,
                             STATE_NUMBER_1,
                             "Лада",
                             "2114",
                             "баd234234",
                             2007,
                             LocalDate.of(2023, 3, 1),
                             true);
            checkOdometerRow(expectedRow2,
                             STATE_NUMBER_2,
                             "Лада",
                             "2114",
                             "6111sssssssss",
                             2007,
                             LocalDate.of(2023, 3, 1),
                             false);
            checkOdometerRow(expectedRow3,
                             STATE_NUMBER_3,
                             "Changan",
                             "V90",
                             "W0934234143131",
                             2024,
                             LocalDate.of(2023, 3, 10),
                             false);
        }
    }
    
    @Test
    @DisplayName("Экспорт, тип отчета - топливо")
    void exportFuel() throws Exception {
        var content = mockMvc.perform(get(FILES_REGISTRY_URI)
                                              .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                                         .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                              .param(REPORT_YEAR_PARAMETER_NAME, "2024")
                                              .param(REPORT_TYPE_PARAMETER_NAME, ReportType.FUEL.name())
                                     )
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
                                            .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                                       .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                           .andExpect(status().isOk())
                           .andReturn()
                           .getResponse().getContentAsByteArray();
        ZipSecureFile.setMinInflateRatio(0.001);
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet(SHEET_NAME);
            assertThat(sheet).isNotNull();
            checkHeaderRow(sheet.getRow(0));
            var expectedRow1 = detectRow(sheet, STATE_NUMBER_1);
            var expectedRow2 = detectRow(sheet, STATE_NUMBER_2);
            var expectedRow3 = detectRow(sheet, STATE_NUMBER_3);
            checkFuelRow(expectedRow1,
                         STATE_NUMBER_1,
                         "Лада",
                         "2114",
                         "баd234234",
                         2007,
                         LocalDate.of(2023, 3, 1),
                         true);
            checkFuelRow(expectedRow2,
                         STATE_NUMBER_2,
                         "Лада",
                         "2114",
                         "6111sssssssss",
                         2007,
                         LocalDate.of(2023, 3, 1),
                         false);
            checkFuelRow(expectedRow3,
                         STATE_NUMBER_3,
                         "Changan",
                         "V90",
                         "W0934234143131",
                         2024,
                         LocalDate.of(2023, 3, 10),
                         false);
        }
    }
    
    @Test
    @DisplayName("Экспорт, тип отчета - пробег")
    void exportMileage() throws Exception {
        var content = mockMvc.perform(get(FILES_REGISTRY_URI)
                                              .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                                         .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                              .param(REPORT_YEAR_PARAMETER_NAME, "2024")
                                              .param(REPORT_TYPE_PARAMETER_NAME, ReportType.MILEAGE.name())
                                     )
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
                                            .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                                       .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                           .andExpect(status().isOk())
                           .andReturn()
                           .getResponse().getContentAsByteArray();
        ZipSecureFile.setMinInflateRatio(0.001);
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet(SHEET_NAME);
            assertThat(sheet).isNotNull();
            checkHeaderRow(sheet.getRow(0));
            var expectedRow1 = detectRow(sheet, STATE_NUMBER_1);
            var expectedRow2 = detectRow(sheet, STATE_NUMBER_2);
            var expectedRow3 = detectRow(sheet, STATE_NUMBER_3);
            checkMileageRow(expectedRow1,
                            STATE_NUMBER_1,
                            "Лада",
                            "2114",
                            "баd234234",
                            2007,
                            LocalDate.of(2023, 3, 1),
                            true);
            checkMileageRow(expectedRow2,
                            STATE_NUMBER_2,
                            "Лада",
                            "2114",
                            "6111sssssssss",
                            2007,
                            LocalDate.of(2023, 3, 1),
                            false);
            checkMileageRow(expectedRow3,
                            STATE_NUMBER_3,
                            "Changan",
                            "V90",
                            "W0934234143131",
                            2024,
                            LocalDate.of(2023, 3, 10),
                            false);
        }
    }
    
    private XSSFRow detectRow(XSSFSheet sheet, String stateNumber) {
        if (sheet.getRow(1).getCell(2).getStringCellValue().equals(stateNumber)) {
            return sheet.getRow(1);
        }
        if (sheet.getRow(2).getCell(2).getStringCellValue().equals(stateNumber)) {
            return sheet.getRow(2);
        }
        if (sheet.getRow(3).getCell(2).getStringCellValue().equals(stateNumber)) {
            return sheet.getRow(3);
        }
        return null;
    }
    
    private void checkLogs(String content, String message) throws JsonProcessingException {
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
        assertEquals(1, LOGGING_EXTENSION_1.getEvents().size());
        var firstEvent = LOGGING_EXTENSION_1.getEvents().get(0);
        assertEquals(ReportResolverImpl.class.getName(), firstEvent.getLoggerName());
        assertEquals(message, firstEvent.getFormattedMessage());
        assertEquals(Level.INFO, firstEvent.getLevel());
        assertEquals(1, LOGGING_EXTENSION_2.getEvents().size());
        var secondEvent = LOGGING_EXTENSION_2.getEvents().get(0);
        assertEquals(XlsxExporter.class.getName(), secondEvent.getLoggerName());
        assertTrue(secondEvent.getFormattedMessage().contains("File exported to "));
        assertEquals(Level.INFO, secondEvent.getLevel());
    }
    
    private void checkMileageRow(
            XSSFRow actualRow, String stateNumber, String brand, String model, String vin, int year, LocalDate exploitationStart,
            boolean checkValues
                                ) {
        checkRow(actualRow, stateNumber, brand, model, vin, year, exploitationStart, ReportType.MILEAGE.getDescription());
        if (checkValues) {
            assertEquals(700, actualRow.getCell(12).getNumericCellValue());
            assertEquals(200, actualRow.getCell(13).getNumericCellValue());
            assertEquals(2000, actualRow.getCell(14).getNumericCellValue());
            assertEquals(1000, actualRow.getCell(15).getNumericCellValue());
            assertEquals(1000, actualRow.getCell(16).getNumericCellValue());
        } else {
            assertTrue(actualRow.getCell(12).getStringCellValue().isEmpty());
            assertTrue(actualRow.getCell(13).getStringCellValue().isEmpty());
            assertTrue(actualRow.getCell(14).getStringCellValue().isEmpty());
            assertTrue(actualRow.getCell(15).getStringCellValue().isEmpty());
            assertTrue(actualRow.getCell(16).getStringCellValue().isEmpty());
        }
    }
    
    private void checkFuelRow(
            XSSFRow actualRow, String stateNumber, String brand, String model, String vin, int year, LocalDate exploitationStart,
            boolean checkValues
                             ) {
        checkRow(actualRow, stateNumber, brand, model, vin, year, exploitationStart, ReportType.FUEL.getDescription());
        if (checkValues) {
            assertEquals(380, actualRow.getCell(12).getNumericCellValue());
            assertEquals(400, actualRow.getCell(13).getNumericCellValue());
            assertEquals(200, actualRow.getCell(14).getNumericCellValue());
            assertEquals(300, actualRow.getCell(15).getNumericCellValue());
            assertEquals(400, actualRow.getCell(16).getNumericCellValue());
        } else {
            assertTrue(actualRow.getCell(12).getStringCellValue().isEmpty());
            assertTrue(actualRow.getCell(13).getStringCellValue().isEmpty());
            assertTrue(actualRow.getCell(14).getStringCellValue().isEmpty());
            assertTrue(actualRow.getCell(15).getStringCellValue().isEmpty());
            assertTrue(actualRow.getCell(16).getStringCellValue().isEmpty());
        }
    }
    
    private void checkOdometerRow(
            XSSFRow actualRow, String stateNumber, String brand, String model, String vin, int year, LocalDate exploitationStart,
            boolean checkValues
                                 ) {
        checkRow(actualRow, stateNumber, brand, model, vin, year, exploitationStart, ReportType.ODOMETER.getDescription());
        if (checkValues) {
            assertEquals(9800, actualRow.getCell(12).getNumericCellValue());
            assertEquals(10000, actualRow.getCell(13).getNumericCellValue());
            assertEquals(12000, actualRow.getCell(14).getNumericCellValue());
            assertEquals(13000, actualRow.getCell(15).getNumericCellValue());
            assertEquals(14000, actualRow.getCell(16).getNumericCellValue());
        } else {
            assertTrue(actualRow.getCell(12).getStringCellValue().isEmpty());
            assertTrue(actualRow.getCell(13).getStringCellValue().isEmpty());
            assertTrue(actualRow.getCell(14).getStringCellValue().isEmpty());
            assertTrue(actualRow.getCell(15).getStringCellValue().isEmpty());
            assertTrue(actualRow.getCell(16).getStringCellValue().isEmpty());
        }
    }
    
    private void checkRow(
            XSSFRow actualRow, String stateNumber, String brand, String model, String vin, int year, LocalDate exploitationStart,
            String reportType
                         ) {
        assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo("Дальневосточный банк (ДВБ)");
        assertThat(actualRow.getCell(1).getStringCellValue()).isEqualTo("10110907");
        assertThat(actualRow.getCell(2).getStringCellValue()).isEqualTo(stateNumber);
        assertThat(actualRow.getCell(3).getStringCellValue()).isEqualTo(brand);
        assertThat(actualRow.getCell(4).getStringCellValue()).isEqualTo(model);
        assertThat(actualRow.getCell(5).getStringCellValue()).isEqualTo(vin);
        assertThat(actualRow.getCell(6).getNumericCellValue()).isEqualTo(year);
        assertThat(actualRow.getCell(7).getLocalDateTimeCellValue().toLocalDate()).isEqualTo(exploitationStart);
        assertThat(actualRow.getCell(8).getStringCellValue()).isEqualTo("Легковой");
        assertThat(actualRow.getCell(9).getStringCellValue()).isEqualTo("На всякий");
        assertThat(actualRow.getCell(10).getNumericCellValue()).isEqualTo(2024);
        assertThat(actualRow.getCell(11).getStringCellValue()).isEqualTo(reportType);
        assertThat(actualRow.getCell(17).getStringCellValue()).isEmpty();
        assertThat(actualRow.getCell(18).getStringCellValue()).isEmpty();
        assertThat(actualRow.getCell(19).getStringCellValue()).isEmpty();
        assertThat(actualRow.getCell(20).getStringCellValue()).isEmpty();
        assertThat(actualRow.getCell(21).getStringCellValue()).isEmpty();
        assertThat(actualRow.getCell(22).getStringCellValue()).isEmpty();
        assertThat(actualRow.getCell(23).getStringCellValue()).isEmpty();
        
    }
    
    private static void checkHeaderRow(XSSFRow row) {
        assertAll("Checking header",
                  () -> assertThat(row.getCell(0).getStringCellValue()).isEqualTo("Организация"),
                  () -> assertThat(row.getCell(1).getStringCellValue()).isEqualTo("Орг. Единица"),
                  () -> assertThat(row.getCell(2).getStringCellValue()).isEqualTo("Государственный номер"),
                  () -> assertThat(row.getCell(3).getStringCellValue()).isEqualTo("Марка"),
                  () -> assertThat(row.getCell(4).getStringCellValue()).isEqualTo("Модель"),
                  () -> assertThat(row.getCell(5).getStringCellValue()).isEqualTo("VIN"),
                  () -> assertThat(row.getCell(6).getStringCellValue()).isEqualTo("Год выпуска"),
                  () -> assertThat(row.getCell(7).getStringCellValue()).isEqualTo("Дата начала эксплуатации"),
                  () -> assertThat(row.getCell(8).getStringCellValue()).isEqualTo("Вид ТС"),
                  () -> assertThat(row.getCell(9).getStringCellValue()).isEqualTo("Подвид ТС"),
                  () -> assertThat(row.getCell(10).getStringCellValue()).isEqualTo("Отчетный год"),
                  () -> assertThat(row.getCell(11).getStringCellValue()).isEqualTo("Вид отчета"),
                  () -> assertThat(row.getCell(12).getStringCellValue()).isEqualTo("Январь"),
                  () -> assertThat(row.getCell(13).getStringCellValue()).isEqualTo("Февраль"),
                  () -> assertThat(row.getCell(14).getStringCellValue()).isEqualTo("Март"),
                  () -> assertThat(row.getCell(15).getStringCellValue()).isEqualTo("Апрель"),
                  () -> assertThat(row.getCell(16).getStringCellValue()).isEqualTo("Май"),
                  () -> assertThat(row.getCell(17).getStringCellValue()).isEqualTo("Июнь"),
                  () -> assertThat(row.getCell(18).getStringCellValue()).isEqualTo("Июль"),
                  () -> assertThat(row.getCell(19).getStringCellValue()).isEqualTo("Август"),
                  () -> assertThat(row.getCell(20).getStringCellValue()).isEqualTo("Сентябрь"),
                  () -> assertThat(row.getCell(21).getStringCellValue()).isEqualTo("Октябрь"),
                  () -> assertThat(row.getCell(22).getStringCellValue()).isEqualTo("Ноябрь"),
                  () -> assertThat(row.getCell(23).getStringCellValue()).isEqualTo("Декабрь")
                 );
    }
}