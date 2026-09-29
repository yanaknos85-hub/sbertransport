package ru.sberbank.ditsib.transport.vehicle.resolver;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.file_works.database.dao.ExportTaskRepository;
import ru.sber.transport.file_works.database.migrations.DatabaseMigration;
import ru.sberbank.ditsib.transport.vehicle.BaseIntegrationTest;
import ru.sberbank.ditsib.transport.vehicle.dto.files.TransportReportDto;
import ru.sberbank.ditsib.transport.vehicle.mapper.TransportMapper;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Duration;
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
import static ru.sberbank.ditsib.transport.vehicle.constants.Role.ROLE_ADMIN_DATA_MASTER;


@Sql("/scripts/vehicle_integration_test.sql")
@Sql("/scripts/transport_integration_test.sql")
@Sql("/scripts/transport_report_integration_test.sql")
class TransportReportResolverImplTest extends BaseIntegrationTest {
    public static final String FILES_TRANSPORT_URI = "/files/transport/";
    public static final String RESULT_URL_URI = "result_url";
    private static final UUID EMPLOYEE_1 = fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8");
    public static final String SHEET_NAME = "Выгрузка из справочника ТС";
    
    @MockitoSpyBean
    protected TransportMapper transportMapper;
    
    @Autowired
    private DatabaseMigration databaseMigration;
    @Autowired
    private ExportTaskRepository exportTaskRepository;

    @BeforeEach
    void setup() {
        Mockito.reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
    }
    
