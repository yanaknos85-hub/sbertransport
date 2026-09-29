package ru.sber.transport.telemechanic.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.dto.DepartmentDto;
import ru.sber.transport.telemechanic.dto.OrganizationDto;
import ru.sber.transport.telemechanic.dto.OrganizationWithDepartmentDto;
import ru.sber.transport.telemechanic.service.OrganizationService;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_DISPATCHER_SUPPORT_SERVICE;

@DisplayName("Проверка контроллера организаций")
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
class OrganizationControllerImplTest {
    public static final String CONTROLLER_URL = "/organization";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AuthorizationManager<?> manager;
    @MockitoBean
    private OrganizationService organizationService;
    
    @Test
    void getTest() throws Exception {
        var expected = new OrganizationDto(UUID.randomUUID(), "officialName");
        var employeeId = UUID.randomUUID();
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        when(organizationService.getByUserId(employeeId)).thenReturn(expected);
        mockMvc.perform(get(CONTROLLER_URL + "/employee")
                                .with(jwt().jwt(builder -> builder.jti(employeeId.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name()))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(expected.id().toString()))
               .andExpect(jsonPath("$.officialName").value(expected.officialName()));
    }
    
    @Test
    void getAll() throws Exception {
        var employeeId = UUID.randomUUID();
        var expected1 = new OrganizationDto(UUID.randomUUID(), "officialName1");
        var expected2 = new OrganizationDto(UUID.randomUUID(), "officialName2");
        var expected = List.of(expected1, expected2);
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        when(organizationService.getAll()).thenReturn(expected);
        mockMvc.perform(get(CONTROLLER_URL)
                                .with(jwt().jwt(builder -> builder.jti(employeeId.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name()))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.[0].id").value(expected1.id().toString()))
               .andExpect(jsonPath("$.[0].officialName").value(expected1.officialName()))
               .andExpect(jsonPath("$.[1].id").value(expected2.id().toString()))
               .andExpect(jsonPath("$.[1].officialName").value(expected2.officialName()));
    }
    
    @Test
    void getAllWithInternalContracor() throws Exception {
        var employeeId = UUID.randomUUID();
        var expected1 = new OrganizationDto(UUID.randomUUID(), "officialName1");
        var expected2 = new OrganizationDto(UUID.randomUUID(), "officialName2");
        var expected = List.of(expected1, expected2);
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        when(organizationService.getAllWithInternalContractor()).thenReturn(expected);
        mockMvc.perform(get(CONTROLLER_URL + "/internal-autopark")
                                .param("internalContractor", "true")
                                .with(jwt().jwt(builder -> builder.jti(employeeId.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name()))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.[0].id").value(expected1.id().toString()))
               .andExpect(jsonPath("$.[0].officialName").value(expected1.officialName()))
               .andExpect(jsonPath("$.[1].id").value(expected2.id().toString()))
               .andExpect(jsonPath("$.[1].officialName").value(expected2.officialName()));
    }
    
    @Test
    void getAllWithDepartment() throws Exception {
        var departmentDto1 = new DepartmentDto(UUID.randomUUID(), "departmentName1", null);
        var departmentDto2 = new DepartmentDto(UUID.randomUUID(), "departmentName2", departmentDto1.id());
        var departmentDto3 = new DepartmentDto(UUID.randomUUID(), "departmentName3", departmentDto1.id());
        var expected1 = new OrganizationWithDepartmentDto("officialName1", List.of(departmentDto1, departmentDto2, departmentDto3));
        var expected2 = new OrganizationWithDepartmentDto("officialName2", Collections.emptyList());
        var organizationId1 = UUID.randomUUID();
        var organizationId2 = UUID.randomUUID();
        var organizationIds = Set.of(organizationId1, organizationId2);
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        when(organizationService.getAllWithDepartment(organizationIds)).thenReturn(List.of(expected1, expected2));
        mockMvc.perform(post(CONTROLLER_URL + "/department")
                                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                                .content(objectMapper.writeValueAsString(organizationIds))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.[0].officialName").value(expected1.officialName()))
               .andExpect(jsonPath("$.[0].departmentDtoList.[0].id").value(expected1
                                                                                   .departmentDtoList().get(0).id().toString()))
               .andExpect(jsonPath("$.[0].departmentDtoList.[0].departmentName").value(expected1
                                                                                               .departmentDtoList().get(0).departmentName()))
               .andExpect(jsonPath("$.[0].departmentDtoList.[0].parentId").value(expected1
                                                                                         .departmentDtoList().get(0).parentId()))
               .andExpect(jsonPath("$.[0].departmentDtoList.[1].id").value(expected1
                                                                                   .departmentDtoList().get(1).id().toString()))
               .andExpect(jsonPath("$.[0].departmentDtoList.[1].departmentName").value(expected1
                                                                                               .departmentDtoList().get(1).departmentName()))
               .andExpect(jsonPath("$.[0].departmentDtoList.[1].parentId").value(expected1
                                                                                         .departmentDtoList().get(1).parentId().toString()))
               .andExpect(jsonPath("$.[0].departmentDtoList.[2].id").value(expected1
                                                                                   .departmentDtoList().get(2).id().toString()))
               .andExpect(jsonPath("$.[0].departmentDtoList.[2].departmentName").value(expected1
                                                                                               .departmentDtoList().get(2).departmentName()))
               .andExpect(jsonPath("$.[0].departmentDtoList.[2].parentId").value(expected1
                                                                                         .departmentDtoList().get(2).parentId().toString()))
               .andExpect(jsonPath("$.[1].officialName").value(expected2.officialName()))
               .andExpect(jsonPath("$.[1].departmentDtoList").isEmpty());
    }
}
