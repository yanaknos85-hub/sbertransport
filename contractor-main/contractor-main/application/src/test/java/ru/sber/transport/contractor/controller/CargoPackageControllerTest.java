package ru.sber.transport.contractor.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.contractor.database.dao.EmployeeRepository;
import ru.sber.transport.contractor.database.model.Employee;
import ru.sber.transport.contractor.messages.CargoPackageMessage;
import ru.sber.transport.contractor.testutils.TestContractors;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.contractor.database.dao.CargoPackageRepository;
import ru.sber.transport.contractor.database.dao.ContractorRepository;
import ru.sber.transport.contractor.dto.cargo.CargoPackageDto;
import ru.sber.transport.contractor.dto.cargo.NewCargoPackageDto;
import ru.sber.transport.contractor.database.model.CargoPackage;
import ru.sber.transport.contractor.database.model.Contractor;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_contractor")
@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера упаковочных материалов")
@Transactional
@MockBean(Key.class)
@ActiveProfiles("test")
class CargoPackageControllerTest {

    private final static String USER_ID = UUID.randomUUID().toString();

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private CargoPackageRepository repository;
    
    @Autowired
    private ContractorRepository contractorRepository;

    @MockBean
    private AuthorizationManager<?> manager;

    @MockBean(name = "cargoPackageOutput")
    private OutputBridge cargoPackageOutput;
    
    private Contractor contractor;
    
    @BeforeEach
    void setup() {
        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
            .consent(true)
            .organizationId(UUID.randomUUID()).build();
        employeeRepository.save(employee);
        AuthorizeUtils.authorize(manager, "ROLE_GUEST");
        contractor = contractorRepository.save(TestContractors.createTestContractor());
    }
    
