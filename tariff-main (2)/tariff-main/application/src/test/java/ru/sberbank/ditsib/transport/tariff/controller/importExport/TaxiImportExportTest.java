package ru.sberbank.ditsib.transport.tariff.controller.importExport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.file_works.database.dao.ExportTaskRepository;
import ru.sber.transport.file_works.dto.PageInfo;
import ru.sber.transport.file_works.services.UploadStates;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff.messaging.TaxiTariffMessage;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.dao.*;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@Slf4j
@DisplayName("Тарифы такси")
@EmbeddedPostgres
@AutoConfigureMockMvc
@RequiredArgsConstructor
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles({"test", "kafka"})
class TaxiImportExportTest extends KafkaTest {
    public static final String USER_ID = UUID.randomUUID().toString();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaxiTariffRepository tariffRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private ContractRepository contractRepository;

    @MockitoBean("taxiTariffOutput")
    private OutputBridge taxiTariffOutput;

    @Autowired
    private UploadStates uploadStates;

    @Autowired
    private ExportTaskRepository exportTaskRepository;

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
        geoZoneRepository.deleteAll();
        tariffRepository.deleteAll();
        contractRepository.deleteAll();
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
            contract.setContractType(ContractType.TRANSITIONAL);
            contract.setRestrictionType(RestrictionType.NONE);
            contract.setActive(true);

