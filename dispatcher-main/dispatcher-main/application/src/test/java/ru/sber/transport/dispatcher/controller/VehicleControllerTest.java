package ru.sber.transport.dispatcher.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.database.dao.AutoparkRepository;
import ru.sber.transport.dispatcher.database.dao.ContractorRepository;
import ru.sber.transport.dispatcher.database.dao.VehicleRepository;
import ru.sber.transport.dispatcher.database.model.Autopark;
import ru.sber.transport.dispatcher.database.model.CarModel;
import ru.sber.transport.dispatcher.database.model.CargoVehicleData;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.database.model.Vehicle;
import ru.sber.transport.dispatcher.dto.CarModelDto;
import ru.sber.transport.dispatcher.dto.VehicleDTO;
import ru.sber.transport.dispatcher.service.IntegrationClientService;
import ru.sber.transport.dispatcher.testutils.TestAutoparks;
import ru.sber.transport.dispatcher.testutils.TestContractors;
import ru.sber.transport.dispatcher.testutils.TestVehicles;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.security.Key;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка контроллера транспорта")
@MockBean(Key.class)
class VehicleControllerTest extends KafkaTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private AutoparkRepository autoparkRepository;

    @MockBean
    private AuthorizationManager<?> manager;

    @MockBean
    private IntegrationClientService integrationClientService;

    private Contractor contractor;
    private Autopark autopark;
    private Autopark autopark2;

    @BeforeEach
    void createRepository() {
        contractor = contractorRepository.save(TestContractors.createTestContractor());
        autopark = autoparkRepository.save(TestAutoparks.createTestAutopark(contractor));
        autopark2 = autoparkRepository.save(TestAutoparks.createTestAutopark(contractor));
    }

    private String getBaseVehicleControllerPath() {
        return "/" + contractor.getId() + "/autopark/" + autopark.getId() + "/vehicle/";
    }

    private String getRandomUUIDBaseVehicleControllerPath() {
        return "/" + contractor.getId() + "/autopark/" + UUID.randomUUID() + "/vehicle/";
    }

    @Test
    @DisplayName("Добавление")
    void add() throws Exception {
        var vehicleDTO = TestVehicles.createTestVehicleDTO();

        var response =
                mockMvc.perform(
                                post(getBaseVehicleControllerPath()).contentType(MediaType.APPLICATION_JSON)
                                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                        .content(objectMapper.writeValueAsString(
                                                vehicleDTO)))
                        .andExpect(status().isOk());

        assertThat(vehicleRepository.count()).isEqualTo(1);

        var actualDb = vehicleRepository.findAll().get(0);

        response.andExpect(jsonPath("$.color").value(vehicleDTO.getColor()));
        response.andExpect(jsonPath("$.model.brand").value(vehicleDTO.getModel().getBrand()));
        response.andExpect(jsonPath("$.model.name").value(vehicleDTO.getModel().getName()));
        response.andExpect(jsonPath("$.model.year").value(vehicleDTO.getModel().getYear()));
        response.andExpect(jsonPath("$.stateNumber").value(vehicleDTO.getStateNumber()));
        response.andExpect(jsonPath("$.vehicleAdditional.semitrailerNumber").value(((CargoVehicleData) vehicleDTO.getVehicleAdditional()).getSemitrailerNumber()));
        response.andExpect(jsonPath("$.vehicleAdditional.volume").value(((CargoVehicleData) vehicleDTO.getVehicleAdditional()).getVolume()));
        response.andExpect(jsonPath("$.vehicleAdditional.length").value(((CargoVehicleData) vehicleDTO.getVehicleAdditional()).getLength()));
        response.andExpect(jsonPath("$.vehicleAdditional.width").value(((CargoVehicleData) vehicleDTO.getVehicleAdditional()).getWidth()));
        response.andExpect(jsonPath("$.vehicleAdditional.height").value(((CargoVehicleData) vehicleDTO.getVehicleAdditional()).getHeight()));

        response.andExpect(jsonPath("$.id").value(actualDb.getId().toString()));
        response.andExpect(jsonPath("$.model.brand").value(actualDb.getModel().getBrand()));
        response.andExpect(jsonPath("$.model.name").value(actualDb.getModel().getName()));
        response.andExpect(jsonPath("$.model.year").value(actualDb.getModel().getYear()));
        response.andExpect(jsonPath("$.color").value(actualDb.getColor()));
        response.andExpect(jsonPath("$.stateNumber").value(actualDb.getStateNumber()));
        response.andExpect(jsonPath("$.vehicleAdditional.semitrailerNumber").value(actualDb.getVehicleAdditional().get("semitrailerNumber")));
        response.andExpect(jsonPath("$.vehicleAdditional.volume").value(actualDb.getVehicleAdditional().get("volume")));
        response.andExpect(jsonPath("$.vehicleAdditional.length").value(actualDb.getVehicleAdditional().get("length")));
        response.andExpect(jsonPath("$.vehicleAdditional.width").value(actualDb.getVehicleAdditional().get("width")));
        response.andExpect(jsonPath("$.vehicleAdditional.height").value(actualDb.getVehicleAdditional().get("height")));
    }

    @Test
    @DisplayName("Добавление в несуществующего контрагента")
    void add_nonExistsContractor() throws Exception {
        var newTransport = TestVehicles.createTestVehicleDTO();

        var result = mockMvc.perform(
                        post(getRandomUUIDBaseVehicleControllerPath()).contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .content(objectMapper.writeValueAsString(
                                        newTransport)))
                .andExpect(status().isNotFound());

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Добавление дубликата")
    void add_duplicate() throws Exception {
        vehicleRepository.save(TestVehicles.createTestVehicle(autopark, 0));

        var newVehicleDTO = TestVehicles.createTestVehicleDTO();
        vehicleRepository.flush();
        var result = mockMvc.perform(post(getBaseVehicleControllerPath()).contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(objectMapper.writeValueAsString(
                                newVehicleDTO)))
                .andExpect(status().isConflict());

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Проверка на дубликат при сохранении ТС c существующим Vin")
    void add_duplicate_vehicle() throws Exception {
        Vehicle vehicle1 = TestVehicles.createTestVehicle(autopark, 0);
        vehicleRepository.save(vehicle1);

        var newVehicleDTO = TestVehicles.createTestVehicleDTO();
        newVehicleDTO.setVin(vehicle1.getVin());

        vehicleRepository.flush();

        var result = mockMvc.perform(post(getBaseVehicleControllerPath()).contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(objectMapper.writeValueAsString(newVehicleDTO)))
                .andExpect(status().isConflict());

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Проверка на дубликат при сохранении ТС c существующим Vin, но такое ТС уже было, просто оно не активно")
    void add_duplicate_inactive_vehicle() throws Exception {
        Vehicle vehicle1 = TestVehicles.createTestVehicle(autopark, 0);
        vehicle1.setInExploitation(false);
        vehicleRepository.save(vehicle1);

        //было такое ТС, но оно не активно
        assertThat(vehicleRepository.count()).isEqualTo(1);

        var newVehicleDTO = TestVehicles.createTestVehicleDTO();
        newVehicleDTO.setVin(vehicle1.getVin());

        vehicleRepository.flush();

        MvcResult result = mockMvc.perform(post(getBaseVehicleControllerPath()).contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(objectMapper.writeValueAsString(newVehicleDTO)))
                .andExpect(status().isOk()).andReturn();
        VehicleDTO actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertThat(vehicleRepository.count()).isEqualTo(2);

        assertThat(actual.getVin()).isEqualTo(newVehicleDTO.getVin());
        assertThat(actual.getInExploitation()).isTrue();
    }

    @Test
    @DisplayName("Изменение")
    void edit() throws Exception {
        var id = vehicleRepository.save(TestVehicles.createTestVehicle(autopark, 0)).getId();

        var newVehicleDto = TestVehicles.incrementVehicleDTO(TestVehicles.createTestVehicleDTO(), 1);
        newVehicleDto.getModel().setYear(2021);
        mockMvc.perform(
                        put(getBaseVehicleControllerPath() + id + "/").contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .content(objectMapper.writeValueAsString(
                                        newVehicleDto)))
                .andExpect(status().isOk());

        assertThat(vehicleRepository.count()).isEqualTo(1);

        var vehicles = vehicleRepository.findAll();
        var actualDb = vehicles.get(0);

        assertThat(vehicles).hasSize(1);
        assertThat(actualDb.getColor()).isEqualTo(newVehicleDto.getColor());
        assertEqual(newVehicleDto.getModel(), actualDb.getModel());
        assertThat(actualDb.getStateNumber()).isEqualTo(newVehicleDto.getStateNumber());
    }

    @Test
    @DisplayName("Изменение несуществующего")
    void edit_nonExists() throws Exception {
        var newTransport = TestVehicles.createTestVehicleDTO();

        var result = mockMvc.perform(put(getBaseVehicleControllerPath() + "/" + UUID.randomUUID())
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTransport)))
                .andExpect(status().isNotFound());

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Изменение на дубликат")
    void edit_toDuplicate() throws Exception {
        var firstVehicle = vehicleRepository.save(TestVehicles.createTestVehicle(autopark, 0));
        var secondVehicle = vehicleRepository.save(TestVehicles.incrementVehicle(firstVehicle, 1));

        var firstVehicleDto = TestVehicles.createTestVehicleDTO();
        firstVehicleDto.setVin(secondVehicle.getVin());
        vehicleRepository.flush();
        var result = mockMvc.perform(
                        put(getBaseVehicleControllerPath() + "/" + firstVehicle.getId() + "/").contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .content(objectMapper.writeValueAsString(
                                        firstVehicleDto)))
                .andExpect(status().isConflict());

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Удаление")
    void deleteTransport() throws Exception {
        var id = vehicleRepository.save(TestVehicles.createTestVehicle(autopark, 0)).getId();

        assertThat(vehicleRepository.count()).isEqualTo(1);

        mockMvc.perform(delete(getBaseVehicleControllerPath() + id + "/")
                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).andExpect(status().isOk());

        var vehicleList = vehicleRepository.findAll();
        assertThat(vehicleList).hasSize(1);
        assertThat(vehicleList.get(0).isActive()).isEqualTo(false);
    }

    @Test
    @DisplayName("Очистка транспорта при удалении Автопарка")
    void deleteContractorTransport() throws Exception {
        var itemCount = new Random().nextInt(100) + 1;

        for (var i = 0; i < itemCount; i++) {
            vehicleRepository.save(TestVehicles.incrementVehicle(TestVehicles.createTestVehicle(autopark, i), i));
        }

        assertThat(vehicleRepository.count()).isEqualTo(itemCount);

        mockMvc.perform(delete("/%s/autopark/%s/".formatted(contractor.getId(), autopark.getId()))
                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).andExpect(status().isOk());

        assertThat(vehicleRepository.findAll().stream().filter(v -> v.getAutopark().getId().equals(autopark.getId()) && v.getAutopark().isActive()).count()).isZero();
    }

    @Test
    @DisplayName("Удаление несуществующего")
    void deleteContractor_nonExists() throws Exception {
        mockMvc.perform(delete(getBaseVehicleControllerPath() + "/" + UUID.randomUUID())
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isNotFound());
    }

    @Disabled
    @Test
    @DisplayName("Получение одного")
    void getOne() throws Exception {
        var id = vehicleRepository.save(TestVehicles.createTestVehicle(autopark, 0)).getId();

        var response =
                mockMvc.perform(get(getBaseVehicleControllerPath() + "/" + id + "/")
                                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).andExpect(status().isOk())
                        .andExpect(status().isOk())
                        .andReturn();

        var actual = objectMapper.readValue(response.getResponse().getContentAsString(), VehicleDTO.class);
        var expected = vehicleRepository.findAll().get(0);

        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getColor()).isEqualTo(expected.getColor());
        assertThat(actual.getStateNumber()).isEqualTo(expected.getStateNumber());
    }

    @Test
    @DisplayName("Получение удаленного")
    void getDeleteOne() throws Exception {
        Vehicle testVehicle = TestVehicles.createTestVehicle(autopark, 0);
        var id = vehicleRepository.save(testVehicle).getId();

        mockMvc.perform(delete(getBaseVehicleControllerPath() + id + "/")
                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
        ).andExpect(status().isOk());

        var response =
                mockMvc.perform(get(getBaseVehicleControllerPath() + id + "/")
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                        .andExpect(status().isNotFound());

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Получение одного несуществующего")
    void getOne_nonExists() throws Exception {
        mockMvc.perform(get(getBaseVehicleControllerPath() + "/" + UUID.randomUUID())
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение всех")
    void getAll() throws Exception {
        var itemCount = 100;

        for (var i = 0; i < itemCount; i++) {
            vehicleRepository.save(TestVehicles.incrementVehicle(TestVehicles.createTestVehicle(autopark, i), i));
        }

        var pageCount = 20;
        var response =
                mockMvc.perform(get(getBaseVehicleControllerPath())
                                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content.length()").value(pageCount));

        var expectedList = vehicleRepository.findAll();

        for (var i = 0; i < pageCount; i++) {
            var expected = expectedList.get(i);
            response
                    .andExpect(jsonPath("$.content[%s].model.brand".formatted(i)).value(expected.getModel().getBrand()))
                    .andExpect(jsonPath("$.content[%s].model.name".formatted(i)).value(expected.getModel().getName()))
                    .andExpect(jsonPath("$.content[%s].model.year".formatted(i)).value(expected.getModel().getYear()))
                    .andExpect(jsonPath("$.content[%s].vin".formatted(i)).value(expected.getVin()))
                    .andExpect(jsonPath("$.content[%s].passport".formatted(i)).value(expected.getPassport()))
                    .andExpect(jsonPath("$.content[%s].stateNumber".formatted(i)).value(expected.getStateNumber()))
                    .andExpect(jsonPath("$.content[%s].insuranceNumber".formatted(i)).value(expected.getInsuranceNumber()))
            ;
        }
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Получение всех транспортных средств контрагента")
    void getAllInContractor() throws Exception {
        var itemCount = 55;

        int increment = 0;
        for (var i = 0; i < itemCount; i++) {
            vehicleRepository.save(TestVehicles.incrementVehicle(TestVehicles.createTestVehicle(autopark, increment), increment));
            increment++;
            vehicleRepository.save(TestVehicles.incrementVehicle(TestVehicles.createTestVehicle(autopark2, increment), increment));
            increment++;
        }

        var response =
                mockMvc.perform(get("/%s/vehicle/".formatted(contractor.getId()))
                                .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content.length()").value(20));

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Фильтр транспортных средств")
    void test_search_vehicle() throws Exception {
        var itemCount = 2;
        for (var i = 0; i < itemCount; i++) {
            vehicleRepository.save(TestVehicles.incrementVehicle(TestVehicles.createTestVehicle(autopark, i), i));
        }
        assertThat(vehicleRepository.count()).isEqualTo(2);

        Vehicle vehicle = vehicleRepository.findAll().get(0);
        vehicle.setAutopark(autopark2);
        vehicle.setStateNumber("X102XX163RUS");
        vehicle.setPassport("00TK000003");
        vehicle.setVin("XYX000000X0001002");
        vehicle.setManufactureYear(2002);
        vehicle.setTransmissionType("Mechanical");
        vehicle.getModel().setBrand("Bentley");
        vehicle.getModel().setName("Motors");
        vehicleRepository.save(vehicle);

        var urlTemplate = ("/%s/vehicle/" +
                "?autopark=%s" +
                "&manufactureYear=%d" +
                "&transmissionType=%s" +
                "&brand=%s" +
                "&model=%s").formatted(contractor.getId(), autopark2.getId(), 2002, "Mechanical", "Bentley", "Motors");

        var response = mockMvc.perform(get(urlTemplate)
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        assertThat(response).isNotNull();
    }

    private static void assertEqual(CarModelDto modelDto, CarModel model) {
        assertThat(modelDto.getName()).isEqualTo(model.getName());
        assertThat(modelDto.getYear()).isEqualTo(model.getYear());
        assertThat(modelDto.getBrand()).isEqualTo(model.getBrand());

    }
}