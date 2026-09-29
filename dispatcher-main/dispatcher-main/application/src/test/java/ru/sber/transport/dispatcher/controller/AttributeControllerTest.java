package ru.sber.transport.dispatcher.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.assertj.core.api.Assertions;
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
import ru.sber.transport.dispatcher.database.dao.AttributeRepository;
import ru.sber.transport.dispatcher.database.dao.ContractorRepository;
import ru.sber.transport.dispatcher.database.model.ActiveStatus;
import ru.sber.transport.dispatcher.database.model.Attribute;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.dto.AttributeDTO;
import ru.sber.transport.dispatcher.dto.NewAttributeDTO;
import ru.sber.transport.dispatcher.testutils.TestContractors;
import ru.sber.transport.dispatcher.testutils.TestDriverTags;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@Transactional
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера признаков водителей")
@MockBean(Key.class)
class AttributeControllerTest extends KafkaTest {

    @Autowired
    private ContractorRepository contractorRepository;
    @Autowired
    private AttributeRepository attributeRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthorizationManager<?> manager;

    @BeforeEach
    void setup() {
    }

    @Test
    @DisplayName("Добавление тэга")
    void addDriverAttribute() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        var driverTagDto = TestDriverTags.createTestDriverTagDto();
        var response = mockMvc.perform(post("/" + contractor.getId() + "/attribute/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(objectMapper.writeValueAsString(List.of(driverTagDto))))
                .andExpect(status().isOk()).andReturn();

        var actual = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<List<AttributeDTO>>() {
        }).get(0);

        assertThat(attributeRepository.count()).isEqualTo(1);

        var actualDb = attributeRepository.findAll().get(0);

        assertThat(actual.getName()).isEqualTo(driverTagDto.getName());
        assertThat(actual.getId()).isEqualTo(actualDb.getId());
        Assertions.assertThat(actualDb.getName()).isEqualTo(actual.getName());
        Assertions.assertThat(actualDb.getContractor().getId()).isEqualTo(actual.getContractor().id());
        Assertions.assertThat(actualDb.getStatus()).isEqualTo(ActiveStatus.ACTIVE);
    }

