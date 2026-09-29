package ru.sberbank.ditsib;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.database.dao.*;
import ru.sberbank.ditsib.enumerate.Role;
import ru.sberbank.ditsib.service.GeoService;

import java.time.Clock;

/**
 * @author skakun-a Базовый класс для интеграционных тестов для переиспользования контекста.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres
public abstract class BaseIntegrationTest {
    @Autowired
    protected EmployeeRepository employeeRepository;
    @MockitoBean
    protected AuthorizationManager<?> manager;
    @MockitoBean
    protected GeoService geoService;
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected EntityManager entityManager;
    @Autowired
    protected DepartmentRepository departmentRepository;
    @Autowired
    protected OrganizationRepository organizationRepository;
    @Autowired
    protected PositionRepository positionRepository;
    @Autowired
    protected LeadRepository leadRepository;
    @Autowired
    protected MainLeadRepository mainLeadRepository;
    @Autowired
    protected PointLeadRepository pointLeadRepository;
    @Autowired
    protected ObjectMapper objectMapper;
    
    @BeforeEach
    protected void mockAuthorization() {
        AuthorizeUtils.authorize(manager, Role.ROLE_DISPATCHER_SUPPORT_SERVICE.name());
    }
    
    @AfterEach
    void cleanupDatabase() {
        pointLeadRepository.deleteAll();
        leadRepository.deleteAll();
        mainLeadRepository.deleteAll();
        employeeRepository.deleteAll();
        positionRepository.deleteAll();
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();
    }
}