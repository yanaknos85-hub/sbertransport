package ru.sberbank.ditsib.transport.tariff.controller.importExport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.file_works.database.dao.ExportTaskRepository;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.tariff.database.dao.*;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;

import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres
@Transactional
@DisplayName("Проверка импорта/экспорта рабочих групп")
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles({"test", "kafka"})
class WorkGroupExportTest extends KafkaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContractRepository contractRepository;


    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private TariffService tariffService;

    @Autowired
    private TaxiTariffRepository taxiTariffRepository;

    @Autowired
    private GeoZoneRepository geoZoneRepository;

    @Autowired
    private ExportTaskRepository exportTaskRepository;

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

            organizationRepository.save(organization);

            var contractor = new Contractor();

            contractor.setId(UUID.randomUUID());
            contractor.setIntegrationType(TaxiExternalIntegrationType.JSON_API_1_0.name());
            contractor.setName(String.format("Контрагент %03d", i + 1));
            contractor.setIntegrationEmail(i + "@mail.ru");
            contractorRepository.save(contractor);
        }
        Mockito.when(restTemplate.postForEntity(any(), any(), any())).thenReturn(null);
    }

    @Test
    @DisplayName("Экспорт")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void test_export() throws Exception {
        var organizations = organizationRepository.findAll(Sort.by(Organization_.NAME));
        var contractors = contractorRepository.findAll(Sort.by(Contractor_.NAME));

        var count = 100;

        for (var i = 0; i < count; i++) {
            final var savedGeozone = geoZoneRepository
                    .save(new GeoZone(UUID.randomUUID(), String.format("Регион %03d", i + 1), i + 1 + "", null));
            final var transportType = TransportTypeEnum.values()[i % TransportTypeEnum.values().length];
            final var taxiClass = TaxiClass.values()[i % TaxiClass.values().length];

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
            // триггерное время
            tariff.getCoopTariffParams().setMinCancelTimeMin(i + 2900);
            tariffService.save(tariff);
        }

        var content = mockMvc.perform(get("/files/workgroup")
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
            var sheet = workbook.getSheet("Рабочие группы");

            assertThat(sheet).isNotNull();

            var header1 = sheet.getRow(0);

            assertThat(header1).isNotNull();
            assertThat(header1.getPhysicalNumberOfCells()).isEqualTo(7);
            assertAll("Checking header",
                    () -> assertThat(header1.getCell(0).getStringCellValue()).isEqualTo("Рабочая группа"),
                    () -> assertThat(header1.getCell(1).getStringCellValue()).isEqualTo("Корп. клиент"),
                    () -> assertThat(header1.getCell(2).getStringCellValue()).isEqualTo("Идентификатор геозоны"),
                    () -> assertThat(header1.getCell(3).getStringCellValue()).isEqualTo("Название геозоны"),
                    () -> assertThat(header1.getCell(4).getStringCellValue()).isEqualTo("Контрагент"),
                    () -> assertThat(header1.getCell(5).getStringCellValue()).isEqualTo("EMAIL контрагента"),
                    () -> assertThat(header1.getCell(6).getStringCellValue()).isEqualTo("№ Договора")
            );

            final var expectedMap = taxiTariffRepository.findAll().stream().collect(Collectors.toMap(TaxiTariff::getWorkGroup, elt -> elt));

            // rowIndex - индекс строки с учетом заголовком.
            for (int rowIndex = 1, i = 0; i < count; rowIndex++, i++) {
                var actualRow = sheet.getRow(rowIndex);
                assertThat(actualRow).isNotNull();
                var expectedItem = expectedMap.get(actualRow.getCell(0).getStringCellValue());
                assertNotNull(expectedItem);

                final var contractor =
                        contractorRepository.findById(expectedItem.getContract().getContractorId()).orElseThrow();
                assertSoftly(it -> {
                    it.assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(
                            expectedItem.getWorkGroup());
                    it.assertThat(actualRow.getCell(1).getStringCellValue()).isEqualTo(
                            expectedItem.getOrganization().getName());
                    it.assertThat(actualRow.getCell(2).getStringCellValue()).isEqualTo(
                            expectedItem.getRegionId().toString());
                    it.assertThat(actualRow.getCell(3).getStringCellValue()).isEqualTo(
                            geoZoneRepository.findById(expectedItem.getRegionId()).map(GeoZone::getName)
                                    .orElse(null));
                    it.assertThat(actualRow.getCell(4).getStringCellValue()).isEqualTo(
                            contractor.getName());
                    it.assertThat(actualRow.getCell(5).getStringCellValue()).isEqualTo(
                            contractor.getIntegrationEmail());
                    it.assertThat(actualRow.getCell(6).getStringCellValue()).isEqualTo(
                            expectedItem.getContract().getContractNumber());
                });
            }
        }

    }

}
