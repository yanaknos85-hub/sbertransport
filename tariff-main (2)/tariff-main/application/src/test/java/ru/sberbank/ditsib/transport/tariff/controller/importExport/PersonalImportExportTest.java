package ru.sberbank.ditsib.transport.tariff.controller.importExport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.api.AssertionsForClassTypes;
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
import org.springframework.web.client.RestTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.file_works.database.dao.ExportTaskRepository;
import ru.sber.transport.file_works.services.UploadStates;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.tariff.messaging.PersonalTariffMessage;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@DisplayName("Тарифы личного транспорта")
@EmbeddedPostgres
@AutoConfigureMockMvc
@RequiredArgsConstructor
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles("test")
class PersonalImportExportTest extends KafkaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonalTariffRepository tariffRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private UploadStates uploadStates;

    @Autowired
    private ExportTaskRepository exportTaskRepository;

    @MockitoBean("personalTariffOutput")
    private OutputBridge personalTariffOutput;

    @Autowired
    private GeoZoneRepository geoZoneRepository;

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @BeforeEach
    void setup() {
        tariffRepository.deleteAllInBatch();
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
        when(restTemplate.postForEntity(any(), any(), any())).thenReturn(null);
    }

    @AfterEach
    void after() {
        contractorRepository.deleteAll();
        contractRepository.deleteAll();
        geoZoneRepository.deleteAll();
        tariffRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    @DisplayName("Импорт")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
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
            contract.setRestrictionType(RestrictionType.NONE);
            contract.setContractType(ContractType.TRANSITIONAL);
            contract.setActive(true);
            contractRepository.save(contract);
        }

        var file = new MockMultipartFile("file", "file.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/tariffPersonal.xlsx"));

        assertRequest(file);
        var actualList = tariffRepository.findAll(Sort.by(BaseTariff_.HUMAN_READABLE_ID));
        final var messageCaptor = ArgumentCaptor.forClass(PersonalTariffMessage.class);
        verify(personalTariffOutput, times(100)).send(messageCaptor.capture());
        var messageList = messageCaptor.getAllValues();

        assertSoftly(it -> {
            it.assertThat(actualList).hasSize(count);
            it.assertThat(messageList).hasSize(count);
        });

        for (var i = 0; i < count; i++) {
            var actual = actualList.get(i);

            var number = i + 1;

            assertDatabase(geoZoneList, actual, i, number);

            var message = messageList.get(i);
            assertMessage(message);

        }
    }

    private void assertMessage(PersonalTariffMessage message) {
        assertSoftly(it -> {
            it.assertThat(message.getId()).isNotNull();
            it.assertThat(message.humanReadableId()).isNotNull();
            it.assertThat(message.transportType()).isEqualTo(TransportTypeEnum.PERSONAL.name());
        });
    }

    private void assertDatabase(List<GeoZone> geoZoneList, PersonalTariff actual, int index, int number) {
        assertSoftly(it -> {
            it.assertThat(actual.getId()).isNotNull();
            it.assertThat(actual.getHumanReadableId()).isNotNull();
            it.assertThat(actual.getServiceType().getDescription()).isEqualTo(TransportServiceType.EMPLOYEE_TRANSPORTATION.getDescription());
            it.assertThat(actual.getOrganization().getName()).isEqualTo(String.format("Организация %03d", number));
            it.assertThat(actual.getRegionId()).isEqualTo(geoZoneList.get(index).getId());
            it.assertThat(actual.getTransportType()).isEqualTo(TransportTypeEnum.PERSONAL);
            // в тарифе
            it.assertThat(actual.getDistanceIncluded()).isEqualTo(actual.getRideCostPerKm() > 0 ? (double) actual.getMinRideDistanceCost() / actual.getRideCostPerKm() : 0);
            it.assertThat(actual.getTimeIncluded()).isEqualTo(actual.getRideCostPerMin() > 0 ? actual.getMinRideTimeCost() / actual.getRideCostPerMin() : 0);
            // стоимость
            it.assertThat(actual.getMinRideDistanceCost()).isEqualTo(100 + number);
            it.assertThat(actual.getMinRideTimeCost()).isEqualTo(200 + number);
            it.assertThat(actual.getRideCostPerKm()).isEqualTo(300 + number);
            it.assertThat(actual.getRideCostPerMin()).isEqualTo(400 + number);
            // за городом
            it.assertThat(actual.getSuburbTariffParams().getCostPerKmSuburb()).isEqualTo(500 + number);
            it.assertThat(actual.getSuburbTariffParams().getCostPerMinSuburb()).isEqualTo(600 + number);
            it.assertThat(actual.getSuburbTariffParams().getSuburbServiceCostPerKm()).isEqualTo(700 + number);
            it.assertThat(actual.getSuburbTariffParams().getSuburbServiceCostPerMin()).isEqualTo(800 + number);
            // между регионами
            assertThat(actual.getSuburbTariffParams().getCostPerKmInterRegion()).isEqualTo(900 + number);
            it.assertThat(actual.getSuburbTariffParams().getCostPerMinInterRegion()).isEqualTo(1000 + number);
            // коэффициенты, index = номера
            assertThat(actual.getCoefMaterialAssets()).isEqualTo(index % 10 + 1);
            it.assertThat(actual.getCoefTraffic()).isEqualTo(10 - index % 10);
            it.assertThat(actual.getTimedTariffParams().getCoefWorkDayMorning()).isEqualTo(index % 10 + 1);
            it.assertThat(actual.getTimedTariffParams().getCoefWorkDayNoon()).isEqualTo(10 - index % 10);
            it.assertThat(actual.getTimedTariffParams().getCoefWorkDayEvening()).isEqualTo(index % 10 + 1);
            it.assertThat(actual.getTimedTariffParams().getCoefWorkDayNight()).isEqualTo(10 - index % 10);
            it.assertThat(actual.getTimedTariffParams().getCoefDayOff()).isEqualTo(index % 10 + 1);

            // сезон
            it.assertThat(actual.getSeasonalCoefficient()).isEqualTo(10 - index % 10);
            it.assertThat(actual.getSeasonStart()).isEqualTo(LocalDate.of(1900, 5, 1).plusDays(index));
            it.assertThat(actual.getSeasonEnd()).isEqualTo(LocalDate.of(1901, 5, 1).plusDays(index));

            // двигатель
            it.assertThat(actual.getEngineTariffParams().getCoefEngine1_6()).isEqualTo(index % 10 + 1);
            it.assertThat(actual.getEngineTariffParams().getCoefEngine1_6_to_2_0()).isEqualTo(10 - index % 10);
            it.assertThat(actual.getEngineTariffParams().getCoefEngine2_0_to_2_5()).isEqualTo(index % 10 + 1);
//            it.assertThat(actual.getCoopTariffParams().getTimeDeviationMin()).isEqualTo(index % 60);
//            it.assertThat(actual.getCoopTariffParams().getMinCancelTimeMin()).isEqualTo(index % 60);
//            it.assertThat(actual.getCoopTariffParams().getSavingsDeviationPct()).isEqualTo(index);
//            it.assertThat(actual.getCoopTariffParams().getDistanceDeviationKm()).isEqualTo(index);
        });
    }

    private void assertRequest(MockMultipartFile file) throws Exception {
        final var userId = UUID.randomUUID().toString();
        mockMvc.perform(multipart("/files/tariff").file(file)
                        .with(jwt().jwt(builder -> builder.jti(userId).claim("roles", List.of("ROLE_USER")))))
                .andExpect(status().isOk());

        await().atMost(Duration.ofSeconds(60)).until(() -> uploadStates.getResults("tariff", userId).size(),
                equalTo(1));
        await().atMost(Duration.ofSeconds(60)).until(() -> uploadStates.getResults("tariff", userId).getFirst().getFinished(), equalTo(true));
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
            contract.setRestrictionType(RestrictionType.NONE);
            contract.setContractType(ContractType.TRANSITIONAL);

            contractRepository.save(contract);

            var tariff = new PersonalTariff();

            tariff.setTransportType(transportType);
            tariff.setOrganization(organizations.get(i));
            tariff.setHumanReadableId("HRI " + i);
            tariff.setActive(i % 2 == 0);
            tariff.setRegionId(savedGeozone.getId());
            tariff.setTransportType(TransportTypeEnum.PERSONAL);

            tariff.setDistanceIncluded(1 + (i / 100D));
            // стоимость
            tariff.setMinRideDistanceCost(i + 100);
            tariff.setMinRideTimeCost(i + 200);
            tariff.setRideCostPerKm(i + 300);
            tariff.setRideCostPerMin(i + 400);
            // за городом
            tariff.getSuburbTariffParams().setCostPerKmSuburb(i + 500);
            tariff.getSuburbTariffParams().setCostPerMinSuburb(i + 600);
            tariff.getSuburbTariffParams().setSuburbServiceCostPerKm(i + 700);
            tariff.getSuburbTariffParams().setSuburbServiceCostPerMin(i + 800);
            // между регионами
            tariff.getSuburbTariffParams().setCostPerKmInterRegion(i + 900);
            tariff.getSuburbTariffParams().setCostPerMinInterRegion(i + 1000);
            // коэффициенты
            tariff.setCoefMaterialAssets(1 + (i / 100D));
            tariff.setCoefTraffic(2 + (i / 100D));
            tariff.getTimedTariffParams().setCoefWorkDayMorning(3 + (i / 100D));
            tariff.getTimedTariffParams().setCoefWorkDayNoon(4 + (i / 100D));
            tariff.getTimedTariffParams().setCoefWorkDayEvening(5 + (i / 100D));
            tariff.getTimedTariffParams().setCoefWorkDayNight(6 + (i / 100D));
            tariff.getTimedTariffParams().setCoefDayOff(7 + (i / 100D));
            // сезон
            tariff.setSeasonalCoefficient(1 + (i + 100D));
            tariff.setSeasonStart(LocalDate.of(2000, 1, 1).plusDays(i));
            tariff.setSeasonEnd(LocalDate.of(2000, 2, 1).plusDays(i));
            // двигатель
            tariff.getEngineTariffParams().setCoefEngine1_6(1 + (i / 100D));
            tariff.getEngineTariffParams().setCoefEngine1_6_to_2_0(2 + (i / 100D));
            tariff.getEngineTariffParams().setCoefEngine2_0_to_2_5(3 + (i / 100D));

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


        var bytes = mockMvc.perform(get(response.get("result_url"))
                        .with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsByteArray();

        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet("Личный транспорт");

            assertThat(sheet).isNotNull();

            var header1 = sheet.getRow(0);

            AssertionsForClassTypes.assertThat(header1).isNotNull();
            assertThat(header1.getPhysicalNumberOfCells()).isEqualTo(8);

            assertHeader1(header1);

            var header2 = sheet.getRow(1);
            assertHeader2(header2);

            var header3 = sheet.getRow(2);
            assertHeader3(header2, header3);

            var expectedList = tariffRepository.findAll();
            assertThat(expectedList).hasSize(count);
            assertThat(sheet.getLastRowNum() - 2).isEqualTo(count);

            //  rowIndex - индекс строки с учетом заголовков.
            //  На текущий момент строки в excel пишутся не упорядоченно, и нам нужно найти объект из БД по ключу HumanReadableId, и проверить
            //  наличие региона и контракта по названию в списке БД
            for (int rowIndex = 3, i = 0; i < count; rowIndex++, i++) {
                assertRow(geoZoneList, sheet, expectedList, rowIndex, i);
            }
        }
    }

    private void assertRow(List<GeoZone> geoZoneList, XSSFSheet sheet, List<PersonalTariff> expectedList, int rowIndex, int i) {
        var actualRow = sheet.getRow(rowIndex);
        var expectedItemOptional = expectedList.stream()
                .filter(tariff -> tariff.getHumanReadableId()
                        .equals(actualRow.getCell(0).getStringCellValue()))
                .findFirst();
        AssertionsForClassTypes.assertThat(expectedItemOptional).isPresent();
        var expectedItem = expectedItemOptional.orElseThrow();
        assertSoftly(it -> {
            it.assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(expectedItem.getHumanReadableId());
            it.assertThat(actualRow.getCell(1).getBooleanCellValue()).isEqualTo(expectedItem.isActive());
            it.assertThat(actualRow.getCell(2).getStringCellValue()).isEqualTo(expectedItem.getOrganization().getName());
            it.assertThat(actualRow.getCell(3).getStringCellValue()).isIn(geoZoneList.stream().map(GeoZone::getName).toList());
            it.assertThat(actualRow.getCell(4).getStringCellValue()).isEqualTo(expectedItem.getServiceType().getDescription());
            // стоимость
            it.assertThat(actualRow.getCell(5).getNumericCellValue()).isEqualTo(expectedItem.getMinRideDistanceCost());
            it.assertThat(actualRow.getCell(6).getNumericCellValue()).isEqualTo(expectedItem.getMinRideTimeCost());
            it.assertThat(actualRow.getCell(7).getNumericCellValue()).isEqualTo(expectedItem.getRideCostPerKm());
            it.assertThat(actualRow.getCell(8).getNumericCellValue()).isEqualTo(expectedItem.getRideCostPerMin());
            // стоимость за городом
            it.assertThat(actualRow.getCell(9).getNumericCellValue()).isEqualTo(expectedItem.getWaitCostPerMinIntermediate());
            it.assertThat(actualRow.getCell(10).getNumericCellValue()).isEqualTo(expectedItem.getSuburbTariffParams().getCostPerKmSuburb());
            it.assertThat(actualRow.getCell(11).getNumericCellValue()).isEqualTo(expectedItem.getSuburbTariffParams().getCostPerMinSuburb());
            it.assertThat(actualRow.getCell(12).getNumericCellValue()).isEqualTo(expectedItem.getSuburbTariffParams().getSuburbServiceCostPerKm());
            it.assertThat(actualRow.getCell(13).getNumericCellValue()).isEqualTo(expectedItem.getSuburbTariffParams().getSuburbServiceCostPerMin());
            // стоимость между регионами
            it.assertThat(actualRow.getCell(14).getNumericCellValue()).isEqualTo(expectedItem.getSuburbTariffParams().getCostPerKmInterRegion());
            it.assertThat(actualRow.getCell(15).getNumericCellValue()).isEqualTo(expectedItem.getSuburbTariffParams().getCostPerMinInterRegion());
            // коэффициенты
            it.assertThat(actualRow.getCell(16).getNumericCellValue()).isEqualTo(expectedItem.getCoefMaterialAssets());
            it.assertThat(actualRow.getCell(17).getNumericCellValue()).isEqualTo(expectedItem.getCoefTraffic());
            it.assertThat(actualRow.getCell(18).getNumericCellValue()).isEqualTo(expectedItem.getTimedTariffParams().getCoefWorkDayMorning());
            it.assertThat(actualRow.getCell(19).getNumericCellValue()).isEqualTo(expectedItem.getTimedTariffParams().getCoefWorkDayNoon());
            it.assertThat(actualRow.getCell(20).getNumericCellValue()).isEqualTo(expectedItem.getTimedTariffParams().getCoefWorkDayEvening());
            it.assertThat(actualRow.getCell(21).getNumericCellValue()).isEqualTo(expectedItem.getTimedTariffParams().getCoefWorkDayNight());
            it.assertThat(actualRow.getCell(22).getNumericCellValue()).isEqualTo(expectedItem.getTimedTariffParams().getCoefDayOff());
            // коэффициенты сезон
            it.assertThat(actualRow.getCell(23).getNumericCellValue()).isEqualTo(expectedItem.getSeasonalCoefficient());
            it.assertThat(actualRow.getCell(24).getLocalDateTimeCellValue()).isEqualTo(expectedItem.getSeasonStart().atStartOfDay());
            it.assertThat(actualRow.getCell(25).getLocalDateTimeCellValue()).isEqualTo(expectedItem.getSeasonEnd().atStartOfDay());
            // коэффициенты двигатель
            it.assertThat(actualRow.getCell(26).getNumericCellValue()).isEqualTo(expectedItem.getEngineTariffParams().getCoefEngine1_6());
            it.assertThat(actualRow.getCell(27).getNumericCellValue()).isEqualTo(expectedItem.getEngineTariffParams().getCoefEngine1_6_to_2_0());
            it.assertThat(actualRow.getCell(28).getNumericCellValue()).isEqualTo(expectedItem.getEngineTariffParams().getCoefEngine2_0_to_2_5());

            it.assertThat(actualRow.getCell(29).getNumericCellValue()).isEqualTo(expectedItem.getTrustIdx());
            it.assertThat(actualRow.getCell(30).getNumericCellValue()).isEqualTo(expectedItem.getCoopTariffParams().getSavingsDeviationPct());
            it.assertThat(actualRow.getCell(31).getNumericCellValue()).isEqualTo(expectedItem.getCoopTariffParams().getDistanceDeviationKm());
            it.assertThat((int) actualRow.getCell(32).getNumericCellValue()).isEqualTo(expectedItem.getCoopTariffParams().getTimeDeviationMin());
            it.assertThat((int) actualRow.getCell(33).getNumericCellValue()).isEqualTo(expectedItem.getCoopTariffParams().getMinCancelTimeMin());
        });
    }

    private void assertHeader3(XSSFRow header2, XSSFRow header3) {
        AssertionsForClassTypes.assertThat(header3).isNotNull();
        AssertionsForClassTypes.assertThat(header2.getPhysicalNumberOfCells()).isEqualTo(21);
        assertSoftly(it -> {
            it.assertThat(header3.getCell(10).getStringCellValue()).isEqualTo("За км");
            it.assertThat(header3.getCell(11).getStringCellValue()).isEqualTo("За мин");
            it.assertThat(header3.getCell(12).getStringCellValue()).isEqualTo("За км подачи");
            it.assertThat(header3.getCell(13).getStringCellValue()).isEqualTo("За мин подачи");
            it.assertThat(header3.getCell(14).getStringCellValue()).isEqualTo("За км");
            it.assertThat(header3.getCell(15).getStringCellValue()).isEqualTo("За мин");
            it.assertThat(header3.getCell(23).getStringCellValue()).isEqualTo("Коэффициент");
            it.assertThat(header3.getCell(24).getStringCellValue()).isEqualTo("Начало");
            it.assertThat(header3.getCell(25).getStringCellValue()).isEqualTo("Окончание");
            it.assertThat(header3.getCell(26).getStringCellValue()).isEqualTo("Меньше 1.6");
            it.assertThat(header3.getCell(27).getStringCellValue()).isEqualTo("1.6 - 2.0");
            it.assertThat(header3.getCell(28).getStringCellValue()).isEqualTo("2.0 - 2.5");
        });
    }

    private void assertHeader2(XSSFRow header2) {
        AssertionsForClassTypes.assertThat(header2).isNotNull();
        AssertionsForClassTypes.assertThat(header2.getPhysicalNumberOfCells()).isEqualTo(21);
        assertSoftly(it -> {
            it.assertThat(header2.getCell(5).getStringCellValue()).isEqualTo("Минимальная за расстояние");
            it.assertThat(header2.getCell(6).getStringCellValue()).isEqualTo("Минимальная за время");
            it.assertThat(header2.getCell(7).getStringCellValue()).isEqualTo("За км");
            it.assertThat(header2.getCell(8).getStringCellValue()).isEqualTo("За мин");
            it.assertThat(header2.getCell(9).getStringCellValue()).isEqualTo("Ожидание в промеж. точке, за мин");
            // стоимость за городом
            it.assertThat(header2.getCell(10).getStringCellValue()).isEqualTo("За городом");
            // стоимость между регионами
            it.assertThat(header2.getCell(14).getStringCellValue()).isEqualTo("Между регионами");
            // коэффициенты
            it.assertThat(header2.getCell(16).getStringCellValue()).isEqualTo("Коэф. перевозки ТМЦ");
            it.assertThat(header2.getCell(17).getStringCellValue()).isEqualTo("Коэф. загруженности");
            it.assertThat(header2.getCell(18).getStringCellValue()).isEqualTo("07:00 - 10:00");
            it.assertThat(header2.getCell(19).getStringCellValue()).isEqualTo("10:00 - 18:00");
            it.assertThat(header2.getCell(20).getStringCellValue()).isEqualTo("18:00 - 22:00");
            it.assertThat(header2.getCell(21).getStringCellValue()).isEqualTo("22:00 - 07:00");
            it.assertThat(header2.getCell(22).getStringCellValue()).isEqualTo("Вых. дни");
            // коэффициенты сезон
            it.assertThat(header2.getCell(23).getStringCellValue()).isEqualTo("Сезон");
            // коэффициенты двигатель
            it.assertThat(header2.getCell(26).getStringCellValue()).isEqualTo("Двигатель");
            it.assertThat(header2.getCell(29).getStringCellValue()).isEqualTo("Индекс затрат страх.");

            it.assertThat(header2.getCell(30).getStringCellValue()).isEqualTo("Экономии от поездки");
            it.assertThat(header2.getCell(31).getStringCellValue()).isEqualTo("Максимальное, км");
            it.assertThat(header2.getCell(32).getStringCellValue()).isEqualTo("Максимальное, мин");
            it.assertThat(header2.getCell(33).getStringCellValue()).isEqualTo("Триггерное время (мин)");
        });
    }

    private void assertHeader1(XSSFRow header1) {
        assertSoftly(it -> {
            it.assertThat(header1.getCell(0).getStringCellValue()).isEqualTo("Идентификатор");
            it.assertThat(header1.getCell(1).getStringCellValue()).isEqualTo("Активен");
            it.assertThat(header1.getCell(2).getStringCellValue()).isEqualTo("Организация");
            it.assertThat(header1.getCell(3).getStringCellValue()).isEqualTo("Регион");
            it.assertThat(header1.getCell(4).getStringCellValue()).isEqualTo("Тип услуги");
            it.assertThat(header1.getCell(5).getStringCellValue()).isEqualTo("Стоимость");
            it.assertThat(header1.getCell(16).getStringCellValue()).isEqualTo("Коэффициенты");
            it.assertThat(header1.getCell(30).getStringCellValue()).isEqualTo("Совместные поездки");
        });
    }


}
