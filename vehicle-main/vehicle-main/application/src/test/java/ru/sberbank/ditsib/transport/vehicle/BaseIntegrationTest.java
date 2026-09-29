package ru.sberbank.ditsib.transport.vehicle;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.vehicle.constants.Role;
import ru.sberbank.ditsib.transport.vehicle.database.dao.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.mockito.Mockito.doReturn;

/**
 * @author skakun-a Базовый класс для интеграционных тестов для переиспользования контекста.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres
public abstract class BaseIntegrationTest {
    public static final LocalDate currentDate = LocalDate.of(2023, 2, 10);
    public final Clock fixedClock = Clock.fixed(currentDate.atStartOfDay().toInstant(ZoneOffset.UTC),
            ZoneId.of(ZoneOffset.UTC.getId()));

    @MockitoSpyBean
    protected EmployeeRepository employeeRepository;

    @MockitoBean
    protected AuthorizationManager<?> manager;

    @MockitoBean
    protected Clock clock;
    
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected EntityManager entityManager;
    
    @MockitoSpyBean
    protected BrandRepository brandRepository;
    
    @MockitoSpyBean
    protected TypeRepository typeRepository;

    @MockitoSpyBean
    protected CategoryRepository categoryRepository;

    @MockitoSpyBean
    protected EngineTypeRepository engineTypeRepository;

    @MockitoSpyBean
    protected DriveRepository driveRepository;

    @MockitoSpyBean
    protected SubtypeRepository subtypeRepository;

    @MockitoSpyBean
    protected ModelRepository modelRepository;

    @MockitoSpyBean
    protected FuelTypeRepository fuelTypeRepository;

    @MockitoSpyBean
    protected VehicleRepository vehicleRepository;

    @MockitoSpyBean
    protected TelematicsRepository telematicsRepository;

    @MockitoSpyBean
    protected TransportRepository transportRepository;

    @MockitoSpyBean
    protected DepartmentRepository departmentRepository;

    @MockitoSpyBean
    protected OrganizationRepository organizationRepository;

    @MockitoSpyBean
    protected PositionRepository positionRepository;

    @MockitoSpyBean
    protected AttorneyRepository attorneyRepository;
    
    @MockitoSpyBean
    protected FuelConsumptionRepository fuelConsumptionRepository;
    
    @MockitoSpyBean
    protected OdometerValueRepository odometerValueRepository;
    
    @MockitoSpyBean
    protected OdometerHistoryRepository odometerHistoryRepository;

    @MockitoSpyBean
    protected BodyTypeRepository bodyTypeRepository;

    @MockitoSpyBean
    protected TransmissionTypeRepository transmissionTypeRepository;

    @MockitoSpyBean
    protected WheelSizeRepository wheelSizeRepository;
    
    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    protected void mockAuthorization() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_DATA_MASTER.name());
    }
    
    @AfterEach
    @Sql("/scripts/clean_db.sql")
    void cleanupNativeDatabase() {
    }

    @AfterEach
    void cleanupDatabase() {
        attorneyRepository.deleteAll();
        fuelConsumptionRepository.deleteAll();
        odometerValueRepository.deleteAll();
        odometerHistoryRepository.deleteAll();
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
        bodyTypeRepository.deleteAll();
        transmissionTypeRepository.deleteAll();
        wheelSizeRepository.deleteAll();
        telematicsRepository.deleteAll();
        employeeRepository.deleteAll();
        positionRepository.deleteAll();
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();
    }
}
