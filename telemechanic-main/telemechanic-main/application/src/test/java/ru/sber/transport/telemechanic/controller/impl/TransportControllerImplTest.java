package ru.sber.transport.telemechanic.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.dto.PageSettingDto;
import ru.sber.transport.telemechanic.dto.transport.GetTransportRequest;
import ru.sber.transport.telemechanic.enumerate.Role;

import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_ADMIN_DATA_MASTER;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_DISPATCHER_ROOM_ADMIN;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres
class TransportControllerImplTest {
    @MockitoBean
    protected AuthorizationManager<?> manager;
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    
    @Test
    @SneakyThrows
    @Sql(value = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/transport.sql" })
    void getTransportByStateNumber() {
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_DATA_MASTER.name());
        mockMvc.perform(post("/transport/statenumber")
                                .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(new GetTransportRequest("777",
                                                                                                 new PageSettingDto(0, 10)))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content", hasSize(2)))
               .andExpect(jsonPath("$.content[*].stateNumber").value(everyItem(containsString("777"))));
    }
    
    @Test
    @SneakyThrows
    @Sql(value = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/transport.sql" })
    void getTransportByStateNumberSelfOrganization() {
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_DATA_MASTER.name());
        mockMvc.perform(post("/transport/statenumber/self-organization")
                                .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(new GetTransportRequest("777",
                                                                                                 new PageSettingDto(0, 10)))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content", hasSize(2)))
               .andExpect(jsonPath("$.content[*].stateNumber").value(everyItem(containsString("777"))));
    }
    
    @Test
    @SneakyThrows
    @Sql(value = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/transport.sql" })
    void getTransportByStateNumberAllOrganizations() {
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_DATA_MASTER.name());
        mockMvc.perform(post("/transport/statenumber/all-organizations")
                                .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(new GetTransportRequest("777",
                                                                                                 new PageSettingDto(0, 10)))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content", hasSize(3)))
               .andExpect(jsonPath("$.content[*].stateNumber").value(everyItem(containsString("777"))));
    }
    
    @ParameterizedTest
    @MethodSource("getTransportSource")
    @SneakyThrows
    @Sql(value = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/transport.sql" })
    void getTransportByFilters(String stateNumber, String organizationId, String departmentId) {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_ROOM_ADMIN.name());
        mockMvc.perform(get("/transport")
                                .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_ROOM_ADMIN.name())))
                                .param("stateNumber", stateNumber)
                                .param("organizationId", organizationId)
                                .param("departmentId", departmentId))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content", hasSize(1)))
               .andExpect(jsonPath("$.content[*].stateNumber").value(everyItem(containsString("А779АА779"))));
    }
    
    static Stream<Arguments> getTransportSource() {
        return Stream.of(
                Arguments.of(null, "cb9f17e7-f658-43ca-a70f-40c1c93ad0a6", "482e6dcb-03a9-4927-90b4-c7081114a9d8"),
                Arguments.of("А779АА779", null, "482e6dcb-03a9-4927-90b4-c7081114a9d8"),
                Arguments.of("А779АА779", "cb9f17e7-f658-43ca-a70f-40c1c93ad0a6", null),
                Arguments.of("А779АА779", "cb9f17e7-f658-43ca-a70f-40c1c93ad0a6", "482e6dcb-03a9-4927-90b4-c7081114a9d8"),
                Arguments.of(null, null, null)
                        );
    }
}