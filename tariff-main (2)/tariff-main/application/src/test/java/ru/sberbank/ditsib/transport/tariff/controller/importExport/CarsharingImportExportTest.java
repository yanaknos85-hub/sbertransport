package ru.sberbank.ditsib.transport.tariff.controller.importExport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
import ru.sber.transport.file_works.services.UploadStates;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.tariff.messaging.TariffMessage;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.tariff.database.dao.*;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;

import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@Slf4j
@DisplayName("Каршеринговые тарифы")
@EmbeddedPostgres
@AutoConfigureMockMvc
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles({"test"})
class CarsharingImportExportTest extends KafkaTest {

    @MockitoBean("tariffOutput")
    private OutputBridge tariffOutput;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UploadStates uploadStates;

    @Autowired
    private CarSharingTariffRepository tariffRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private ContractRepository contractRepository;


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
        tariffRepository.deleteAll();
        for (var i = 0; i < 100; i++) {
            var organization = new Organization();

            organization.setDigitId(i + 1L);
            organization.setName(String.format("Организация %03d", i + 1));
            organization.setActive(true);
            organization.setId(UUID.randomUUID());

            organizationRepository.saveAndFlush(organization);

            var contractor = new Contractor();

            contractor.setId(UUID.randomUUID());
            contractor.setIntegrationType(TaxiExternalIntegrationType.JSON_API_1_0.name());
            contractor.setName(String.format("Контрагент %03d", i + 1));
            contractor.setActive(true);

            contractorRepository.saveAndFlush(contractor);
        }
    }

    @AfterEach
    void after() {
        contractorRepository.deleteAll();
        geoZoneRepository.deleteAll();
        tariffRepository.deleteAll();
        contractRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("Импорт")
    void test_import() throws Exception {
        var count = 100; // в файле
        var contractors = contractorRepository.findAll(Sort.by(Contractor_.NAME));
        List<GeoZone> geoZoneList = new ArrayList<>();
        for (var i = 0; i < count; i++) {
            GeoZone savedGeozone = geoZoneRepository
                    .saveAndFlush(new GeoZone(UUID.randomUUID(), String.format("Регион %03d", i + 1), i + 1 + "", null));
            geoZoneList.add(savedGeozone);
            var transportType = TransportTypeEnum.values()[i % TransportTypeEnum.values().length];

            var contract = new Contract();

            contract.setContractNumber(String.format("Договор %03d", i + 1));
            contract.setContractorId(contractors.get(i).getId());
            contract.setRegionIds(Set.of(savedGeozone.getId()));
            contract.setTransportType(transportType);
            contract.setUserId(UUID.randomUUID());
            contract.setCreationTime(LocalDateTime.now());
            contract.setStartDate(LocalDate.now());
            contract.setEndDate(LocalDate.now().plusDays(1));
            contract.setSum(100L);
            contract.setContractType(ContractType.TRANSITIONAL);
            contract.setRestrictionType(RestrictionType.NONE);
            contract.setActive(true);

            contractRepository.saveAndFlush(contract);
        }

        var file = new MockMultipartFile("file", "file.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/tariffCarsharing.xlsx"));

        assertRequest(file);
        var actualList = tariffRepository.findAll(Sort.by(BaseTariff_.HUMAN_READABLE_ID));
        final var messageCaptor = ArgumentCaptor.forClass(TariffMessage.class);
        verify(tariffOutput, times(count)).send(messageCaptor.capture());
        var messageList = messageCaptor.getAllValues();

        assertAll("Sizes",
                () -> assertThat(actualList).hasSize(count),
                () -> assertThat(messageList).hasSize(count)
        );
        actualList.forEach(item -> log.info(item.toString()));
        messageList.forEach(item -> log.info(item.toString()));
        for (var i = 0; i < count; i++) {
            var actual = actualList.get(i);

            var number = i + 1;
            assertDatabase(geoZoneList, actual, i, number);

            var message = messageList.get(i);
            assertMessage(geoZoneList, actual, i, message);
        }
    }

    private void assertMessage(List<GeoZone> geoZoneList, CarSharingTariff actual, int index, TariffMessage message) {
        assertAll("Checking message index " + index,
                () -> assertThat(message.getId()).isNotNull(),
                () -> assertThat(message.humanReadableId()).isNotNull(),
                () -> assertThat(message.contractId()).isEqualTo(actual.getContract().getId()),
                () -> assertThat(message.regionId()).isEqualTo(geoZoneList.get(index).getId()),
                () -> assertThat(message.transportTypeId()).isEqualTo(TransportTypeEnum.CARSHARING.getId())
        );
    }

    private void assertDatabase(List<GeoZone> geoZoneList, CarSharingTariff actual, int index, int number) {
        assertAll("Checking database index " + index,
                () -> assertThat(actual.getId()).isNotNull(),
                () -> assertThat(actual.getHumanReadableId()).isNotNull(),
                () -> assertThat(actual.getContract().getContractNumber()).isEqualTo(String.format("Договор %03d",
                        number)),
                () -> assertThat(actual.getOrganization().getName()).isEqualTo(String.format(
                        "Организация %03d", number)),
                () -> assertThat(actual.getRegionId()).isEqualTo(geoZoneList.get(index).getId()),
                () -> assertThat(actual.getTransportType()).isEqualTo(TransportTypeEnum.CARSHARING),
                () -> assertThat(actual.getRideCostPerKm()).isEqualTo(number),
                () -> assertThat(actual.getRideCostPerMin()).isEqualTo(100 + number),
                () -> assertThat(actual.getWaitCostPerMin()).isEqualTo(200 + number),
                () -> assertThat(actual.getCoefCasko()).isEqualTo(index % 10 + 1),
                () -> assertThat(actual.getCoefChildSeat()).isEqualTo(10 - index % 10),
                () -> assertThat(actual.getCoefPetTransport()).isEqualTo(index % 10 + 1),
                () -> assertThat(actual.getCoefTraffic()).isEqualTo(10 - index % 10),
                () -> assertThat(actual.getTimedTariffParams().getCoefWorkDayMorning()).isEqualTo(index % 10 + 1),
                () -> assertThat(actual.getTimedTariffParams().getCoefWorkDayNoon()).isEqualTo(10 - index % 10),
                () -> assertThat(actual.getTimedTariffParams().getCoefWorkDayEvening()).isEqualTo(index % 10 + 1),
                () -> assertThat(actual.getTimedTariffParams().getCoefWorkDayNight()).isEqualTo(10 - index % 10),
                () -> assertThat(actual.getTimedTariffParams().getCoefDayOff()).isEqualTo(index % 10 + 1)
        );
    }

    private void assertRequest(MockMultipartFile file) throws Exception {
        final var userId = UUID.randomUUID().toString();
        mockMvc.perform(multipart("/files/tariff").file(file)
                        .with(jwt().jwt(builder -> builder.jti(userId).claim("roles", "ROLE_USER"))))
                .andExpect(status().isOk());

        await().until(() -> uploadStates.getResults("tariff", userId).size(), equalTo(1));
        await().until(() -> uploadStates.getResults("tariff", userId).getFirst().getFinished(), equalTo(true));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("Экспорт")
    void test_export() throws Exception {
        var organizations = organizationRepository.findAll(Sort.by(Organization_.NAME));
        var contractors = contractorRepository.findAll(Sort.by(Contractor_.NAME));

        var count = 100;

        List<GeoZone> geoZoneList = new ArrayList<>();
        for (var i = 0; i < count; i++) {
            GeoZone savedGeozone = geoZoneRepository
                    .save(new GeoZone(UUID.randomUUID(), String.format("Регион %03d", i + 1), i + 1 + "", null));
            geoZoneList.add(savedGeozone);
            var transportType = TransportTypeEnum.values()[i % TransportTypeEnum.values().length];

            var contract = new Contract();

            contract.setContractNumber(String.format("Договор %03d", i + 1));
            contract.setContractorId(contractors.get(i).getId());
            contract.setRegionIds(Set.of(savedGeozone.getId()));
            contract.setTransportType(transportType);
            contract.setUserId(UUID.randomUUID());
            contract.setCreationTime(LocalDateTime.now());
            contract.setStartDate(LocalDate.now());
            contract.setEndDate(LocalDate.now().plusDays(1));
            contract.setSum(100L);
            contract.setContractType(ContractType.TRANSITIONAL);
            contract.setRestrictionType(RestrictionType.NONE);

            contract = contractRepository.save(contract);

            var tariff = new CarSharingTariff();

            tariff.setTransportType(transportType);
            tariff.setOrganization(organizations.get(i));
            tariff.setHumanReadableId("HRI " + i);
            tariff.setContract(contract);
            tariff.setActive(i % 2 == 0);
            tariff.setRegionId(savedGeozone.getId());
            tariff.setTransportType(TransportTypeEnum.CARSHARING);

            tariff.getTimedTariffParams().setCoefDayOff(i);
            tariff.getTimedTariffParams().setCoefWorkDayEvening(i + 100D);
            tariff.getTimedTariffParams().setCoefWorkDayMorning(i + 200D);
            tariff.getTimedTariffParams().setCoefWorkDayNight(i + 300D);
            tariff.getTimedTariffParams().setCoefWorkDayNoon(i + 400D);

            tariff.setRideCostPerKm(i + 700);
            tariff.setRideCostPerMin(i + 800);
            tariff.setWaitCostPerMin(i + 900);
            tariff.setCoefChildSeat(2 + (i / 100D));
            tariff.setCoefCasko(1 + (i / 100D));
            tariff.setCoefPetTransport(3 + (i / 100D));
            tariff.setCoefTraffic(4 + (i / 100D));

            tariffRepository.save(tariff);
        }

        var content = mockMvc.perform(get("/files/tariff")
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
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

        var bytes = mockMvc.perform(get(file)
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsByteArray();

        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet("Каршеринг");

            assertThat(sheet).isNotNull();

            var header1 = sheet.getRow(0);

            assertHeader1(header1);

            var header2 = sheet.getRow(1);

            assertHeader2(header2);

            var expectedList = tariffRepository.findAll();

            final var lastRowNum = sheet.getLastRowNum();
            assertThat(lastRowNum).isEqualTo(count + 1); // 1 заголовок у таблицы. Отсекаем неактивные
            //  rowIndex - индекс строки с учетом заголовков.
            //  На текущий момент строки в excel пишутся не упорядоченно, и нам нужно найти объект из БД по ключу HumanReadableId, и проверить
            //  наличие региона и контракта по названию в списке БД
            for (int rowIndex = 2, i = 0; i < lastRowNum - 1; rowIndex++, i++) {
                assertRow(contractors, geoZoneList, sheet, expectedList, rowIndex, i);
            }
        }
    }

    private void assertRow(List<Contractor> contractors, List<GeoZone> geoZoneList, XSSFSheet sheet, List<CarSharingTariff> expectedList, int rowIndex, int i) {
        var actualRow = sheet.getRow(rowIndex);
        var expectedItemOptional = expectedList.stream()
                .filter(tariff -> tariff.getHumanReadableId().equals(actualRow.getCell(0).getStringCellValue()))
                .findFirst();
        assertThat(expectedItemOptional).isPresent();
        var expectedItem = expectedItemOptional.get();
        int number = expectedList.indexOf(expectedItem);

        assertSoftly(it -> {
            it.assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(expectedItem.getHumanReadableId());
            it.assertThat(actualRow.getCell(1).getBooleanCellValue()).isEqualTo(expectedItem.isActive());
            it.assertThat(contractors.stream().anyMatch(item -> item.getName().equals(actualRow.getCell(2).getStringCellValue()))).isTrue();
            it.assertThat(actualRow.getCell(3).getStringCellValue()).isEqualTo(expectedItem.getContract().getContractNumber());
            it.assertThat(actualRow.getCell(4).getStringCellValue()).isEqualTo(expectedItem.getOrganization().getName());
            it.assertThat(actualRow.getCell(5).getStringCellValue()).isIn(geoZoneList.stream().map(GeoZone::getName).toList());
            it.assertThat(actualRow.getCell(7).getNumericCellValue()).isEqualTo(expectedItem.getRideCostPerKm());
            it.assertThat(actualRow.getCell(8).getNumericCellValue()).isEqualTo(expectedItem.getRideCostPerMin());
            it.assertThat(actualRow.getCell(9).getNumericCellValue()).isEqualTo(expectedItem.getWaitCostPerMin());
            it.assertThat(actualRow.getCell(10).getNumericCellValue()).isEqualTo(1 + (number / 100D));
            it.assertThat(actualRow.getCell(11).getNumericCellValue()).isEqualTo(2 + (number / 100D));
            it.assertThat(actualRow.getCell(12).getNumericCellValue()).isEqualTo(3 + (number / 100D));
            it.assertThat(actualRow.getCell(13).getNumericCellValue()).isEqualTo(4 + (number / 100D));
            it.assertThat(actualRow.getCell(14).getNumericCellValue()).isEqualTo(expectedItem.getTimedTariffParams().getCoefWorkDayMorning());
            it.assertThat(actualRow.getCell(15).getNumericCellValue()).isEqualTo(expectedItem.getTimedTariffParams().getCoefWorkDayNoon());
            it.assertThat(actualRow.getCell(16).getNumericCellValue()).isEqualTo(expectedItem.getTimedTariffParams().getCoefWorkDayEvening());
            it.assertThat(actualRow.getCell(17).getNumericCellValue()).isEqualTo(expectedItem.getTimedTariffParams().getCoefWorkDayNight());
            it.assertThat(actualRow.getCell(18).getNumericCellValue()).isEqualTo(expectedItem.getTimedTariffParams().getCoefDayOff());
        });
    }

    private void assertHeader2(XSSFRow header2) {
        assertThat((Iterable<? extends Cell>) header2).isNotNull();
        assertThat(header2.getPhysicalNumberOfCells()).isEqualTo(12);
        assertAll("Checking header",
                () -> assertThat(header2.getCell(7).getStringCellValue()).isEqualTo("За км"),
                () -> assertThat(header2.getCell(8).getStringCellValue()).isEqualTo("За мин"),
                () -> assertThat(header2.getCell(9).getStringCellValue()).isEqualTo("За мин. ожидания"),
                () -> assertThat(header2.getCell(10).getStringCellValue()).isEqualTo("Коэф. КАСКО"),
                () -> assertThat(header2.getCell(11).getStringCellValue()).isEqualTo("Коэф. детские кресла"),
                () -> assertThat(header2.getCell(12).getStringCellValue()).isEqualTo("Коэф. животные"),
                () -> assertThat(header2.getCell(13).getStringCellValue()).isEqualTo("Коэф. загруженности дорог"),
                () -> assertThat(header2.getCell(14).getStringCellValue()).isEqualTo("07:00 - 10:00"),
                () -> assertThat(header2.getCell(15).getStringCellValue()).isEqualTo("10:00 - 18:00"),
                () -> assertThat(header2.getCell(16).getStringCellValue()).isEqualTo("18:00 - 22:00"),
                () -> assertThat(header2.getCell(17).getStringCellValue()).isEqualTo("22:00 - 07:00"),
                () -> assertThat(header2.getCell(18).getStringCellValue()).isEqualTo("Вых. дни")
        );
    }

    private void assertHeader1(XSSFRow header1) {
        assertThat((Iterable<? extends Cell>) header1).isNotNull();
        assertThat(header1.getPhysicalNumberOfCells()).isEqualTo(9);
        assertAll("Checking header",
                () -> assertThat(header1.getCell(0).getStringCellValue()).isEqualTo("Идентификатор"),
                () -> assertThat(header1.getCell(1).getStringCellValue()).isEqualTo("Активен"),
                () -> assertThat(header1.getCell(2).getStringCellValue()).isEqualTo("Контрагент"),
                () -> assertThat(header1.getCell(3).getStringCellValue()).isEqualTo("Договор"),
                () -> assertThat(header1.getCell(4).getStringCellValue()).isEqualTo("Организация"),
                () -> assertThat(header1.getCell(5).getStringCellValue()).isEqualTo("Регион"),
                () -> assertThat(header1.getCell(7).getStringCellValue()).isEqualTo("Стоимость"),
                () -> assertThat(header1.getCell(10).getStringCellValue()).isEqualTo("Коэффициенты")
        );
    }
}
