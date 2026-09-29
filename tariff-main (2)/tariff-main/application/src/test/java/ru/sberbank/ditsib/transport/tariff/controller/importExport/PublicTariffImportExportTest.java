package ru.sberbank.ditsib.transport.tariff.controller.importExport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.apache.poi.ss.usermodel.Cell;
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
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.file_works.database.dao.ExportTaskRepository;
import ru.sber.transport.file_works.dto.PageInfo;
import ru.sber.transport.file_works.services.UploadStates;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.tariff.messaging.PublicTariffMessage;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@DisplayName("Тарифы общественного транспорта")
@Transactional
@EmbeddedPostgres
@AutoConfigureMockMvc
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles({"test", "kafka"})
class PublicTariffImportExportTest extends KafkaTest {

    public static final String USER_ID = UUID.randomUUID().toString();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean("publicTariffOutput")
    private OutputBridge publicTariffOutput;

    @Autowired
    private PublicTariffRepository tariffRepository;

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

    @Autowired
    private GeoZoneRepository geoZoneRepository;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @BeforeEach
    void setup() {
        contractRepository.findAll().stream().peek(c -> c.setRegionIds(new HashSet<>())).forEach(contractRepository::save);
        geoZoneRepository.deleteAllInBatch();
        tariffRepository.deleteAllInBatch();
        contractRepository.deleteAllInBatch();
        contractorRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();

        for (var i = 0; i <= 100; i++) {
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
            contract.setActive(true);
            contract.setContractType(ContractType.TRANSITIONAL);
            contract.setRestrictionType(RestrictionType.NONE);

            contractRepository.saveAndFlush(contract);
        }

        var file = new MockMultipartFile("file", "file.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/tariffPublic.xlsx"));

        assertRequest(file);
        var actualList = tariffRepository.findAll(Sort.by(BaseTariff_.HUMAN_READABLE_ID));
        final var messageCaptor = ArgumentCaptor.forClass(PublicTariffMessage.class);
        verify(publicTariffOutput, times(100)).send(messageCaptor.capture());
        var messageList = messageCaptor.getAllValues();

        assertAll("Sizes",
                () -> assertThat(actualList).hasSize(count),
                () -> assertThat(messageList).hasSize(count)
        );

