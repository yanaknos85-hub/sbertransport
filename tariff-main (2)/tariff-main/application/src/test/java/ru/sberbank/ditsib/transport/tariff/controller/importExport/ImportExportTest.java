package ru.sberbank.ditsib.transport.tariff.controller.importExport;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Sort;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.file_works.services.UploadStates;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.tariff.database.dao.*;
import ru.sberbank.ditsib.transport.tariff.database.model.BaseTariff_;
import ru.sberbank.ditsib.transport.tariff.database.model.Contract;
import ru.sberbank.ditsib.transport.tariff.database.model.Contractor;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Department;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;

import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@DisplayName("TRANSPORT-9113")
@EmbeddedPostgres
@AutoConfigureMockMvc
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles({"test", "kafka"})
class ImportExportTest extends KafkaTest {
    public static final String USERNAME = "name";
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private TaxiTariffRepository tariffRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private ContractorRepository contractorRepository;
    
    @Autowired
    private ContractRepository contractRepository;
    
    @Autowired
    private UploadStates uploadStates;
    
    @Autowired
    private GeoZoneRepository geoZoneRepository;
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
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
    @DisplayName("Тест импорта")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @WithMockUser(username = USERNAME, roles = "GUEST")
    @Disabled("Требуется актуализация")
    void test_import() throws Exception {
        var geoZone = new GeoZone();
        geoZone.setId(UUID.randomUUID());
        geoZone.setName("Салават, город");
        geoZone = geoZoneRepository.save(geoZone);
        
        Organization organization = createOrganization();
        
        var department = Department.builder()
                                   .id(UUID.randomUUID())
                                   .departmentName("Тестовый_департамент_01")
                                   .organizationId(organization.getId())
                                   .build();
        
        departmentRepository.save(department);
        
        var contractor = new Contractor();
        contractor.setId(UUID.randomUUID());
        contractor.setIntegrationType(TaxiExternalIntegrationType.JSON_API_1_0.name());
        contractor.setName("КонтрагентИмп");
        contractor.setRegionIds(List.of(geoZone.getId()));
        
        contractor = contractorRepository.save(contractor);
        
        var contract = new Contract();
        contract.setId(UUID.randomUUID());
        contract.setContractNumber("1595753685");
        contract.setContractorId(contractor.getId());
        contract.setOrganizations(Set.of(organization));
        contract.setRegionIds(Set.of(geoZone.getId()));
        contract.setSum(100L);
        contract.setStartDate(LocalDate.now().minusDays(1));
        contract.setEndDate(LocalDate.now().plusDays(1));
        contract.setCreationTime(LocalDateTime.now());
        contract.setTransportType(TransportTypeEnum.TAXI);
        contract.setUserId(UUID.randomUUID());
        
        contractRepository.save(contract);
        
        var file = new MockMultipartFile("file", "file.xls",
                                         "application/vnd.ms-excel",
                                         getClass().getClassLoader().getResourceAsStream("load/Tariff_new.xls"));
        
        assertRequest(file);
        var actualList = tariffRepository.findAll(Sort.by(BaseTariff_.HUMAN_READABLE_ID));
        
        assertThat(actualList).hasSize(1);;
        
        var actual = actualList.getFirst();
        
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getHumanReadableId()).isNotNull();
        assertThat(actual.isActive()).isTrue();
        assertThat(actual.getTaxiClass()).isEqualTo(TaxiClass.ECONOMY);
        assertThat(actual.getContract().getContractNumber()).isEqualTo("1595753685");
        assertThat(actual.getOrganization().getName()).isEqualTo("Тестовая_организация_01");
        assertThat(actual.getRegionId()).isEqualTo(geoZone.getId());
        assertThat(actual.getTransportType()).isEqualTo(TransportTypeEnum.TAXI);
        assertThat(actual.getRideCostPerKm()).isEqualTo(1000);
        assertThat(actual.getRideCostPerMin()).isEqualTo(1000);
        assertThat(actual.getWaitCostPerMin()).isEqualTo(1000);
        assertThat(actual.getWaitCostPerMinIntermediate()).isEqualTo(1000);
    }
    
    private Organization createOrganization() {
        var organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setName("Тестовая_организация_01");
        organization.setActive(true);
        organization.setDigitId(new Random().nextLong(9999));
        organization = organizationRepository.save(organization);
        return organization;
    }
    
    private void assertRequest(MockMultipartFile file) throws Exception {
        mockMvc.perform(multipart("/files/tariff").file(file))
               .andExpect(status().isOk());
        
        await().timeout(Duration.ofSeconds(30))
               .pollInterval(Duration.ofSeconds(1))
               .until(() -> uploadStates.getResults("tariff", USERNAME).size(), equalTo(1));
        await().timeout(Duration.ofSeconds(30))
               .pollInterval(Duration.ofSeconds(1))
               .until(() -> uploadStates.getResults("tariff", USERNAME).getFirst().getFinished(), equalTo(true));
    }
    
}
