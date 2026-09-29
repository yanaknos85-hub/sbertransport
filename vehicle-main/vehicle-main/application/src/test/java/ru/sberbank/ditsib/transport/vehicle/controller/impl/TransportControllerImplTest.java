package ru.sberbank.ditsib.transport.vehicle.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.vehicle.database.dao.AccessiblePositionRepository;
import ru.sberbank.ditsib.transport.vehicle.database.dao.TransportRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.AccessiblePosition;
import ru.sberbank.ditsib.transport.vehicle.mapper.TransportMapper;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.authorization.test.AuthorizeUtils.authorize;
import static ru.sberbank.ditsib.transport.vehicle.constants.Role.ROLE_ADMIN_CORP_CLIENT;

@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@DisplayName("Тест контроллера транспорта")
class TransportControllerImplTest {

    private static final String CONTROLLER_URL = "/transport";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransportRepository transportRepository;

    @Autowired
    private AccessiblePositionRepository accessiblePositionRepository;

    @Autowired
    private TransportMapper transportMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthorizationManager<?> manager;

    @BeforeEach
    void setUp() {
        authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
    }

    @Test
    @DisplayName("Получение транспортного средства по id")
    @Sql({"/scripts/vehicle_integration_test.sql",
            "/scripts/transport_integration_test.sql",
            "/scripts/transport_controller_test.sql"})
    @Sql(value = "/scripts/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void getById() throws Exception {
        var userId = UUID.randomUUID();
        var transportId = "2fc032da-b668-4bcc-b0fc-a34e16caafe2";

        mockMvc.perform(get(CONTROLLER_URL + "/{transportId}", transportId)
                        .with(jwt().jwt(builder -> builder.jti(userId.toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessiblePositionId").value("569e6465-7788-48b7-8227-de0a593464ca"));
    }

    @Test
    @DisplayName("Получение транспортного средства по несуществующему id")
    void getByNonExistingId() throws Exception {
        var userId = UUID.randomUUID();
        var invalidTransportId = UUID.randomUUID();

        mockMvc.perform(get(CONTROLLER_URL + "/{transportId}", invalidTransportId)
                        .with(jwt().jwt(builder -> builder.jti(userId.toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString(String.format("Транспортное средство с идентификатором %s не найдено", invalidTransportId))));
    }

    @Test
    @DisplayName("Обновление транспортного средства по id")
    @Sql({"/scripts/vehicle_integration_test.sql",
            "/scripts/transport_integration_test.sql",
            "/scripts/transport_controller_test.sql"})
    @Sql(value = "/scripts/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void update() throws Exception {
        var userId = UUID.randomUUID();
        var transportId = UUID.fromString("2fc032da-b668-4bcc-b0fc-a34e16caafe2");
        var newAccessiblePositionId = UUID.fromString("78843169-1557-4531-8f61-549143e36892");

        var transport = transportRepository.findById(transportId).get();
        var accessiblePosition = accessiblePositionRepository.findById(newAccessiblePositionId).get();
        transport.setAccessiblePosition(accessiblePosition);
        var transportUpdateDto = transportMapper.transportToTransportUpdateDto(transport);

        mockMvc.perform(patch(CONTROLLER_URL + "/{transportId}", transportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transportUpdateDto))
                        .with(jwt().jwt(builder -> builder.jti(userId.toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessiblePositionId").value(newAccessiblePositionId.toString()));
    }

    @Test
    @DisplayName("Обновление транспортного средства по id несуществующими данными")
    @Sql({"/scripts/vehicle_integration_test.sql",
            "/scripts/transport_integration_test.sql",
            "/scripts/transport_controller_test.sql"})
    @Sql(value = "/scripts/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void updateWithNonExistingAccessiblePositionId() throws Exception {
        var userId = UUID.randomUUID();
        var transportId = UUID.fromString("2fc032da-b668-4bcc-b0fc-a34e16caafe2");

        var transport = transportRepository.findById(transportId).get();
        var accessiblePosition = Instancio.create(AccessiblePosition.class);
        transport.setAccessiblePosition(accessiblePosition);
        var transportUpdateDto = transportMapper.transportToTransportUpdateDto(transport);

        mockMvc.perform(patch(CONTROLLER_URL + "/{transportId}", transportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transportUpdateDto))
                        .with(jwt().jwt(builder -> builder.jti(userId.toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString(String.format("Не найдена в справочнике позиция с идентификатором %s", accessiblePosition.getId()))));
    }
}