        for (var i = 0; i < count; i++) {
            var actual = actualList.get(i);

            var number = i + 1;
            var expectedBus = i % 2 == 0 ? number : 0;
            var expectedTrolley = i % 2 != 0 ? number + 100 : 0;
            var expectedTram = i % 2 == 0 ? number + 200 : 0;
            var expectedMetro = i % 2 != 0 ? number + 300 : 0;

            assertThat(actual.getId()).isNotNull();
            assertThat(actual.getHumanReadableId()).isNotNull();
            assertThat(actual.getOrganization().getName()).isEqualTo(String.format("Организация %03d", number));
            assertThat(actual.getRegionId()).isEqualTo(geoZoneList.get(i).getId());
            assertThat(actual.getTransportType()).isEqualTo(TransportTypeEnum.PUBLIC);
            assertThat(actual.isBusAvailability()).isEqualTo(i % 2 == 0);
            assertThat(actual.getBusTicketCost()).isEqualTo(expectedBus);
            assertThat(actual.isTrolleybusAvailability()).isEqualTo(i % 2 != 0);
            assertThat(actual.getTrolleybusTicketCost()).isEqualTo(expectedTrolley);
            assertThat(actual.isTramAvailability()).isEqualTo(i % 2 == 0);
            assertThat(actual.getTramTicketCost()).isEqualTo(expectedTram);
            assertThat(actual.isMetroAvailability()).isEqualTo(i % 2 != 0);
            assertThat(actual.getMetroTicketCost()).isEqualTo(expectedMetro);

            var message = messageList.get(i);
            assertAll("Checking message index " + i,
                    () -> assertThat(message.getId()).isNotNull(),
                    () -> assertThat(message.humanReadableId()).isNotNull(),
                    () -> assertThat(message.transportType()).isEqualTo(TransportTypeEnum.PUBLIC.name())
            );
        }
    }

    private void assertRequest(MockMultipartFile file) throws Exception {
        mockMvc.perform(multipart("/files/tariff").file(file)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("role", "ROLE_USER"))))
                .andExpect(status().isOk());

        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(3))
                .until(() -> uploadStates.getResults("tariff", USER_ID).size(), equalTo(1));
        await().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(3))
                .until(() -> uploadStates.getResults("tariff", USER_ID).getFirst().getFinished(), equalTo(true));
        assertThat(uploadStates.getResults("tariff", USER_ID).getFirst().getPages().stream()
                .filter(pageInfo -> pageInfo.getName().equals("Общественный транспорт")).allMatch(page -> page.getExceptionStrings().isEmpty()))
                .isTrue();
        assertThat(uploadStates.getResults("tariff", USER_ID).getFirst().getPages().stream().map(PageInfo::getRow)
                .flatMap(Collection::stream).allMatch(page -> page.getExceptionStrings().isEmpty())).isTrue();
    }

    @Test
    @DisplayName("Экспорт")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
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

            contractRepository.save(contract);

            var tariff = new PublicTariff();

            tariff.setTransportType(transportType);
            tariff.setOrganization(organizations.get(i));
            tariff.setHumanReadableId("HRI %03d".formatted(i));
            tariff.setActive(i % 2 == 0);
            tariff.setRegionId(savedGeozone.getId());
            tariff.setTransportType(TransportTypeEnum.PUBLIC);
            tariff.setBusAvailability(i % 2 == 0);
            tariff.setBusTicketCost(i);
            tariff.setTrolleybusAvailability(i % 2 != 0);
            tariff.setTrolleybusTicketCost(i + 100);
            tariff.setTramAvailability(i % 2 == 0);
            tariff.setTramTicketCost(i + 200);
            tariff.setMetroAvailability(i % 2 != 0);
            tariff.setMetroTicketCost(i + 300);
            tariff.setTravelCardBusAvailability(i % 2 != 0);
            tariff.setTravelCardBusCost(i + 400);
            tariff.setTravelCardTrolleybusAvailability(i % 2 != 0);
            tariff.setTravelCardTrolleybusCost(i + 500);
            tariff.setTravelCardTramAvailability(i % 2 != 0);
            tariff.setTravelCardTramCost(i + 600);
            tariff.setTravelCardMetroAvailability(i % 2 != 0);
            tariff.setTravelCardMetroCost(i + 700);

            tariffRepository.save(tariff);
        }

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
            var sheet = workbook.getSheet("Общественный транспорт");

            assertThat(sheet).isNotNull();

            var header1 = sheet.getRow(0);

            assertHeader1(header1);

            var header2 = sheet.getRow(1);

            assertHeader2(header2);

            var expectedList = tariffRepository.findAll();
            assertThat(expectedList).hasSize(count);
            assertThat(sheet.getLastRowNum() - 1).isEqualTo(count);

            //  rowIndex - индекс строки с учетом заголовков.
            //  На текущий момент строки в excel пишутся не упорядоченно, и нам нужно найти объект из БД по ключу HumanReadableId, и проверить
            //  наличие региона и контракта по названию в списке БД
            for (int rowIndex = 2, index = 0; index < count; rowIndex++, index++) {
                assertRow(geoZoneList, sheet, expectedList, rowIndex, index);
            }
        }
    }

    private void assertRow(List<GeoZone> geoZoneList, XSSFSheet sheet, List<PublicTariff> expectedList, int rowIndex, int index) {
        var actualRow = sheet.getRow(rowIndex);
        var expectedItemOptional = expectedList.stream()
                .filter(tariff -> tariff.getHumanReadableId().equals(actualRow.getCell(0).getStringCellValue()))
                .findFirst();
        AssertionsForClassTypes.assertThat(expectedItemOptional).isPresent();
        var expectedItem = expectedItemOptional.orElseThrow();
        assertSoftly(it -> {
            it.assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(expectedItem.getHumanReadableId());
            it.assertThat(actualRow.getCell(1).getBooleanCellValue()).isEqualTo(expectedItem.isActive());
            it.assertThat(actualRow.getCell(2).getStringCellValue()).isEqualTo(expectedItem.getOrganization().getName());
            it.assertThat(actualRow.getCell(3).getStringCellValue()).isIn(geoZoneList.stream().map(GeoZone::getName).toList());
            it.assertThat(actualRow.getCell(4).getStringCellValue()).isEqualTo(expectedItem.getServiceType().getDescription());
            it.assertThat(actualRow.getCell(5).getNumericCellValue()).isEqualTo(expectedItem.isBusAvailability() ? expectedItem.getBusTicketCost() : 0.0);
            it.assertThat(actualRow.getCell(6).getNumericCellValue()).isEqualTo(expectedItem.isTrolleybusAvailability() ? expectedItem.getTrolleybusTicketCost() : 0.0);
            it.assertThat(actualRow.getCell(7).getNumericCellValue()).isEqualTo(expectedItem.isTramAvailability() ? expectedItem.getTramTicketCost() : 0.0);
            it.assertThat(actualRow.getCell(8).getNumericCellValue()).isEqualTo(expectedItem.isMetroAvailability() ? expectedItem.getMetroTicketCost() : 0.0);
            it.assertThat(actualRow.getCell(9).getNumericCellValue()).isEqualTo(expectedItem.isTravelCardBusAvailability() ? expectedItem.getTravelCardBusCost() : 0.0);
            it.assertThat(actualRow.getCell(10).getNumericCellValue()).isEqualTo(expectedItem.isTravelCardTrolleybusAvailability() ? expectedItem.getTravelCardTrolleybusCost() : 0.0);
            it.assertThat(actualRow.getCell(11).getNumericCellValue()).isEqualTo(expectedItem.isTravelCardTramAvailability() ? expectedItem.getTravelCardTramCost() : 0.0);
            it.assertThat(actualRow.getCell(12).getNumericCellValue()).isEqualTo(expectedItem.isTravelCardMetroAvailability() ? expectedItem.getTravelCardMetroCost() : 0.0);
        });
    }

    private void assertHeader2(XSSFRow header2) {
        assertThat((Iterable<? extends Cell>) header2).isNotNull();
        assertThat(header2.getPhysicalNumberOfCells()).isEqualTo(8);
        assertAll("Checking header level 2",
                () -> assertThat(header2.getCell(5).getStringCellValue()).isEqualTo("Автобус"),
                () -> assertThat(header2.getCell(6).getStringCellValue()).isEqualTo("Троллейбус"),
                () -> assertThat(header2.getCell(7).getStringCellValue()).isEqualTo("Трамвай"),
                () -> assertThat(header2.getCell(8).getStringCellValue()).isEqualTo("Метро"),
                () -> assertThat(header2.getCell(9).getStringCellValue()).isEqualTo("Автобус"),
                () -> assertThat(header2.getCell(10).getStringCellValue()).isEqualTo("Троллейбус"),
                () -> assertThat(header2.getCell(11).getStringCellValue()).isEqualTo("Трамвай"),
                () -> assertThat(header2.getCell(12).getStringCellValue()).isEqualTo("Метро")
        );
    }

    private void assertHeader1(XSSFRow header1) {
        assertThat((Iterable<? extends Cell>) header1).isNotNull();
        assertThat(header1.getPhysicalNumberOfCells()).isEqualTo(7);
        assertAll("Checking header level 1",
                () -> assertThat(header1.getCell(0).getStringCellValue()).isEqualTo("Идентификатор"),
                () -> assertThat(header1.getCell(1).getStringCellValue()).isEqualTo("Активен"),
                () -> assertThat(header1.getCell(2).getStringCellValue()).isEqualTo("Организация"),
                () -> assertThat(header1.getCell(3).getStringCellValue()).isEqualTo("Регион"),
                () -> assertThat(header1.getCell(4).getStringCellValue()).isEqualTo("Тип услуги"),
                () -> assertThat(header1.getCell(5).getStringCellValue()).isEqualTo("Стоимость"),
                () -> assertThat(header1.getCell(9).getStringCellValue()).isEqualTo("Стоимость проездного")
        );
    }

}
