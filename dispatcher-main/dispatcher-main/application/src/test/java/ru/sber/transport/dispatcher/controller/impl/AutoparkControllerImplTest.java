package ru.sber.transport.dispatcher.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.database.dao.AutoparkRepository;
import ru.sber.transport.dispatcher.database.dao.ContractorRepository;
import ru.sber.transport.dispatcher.database.model.Autopark;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.dto.AutoparkDTO;
import ru.sber.transport.dispatcher.dto.NewAutoparkDTO;
import ru.sber.transport.dispatcher.testutils.TestContractors;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.security.Key;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера автопарка")
@MockBean(Key.class)
@Transactional
class AutoparkControllerImplTest extends KafkaTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private AutoparkRepository autoparkRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorizationManager<?> manager;

    private UUID contractorId;

    @BeforeEach
    void createRepository() {
        contractorId = contractorRepository.save(TestContractors.createTestContractor()).getId();
    }

    @Test
    @DisplayName("Добавление")
    void add() throws Exception {
        var newAutoPark = NewAutoparkDTO.builder()
                .name("testAutopark")
                .vehicleCountNorm(10)
                .build();
        mockMvc.perform(
                        post("/" + contractorId + "/autopark/").contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .content(objectMapper.writeValueAsString(
                                        newAutoPark)))
                .andExpect(status().isOk());

        assertThat(autoparkRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Добавление в несуществующий контрактор")
    void addNonExistsAutoPark() throws Exception {
        var newAutoPark =
                NewAutoparkDTO.builder().name("Non Exit Autopark").vehicleCountNorm(10).build();
        mockMvc.perform(
                        post("/" + UUID.randomUUID() + "/autopark/")
                                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        newAutoPark)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение автопарка по ID")
    void getTest() throws Exception {
        var id = autoparkRepository
                .save(Autopark.builder().name("Get Autopark")
                        .contractor(contractorRepository.findById(contractorId).orElseThrow())
                        .build()).getId();

        var response =
                mockMvc.perform(get("/" + contractorId + "/autopark/" + id + "/")
                                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).andExpect(status().isOk())
                        .andReturn();

        var actual = objectMapper.readValue(response.getResponse().getContentAsString(), AutoparkDTO.class);
        var expected = autoparkRepository.findAll().get(0);

        assertThat(actual.id()).isEqualTo(id);
        assertThat(actual.name()).isEqualTo(expected.getName());
    }

    @Test
    @DisplayName("Получение неактивного автопарка по ID")
    void getDeactivatedTest() throws Exception {
        var autoPark = Autopark.builder()
                .name("Get Autopark")
                .contractor(contractorRepository.findById(contractorId).orElseThrow())
                .build();

        var id = autoparkRepository.save(autoPark).getId();
        mockMvc.perform(delete("/%s/autopark/%s/".formatted(contractorId, id))
                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).andExpect(status().isOk());

        mockMvc.perform(get("/%s/autopark/%s/".formatted(contractorId, id))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Удаление автопарка по ID")
    void getDeleteTest() throws Exception {
        var autoPark = Autopark.builder()
                .name("Get Autopark")
                .contractor(contractorRepository.findById(contractorId).orElseThrow())
                .build();

        autoparkRepository.save(autoPark);
        var id = autoparkRepository.save(autoPark).getId();
        mockMvc.perform(delete("/" + contractorId + "/autopark/" + id + "/")
                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).andExpect(status().isOk());

        assertThat(autoparkRepository.findAll().stream().filter(Autopark::isActive).count()).isZero();
    }

    @Test
    @DisplayName("Изменение")
    void edit() throws Exception {
        var id =
                autoparkRepository
                        .save(Autopark.builder()
                                .name("Test AP")
                                .contractor(contractorRepository.findById(contractorId).orElseThrow())
                                .vehicleCountNorm(10)
                                .build()).getId();

        var newAutoPark = NewAutoparkDTO.builder()
                .name("Test AP")
                .vehicleCountNorm(2)
                .build();
        mockMvc.perform(
                        put("/" + contractorId + "/autopark/" + id + "/")
                                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        newAutoPark)))
                .andExpect(status().isOk());

        assertThat(autoparkRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Изменение (проверка нормы при одном автопарке)")
    void edit_vehicleNorm() throws Exception {
        final int vehicleCountNorm = 10;
        var id =
                autoparkRepository
                        .saveAndFlush(Autopark.builder()
                                .name("Test AP")
                                .contractor(contractorRepository.findById(contractorId).orElseThrow())
                                .vehicleCountNorm(100)
                                .build()).getId();

        var newAutoPark = NewAutoparkDTO.builder().name("Test AP").vehicleCountNorm(vehicleCountNorm).build();
        mockMvc.perform(
                        put("/" + contractorId + "/autopark/" + id + "/")
                                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        newAutoPark)))
                .andExpect(status().isOk());

        Autopark autopark = autoparkRepository.findById(id).orElseThrow();
        assertThat(autopark.getVehicleCountNorm()).isEqualTo(vehicleCountNorm);
    }

    @Test
    @DisplayName("Изменение (проверка нормы при нескольких автопарках)")
    void edit_vehicleNormWithSomeAutoparks() throws Exception {
        Contractor contractor = contractorRepository.saveAndFlush(TestContractors.createTestContractor());
        contractor.setVehicleCountNorm(500);
        contractorRepository.saveAndFlush(contractor);

        var firstAutoparkId =
                autoparkRepository
                        .saveAndFlush(Autopark.builder()
                                .name("Test AP")
                                .contractor(contractor)
                                .vehicleCountNorm(100)
                                .build()).getId();
        var secondAutoparkId =
                autoparkRepository
                        .saveAndFlush(Autopark.builder()
                                .name("Test AP2")
                                .contractor(contractor)
                                .vehicleCountNorm(100)
                                .build()).getId();

        var thirdAutoparkId =
                autoparkRepository
                        .saveAndFlush(Autopark.builder()
                                .name("Test AP3")
                                .contractor(contractor)
                                .vehicleCountNorm(100)
                                .build()).getId();

        var newAutoPark = NewAutoparkDTO.builder().name("Test AP3").vehicleCountNorm(300).build();
        mockMvc.perform(
                        put("/" + contractor.getId() + "/autopark/" + thirdAutoparkId + "/")
                                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        newAutoPark)))
                .andExpect(status().isOk());

        Autopark autopark = autoparkRepository.findById(thirdAutoparkId).orElseThrow();
        assertThat(autopark.getVehicleCountNorm()).isEqualTo(300);
    }

    @Test
    @DisplayName("Изменение (проверка нормы при нескольких автопарках, ошибка)")
    void edit_vehicleNormWithSomeAutoparksException() throws Exception {
        Contractor contractor = contractorRepository.saveAndFlush(TestContractors.createTestContractor());
        contractor.setVehicleCountNorm(500);
        contractorRepository.saveAndFlush(contractor);

        var firstAutoparkId =
                autoparkRepository
                        .saveAndFlush(Autopark.builder()
                                .name("Test AP")
                                .contractor(contractor)
                                .vehicleCountNorm(100)
                                .build()).getId();
        var secondAutoparkId =
                autoparkRepository
                        .saveAndFlush(Autopark.builder()
                                .name("Test AP2")
                                .contractor(contractor)
                                .vehicleCountNorm(100)
                                .build()).getId();

        var thirdAutoparkId =
                autoparkRepository
                        .saveAndFlush(Autopark.builder()
                                .name("Test AP3")
                                .contractor(contractor)
                                .vehicleCountNorm(100)
                                .build()).getId();

        var newAutoPark = NewAutoparkDTO.builder().name("Test AP3").vehicleCountNorm(301).build();
        mockMvc.perform(
                        put("/" + contractor.getId() + "/autopark/" + thirdAutoparkId + "/")
                                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        newAutoPark)))
                .andExpect(status().isConflict());

        Autopark autopark = autoparkRepository.findById(thirdAutoparkId).orElseThrow();
        assertThat(autopark.getVehicleCountNorm()).isEqualTo(100);
    }

    @Test
    @DisplayName("Получение всех")
    void getAll() throws Exception {
        var itemCount = 10;

        for (var i = 0; i < itemCount; i++) {
            var autoPark = Autopark.builder()
                    .name("Autopark name " + i)
                    .contractor(contractorRepository.findById(contractorId).orElseThrow())
                    .vehicleCountNorm(i)
                    .build();

            autoparkRepository.save(autoPark);
        }

        var response = mockMvc.perform(get("/" + contractorId + "/autopark/")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).andExpect(status().isOk())
                .andReturn();

        var actualMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        var actualList = objectMapper.convertValue(actualMap.get("content"), new TypeReference<List<AutoparkDTO>>(){});
        assertThat(actualList).hasSize((int) autoparkRepository.count());

        for (var i = 0; i < itemCount; i++) {
            var actual = actualList.get(i);
            var expected = autoparkRepository.findById(actual.id());
            assertThat(expected).isPresent();

            assertThat(actual.name()).isEqualTo(expected.get().getName());
            assertThat(actual.vehicleCountNorm()).isEqualTo(expected.get().getVehicleCountNorm());
        }
    }

    @Test
    @DisplayName("Получение всех с фильтром по наименованию")
    void getAllByName() throws Exception {
        var itemCount = 20;

        for (var i = 0; i < itemCount; i++) {
            var autoPark = Autopark.builder()
                    .name("Autopark name " + i)
                    .contractor(contractorRepository.findById(contractorId).orElseThrow())
                    .build();

            autoparkRepository.save(autoPark);
        }

        var response = mockMvc.perform(get("/" + contractorId + "/autopark/?name=" + "Autopark name 1")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).andExpect(status().isOk())
                .andReturn();

        var actualMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        var actualList = objectMapper.convertValue(actualMap.get("content"), new TypeReference<List<AutoparkDTO>>(){});

        assertThat(actualList).hasSize(11);

        for (var actual : actualList) {
            var expected = autoparkRepository.findById(actual.id());
            assertThat(expected).isPresent();

            assertThat(actual.name()).isEqualTo(expected.get().getName());
        }
    }

    @Test
    @DisplayName("Получение всех с фильтром по активности")
    void getAllByActive() throws Exception {
        var itemCount = 20;
        var active = true;

        for (var i = 0; i < itemCount; i++) {
            var autoPark = Autopark.builder()
                    .name("Autopark name " + i)
                    .contractor(contractorRepository.findById(contractorId).orElseThrow())
                    .active(active)
                    .build();
            autoparkRepository.save(autoPark);
            active = !active;
        }

        var response = mockMvc.perform(get("/" + contractorId + "/autopark/?active=" + "true")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).andExpect(status().isOk())
                .andReturn();

        var actualMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        var actualList = objectMapper.convertValue(actualMap.get("content"), new TypeReference<List<AutoparkDTO>>(){});

        assertThat(actualList).hasSize(10);

        for (var actual : actualList) {
            var expected = autoparkRepository.findById(actual.id());
            assertThat(expected).isPresent();
            assertThat(actual.name()).isEqualTo(expected.get().getName());
            assertThat(actual.active()).isEqualTo(expected.get().isActive());
        }
    }

    @Test
    @DisplayName("Получение филиала с routingId")
    void getAllByRoutingId() throws Exception {
        var itemCount = 20;
        var active = true;

        for (var i = 0; i < itemCount; i++) {
            var autoPark = Autopark.builder()
                    .name("Autopark name " + i)
                    .contractor(contractorRepository.findById(contractorId).orElseThrow())
                    .active(active)
                    .build();
            autoparkRepository.save(autoPark);
            active = !active;
        }

        var autoParkWithRoutingId = Autopark.builder()
                .name("Autopark name " + itemCount)
                .contractor(contractorRepository.findById(contractorId).orElseThrow())
                .active(active)
                .routingId(UUID.randomUUID())
                .build();
        autoparkRepository.save(autoParkWithRoutingId);

        var response = mockMvc.perform(get("/" + contractorId + "/autopark/?routingId=" + autoParkWithRoutingId.getRoutingId())
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).andExpect(status().isOk())
                .andReturn();

        var actualMap = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<LinkedHashMap<String, Object>>() {});
        var actualList = objectMapper.convertValue(actualMap.get("content"), new TypeReference<List<AutoparkDTO>>(){});

        assertThat(actualList).hasSize(1);

        for (var actual : actualList) {
            var expected = autoparkRepository.findById(actual.id());
            assertThat(expected).isPresent();
            assertThat(actual.name()).isEqualTo(expected.get().getName());
            assertThat(actual.active()).isEqualTo(expected.get().isActive());
            assertThat(actual.routingId()).isEqualTo(expected.get().getRoutingId());
        }
    }
}