    @Test
    @DisplayName("Добавление дубликата тэга")
    void addDuplicate() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        var driverTag = attributeRepository.save(TestDriverTags.createTestDriverTag(contractor));
        var driverTagDto = TestDriverTags.createTestDriverTagDto();
        attributeRepository.flush();
        var result = mockMvc.perform(post("/%s/attribute/".formatted(contractor.getId())).contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(objectMapper.writeValueAsString(List.of(driverTagDto))))
                .andExpect(status().isConflict());
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Добавление тэга, его удаление и повторное добавление такого же")
    void addTagAfterDelete() throws Exception {
        //добавление признака
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        var driverTagDto = TestDriverTags.createTestDriverTagDto();
        var response = mockMvc.perform(post("/" + contractor.getId() + "/attribute/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(objectMapper.writeValueAsString(List.of(driverTagDto))))
                .andExpect(status().isOk()).andReturn();

        var actual = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<List<AttributeDTO>>() {
        }).get(0);

        assertThat(attributeRepository.count()).isEqualTo(1);
        Assertions.assertThat(attributeRepository.findAll().get(0).getStatus()).isEqualTo(ActiveStatus.ACTIVE);

        var actualDb = attributeRepository.findAll().get(0);

        assertThat(actual.getName()).isEqualTo(driverTagDto.getName());
        assertThat(actual.getId()).isEqualTo(actualDb.getId());
        Assertions.assertThat(actualDb.getName()).isEqualTo(actual.getName());
        Assertions.assertThat(actualDb.getContractor().getId()).isEqualTo(actual.getContractor().id());

        //удаление признака
        mockMvc.perform(delete("/" + contractor.getId() + "/attribute/" + actual.getId() + "/")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).andExpect(status().isOk());
        mockMvc.perform(get("/" + contractor.getId() + "/attribute/" + actual.getId() + "/")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).andExpect(status().isNotFound());
        assertThat(attributeRepository.count()).isEqualTo(1);
        Assertions.assertThat(attributeRepository.findAll().get(0).getStatus()).isEqualTo(ActiveStatus.INACTIVE);

        //добавление признака с таким же названием, но который уже есть в системе, только неактивен
        mockMvc.perform(post("/" + contractor.getId() + "/attribute/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(objectMapper.writeValueAsString(List.of(driverTagDto))))
                .andExpect(status().isOk());

        assertThat(attributeRepository.count()).isEqualTo(1);
        Assertions.assertThat(attributeRepository.findAll().get(0).getStatus()).isEqualTo(ActiveStatus.ACTIVE);

        //повторное добавление существующего признака
        mockMvc.perform(post("/" + contractor.getId() + "/attribute/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(objectMapper.writeValueAsString(List.of(driverTagDto))))
                .andExpect(status().isConflict());

        assertThat(attributeRepository.count()).isEqualTo(1);
        Assertions.assertThat(attributeRepository.findAll().get(0).getStatus()).isEqualTo(ActiveStatus.ACTIVE);
    }

    @Test
    @DisplayName("Добавление списка")
    void addMultiple() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        var driverTags = new ArrayList<NewAttributeDTO>();
        driverTags.add(TestDriverTags.createTestDriverTagDto());
        for (int i = 1; i < 100; i++) {
            driverTags.add(TestDriverTags.incrementDriverTagDto(driverTags.get(0), i));
        }
        var response = mockMvc.perform(post("/" + contractor.getId() + "/attribute/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(objectMapper.writeValueAsString(driverTags)))
                .andExpect(status().isOk()).andReturn();
        Assertions.assertThat(attributeRepository.findAll()).hasSize(100);
    }

    @Test
    @DisplayName("Добавление списка с дубликатом")
    void addMultipleWithDuplicate() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        var driverTags = new ArrayList<NewAttributeDTO>();
        driverTags.add(TestDriverTags.createTestDriverTagDto());
        for (int i = 1; i < 99; i++) {
            driverTags.add(TestDriverTags.incrementDriverTagDto(driverTags.get(0), i));
        }
        driverTags.add(TestDriverTags.createTestDriverTagDto());
        contractorRepository.flush();
        var response = mockMvc.perform(post("/%s/attribute/".formatted(contractor.getId())).contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(objectMapper.writeValueAsString(driverTags)))
                .andExpect(status().isConflict()).andReturn();
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Получение одного")
    void getOne() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        var driverTag = attributeRepository.save(TestDriverTags.createTestDriverTag(contractor));
        var response = mockMvc.perform(get("/" + contractor.getId() + "/attribute/" + driverTag.getId() + "/")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).
                andExpect(status().isOk()).andReturn();
        var driverTagDto = objectMapper.readValue(response.getResponse().getContentAsString(), AttributeDTO.class);
        assertThat(driverTagDto.getName()).isEqualTo(driverTag.getName());

    }

    @Test
    @DisplayName("Получение списка")
    void getList() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        var driverTags = new ArrayList<Attribute>();
        driverTags.add(TestDriverTags.createTestDriverTag(contractor));
        for (int i = 1; i < 100; i++) {
            driverTags.add(TestDriverTags.incrementDriverTag(driverTags.get(0), i));
        }
        attributeRepository.saveAll(driverTags);
        var response = mockMvc.perform(get("/" + contractor.getId() + "/attribute/")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).andExpect(status().isOk())
                .andReturn();
        var driverTagsDto = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<List<AttributeDTO>>() {
        });
        assertThat(driverTagsDto).hasSize(100);

    }

    @Test
    @DisplayName("Редактирование")
    void editAttribute() throws Exception {
        //создаем признак
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        var driverTagDto = TestDriverTags.createTestDriverTagDto();
        var response = mockMvc.perform(post("/" + contractor.getId() + "/attribute/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(objectMapper.writeValueAsString(List.of(driverTagDto))))
                .andExpect(status().isOk()).andReturn();

        var actual = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<List<AttributeDTO>>() {
        }).get(0);

        assertThat(attributeRepository.count()).isEqualTo(1);

        var actualDb = attributeRepository.findAll().get(0);

        assertThat(actual.getName()).isEqualTo(driverTagDto.getName());
        assertThat(actual.getId()).isEqualTo(actualDb.getId());
        Assertions.assertThat(actualDb.getName()).isEqualTo(actual.getName());
        Assertions.assertThat(actualDb.getContractor().getId()).isEqualTo(actual.getContractor().id());

        //редактируем тэг
        var newDriverTagDto = NewAttributeDTO.builder().name("newAttributeDTO").build();
        mockMvc.perform(put("/" + contractor.getId() + "/attribute/" + actual.getId() + "/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .content(objectMapper.writeValueAsString(newDriverTagDto)))
                .andExpect(status().isOk());

        //проверяем что он изменился
        response = mockMvc.perform(get("/" + contractor.getId() + "/attribute/" + actual.getId() + "/")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk()).andReturn();
        actual = objectMapper.readValue(response.getResponse().getContentAsString(), AttributeDTO.class);
        actualDb = attributeRepository.findAll().get(0);
        assertThat(actual.getId()).isEqualTo(actualDb.getId());
        Assertions.assertThat(actualDb.getName()).isEqualTo("newAttributeDTO");
        Assertions.assertThat(actualDb.getContractor().getId()).isEqualTo(actual.getContractor().id());
    }

    @Test
    @DisplayName("Удаление")
    void deleteAttribute() throws Exception {
        Contractor contractor = contractorRepository.save(TestContractors.createTestContractor());
        Attribute driverTag = attributeRepository.save(TestDriverTags.createTestDriverTag(contractor));

        var result = mockMvc.perform(delete("/%s/attribute/%s/".formatted(contractor.getId(), driverTag.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).
                andExpect(status().isOk());
        assertThat(result).isNotNull();

        result = mockMvc.perform(get("/" + contractor.getId() + "/attribute/" + driverTag.getId())
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).
                andExpect(status().isNotFound());
        assertThat(result).isNotNull();
    }

}