            contractRepository.save(contract);
        }


        var file = new MockMultipartFile("file", "file.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/tariffTaxi.xlsx"));

        assertRequest(file);
        var actualList = tariffRepository.findAll(Sort.by(BaseTariff_.HUMAN_READABLE_ID));
        final var messageCaptor = ArgumentCaptor.forClass(TaxiTariffMessage.class);
        verify(taxiTariffOutput, times(100)).send(messageCaptor.capture());
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
            assertMessage(actual, i, number, message);
        }
    }

    private void assertMessage(TaxiTariff actual, int index, int number, TaxiTariffMessage message) {
        assertSoftly(it -> {
            it.assertThat(message.getId()).isNotNull();
            it.assertThat(message.humanReadableId()).isNotNull();
            it.assertThat(message.contractId()).isEqualTo(actual.getContract().getId());
            it.assertThat(message.transportType()).isEqualTo(TransportTypeEnum.TAXI.name());
        });
    }

    private void assertDatabase(List<GeoZone> geoZoneList, TaxiTariff actual, int index, int number) {
        assertSoftly(it -> {
            it.assertThat(actual.getId()).isNotNull();
            it.assertThat(actual.getHumanReadableId()).isNotNull();
            it.assertThat(actual.getContract().getContractNumber()).isEqualTo(String.format("Договор %03d", number));
            it.assertThat(actual.getOrganization().getName()).isEqualTo(String.format("Организация %03d", number));
            it.assertThat(actual.getRegionId()).isEqualTo(geoZoneList.get(index).getId());
            it.assertThat(actual.getTransportType()).isEqualTo(TransportTypeEnum.TAXI);
            it.assertThat(actual.getTaxiClass()).isEqualTo(TaxiClass.values()[index % 3]);
            // в тарифе
            it.assertThat(actual.getDistanceIncluded()).isEqualTo(index);
            it.assertThat(actual.getTimeIncluded()).isEqualTo(200 + number);
//            it.assertThat(actual.getFreeWaitingTime()).isEqualTo(300 + number);
            it.assertThat(actual.getMinRideDistanceCost()).isEqualTo(400 + number);
            it.assertThat(actual.getMinRideTimeCost()).isEqualTo(500 + number);
            // стоимость
//            it.assertThat(actual.getCarServiceCost()).isEqualTo(number);
            it.assertThat(actual.getRideCostPerKm()).isEqualTo(600 + number);
            it.assertThat(actual.getRideCostPerMin()).isEqualTo(700 + number);
            it.assertThat(actual.getWaitCostPerMin()).isEqualTo(800 + number);
            it.assertThat(actual.getWaitCostPerMinIntermediate()).isEqualTo(900 + number);
            // за городом
            it.assertThat(actual.getSuburbTariffParams().getCostPerKmSuburb()).isEqualTo(1000 + number);
            it.assertThat(actual.getSuburbTariffParams().getCostPerMinSuburb()).isEqualTo(1100 + number);
            it.assertThat(actual.getSuburbTariffParams().getSuburbServiceCostPerKm()).isEqualTo(1200 + number);
            it.assertThat(actual.getSuburbTariffParams().getSuburbServiceCostPerMin()).isEqualTo(1300 + number);
            // между регионами
            it.assertThat(actual.getSuburbTariffParams().getCostPerKmInterRegion()).isEqualTo(1400 + number);
            it.assertThat(actual.getSuburbTariffParams().getCostPerMinInterRegion()).isEqualTo(1500 + number);
            // коэффициенты, index = номера
            it.assertThat(actual.getCoefBicycle()).isEqualTo(1.0 + 0.0 * number);
            it.assertThat(actual.getCoefOrg()).isEqualTo(10 - index % 10);
            it.assertThat(actual.getCoefPetTransport()).isEqualTo(index % 10 + 1);
            it.assertThat(actual.getCoefChildSeat()).isEqualTo(10 - index % 10);
            it.assertThat(actual.getCoefTraffic()).isEqualTo(index % 10 + 1);
            it.assertThat(actual.getTimedTariffParams().getCoefWorkDayMorning()).isEqualTo(10 - index % 10);
            it.assertThat(actual.getTimedTariffParams().getCoefWorkDayNoon()).isEqualTo(index % 10 + 1);
            it.assertThat(actual.getTimedTariffParams().getCoefWorkDayEvening()).isEqualTo(10 - index % 10);
            it.assertThat(actual.getTimedTariffParams().getCoefWorkDayNight()).isEqualTo(index % 10 + 1);
            it.assertThat(actual.getTimedTariffParams().getCoefDayOff()).isEqualTo(10 - index % 10);
        });
    }

    private void assertRequest(MockMultipartFile file) throws Exception {
        mockMvc.perform(multipart("/files/tariff").file(file)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", "ROLE_USER"))))
                .andExpect(status().isOk());

        await().atMost(Duration.ofSeconds(60)).until(() -> uploadStates.getResults("tariff", USER_ID).size(), equalTo(1));
        await().atMost(Duration.ofSeconds(60)).until(() -> uploadStates.getResults("tariff", USER_ID).getFirst().getFinished(), equalTo(true));
        assertThat(uploadStates.getResults("tariff", USER_ID).getFirst().getPages().stream()
                .filter(pageInfo -> pageInfo.getName().equals("Такси"))
                .allMatch(page -> page.getExceptionStrings().isEmpty()))
                .isTrue();
        assertThat(uploadStates.getResults("tariff", USER_ID).getFirst().getPages().stream().map(PageInfo::getRow)
                .flatMap(Collection::stream).allMatch(page -> page.getExceptionStrings().isEmpty()))
                .isTrue();
    }

    @Test
    @DisplayName("Экспорт")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void test_export() throws Exception {
        var organizations = organizationRepository.findAll(Sort.by(Organization_.NAME));
        var contractors = contractorRepository.findAll(Sort.by(Contractor_.NAME));

        var count = 100;

        var geoZoneList = new ArrayList<GeoZone>();
        var tariffs = new ArrayList<TaxiTariff>();
        for (var i = 0; i < count; i++) {
            GeoZone savedGeozone = geoZoneRepository
                    .save(new GeoZone(UUID.randomUUID(), String.format("Регион %03d", i + 1), i + 1 + "", null));
            geoZoneList.add(savedGeozone);
            var transportType = TransportTypeEnum.values()[i % TransportTypeEnum.values().length];
            var taxiClass = TaxiClass.values()[i % TaxiClass.values().length];

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

            contract = contractRepository.save(contract);

            var tariff = new TaxiTariff();

            tariff.setTransportType(transportType);
            tariff.setOrganization(organizations.get(i));
            tariff.setHumanReadableId("HRI " + i);
            tariff.setContract(contract);
            tariff.setActive(i % 2 == 0);
            tariff.setRegionId(savedGeozone.getId());
            tariff.setTaxiClass(taxiClass);
            tariff.setTransportType(TransportTypeEnum.TAXI);

            // В тарифе
            tariff.setDistanceIncluded(1.0 + 0.0 * i);

            for (var j = 0; j < 60; j++) {
                tariff.setTimeIncluded(j + 1);
            }

            tariff.setFreeWaitingTime(300);
            tariff.setMinRideDistanceCost(i + 400);
            tariff.setMinRideTimeCost(i + 500);
            // стоимость
            tariff.setCarServiceCost(i + 1);
            tariff.setRideCostPerKm(i + 600);
            tariff.setRideCostPerMin(i + 700);
            tariff.setWaitCostPerMin(i + 800);
            tariff.setWaitCostPerMinIntermediate(i + 900);
            // за городом
            tariff.getSuburbTariffParams().setCostPerKmSuburb(i + 1000);
            tariff.getSuburbTariffParams().setCostPerMinSuburb(i + 1100);
            tariff.getSuburbTariffParams().setSuburbServiceCostPerKm(i + 1200);
            tariff.getSuburbTariffParams().setSuburbServiceCostPerMin(i + 1300);
            // между регионами
            tariff.getSuburbTariffParams().setCostPerKmInterRegion(i + 1400);
            tariff.getSuburbTariffParams().setCostPerMinInterRegion(i + 1500);
            // коэффициенты
            tariff.setCoefBicycle(1.6 + 0.001 * i);
            tariff.setCoefOrg(1.7 + 0.001 * i);
            tariff.setCoefPetTransport(1.8 + 0.001 * i);
            tariff.setCoefChildSeat(1.9 + 0.001 * i);
            tariff.setCoefTraffic(2.0 + 0.001 * i);
            tariff.getTimedTariffParams().setCoefWorkDayMorning(2.1 + 0.001 * i);
            tariff.getTimedTariffParams().setCoefWorkDayNoon(2.2 + 0.001 * i);
            tariff.getTimedTariffParams().setCoefWorkDayEvening(2.3 + 0.001 * i);
            tariff.getTimedTariffParams().setCoefWorkDayNight(2.4 + 0.001 * i);
            tariff.getTimedTariffParams().setCoefDayOff(2.5 + 0.001 * i);
            // отклонение
            tariff.getCoopTariffParams().setSavingsDeviationPct(2.6 + 0.001 * i);
            tariff.getCoopTariffParams().setDistanceDeviationKm(2.7 + 0.001 * i);
            tariff.getCoopTariffParams().setTimeDeviationMin(i + 2800);
            //
            tariff.getContractorDeviationParams().setMaxDiffComputedDistancePercent(25);
            tariff.getContractorDeviationParams().setMaxDiffComputedCostPercent(50);
            tariff.getContractorDeviationParams().setMaxDiffComputedWaitingPercent(75);
            tariff.getContractorDeviationParams().setMaxDiffFactDistancePercent(100);
            tariff.getContractorDeviationParams().setMaxDiffContractorCostPercent(125);
            // триггерное время
            tariff.getCoopTariffParams().setMinCancelTimeMin(i + 2900);
            tariffs.add(tariff);
        }
        tariffRepository.saveAll(tariffs);

        var content = mockMvc.perform(get("/files/tariff")
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
            var sheet = workbook.getSheet("Такси");

            assertThat(sheet).isNotNull();

            var header1 = sheet.getRow(0);

            assertHeader1(header1);

            var header2 = sheet.getRow(1);
            assertHeader2(header2);
            var header3 = sheet.getRow(2);
            assertHeader3(header3);

            var expectedList = tariffRepository.findAll();

            //  rowIndex - индекс строки с учетом заголовков.
            //  На текущий момент строки в excel пишутся не упорядоченно, и нам нужно найти объект из БД по ключу HumanReadableId, и проверить
            //  наличие региона и контракта по названию в списке БД
            for (int rowIndex = 3, i = 0; i < count; rowIndex++, i++) {
                assertRow(contractors, geoZoneList, sheet, expectedList, rowIndex, i);
            }
        }
    }

    private void assertRow(
            List<Contractor> contractors, List<GeoZone> geoZoneList, XSSFSheet sheet, List<TaxiTariff> expectedList, int rowIndex, int i
    ) {
        var actualRow = sheet.getRow(rowIndex);
        var expectedItemOptional = expectedList.stream()
                .filter(tariff -> tariff.getHumanReadableId().equals(actualRow.getCell(0).getStringCellValue()))
                .findFirst();
        assertThat(expectedItemOptional).isPresent();
        var expectedItem = expectedItemOptional.get();
        assertSoftly(it -> {
            it.assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(expectedItem.getHumanReadableId());
            it.assertThat(actualRow.getCell(1).getBooleanCellValue()).isEqualTo(expectedItem.isActive());
            it.assertThat(actualRow.getCell(2).getStringCellValue()).isIn(contractors.stream().map(Contractor::getName).toList());
            it.assertThat(actualRow.getCell(3).getStringCellValue()).isEqualTo(expectedItem.getContract().getContractNumber());
            it.assertThat(actualRow.getCell(4).getStringCellValue()).isEqualTo(expectedItem.getOrganization().getName());
            it.assertThat(actualRow.getCell(5).getStringCellValue()).isIn(geoZoneList.stream().map(GeoZone::getName).toList());
            it.assertThat(actualRow.getCell(7).getStringCellValue()).isEqualTo(expectedItem.getTaxiClass().getRusName());
            it.assertThat(actualRow.getCell(8).getStringCellValue()).isEqualTo(expectedItem.getTransportType().getRusName());
            it.assertThat(actualRow.getCell(9).getStringCellValue()).isEqualTo(expectedItem.getWorkGroup() == null ? "" : expectedItem.getWorkGroup());
            // в тарифе
            it.assertThat(actualRow.getCell(10).getNumericCellValue()).isEqualTo(expectedItem.getDistanceIncluded());
            it.assertThat(actualRow.getCell(11).getNumericCellValue()).isEqualTo(expectedItem.getTimeIncluded());
            it.assertThat(actualRow.getCell(12).getNumericCellValue()).isEqualTo(expectedItem.getFreeWaitingTime());
            it.assertThat(actualRow.getCell(13).getNumericCellValue()).isEqualTo(expectedItem.getMinRideDistanceCost());
            it.assertThat(actualRow.getCell(14).getNumericCellValue()).isEqualTo(expectedItem.getMinRideTimeCost());
            it.assertThat(actualRow.getCell(15).getNumericCellValue()).isEqualTo(expectedItem.getCoopTariffParams().getMinCancelTimeMin());
            // стоимость
            it.assertThat(actualRow.getCell(16).getNumericCellValue()).isEqualTo(expectedItem.getRideCostPerKm());
            it.assertThat(actualRow.getCell(17).getNumericCellValue()).isEqualTo(expectedItem.getRideCostPerMin());
            it.assertThat(actualRow.getCell(18).getNumericCellValue()).isEqualTo(expectedItem.getWaitCostPerMin());
            it.assertThat(actualRow.getCell(19).getNumericCellValue()).isEqualTo(expectedItem.getWaitCostPerMinIntermediate());
            // за городом
            it.assertThat(actualRow.getCell(20).getNumericCellValue()).isEqualTo(expectedItem.getSuburbTariffParams().getCostPerKmSuburb());
            it.assertThat(actualRow.getCell(21).getNumericCellValue()).isEqualTo(expectedItem.getSuburbTariffParams().getCostPerMinSuburb());
            it.assertThat(actualRow.getCell(22).getNumericCellValue()).isEqualTo(expectedItem.getSuburbTariffParams().getSuburbServiceCostPerKm());
            it.assertThat(actualRow.getCell(23).getNumericCellValue()).isEqualTo(expectedItem.getSuburbTariffParams().getSuburbServiceCostPerMin());
            it.assertThat(actualRow.getCell(24).getNumericCellValue()).isEqualTo(expectedItem.getSuburbTariffParams().getCostPerKmInterRegion());
            it.assertThat(actualRow.getCell(25).getNumericCellValue()).isEqualTo(expectedItem.getSuburbTariffParams().getCostPerMinInterRegion());
            // коэффициенты
            it.assertThat(actualRow.getCell(26).getNumericCellValue()).isEqualTo(expectedItem.getCoefBicycle());
            it.assertThat(actualRow.getCell(27).getNumericCellValue()).isEqualTo(expectedItem.getCoefOrg());
            it.assertThat(actualRow.getCell(28).getNumericCellValue()).isEqualTo(expectedItem.getCoefPetTransport());
            it.assertThat(actualRow.getCell(29).getNumericCellValue()).isEqualTo(expectedItem.getCoefChildSeat());
            it.assertThat(actualRow.getCell(30).getNumericCellValue()).isEqualTo(expectedItem.getCoefTraffic());
            it.assertThat(actualRow.getCell(31).getNumericCellValue()).isEqualTo(expectedItem.getTimedTariffParams().getCoefWorkDayMorning());
            it.assertThat(actualRow.getCell(32).getNumericCellValue()).isEqualTo(expectedItem.getTimedTariffParams().getCoefWorkDayNoon());
            it.assertThat(actualRow.getCell(33).getNumericCellValue()).isEqualTo(expectedItem.getTimedTariffParams().getCoefWorkDayEvening());
            it.assertThat(actualRow.getCell(34).getNumericCellValue()).isEqualTo(expectedItem.getTimedTariffParams().getCoefWorkDayNight());
            it.assertThat(actualRow.getCell(35).getNumericCellValue()).isEqualTo(expectedItem.getTimedTariffParams().getCoefDayOff());
            it.assertThat(actualRow.getCell(36).getNumericCellValue()).isEqualTo(expectedItem.getCoopTariffParams().getSavingsDeviationPct());
            it.assertThat(actualRow.getCell(37).getNumericCellValue()).isEqualTo(expectedItem.getCoopTariffParams().getDistanceDeviationKm());
            it.assertThat(actualRow.getCell(38).getNumericCellValue()).isEqualTo(expectedItem.getCoopTariffParams().getTimeDeviationMin());
            it.assertThat(actualRow.getCell(39).getNumericCellValue()).isEqualTo(expectedItem.getCoopTariffParams().getMinCancelTimeMin());
            it.assertThat((int) actualRow.getCell(40).getNumericCellValue()).isEqualTo(expectedItem.getContractorDeviationParams().getMaxDiffComputedDistancePercent());
            it.assertThat((int) actualRow.getCell(41).getNumericCellValue()).isEqualTo(expectedItem.getContractorDeviationParams().getMaxDiffFactDistancePercent());
            it.assertThat((int) actualRow.getCell(42).getNumericCellValue()).isEqualTo(expectedItem.getContractorDeviationParams().getMaxDiffComputedCostPercent());
            it.assertThat((int) actualRow.getCell(43).getNumericCellValue()).isEqualTo(expectedItem.getContractorDeviationParams().getMaxDiffContractorCostPercent());
            it.assertThat((int) actualRow.getCell(44).getNumericCellValue()).isEqualTo(expectedItem.getContractorDeviationParams().getMaxDiffComputedWaitingPercent());
            it.assertThat(actualRow.getCell(45).getBooleanCellValue()).isEqualTo(expectedItem.getIsNightTariff());
        });
    }

    private void assertHeader3(XSSFRow header3) {
        assertThat((Iterable<? extends Cell>) header3).isNotNull();
        assertThat(header3.getPhysicalNumberOfCells()).isEqualTo(6);
        assertSoftly(it -> {
            // за городом
            it.assertThat(header3.getCell(20).getStringCellValue()).isEqualTo("За км");
            it.assertThat(header3.getCell(21).getStringCellValue()).isEqualTo("За мин");
            it.assertThat(header3.getCell(22).getStringCellValue()).isEqualTo("За км подачи");
            it.assertThat(header3.getCell(23).getStringCellValue()).isEqualTo("За мин подачи");
            // между регионами
            it.assertThat(header3.getCell(24).getStringCellValue()).isEqualTo("За км");
            it.assertThat(header3.getCell(25).getStringCellValue()).isEqualTo("За мин");
        });
    }

    private void assertHeader2(XSSFRow header2) {
        assertThat((Iterable<? extends Cell>) header2).isNotNull();

        assertThat(header2.getPhysicalNumberOfCells()).isEqualTo(31);
        assertSoftly(it -> {
            // в тарифе
            it.assertThat(header2.getCell(10).getStringCellValue()).isEqualTo("Включенное расстояние (км)");
            it.assertThat(header2.getCell(11).getStringCellValue()).isEqualTo("Включенное время (мин)");
            it.assertThat(header2.getCell(12).getStringCellValue()).isEqualTo("Бесплатное время ожидания (мин)");
            it.assertThat(header2.getCell(13).getStringCellValue()).isEqualTo("Минимальное расстояние (км)");
            it.assertThat(header2.getCell(14).getStringCellValue()).isEqualTo("Минимальное время (мин)");
            it.assertThat(header2.getCell(15).getStringCellValue()).isEqualTo("Минимальное время отмены заказа (мин)");
                    // стоимость
                    it.assertThat(header2.getCell(16).getStringCellValue()).isEqualTo("За км");
            it.assertThat(header2.getCell(17).getStringCellValue()).isEqualTo("За мин");
            it.assertThat(header2.getCell(18).getStringCellValue()).isEqualTo("Ожидание, за мин");
            it.assertThat(header2.getCell(19).getStringCellValue()).isEqualTo("Ожидание в промеж. точке, за мин");
                    // за городом
                    it.assertThat(header2.getCell(20).getStringCellValue()).isEqualTo("За городом");
                    // между регионами
                    it.assertThat(header2.getCell(24).getStringCellValue()).isEqualTo("Между регионами");
                    // коэффициенты
                    it.assertThat(header2.getCell(26).getStringCellValue()).isEqualTo("За велосипед/лыжи/сноуборд");
            it.assertThat(header2.getCell(27).getStringCellValue()).isEqualTo("Организации");
            it.assertThat(header2.getCell(28).getStringCellValue()).isEqualTo("За животное");
            it.assertThat(header2.getCell(29).getStringCellValue()).isEqualTo("За дет. кресло");
            it.assertThat(header2.getCell(30).getStringCellValue()).isEqualTo("За загруженность");
            it.assertThat(header2.getCell(31).getStringCellValue()).isEqualTo("07:00 - 10:00");
            it.assertThat(header2.getCell(32).getStringCellValue()).isEqualTo("10:00 - 18:00");
            it.assertThat(header2.getCell(33).getStringCellValue()).isEqualTo("18:00 - 22:00");
            it.assertThat(header2.getCell(34).getStringCellValue()).isEqualTo("22:00 - 07:00");
            it.assertThat(header2.getCell(35).getStringCellValue()).isEqualTo("Вых. дни");
                    // отклонение
                    it.assertThat(header2.getCell(36).getStringCellValue()).isEqualTo("Экономии от поездки");
            it.assertThat(header2.getCell(37).getStringCellValue()).isEqualTo("Максимальное, км");
            it.assertThat(header2.getCell(38).getStringCellValue()).isEqualTo("Максимальное, мин");
            it.assertThat(header2.getCell(39).getStringCellValue()).isEqualTo("Триггерное время (мин)");
            it.assertThat(header2.getCell(40).getStringCellValue()).isEqualTo("Расчетная протяженность");
            it.assertThat(header2.getCell(41).getStringCellValue()).isEqualTo("Фактическая протяженность");
            it.assertThat(header2.getCell(42).getStringCellValue()).isEqualTo("Расчетная стоимость");
            it.assertThat(header2.getCell(43).getStringCellValue()).isEqualTo("Фактическая стоимость");
            it.assertThat(header2.getCell(44).getStringCellValue()).isEqualTo("Время ожидания");
        });
    }

    private void assertHeader1(XSSFRow header1) {
        assertThat((Iterable<? extends Cell>) header1).isNotNull();
        assertThat(header1.getPhysicalNumberOfCells()).isEqualTo(16);

        assertSoftly(it -> {
            it.assertThat(header1.getCell(0).getStringCellValue()).isEqualTo("Идентификатор");
            it.assertThat(header1.getCell(1).getStringCellValue()).isEqualTo("Активен");
            it.assertThat(header1.getCell(2).getStringCellValue()).isEqualTo("Контрагент");
            it.assertThat(header1.getCell(3).getStringCellValue()).isEqualTo("Договор");
            it.assertThat(header1.getCell(4).getStringCellValue()).isEqualTo("Организация");
            it.assertThat(header1.getCell(5).getStringCellValue()).isEqualTo("Регион");
            it.assertThat(header1.getCell(6).getStringCellValue()).isEqualTo("Подразделение");
            it.assertThat(header1.getCell(7).getStringCellValue()).isEqualTo("Класс");
            it.assertThat(header1.getCell(8).getStringCellValue()).isEqualTo("Тип услуги");
            it.assertThat(header1.getCell(9).getStringCellValue()).isEqualTo("Рабочая группа");
            it.assertThat(header1.getCell(10).getStringCellValue()).isEqualTo("В тарифе");
            it.assertThat(header1.getCell(16).getStringCellValue()).isEqualTo("Стоимость");
            it.assertThat(header1.getCell(26).getStringCellValue()).isEqualTo("Коэффициенты");
            it.assertThat(header1.getCell(36).getStringCellValue()).isEqualTo("Совместные поездки");
            it.assertThat(header1.getCell(40).getStringCellValue()).isEqualTo("Отклонения контрагента");
            it.assertThat(header1.getCell(45).getStringCellValue()).isEqualTo("Ночной тариф");
        });
    }
}
