package ru.sberbank.ditsib.transport.role.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.jooq.JSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.roles.database.roles.tables.records.RoleRecord;
import ru.sber.transport.roles.messages.RoleMessage;
import ru.sberbank.ditsib.transport.role.dao.RoleRepository;
import ru.sberbank.ditsib.transport.role.dto.ExeclusiveUsing;
import ru.sberbank.ditsib.transport.role.dto.RoleDto;
import ru.sberbank.ditsib.transport.role.dto.Scope;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_roles")
@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера ролей")
@Transactional
@ActiveProfiles("test")
class RoleControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @MockitoBean
    private AuthorizationManager<?> authorizationManager;

    @MockitoBean(name = "rolesOutput")
    private OutputBridge rolesOutput;

    @MockitoBean(name = "rolesOutputSsl")
    private OutputBridge rolesOutputSsl;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(authorizationManager);
    }

    @Test
    @DisplayName("Добавление роли")
    @WithMockUser(roles = "GUEST")
    void addRole() throws Exception {
        var newRole = RoleDto.builder()
                .code("ROLE_NAME")
                .description("A long long long description")
                .name("Short name").build();

        var response = mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(objectMapper.writeValueAsString(newRole)))
                .andExpect(status().isOk()).andReturn();

        assertThat(roleRepository.count()).isEqualTo(1);

        var actualDto = objectMapper.readValue(response.getResponse().getContentAsString(), RoleDto.class);
        var actualDb = roleRepository.findAll(PageRequest.of(0, 1)).iterator().next();

        var messageCaptor = ArgumentCaptor.forClass(RoleMessage.class);
        verify(rolesOutput).send(messageCaptor.capture());

        var actualMessage = messageCaptor.getValue();

        assertThat(actualDto.getCode()).isEqualTo(newRole.getCode());
        assertThat(actualDto.getDescription()).isEqualTo(newRole.getDescription());
        assertThat(actualDto.getName()).isEqualTo(newRole.getName());
        assertThat(actualDto.getExclusive()).hasSameElementsAs(List.of(ExeclusiveUsing.INTERNAL, ExeclusiveUsing.EXTERNAL));

        assertThat(actualDb.getCode()).isEqualTo(newRole.getCode());
        assertThat(actualDb.getDescription()).isEqualTo(newRole.getDescription());
        assertThat(actualDb.getName()).isEqualTo(newRole.getName());

        assertThat(actualMessage.code()).isEqualTo(newRole.getCode());
        assertThat(actualMessage.description()).isEqualTo(newRole.getDescription());
        assertThat(actualMessage.name()).isEqualTo(newRole.getName());
        assertThat(actualMessage.deleted()).isFalse();
    }

    @Test
    @DisplayName("Добавление роли по умолчанию")
    @WithMockUser(roles = "GUEST")
    void addRole_default() throws Exception {
        var newRole = RoleDto.builder()
                .code("ROLE_NAME_DEFAULT")
                .description("A long long long description of the default role")
                .name("Default role")
                .build();

        var response = mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(objectMapper.writeValueAsString(newRole)))
                .andExpect(status().isOk()).andReturn();

        assertThat(roleRepository.count()).isEqualTo(1);

        var actualDto = objectMapper.readValue(response.getResponse().getContentAsString(), RoleDto.class);
        var actualDb = roleRepository.findAll(PageRequest.of(0, 1)).iterator().next();

        var messageCaptor = ArgumentCaptor.forClass(RoleMessage.class);
        verify(rolesOutputSsl).send(messageCaptor.capture());

        var actualMessage = messageCaptor.getValue();

        assertThat(actualDto.getDescription()).isEqualTo(newRole.getDescription());
        assertThat(actualDto.getName()).isEqualTo(newRole.getName());

        assertThat(actualDb.getCode()).isEqualTo(newRole.getCode());
        assertThat(actualDb.getDescription()).isEqualTo(newRole.getDescription());
        assertThat(actualDb.getName()).isEqualTo(newRole.getName());

        assertThat(actualMessage.code()).isEqualTo(newRole.getCode());
        assertThat(actualMessage.description()).isEqualTo(newRole.getDescription());
        assertThat(actualMessage.name()).isEqualTo(newRole.getName());
        assertThat(actualMessage.deleted()).isFalse();
    }

    @Test
    @DisplayName("Добавление роли. Код начинается не с ROLE")
    @WithMockUser(roles = "GUEST")
    void addRole_codeNotStartsWithRole() throws Exception {
        var newRole = RoleDto.builder()
                .code("NAME")
                .description("A long long long description")
                .name("Short name").build();

        mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(objectMapper.writeValueAsString(newRole)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].field").value("code"))
                .andExpect(jsonPath("$.problems[0].value").value("NAME"))
                .andExpect(jsonPath("$.problems[0].constraints.length()").value(1))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("Pattern"))
                .andExpect(jsonPath("$.problems[0].constraints[0].value.pattern").value("(ROLE|role)_[A-Za-z_]+"))
        ;
    }

    @Test
    @DisplayName("Добавление роли. Код маленькими буквами")
    @WithMockUser(roles = "GUEST")
    void addRole_codeWithLittleSymbols() throws Exception {
        var newRole = RoleDto.builder()
                .code("role_name")
                .description("A long long long description")
                .name("Short name").build();

        var response = mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(objectMapper.writeValueAsString(newRole)))
                .andExpect(status().isOk()).andReturn();

        assertThat(roleRepository.count()).isEqualTo(1);

        var actualDto = objectMapper.readValue(response.getResponse().getContentAsString(), RoleDto.class);
        var actualDb = roleRepository.findAll(PageRequest.of(0, 1)).iterator().next();

        assertThat(actualDto.getCode()).isEqualTo(newRole.getCode().toUpperCase(Locale.ROOT));
        assertThat(actualDto.getDescription()).isEqualTo(newRole.getDescription());
        assertThat(actualDto.getName()).isEqualTo(newRole.getName());
        assertThat(actualDb.getCode()).isEqualTo(newRole.getCode().toUpperCase(Locale.ROOT));
        assertThat(actualDb.getDescription()).isEqualTo(newRole.getDescription());
        assertThat(actualDb.getName()).isEqualTo(newRole.getName());
    }

    @Test
    @DisplayName("Добавление роли. Код некорректный")
    @WithMockUser(roles = "GUEST")
    void addRole_incorrectCode() throws Exception {
        var newRole = RoleDto.builder()
                .code("имя роли")
                .description("A long long long description")
                .name("Short name").build();

        mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(objectMapper.writeValueAsString(newRole)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Добавление роли. Дубль кода")
    @WithMockUser(roles = "GUEST")
    void addRole_codeDuplicate() throws Exception {
        var role = new RoleRecord();
        role.setCode("ROLE_NAME");
        role.setCode("A long description");
        role.setName("Short name");

        roleRepository.save(role);

        var newRole = RoleDto.builder()
                .code("role_name")
                .description("A long long long description")
                .name("Short name").build();

        mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(objectMapper.writeValueAsString(newRole)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Изменение роли")
    @WithMockUser(roles = "GUEST")
    void editRole() throws Exception {
        var role = new RoleRecord();
        role.setCode("ROLE_NAME");
        role.setDescription("A long description");
        role.setName("Short name");

        roleRepository.save(role);

        var newRole = RoleDto.builder()
                .code("ROLE_NAME")
                .description("A long long long description")
                .name("A little bit longer name").build();

        mockMvc.perform(put("/role_name").contentType(MediaType.APPLICATION_JSON)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(objectMapper.writeValueAsString(newRole)))
                .andExpect(status().isOk());

        assertThat(roleRepository.count()).isEqualTo(1);

        var actualDb = roleRepository.findAll(PageRequest.of(0, 1)).iterator().next();
        var messageCaptor = ArgumentCaptor.forClass(RoleMessage.class);
        verify(rolesOutput).send(messageCaptor.capture());

        var actualMessage = messageCaptor.getValue();

        assertThat(actualDb.getCode()).isEqualTo(newRole.getCode());
        assertThat(actualDb.getDescription()).isEqualTo(newRole.getDescription());
        assertThat(actualDb.getName()).isEqualTo(newRole.getName());

        assertThat(actualMessage.code()).isEqualTo(newRole.getCode());
        assertThat(actualMessage.description()).isEqualTo(newRole.getDescription());
        assertThat(actualMessage.name()).isEqualTo(newRole.getName());
        assertThat(actualMessage.deleted()).isFalse();
    }

    @Test
    @DisplayName("Изменение несуществующей роли")
    @WithMockUser(roles = "GUEST")
    void editRole_unExists() throws Exception {
        var newRole = RoleDto.builder()
                .code("ROLE_EDITED_NAME")
                .description("A long long long description")
                .name("A little bit longer name").build();

        mockMvc.perform(put("/ROLE_NAME").contentType(MediaType.APPLICATION_JSON)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(objectMapper.writeValueAsString(newRole)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Удаление роли")
    @WithMockUser(roles = "GUEST")
    void delete_role() throws Exception {
        var role = new RoleRecord();
        role.setCode("ROLE_NAME");
        role.setDescription("A long description");
        role.setName("Short name");

        roleRepository.save(role);

        assertThat(roleRepository.count()).isEqualTo(1);

        mockMvc.perform(delete("/ROLE_NAME")
            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isOk());

        assertThat(roleRepository.count()).isZero();

        var messageCaptor = ArgumentCaptor.forClass(RoleMessage.class);
        verify(rolesOutputSsl).send(messageCaptor.capture());

        var actualMessage = messageCaptor.getValue();

        assertThat(actualMessage.code()).isEqualTo(role.getCode());
        assertThat(actualMessage.deleted()).isTrue();
    }

    @Test
    @DisplayName("Удаление несуществующей роли")
    @WithMockUser(roles = "GUEST")
    void delete_unExist_role() throws Exception {
        mockMvc.perform(delete("/ROLE_NAME")
            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение роли")
    @WithMockUser(roles = "GUEST")
    void getRole() throws Exception {
        var role = new RoleRecord();
        role.setCode("ROLE_NAME");
        role.setDescription("A long description");
        role.setName("Short name");

        roleRepository.save(role);

        var response = mockMvc.perform(get("/ROLE_NAME")
            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isOk()).andReturn();

        var actual = objectMapper.readValue(response.getResponse().getContentAsString(), RoleDto.class);
        assertThat(actual.getCode()).isEqualTo(role.getCode());
        assertThat(actual.getDescription()).isEqualTo(role.getDescription());
        assertThat(actual.getName()).isEqualTo(role.getName());
    }

    @Test
    @DisplayName("Получение роли без ROLE")
    @WithMockUser(roles = "GUEST")
    void getRole_noRole() throws Exception {
        var role = new RoleRecord();
        role.setCode("NAME");
        role.setDescription("A long description");
        role.setName("Short name");

        roleRepository.save(role);

        mockMvc.perform(get("/name")
            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение несуществующей роли")
    @WithMockUser(roles = "GUEST")
    void getRole_unExists() throws Exception {
        mockMvc.perform(get("/ROLE_NAME")
            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение ролей")
    @WithMockUser(roles = "GUEST")
    void getRoles() throws Exception {
        var rolesCount = 100;
        for (var i = 0; i < rolesCount; i++) {
            var role = new RoleRecord();
            role.setCode("" + i);
            role.setName("" + i);
            role.setDescription("" + i);
            role.setDefaultFor(JSON.json("[\"%s\"]".formatted(Scope.values()[i % Scope.values().length])));

            roleRepository.save(role);
        }

        var response = mockMvc.perform(get("/").header("X-Paged", "true")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20))
                .andExpect(jsonPath("$.totalPages").value(rolesCount / 20))
                .andExpect(jsonPath("$.totalElements").value(rolesCount))
                ;

        for (var i = 0; i < 20; i++) {
            var expected = roleRepository.findAll(PageRequest.of(i, 1, Sort.by("name"))).iterator().next();
            response
                    .andExpect(jsonPath("$.content.[%s].code".formatted(i)).value(expected.getCode()))
                    .andExpect(jsonPath("$.content.[%s].description".formatted(i)).value(expected.getDescription()))
                    .andExpect(jsonPath("$.content.[%s].name".formatted(i)).value(expected.getName()))
                    .andExpect(jsonPath("$.content.[%s].scopes.length()".formatted(i)).value(1))
                    .andExpect(jsonPath("$.content.[%s].scopes.[0]".formatted(i)).value(expected.getDefaultFor().data().replace("[\"", "").replace("\"]", "")))
                    ;
        }
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Получение ролей. Нет пагинации")
    @WithMockUser(roles = "GUEST")
    void getRoles_noPage() throws Exception {
        var rolesCount = 100;
        for (var i = 0; i < rolesCount; i++) {
            var role = new RoleRecord();
            role.setCode("" + i);
            role.setName("" + i);
            role.setDescription("" + i);
            role.setDefaultFor(JSON.json("[\"%s\"]".formatted(Scope.values()[i % Scope.values().length])));

            roleRepository.save(role);
        }

        var response = mockMvc.perform(get("/")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(rolesCount))
                ;
        var expectedList = roleRepository.findAll().stream().sorted(Comparator.comparing(RoleRecord::getName)).toList();
        for (var i = 0; i < 20; i++) {
            var expected = expectedList.get(i);
            response
                    .andExpect(jsonPath("$.[%s].code".formatted(i)).value(expected.getCode()))
                    .andExpect(jsonPath("$.[%s].description".formatted(i)).value(expected.getDescription()))
                    .andExpect(jsonPath("$.[%s].name".formatted(i)).value(expected.getName()))
                    .andExpect(jsonPath("$.[%s].scopes.length()".formatted(i)).value(1))
                    .andExpect(jsonPath("$.[%s].scopes.[0]".formatted(i)).value(expected.getDefaultFor().data().replace("[\"", "").replace("\"]", "")))
                    ;
        }
        assertThat(response).isNotNull();
    }
}