    @Test
    @DisplayName("Выгрузка ТС по всем организациям")
    void exportTransportReportAllOrg() throws Exception {
        Mockito.reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_DATA_MASTER.name());
        var content = mockMvc.perform(get(FILES_TRANSPORT_URI)
                                              .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                                         .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name()))))
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
                                                       .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name()))))
                           .andExpect(status().isOk())
                           .andReturn()
                           .getResponse().getContentAsByteArray();
        ZipSecureFile.setMinInflateRatio(0.001);
    
        var transportReportProjectionList = transportRepository.findAllByOrganizationId(null);
    
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet(SHEET_NAME);
            assertThat(sheet).isNotNull();
            assertEquals(4, sheet.getPhysicalNumberOfRows());
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("КОНФИДЕНЦИАЛЬНО");
            checkHeaderRow(sheet.getRow(1));
            checkRow(sheet.getRow(2), transportMapper.listTransportReportProjectionToTransportReportDto(transportReportProjectionList).get(0));
            checkRow(sheet.getRow(3), transportMapper.listTransportReportProjectionToTransportReportDto(transportReportProjectionList).get(1));
        }
    }
    
    @Test
    @DisplayName("Выгрузка ТС по организации пользователя")
    void exportTransportReportYourSelfOrg() throws Exception {
        var content = mockMvc.perform(get(FILES_TRANSPORT_URI)
                                              .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
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
                                            .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                                       .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                           .andExpect(status().isOk())
                           .andReturn()
                           .getResponse().getContentAsByteArray();
        ZipSecureFile.setMinInflateRatio(0.001);
        var transportReportProjectionList = transportRepository.findAllByOrganizationId(UUID.fromString("fc73b25b-9564-4560-98b5-abc0f16af9b2"));
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet(SHEET_NAME);
            assertThat(sheet).isNotNull();
            assertEquals(3, sheet.getPhysicalNumberOfRows());
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("КОНФИДЕНЦИАЛЬНО");
            checkHeaderRow(sheet.getRow(1));
            checkRow(sheet.getRow(2), transportMapper.listTransportReportProjectionToTransportReportDto(transportReportProjectionList).get(0));
        }
    }

    @Test
    @DisplayName("Выгрузка ТС по контрагенту и филиалу контрагента")
    void exportTransportReportByContractorIdAndAutoparkId() throws Exception {
        var content = mockMvc.perform(get(FILES_TRANSPORT_URI+"?contractorId=8f4a5468-38dd-493f-8be4-dd930056f80e&autoparkId=90ef9bca-beeb-45af-b8c7-121204e67f15")
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
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
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsByteArray();
        ZipSecureFile.setMinInflateRatio(0.001);
        var transportReportProjectionList = transportRepository.findAllByContractorIdAndAutoparkId(
                UUID.fromString("8f4a5468-38dd-493f-8be4-dd930056f80e"), UUID.fromString("90ef9bca-beeb-45af-b8c7-121204e67f15"));
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet(SHEET_NAME);
            assertThat(sheet).isNotNull();
            assertEquals(3, sheet.getPhysicalNumberOfRows());
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("КОНФИДЕНЦИАЛЬНО");
            checkHeaderRow(sheet.getRow(1));
            checkRow(sheet.getRow(2), transportMapper.listTransportReportProjectionToTransportReportDto(transportReportProjectionList).get(0));
        }
    }

    @Test
    @DisplayName("Выгрузка ТС по контрагенту")
    void exportTransportReportByContractorId() throws Exception {
        var content = mockMvc.perform(get(FILES_TRANSPORT_URI+"?contractorId=8f4a5468-38dd-493f-8be4-dd930056f80e")
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
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
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsByteArray();
        ZipSecureFile.setMinInflateRatio(0.001);
        var transportReportProjectionList = transportRepository.findAllByContractorIdAndAutoparkId(
                UUID.fromString("8f4a5468-38dd-493f-8be4-dd930056f80e"), null);
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet(SHEET_NAME);
            assertThat(sheet).isNotNull();
            assertEquals(3, sheet.getPhysicalNumberOfRows());
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("КОНФИДЕНЦИАЛЬНО");
            checkHeaderRow(sheet.getRow(1));
            checkRow(sheet.getRow(2), transportMapper.listTransportReportProjectionToTransportReportDto(transportReportProjectionList).get(0));
        }
    }
    
    private void checkRow(XSSFRow actualRow, TransportReportDto dto) {
        assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(dto.inventoryNumber());
        assertThat(actualRow.getCell(1).getStringCellValue()).isEqualTo(dto.assetNumber());
        assertThat(actualRow.getCell(2).getStringCellValue()).isEqualTo(dto.officialName());
        assertThat(actualRow.getCell(3).getStringCellValue()).isEqualTo(dto.departmentName());
        assertThat(actualRow.getCell(4).getStringCellValue()).isEqualTo(dto.locationAddress());
        assertThat(actualRow.getCell(5).getStringCellValue()).isEqualTo(dto.parkingAddress());
        assertThat(actualRow.getCell(6).getStringCellValue()).isEqualTo(dto.typeTitle());
        assertThat(actualRow.getCell(7).getStringCellValue()).isEqualTo(dto.subtypeTitle());
        assertThat(actualRow.getCell(8).getStringCellValue()).isEqualTo(dto.stateNumber());
        assertThat(actualRow.getCell(9).getStringCellValue()).isEqualTo(dto.vinCode());
        assertThat(actualRow.getCell(10).getStringCellValue()).isEqualTo(dto.chassisNumber());
        assertThat(actualRow.getCell(11).getStringCellValue()).isEqualTo(dto.bodyNumber());
        assertThat(actualRow.getCell(12).getStringCellValue()).isEqualTo(dto.certificateNumber());
        assertThat(actualRow.getCell(13).getLocalDateTimeCellValue().toLocalDate()).isEqualTo(dto.certificateIssuedDate());
        assertThat(actualRow.getCell(14).getStringCellValue()).isEqualTo(dto.passportNumber());
        assertThat(actualRow.getCell(15).getLocalDateTimeCellValue().toLocalDate()).isEqualTo(dto.passportIssuedDate());
        assertThat(actualRow.getCell(16).getStringCellValue()).isEqualTo(dto.brandByPassport());
        assertThat(actualRow.getCell(17).getStringCellValue()).isEqualTo(dto.modelByPassport());
        assertThat(actualRow.getCell(18).getStringCellValue()).isEqualTo(dto.bodyColor());
        assertThat(actualRow.getCell(19).getStringCellValue()).isEqualTo(dto.telematicsIMEI());
        assertThat(actualRow.getCell(20).getStringCellValue()).isEqualTo(dto.telematicsTitle());
        assertThat(actualRow.getCell(21).getLocalDateTimeCellValue().toLocalDate()).isEqualTo(dto.exploitationStart());
        assertNull(actualRow.getCell(22).getLocalDateTimeCellValue());
        assertThat(Double.valueOf(actualRow.getCell(23).getNumericCellValue()).intValue()).isEqualTo(dto.currentMileage());
        assertThat(actualRow.getCell(24).getStringCellValue()).isEqualTo(dto.status());
        assertThat(Double.valueOf(actualRow.getCell(25).getNumericCellValue()).intValue()).isEqualTo(dto.year());
        assertThat(actualRow.getCell(26).getStringCellValue()).isEqualTo(dto.vehicleType());
        assertThat(actualRow.getCell(27).getStringCellValue()).isEqualTo(dto.modelTitle());
        assertThat(actualRow.getCell(28).getStringCellValue()).isEqualTo(dto.brandTitle());
        assertThat(actualRow.getCell(29).getStringCellValue()).isEqualTo(dto.categoryTitle());
        assertThat(actualRow.getCell(30).getStringCellValue()).isEqualTo(dto.manufacturer());
        assertThat(actualRow.getCell(31).getStringCellValue()).isEqualTo(dto.ecologicalClass());
        assertThat(actualRow.getCell(32).getNumericCellValue()).isEqualTo(dto.enginePower());
        assertThat(actualRow.getCell(33).getStringCellValue()).isEqualTo(dto.engineTypeTitle());
        assertThat(actualRow.getCell(34).getNumericCellValue()).isEqualTo(dto.engineCapacity());
        assertThat(actualRow.getCell(35).getNumericCellValue()).isEqualTo(dto.fuelTankVolume());
        assertThat(actualRow.getCell(36).getStringCellValue()).isEqualTo(dto.fuelTypeTitle());
        assertEquals(0, BigDecimal.valueOf(actualRow.getCell(37).getNumericCellValue()).compareTo(dto.cityConsumptionRate()));
        assertEquals(0, BigDecimal.valueOf(actualRow.getCell(38).getNumericCellValue()).compareTo(dto.countryConsumptionRate()));
        assertEquals(0, BigDecimal.valueOf(actualRow.getCell(39).getNumericCellValue()).compareTo(dto.hybridConsumptionRate()));
        assertThat(actualRow.getCell(40).getStringCellValue()).isEqualTo(dto.driveTitle());
        assertThat(actualRow.getCell(41).getBooleanCellValue()).isEqualTo(dto.mudguardInstalled());
        assertThat(actualRow.getCell(42).getBooleanCellValue()).isEqualTo(dto.spareWheelHolderInstalled());
        assertThat(actualRow.getCell(43).getNumericCellValue()).isEqualTo(dto.weight());
        assertThat(actualRow.getCell(44).getNumericCellValue()).isEqualTo(dto.maxWeight());
        assertThat(actualRow.getCell(45).getNumericCellValue()).isEqualTo(dto.height());
        assertThat(actualRow.getCell(46).getNumericCellValue()).isEqualTo(dto.width());
        assertThat(actualRow.getCell(47).getNumericCellValue()).isEqualTo(dto.length());
        assertThat(actualRow.getCell(48).getNumericCellValue()).isEqualTo(dto.serviceIntervalDays());
        assertThat(actualRow.getCell(49).getNumericCellValue()).isEqualTo(dto.serviceIntervalMileage());
        assertThat(actualRow.getCell(50).getNumericCellValue()).isEqualTo(dto.serviceAuthorizationDays());
        assertThat(actualRow.getCell(51).getNumericCellValue()).isEqualTo(dto.serviceAuthorizationMileage());
        assertThat(actualRow.getCell(52).getStringCellValue()).isEqualTo(dto.bodyTypeTitle());
        assertThat(actualRow.getCell(53).getStringCellValue()).isEqualTo(dto.transmissionTypeTitle());
        assertThat(actualRow.getCell(54).getStringCellValue()).isEqualTo(dto.frontWheelSizeTitle());
        assertThat(actualRow.getCell(55).getStringCellValue()).isEqualTo(dto.rearWheelSizeTitle());
        assertThat(actualRow.getCell(56).getNumericCellValue()).isEqualTo(dto.yearManufactureBegin());
        assertThat(Double.valueOf(actualRow.getCell(57).getNumericCellValue()).intValue()).isEqualTo(dto.yearManufactureEnd());
        assertThat(actualRow.getCell(58).getStringCellValue()).isEqualTo(dto.comment());
        assertThat(actualRow.getCell(59).getStringCellValue()).isEqualTo(dto.accessiblePositionTitle());
    }
    
    private static void checkHeaderRow(XSSFRow row) {
        assertAll("Checking header",
                  () -> assertThat(row.getCell(0).getStringCellValue()).isEqualTo("Инвентарный номер"),
                  () -> assertThat(row.getCell(1).getStringCellValue()).isEqualTo("Номер основного средства"),
                  () -> assertThat(row.getCell(2).getStringCellValue()).isEqualTo("Организация"),
                  () -> assertThat(row.getCell(3).getStringCellValue()).isEqualTo("Подразделение"),
                  () -> assertThat(row.getCell(4).getStringCellValue()).isEqualTo("Место базирование автомобиля"),
                  () -> assertThat(row.getCell(5).getStringCellValue()).isEqualTo("Адрес стоянки автомобиля"),
                  () -> assertThat(row.getCell(6).getStringCellValue()).isEqualTo("Вид"),
                  () -> assertThat(row.getCell(7).getStringCellValue()).isEqualTo("Подвид"),
                  () -> assertThat(row.getCell(8).getStringCellValue()).isEqualTo("Государственный номер ТС"),
                  () -> assertThat(row.getCell(9).getStringCellValue()).isEqualTo("VIN-номер"),
                  () -> assertThat(row.getCell(10).getStringCellValue()).isEqualTo("№ Шасси"),
                  () -> assertThat(row.getCell(11).getStringCellValue()).isEqualTo("№ Кузова"),
                  () -> assertThat(row.getCell(12).getStringCellValue()).isEqualTo("Свидетельство о регистрации (СТС)"),
                  () -> assertThat(row.getCell(13).getStringCellValue()).isEqualTo("Дата выдачи СТС"),
                  () -> assertThat(row.getCell(14).getStringCellValue()).isEqualTo("Номер ПТС"),
                  () -> assertThat(row.getCell(15).getStringCellValue()).isEqualTo("Дата выдачи ПТС"),
                  () -> assertThat(row.getCell(16).getStringCellValue()).isEqualTo("Марка по ПТС"),
                  () -> assertThat(row.getCell(17).getStringCellValue()).isEqualTo("Модель по ПТС"),
                  () -> assertThat(row.getCell(18).getStringCellValue()).isEqualTo("Цвет кузова"),
                  () -> assertThat(row.getCell(19).getStringCellValue()).isEqualTo("EMEI телематики"),
                  () -> assertThat(row.getCell(20).getStringCellValue()).isEqualTo("Наименование телематики"),
                  () -> assertThat(row.getCell(21).getStringCellValue()).isEqualTo("Дата начала эксплуатации"),
                  () -> assertThat(row.getCell(22).getStringCellValue()).isEqualTo("Дата окончания эксплуатации"),
                  () -> assertThat(row.getCell(23).getStringCellValue()).isEqualTo("Текущий пробег"),
                  () -> assertThat(row.getCell(24).getStringCellValue()).isEqualTo("Статус"),
                  () -> assertThat(row.getCell(25).getStringCellValue()).isEqualTo("Год выпуска"),
                  () -> assertThat(row.getCell(26).getStringCellValue()).isEqualTo("Тип ТС"),
                  () -> assertThat(row.getCell(27).getStringCellValue()).isEqualTo("Модель ТС"),
                  () -> assertThat(row.getCell(28).getStringCellValue()).isEqualTo("Марка ТС"),
                  () -> assertThat(row.getCell(29).getStringCellValue()).isEqualTo("Категория ТС"),
                  () -> assertThat(row.getCell(30).getStringCellValue()).isEqualTo("Организация изготовитель (страна)"),
                  () -> assertThat(row.getCell(31).getStringCellValue()).isEqualTo("Экологический класс"),
                  () -> assertThat(row.getCell(32).getStringCellValue()).isEqualTo("Мощность ЛС"),
                  () -> assertThat(row.getCell(33).getStringCellValue()).isEqualTo("Тип двигателя транспортного средства"),
                  () -> assertThat(row.getCell(34).getStringCellValue()).isEqualTo("Объем двигателя"),
                  () -> assertThat(row.getCell(35).getStringCellValue()).isEqualTo("Объем топливного бака"),
                  () -> assertThat(row.getCell(36).getStringCellValue()).isEqualTo("Вид топлива"),
                  () -> assertThat(row.getCell(37).getStringCellValue()).isEqualTo("Расход топлива в городе"),
                  () -> assertThat(row.getCell(38).getStringCellValue()).isEqualTo("Расход топлива в за городом"),
                  () -> assertThat(row.getCell(39).getStringCellValue()).isEqualTo("Смешанный расход топлива(базовый)"),
                  () -> assertThat(row.getCell(40).getStringCellValue()).isEqualTo("Привод"),
                  () -> assertThat(row.getCell(41).getStringCellValue()).isEqualTo("Наличие брызговиков"),
                  () -> assertThat(row.getCell(42).getStringCellValue()).isEqualTo("Держать запасного колеса"),
                  () -> assertThat(row.getCell(43).getStringCellValue()).isEqualTo("Масса без нагрузки"),
                  () -> assertThat(row.getCell(44).getStringCellValue()).isEqualTo("Макс снаряженная масса"),
                  () -> assertThat(row.getCell(45).getStringCellValue()).isEqualTo("Высота, мм"),
                  () -> assertThat(row.getCell(46).getStringCellValue()).isEqualTo("Ширина, мм"),
                  () -> assertThat(row.getCell(47).getStringCellValue()).isEqualTo("Длина, мм"),
                  () -> assertThat(row.getCell(48).getStringCellValue()).isEqualTo("Межсервисный интервал по времени"),
                  () -> assertThat(row.getCell(49).getStringCellValue()).isEqualTo("Межсервисный интервал по пробегу"),
                  () -> assertThat(row.getCell(50).getStringCellValue()).isEqualTo("Допуск по времени"),
                  () -> assertThat(row.getCell(51).getStringCellValue()).isEqualTo("Допуск по пробегу"),
                  () -> assertThat(row.getCell(52).getStringCellValue()).isEqualTo("Тип кузова"),
                  () -> assertThat(row.getCell(53).getStringCellValue()).isEqualTo("Тип трансмиссии"),
                  () -> assertThat(row.getCell(54).getStringCellValue()).isEqualTo("Размер переднего колеса"),
                  () -> assertThat(row.getCell(55).getStringCellValue()).isEqualTo("Размер заднего колеса"),
                  () -> assertThat(row.getCell(56).getStringCellValue()).isEqualTo("Год начала производства"),
                  () -> assertThat(row.getCell(57).getStringCellValue()).isEqualTo("Год снятия с производства"),
                  () -> assertThat(row.getCell(58).getStringCellValue()).isEqualTo("Комментарий"),
                  () -> assertThat(row.getCell(59).getStringCellValue()).isEqualTo("Закрепление за должностью"));

    }
}