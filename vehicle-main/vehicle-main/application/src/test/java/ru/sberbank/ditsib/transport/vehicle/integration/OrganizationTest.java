package ru.sberbank.ditsib.transport.vehicle.integration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sberbank.ditsib.transport.vehicle.BaseIntegrationTest;
import ru.sberbank.ditsib.transport.vehicle.constants.Role;
import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;
import ru.sberbank.ditsib.transport.vehicle.dto.GetDepartmentsInfo;
import ru.sberbank.ditsib.transport.vehicle.dto.OrganizationDto;
import ru.sberbank.ditsib.transport.vehicle.mapper.OrganizationMapper;
import ru.sberbank.ditsib.transport.vehicle.mapper.OrganizationMapperImpl;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/scripts/basic_corp_structure.sql")
class OrganizationTest extends BaseIntegrationTest {
    
    private final OrganizationMapper organizationMapper = new OrganizationMapperImpl();
    
    @Test
    @SneakyThrows
    void getActiveOrganizations() {
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_CORP_CLIENT.name());
        var response = mockMvc.perform(
                                      get("/organization")
                                              .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                                         .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                                      )
                              .andExpect(status().isOk())
                              .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        var actualList = objectMapper.readValue(response, OrganizationDto[].class);
        var expectedList = organizationRepository.findAll().stream()
                                                 .filter(Organization::isActive)
                                                 .sorted(Comparator.comparing(Organization::getOfficialName))
                                                 .map(organizationMapper::organizationToOrganizationDto)
                                                 .toList();
        
        assertThat(Arrays.asList(actualList)).isEqualTo(expectedList);
    }
    
    @Test
    @SneakyThrows
    void getActiveOrganizationWithDepartments() {
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_CORP_CLIENT.name());
        var response = mockMvc.perform(
                                      post("/organization/department")
                                              .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                                         .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(Arrays.asList("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6",
                                                                                                     "621c288d-e348-46e5-a319-cbf61ef1e396")))
                                      )
                              .andExpect(status().isOk())
                              .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        var actualList = objectMapper.readValue(response, GetDepartmentsInfo[].class);
        
        assertThat(Arrays.asList(actualList)).hasSize(2);
    }
    
    @Test
    @SneakyThrows
    void getTest() {
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_CORP_CLIENT.name());
        var response = mockMvc.perform(get("/organization/employee")
                                               .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                                          .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name()))
                                                    )).andExpect(status().isOk())
                              .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        var actual = objectMapper.readValue(response, OrganizationDto.class);
        assertThat(actual).isNotNull()
                          .satisfies(organization -> {
                              assertThat(organization.id()).isEqualTo(UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"));
                              assertThat(organization.officialName()).isEqualTo("ЦА");
                          });
    }
}
