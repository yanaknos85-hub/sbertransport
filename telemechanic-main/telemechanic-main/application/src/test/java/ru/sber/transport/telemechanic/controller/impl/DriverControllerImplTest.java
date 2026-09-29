package ru.sber.transport.telemechanic.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.JUnitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.DriverRepository;
import ru.sber.transport.telemechanic.dto.PageSettingDto;
import ru.sber.transport.telemechanic.dto.driver.DriverByFioRequest;
import ru.sber.transport.telemechanic.dto.driver.DriverSearchRequest;
import ru.sber.transport.telemechanic.dto.driver.GetDriverResponse;
import ru.sber.transport.telemechanic.mapper.DriverMapper;
import ru.sber.transport.telemechanic.mapper.DrivingLicenseMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.telemechanic.dto.driver.DriverSortOption.ORGANIZATION_NAME;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_ADMIN_CORP_CLIENT;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_DISPATCHER_ROOM_ADMIN;

@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
class DriverControllerImplTest {
    
    private static final String ROOT_PATH = "/drivers";
    
    @MockitoBean
    private AuthorizationManager<?> manager;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Autowired
    private DrivingLicenseMapper drivingLicenseMapper;
    
    @Autowired
    private DriverMapper driverMapper;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/fleet_owner_organization.sql",
            "/scripts/ewb_contract.sql",
            "/scripts/ewb_tariff.sql",
            "/scripts/driver.sql"
    })
    void addDriver() {
        mockMvc.perform(
                       post(ROOT_PATH)
                               .with(jwt().jwt(builder -> builder.jti("15BDC75D-533B-44BB-9539-3DDC8693F3D3"))
                                          .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                               .contentType(MediaType.APPLICATION_JSON_VALUE)
                               .content("""
                                        {
                                        	"driver": {
                                        		"employeeId": "15BDC75D-533B-44BB-9539-3DDC8693F3D3",
                                        		"departmentId": "489A0090-1819-4C60-A611-572EA115C6A4",
                                        		"tin": "012345678912",
                                        		"snils": "112-233-445 95"
                                        	},
                                        	"drivingLicense": {
                                        		"series": "011S02",
                                        		"number": "00442534",
                                        		"issueDate": "2025-01-01",
                                        		"expiryDate": "2035-01-01",
                                        		"categoryIds": [
                                        			"be8abe82-1cbd-4f0e-9b31-2b9db90231df",
                                        			"66872805-6447-4ae9-8336-c81afb24bf06"
                                        		]
                                        	}
                                        }
                                        """)
                       )
               .andExpect(status().isOk());
        
        var driverByEmployee = driverRepository.findWithLicenseByEmployeeId(UUID.fromString("15BDC75D-533B-44BB-9539-3DDC8693F3D3"))
                                               .orElseThrow(() -> new JUnitException("Driver not found"));
        var driver = driverRepository.findById(driverByEmployee.getId())
                                     .orElseThrow(() -> new JUnitException("Driver not found"));
        assertThat(driver.getTin()).isEqualTo("0000001");
        assertThat(driver.getSnils()).isEqualTo("186-345-573 03");
        assertThat(driver.getDrivingLicense().getSeries()).isEqualTo("011S02");
        assertThat(driver.getDrivingLicense().getNumber()).isEqualTo("00442534");
        assertThat(driver.getDrivingLicense().getIssueDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(driver.getDrivingLicense().getExpiryDate()).isEqualTo(LocalDate.of(2035, 1, 1));
        assertThat(driver.getDrivingLicense().getCategories()).hasSize(2);
        assertThat(driver.getDrivingLicense().getCategories()).extracting("id")
                                                              .containsExactlyInAnyOrder(UUID.fromString("be8abe82-1cbd-4f0e-9b31-2b9db90231df"),
                                                                                         UUID.fromString("66872805-6447-4ae9-8336-c81afb24bf06"));
        
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/fleet_owner_organization.sql",
            "/scripts/ewb_contract.sql",
            "/scripts/ewb_tariff.sql",
            "/scripts/driver.sql"
    })
    void editDriver() {
        mockMvc.perform(patch(ROOT_PATH + "/0dd8dd1c-04d0-4c1f-9889-2c846881e2ed")
                                .with(jwt().jwt(builder -> builder.jti("15BDC75D-533B-44BB-9539-3DDC8693F3D3"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content("""
                                         {
                                         	"tin": "012345678912",
                                         	"snils": "112-233-445 95",
                                         	"drivingLicense": {
                                         		"series": "001series",
                                         		"number": 123323,
                                         		"issueDate": "2024-01-01",
                                         		"expiryDate": "2034-01-01",
                                         		"categoryIds": [
                                         			"ac98cc7c-8bb8-488c-b4f1-c24d96da3b62"
                                         		]
                                         	}
                                         }
                                         """))
               .andExpect(status().isOk());
        
        var driver = driverRepository.findById(UUID.fromString("0dd8dd1c-04d0-4c1f-9889-2c846881e2ed"))
                                     .orElseThrow(() -> new JUnitException("Driver not found"));
        assertThat(driver).isNotNull();
        assertThat(driver.getTin()).isEqualTo("012345678912");
        assertThat(driver.getSnils()).isEqualTo("112-233-445 95");
        assertThat(driver.getDrivingLicense().getSeries()).isEqualTo("001series");
        assertThat(driver.getDrivingLicense().getNumber()).isEqualTo("123323");
        assertThat(driver.getDrivingLicense().getIssueDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(driver.getDrivingLicense().getExpiryDate()).isEqualTo(LocalDate.of(2034, 1, 1));
        assertThat(driver.getDrivingLicense().getCategories()).isNotNull();
        assertThat(driver.getDrivingLicense().getCategories()).hasSize(1);
        assertThat(driver.getDrivingLicense().getCategories()).extracting("id")
                                                              .containsExactly(UUID.fromString("ac98cc7c-8bb8-488c-b4f1-c24d96da3b62"));
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/fleet_owner_organization.sql",
            "/scripts/ewb_contract.sql",
            "/scripts/ewb_tariff.sql",
            "/scripts/driver.sql"
    })
    void deactivateDriver() {
        mockMvc.perform(patch(ROOT_PATH + "/0dd8dd1c-04d0-4c1f-9889-2c846881e2ed/deactivate")
                                .with(jwt().jwt(builder -> builder.jti("15BDC75D-533B-44BB-9539-3DDC8693F3D3"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                       )
               .andExpect(status().isOk());
        
        var driver = driverRepository.findById(UUID.fromString("0dd8dd1c-04d0-4c1f-9889-2c846881e2ed"))
                                     .orElseThrow(() -> new JUnitException("Driver not found"));
        assertThat(driver).isNotNull();
        assertThat(driver.getDrivingLicense().isActive()).isFalse();
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/fleet_owner_organization.sql",
            "/scripts/ewb_contract.sql",
            "/scripts/ewb_tariff.sql",
            "/scripts/driver.sql"
    })
    void getDriverById() {
        var response = mockMvc.perform(get(ROOT_PATH + "/0dd8dd1c-04d0-4c1f-9889-2c846881e2ed")
                                               .with(jwt().jwt(builder -> builder.jti("15BDC75D-533B-44BB-9539-3DDC8693F3D3"))
                                                          .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                      )
                              .andExpect(status().isOk())
                              .andReturn()
                              .getResponse()
                              .getContentAsString(StandardCharsets.UTF_8);
        
        var actual = objectMapper.readValue(response, GetDriverResponse.class);
        var expected = driverRepository.findById(UUID.fromString("0dd8dd1c-04d0-4c1f-9889-2c846881e2ed"))
                                       .orElseThrow(() -> new JUnitException("Driver not found"));
        assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(driverMapper.driverToGetDriverResponse(expected));
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = { "/scripts/cleanup_database.sql",
                     "/scripts/basic_corp_structure.sql",
                     "/scripts/fleet_owner_organization.sql",
                     "/scripts/driver.sql",
                     "/scripts/transport.sql", })
    void getDriversByFio() {
        var transportId = UUID.fromString("057b4fb4-16ab-4420-a16a-5d177a216901");
        var request = new DriverByFioRequest("ива", transportId, null);
        mockMvc.perform(post(ROOT_PATH + "/fio")
                                .with(jwt().jwt(builder -> builder.jti("15BDC75D-533B-44BB-9539-3DDC8693F3D3"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.totalElements").value(1))
               .andExpect(jsonPath("$.content[0].driver.id").value("0dd8dd1c-04d0-4c1f-9889-2c846881e2ed"))
               .andExpect(jsonPath("$.content[0].driver.departmentId").value("489a0090-1819-4c60-a611-572ea115c6a4"));
    }
    
    @ParameterizedTest
    @SneakyThrows
    @MethodSource("getDriverSource")
    @Sql(scripts = { "/scripts/cleanup_database.sql",
                     "/scripts/basic_corp_structure.sql",
                     "/scripts/fleet_owner_organization.sql",
                     "/scripts/driver.sql",
                     "/scripts/transport.sql", })
    void getDriversByFilters(String transportId, String searchText) {
        mockMvc.perform(get(ROOT_PATH)
                                .with(jwt().jwt(builder -> builder.jti("15BDC75D-533B-44BB-9539-3DDC8693F3D3"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_ROOM_ADMIN.name())))
                                .param("transportId", transportId)
                                .param("searchText", searchText))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.totalElements").value(1))
               .andExpect(jsonPath("$.content[0].driver.id").value("bc2f5dbf-3585-4b8f-9f37-60ba133e5670"))
               .andExpect(jsonPath("$.content[0].driver.departmentId").value("482e6dcb-03a9-4927-90b4-c7081114a9d8"));
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = { "/scripts/cleanup_database.sql", "/scripts/basic_corp_structure.sql", "/scripts/driver.sql" })
    void search() {
        var request = new DriverSearchRequest(
                null,
                null,
                null,
                null,
                new PageSettingDto(0, 10),
                new DriverSearchRequest.SortSettingDto(ORGANIZATION_NAME, false));
        mockMvc.perform(post(ROOT_PATH + "/search")
                                .with(jwt().jwt(builder -> builder.jti("15BDC75D-533B-44BB-9539-3DDC8693F3D3"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.totalElements").value(2))
               .andExpect(jsonPath("$.content[0].driver.id").value("bc2f5dbf-3585-4b8f-9f37-60ba133e5670"))
               .andExpect(jsonPath("$.content[0].drivingLicense.active").value(false))
               .andExpect(jsonPath("$.content[1].driver.id").value("0dd8dd1c-04d0-4c1f-9889-2c846881e2ed"))
               .andExpect(jsonPath("$.content[1].drivingLicense.active").value(true));
    }
    
    static Stream<Arguments> getDriverSource() {
        return Stream.of(
                Arguments.of("db6dd8bb-8101-48bc-a9c3-d8466ed5a4ee", "Алек"),
                Arguments.of("db6dd8bb-8101-48bc-a9c3-d8466ed5a4ee", null)
                        );
    }
}
