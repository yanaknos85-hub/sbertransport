package ru.sber.transport.telemechanic.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.DispatcherRepository;
import ru.sber.transport.telemechanic.database.model.Dispatcher;
import ru.sber.transport.telemechanic.dto.dispatcher.GetDispatcherResponse;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.authorization.test.AuthorizeUtils.authorize;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_ADMIN_CORP_CLIENT;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres
class DispatcherControllerImplTest {
    
    private static final String ROOT_PATH = "/dispatchers";
    
    @MockitoBean
    private AuthorizationManager<?> manager;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DispatcherRepository dispatcherRepository;
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/ewb_contract.sql",
            "/scripts/fleet_owner_organization.sql",
            "/scripts/ewb_tariff.sql"
    })
    void addDispatcher() {
        mockMvc.perform(
                       post(ROOT_PATH)
                               .with(jwt().jwt(builder -> builder.jti("15BDC75D-533B-44BB-9539-3DDC8693F3D3"))
                                          .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                               .contentType(MediaType.APPLICATION_JSON)
                               .content("""
                                        {
                                        	"employeeId": "15BDC75D-533B-44BB-9539-3DDC8693F3D3",
                                        	"organizationId": "621c288d-e348-46e5-a319-cbf61ef1e396",
                                        	"departmentId": "489A0090-1819-4C60-A611-572EA115C6A4",
                                        	"attorneyNumber": "95c2bd4f-3a79-4b30-8d7e-9308e751b40a",
                                        	"issueDate": "2020-01-01",
                                        	"expiryDate": "2035-01-01",
                                        	"creationSystem": "API"
                                        }
                                        """)
                       )
               .andExpect(status().isOk());
        
        var savedDispatcher = dispatcherRepository.findAll();
        assertThat(savedDispatcher).hasSize(1);
        var dispatcher = savedDispatcher.get(0);
        assertThat(dispatcher)
                .extracting(
                        el -> el.getEmployee().getId(),
                        el -> el.getOrganization().getId(),
                        el -> el.getDepartment().getId(),
                        el -> el.getAttorney().getNumber(),
                        el -> el.getAttorney().getIssueDate(),
                        el -> el.getAttorney().getExpiryDate(),
                        el -> el.getAttorney().getCreationSystem(),
                        Dispatcher::isActive
                           )
                .containsExactly(
                        UUID.fromString("15BDC75D-533B-44BB-9539-3DDC8693F3D3"),
                        UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396"),
                        UUID.fromString("489A0090-1819-4C60-A611-572EA115C6A4"),
                        UUID.fromString("95c2bd4f-3a79-4b30-8d7e-9308e751b40a"),
                        LocalDate.of(2020, 1, 1),
                        LocalDate.of(2035, 1, 1),
                        "API",
                        true
                                );
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/ewb_contract.sql",
            "/scripts/fleet_owner_organization.sql",
            "/scripts/ewb_tariff.sql",
            "/scripts/dispatcher.sql"
    })
    void editDispatcher() {
        mockMvc.perform(
                       patch(ROOT_PATH + "/983e4049-e4ac-4639-a782-5a266f4321a8")
                               .with(jwt().jwt(builder -> builder.jti("15BDC75D-533B-44BB-9539-3DDC8693F3D3"))
                                          .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                               .contentType(MediaType.APPLICATION_JSON)
                               .content("""
                                        {
                                        	"attorneyNumber": "95c2bd4f-3a79-4b30-8d7e-9308e751b40a",
                                        	"issueDate": "2000-01-01",
                                        	"expiryDate": "2199-01-01",
                                        	"creationSystem": "API2"
                                        }
                                        """)
                       )
               .andExpect(status().isOk());
        
        var editedDispatcher = dispatcherRepository.findById(UUID.fromString("983e4049-e4ac-4639-a782-5a266f4321a8"))
                                                   .orElseThrow(() -> new JUnitException("Dispatcher not found"));
        assertThat(editedDispatcher)
                .extracting(
                        Dispatcher::getId,
                        el -> el.getEmployee().getId(),
                        el -> el.getOrganization().getId(),
                        el -> el.getDepartment().getId(),
                        el -> el.getAttorney().getNumber(),
                        el -> el.getAttorney().getIssueDate(),
                        el -> el.getAttorney().getExpiryDate(),
                        el -> el.getAttorney().getCreationSystem(),
                        Dispatcher::isActive
                           )
                .containsExactly(
                        UUID.fromString("983e4049-e4ac-4639-a782-5a266f4321a8"),
                        UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"),
                        UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"),
                        UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8"),
                        UUID.fromString("95c2bd4f-3a79-4b30-8d7e-9308e751b40a"),
                        LocalDate.of(2000, 1, 1),
                        LocalDate.of(2199, 1, 1),
                        "API2",
                        true
                                );
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/ewb_contract.sql",
            "/scripts/fleet_owner_organization.sql",
            "/scripts/ewb_tariff.sql",
            "/scripts/dispatcher.sql"
    })
    void getDispatcher() {
        var response = mockMvc.perform(
                                      get(ROOT_PATH + "/983e4049-e4ac-4639-a782-5a266f4321a8")
                                              .with(jwt().jwt(builder -> builder.jti("15BDC75D-533B-44BB-9539-3DDC8693F3D3"))
                                                         .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                      )
                              .andExpect(status().isOk())
                              .andReturn()
                              .getResponse()
                              .getContentAsString(StandardCharsets.UTF_8);
        var actual = objectMapper.readValue(response, GetDispatcherResponse.class);
        
        var expected = dispatcherRepository.findById(UUID.fromString("983e4049-e4ac-4639-a782-5a266f4321a8"))
                                           .orElseThrow(() -> new JUnitException("Dispatcher not found"));
        assertThat(actual).isNotNull();
        assertThat(actual)
                .extracting(
                        GetDispatcherResponse::id,
                        el -> el.dispatcher().personnelNumber(),
                        el -> el.dispatcher().fullName(),
                        el -> el.dispatcher().organizationName(),
                        el -> el.dispatcher().departmentName(),
                        GetDispatcherResponse::attorneyNumber,
                        GetDispatcherResponse::issueDate,
                        GetDispatcherResponse::expiryDate,
                        GetDispatcherResponse::creationSystem
                           )
                .containsExactly(
                        expected.getId(),
                        expected.getEmployee().getPersonnelNumber(),
                        expected.getEmployee().getFIO(),
                        expected.getOrganization().getOfficialName(),
                        expected.getDepartment().getDepartmentName(),
                        expected.getAttorney().getNumber(),
                        expected.getAttorney().getIssueDate(),
                        expected.getAttorney().getExpiryDate(),
                        expected.getAttorney().getCreationSystem()
                                );
    }
    
    @Test
    @Transactional
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/ewb_contract.sql",
            "/scripts/fleet_owner_organization.sql",
            "/scripts/ewb_tariff.sql",
            "/scripts/dispatcher.sql"
    })
    void deactivateDispatcher() {
        var preDeactivateDispatcher = dispatcherRepository.findById(UUID.fromString("983e4049-e4ac-4639-a782-5a266f4321a8"))
                                                          .orElseThrow(() -> new JUnitException("Dispatcher not found"));
        assertThat(preDeactivateDispatcher.isActive()).isTrue();
        mockMvc.perform(
                       patch(ROOT_PATH + "/983e4049-e4ac-4639-a782-5a266f4321a8/deactivate")
                               .with(jwt().jwt(builder -> builder.jti("15BDC75D-533B-44BB-9539-3DDC8693F3D3"))
                                          .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                       )
               .andExpect(status().isOk());
        
        var postDeactivateDispatcher = dispatcherRepository.findById(UUID.fromString("983e4049-e4ac-4639-a782-5a266f4321a8"))
                                                           .orElseThrow(() -> new JUnitException("Dispatcher not found"));
        assertThat(postDeactivateDispatcher.isActive()).isFalse();
        assertThat(postDeactivateDispatcher)
                .usingRecursiveComparison()
                .ignoringFields("active")
                .isEqualTo(preDeactivateDispatcher);
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/ewb_contract.sql",
            "/scripts/fleet_owner_organization.sql",
            "/scripts/ewb_tariff.sql",
            "/scripts/dispatcher.sql"
    })
    void search() {
        var response = mockMvc.perform(
                                      post(ROOT_PATH + "/search")
                                              .with(jwt().jwt(builder -> builder.jti("15BDC75D-533B-44BB-9539-3DDC8693F3D3"))
                                                         .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content("""
                                                       {}
                                                       """)
                                      )
                              .andExpect(status().isOk())
                              .andReturn()
                              .getResponse()
                              .getContentAsString(StandardCharsets.UTF_8);
        var actual = objectMapper.readValue(response, new TypeReference<Page<GetDispatcherResponse>>() {});
        assertThat(actual).isNotNull();
        assertThat(actual.getContent()).hasSize(3);
        assertThat(actual.getTotalElements()).isEqualTo(3);
        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getContent())
                .isSortedAccordingTo(Comparator.comparing(el -> el.dispatcher().organizationName()));
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/ewb_contract.sql",
            "/scripts/fleet_owner_organization.sql",
            "/scripts/ewb_tariff.sql",
            "/scripts/dispatcher.sql",
            "/scripts/region.sql",
            "/scripts/organization_address.sql"
    })
    void getSelfOrganizationInfo() {
        mockMvc.perform(
                       get(ROOT_PATH + "/self")
                               .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                          .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                               .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk());
    }
}