    @Test
    @DisplayName("Добавление")
    void addTest() throws Exception {
        NewCargoPackageDto newCargoPackageDto = createDefaultDto();
        
        var request = objectMapper.writeValueAsString(newCargoPackageDto);
        
        mockMvc.perform(
                post("/" + contractor.getId() + "/cargo/package/")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
               .andExpect(status().isOk()).andReturn();
        
        CargoPackage cargoPackage  =
                repository.findAllByContractor(contractor.getId()).get(0);
        
        assertEquals(newCargoPackageDto.getContractor(), cargoPackage.getContractor().getId());
        assertEquals(newCargoPackageDto.getLabel(), cargoPackage.getLabel());
        assertEquals(newCargoPackageDto.getUnit(), cargoPackage.getUnit());
        assertEquals(newCargoPackageDto.getCost(), cargoPackage.getCost());

        var messageCaptor = ArgumentCaptor.forClass(CargoPackageMessage.class);
        verify(cargoPackageOutput).send(messageCaptor.capture());
        var messages = messageCaptor.getAllValues();
        
        var addMessage = messages.get(0);
        assertFalse(addMessage.deleted());
        assertEquals(addMessage.getId(), cargoPackage.getId());
        assertEquals(addMessage.organization(), cargoPackage.getContractor().getId());
        assertEquals(addMessage.label(), cargoPackage.getLabel());
        assertEquals(addMessage.unit(), cargoPackage.getUnit());
        assertEquals(addMessage.cost(), cargoPackage.getCost());
    }
    
    @Test
    @DisplayName("Изменение")
    void updateTest() throws Exception {
        CargoPackage cargoPackage = repository.save(createDefault("new cargo package"));
        
        NewCargoPackageDto updateDTO = NewCargoPackageDto.builder()
                                                         .label("edited cargo package")
                                                         .contractor(cargoPackage.getContractor().getId())
                                                         .unit(cargoPackage.getUnit())
                                                         .cost(cargoPackage.getCost())
                                                         .build();
        
        var request = objectMapper.writeValueAsString(updateDTO);
        
        mockMvc.perform(
                put("/" + contractor.getId() + "/cargo/package/" + cargoPackage.getId() +"/")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
               .andExpect(status().isOk())
               .andReturn();
        
        CargoPackage editedCargoPackage  = repository.findAllByContractor(contractor.getId()).get(0);
        assertNotEquals(cargoPackage.getId(), editedCargoPackage.getId());
        assertEquals(updateDTO.getLabel(), editedCargoPackage.getLabel());

        var messageCaptor = ArgumentCaptor.forClass(CargoPackageMessage.class);
        verify(cargoPackageOutput, times(2)).send(messageCaptor.capture());
        var messages = messageCaptor.getAllValues();

        assertThat(messages).hasSize(2);
        messages.sort(Comparator.comparing(CargoPackageMessage::deleted).reversed());
        var deleteMessage = messages.get(0);
        assertTrue(deleteMessage.deleted());
        assertEquals(deleteMessage.getId(), cargoPackage.getId());
        
        var addMessage = messages.get(1);
        assertFalse(addMessage.deleted());
        assertEquals(addMessage.getId(), editedCargoPackage.getId());
        assertEquals(addMessage.label(), editedCargoPackage.getLabel());
    }
    
    @Test
    @DisplayName("Получение всех упаковок")
    void getAllTest() throws Exception {
        repository.save(createDefault("test cargo package1"));
    
        repository.save(createDefault("test cargo package2"));
        
        var result = mockMvc.perform(get("/" + contractor.getId() + "/cargo/packages/")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.length()").value(2))
                            .andReturn();
        
        List<Map<String, Object>> objectMap =
                new ObjectMapper().readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                             new TypeReference<>() {
                                             });
        
        for (Map<String, Object> map : objectMap) {
            UUID id = UUID.fromString(String.valueOf(map.get("id")));
            CargoPackage expected = repository.findById(id).orElseThrow();
            assertThat(map).containsEntry("label", expected.getLabel());
        }
    }
    
    @Test
    @DisplayName("Получение упаковки")
    void getTest() throws Exception {
        CargoPackage cargoPackage = repository.save(createDefault("test cargo package"));
        
        var result = mockMvc.perform(
                                    get("/" + contractor.getId() + "/cargo/package/" + cargoPackage.getId() +"/")
                                            .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                            .andExpect(status().isOk())
                            .andReturn();
        
        CargoPackageDto cargoPackageDto =
                new ObjectMapper().readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                             CargoPackageDto.class);
    
        assertEquals(cargoPackage.getId(), cargoPackageDto.getId());
        assertEquals(cargoPackage.getContractor().getId(), cargoPackageDto.getContractor());
        assertEquals(cargoPackage.getLabel(), cargoPackageDto.getLabel());
        assertEquals(cargoPackage.getUnit(), cargoPackageDto.getUnit());
        assertEquals(cargoPackage.getCost(), cargoPackageDto.getCost());
    }
    
    @Test
    @DisplayName("Удаление")
    void deleteTest() throws Exception {
        CargoPackage cargoPackage = repository.save(createDefault("new cargo package"));
        
        mockMvc.perform(
                delete("/" + contractor.getId() + "/cargo/package/" + cargoPackage.getId()+"/")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
               .andExpect(status().isOk())
               .andReturn();
        
        assertEquals(Collections.emptyList(), repository.findAllByContractor(contractor.getId()));

        var messageCaptor = ArgumentCaptor.forClass(CargoPackageMessage.class);
        verify(cargoPackageOutput).send(messageCaptor.capture());
        var messages = messageCaptor.getAllValues();
        
        var deleteMessage = messages.get(0);
        assertTrue(deleteMessage.deleted());
        assertEquals(deleteMessage.getId(), cargoPackage.getId());
    }
    
    private CargoPackage createDefault(String label) {
        return CargoPackage.builder()
                           .label(label)
                           .unit("test unit")
                           .contractor(contractor)
                           .cost(new Random().nextDouble())
                           .build();
    }
    
    private NewCargoPackageDto createDefaultDto() {
        return NewCargoPackageDto.builder()
                           .label("new cargo package")
                           .unit("test unit")
                           .contractor(contractor.getId())
                           .cost(new Random().nextDouble())
                           .build();
    }
}
