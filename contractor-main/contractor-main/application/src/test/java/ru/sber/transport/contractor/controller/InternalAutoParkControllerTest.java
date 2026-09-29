package ru.sber.transport.contractor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.contractor.database.dao.ContractorRepository;
import ru.sber.transport.contractor.database.dao.EmployeeRepository;
import ru.sber.transport.contractor.database.model.ContractorType;
import ru.sber.transport.contractor.database.model.Employee;
import ru.sber.transport.contractor.database.model.ServiceType;
import ru.sber.transport.contractor.dto.NewContractorDTO;
import ru.sber.transport.contractor.dto.PatchData;
import ru.sber.transport.contractor.dto.enums.StaffSpeciality;
import ru.sber.transport.contractor.dto.internal.*;
import ru.sber.transport.contractor.feign.BranchesClient;
import ru.sber.transport.contractor.feign.InternalClient;
import ru.sber.transport.contractor.feign.StaffClient;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.security.Key;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import feign.Response;
import feign.RequestTemplate;
import feign.Request;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.contractor.dto.enums.StaffSpeciality.CARGO_DRIVER;
import static ru.sber.transport.contractor.dto.enums.StaffSpeciality.PASSENGER_DRIVER;

@UnitTest
@IsolatedTest
@Feature("app_platform_contractor")
@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка контроллера внутреннего автопарка")
@MockBean(Key.class)
@TestPropertySource(properties = "spring.jpa.show-sql=true")
@ActiveProfiles("test")
public class InternalAutoParkControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @MockBean
    private AuthorizationManager<?> manager;

    @MockBean
    private BranchesClient branchesClient;

    @MockBean
    private StaffClient staffClient;

    @MockBean
    private InternalClient internalClient;

    private Employee employee;

    private static final String USER_ID = "95a9ddc6-e62d-4061-9e65-47982ec2cf4c";

    @BeforeEach
    void setUp() throws Exception{
        when(internalClient.add(any(), any(), any())).thenReturn(new DispatcherResponseDTO(UUID.randomUUID()));
        when(staffClient.getDrivers(any(), any(), anyInt(), anyInt(), anyBoolean(), any())).thenReturn(new PageImpl<StaffDto>(Collections.emptyList()));
        when(staffClient.getDispatchers(any(), any(), anyInt(), anyInt(), anyBoolean(), any())).thenReturn(new PageImpl<StaffDto>(Collections.emptyList()));
        when(branchesClient.getBranches(any(), any(), any(), anyInt(), anyInt(), anyBoolean(), any(), any())).thenReturn(new PageImpl<BranchResponseDto>(Collections.emptyList()));
        doNothing().when(internalClient).edit(any(), any(), any(), any());
        doNothing().when(branchesClient).updateBranch(any(), any(), any(), any(), any());
        doNothing().when(branchesClient).deleteBranch(any(), any(), any(), any());
        doNothing().when(internalClient).delete(any(), any(), any());
        doNothing().when(staffClient).deleteDispatcher(any(), any(), any(), any());
        doNothing().when(staffClient).deleteDriver(any(), any(), any(), any());
        doNothing().when(branchesClient).addBranch(any(), any(), any(), any());
        AuthorizeUtils.authorize(manager);
        var msrn = "1234567890123";
        var tin = "1234567890";
        var serviceType = ServiceType.INTERNAL_AUTO_PARK;
        var contractorType = ContractorType.DISPATCHER_INTERNAL;
        var contactPersonFirstName = "John";
        var contactPersonLastName = "Doe";
        var contactPersonPatronymic = "Smith";
        var contactPersonPhone = "contact person phone";
        var contractorName = "contactorName";
        var email = "email@email.ru";
        var rating = 0;
        var vehicleCountNorm = 300;

        employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(UUID.randomUUID()).build();
        employee = employeeRepository.save(employee);

        var newContractor = new NewContractorDTO(contractorName, msrn, tin, contactPersonFirstName, contactPersonLastName,
                contactPersonPatronymic, contactPersonPhone, email, UUID.randomUUID(),
                rating, null, null, null, serviceType, contractorType, vehicleCountNorm);

        mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .header("X-Organization-Id", employee.getOrganizationId())
                        .content(objectMapper.writeValueAsString(newContractor)))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void addStaffMainDispatcher() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        var employeeId = UUID.randomUUID();
        var newEmployee = Employee.builder().id(employeeId).userId(employeeId)
                .consent(true)
                .organizationId(employee.getOrganizationId()).build();
        newEmployee = employeeRepository.save(newEmployee);

        var createStaffDTO = new CreateStaffDto(
                StaffSpeciality.MANAGER,
                "firstname",
                "lastname",
                "patronymic",
                "+79999999999",
                "mail@mail.ru",
                employee.getOrganizationId(),
                newEmployee.getId(),
                UUID.randomUUID(),
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        mockMvc.perform(post("/internal-auto-park/staff/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt)
                        .header("Authorization", jwt)
                        .content(objectMapper.writeValueAsString(createStaffDTO)))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void addStaffDispatcher() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        var employeeId = UUID.randomUUID();
        var newEmployee = Employee.builder().id(employeeId).userId(employeeId)
                .consent(true)
                .organizationId(employee.getOrganizationId()).build();
        newEmployee = employeeRepository.save(newEmployee);

        var createStaffDTO = new CreateStaffDto(
                StaffSpeciality.DISPATCHER,
                "firstname",
                "lastname",
                "patronymic",
                "+79999999999",
                "mail@mail.ru",
                employee.getOrganizationId(),
                newEmployee.getId(),
                UUID.randomUUID(),
                true,
                "123",
                "345",
                "23.01.2025",
                "24.01.2025",
                "WEB",
                null,
                null,
                null,
                null
        );
        mockMvc.perform(post("/internal-auto-park/staff/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt)
                        .header("Authorization", jwt)
                        .content(objectMapper.writeValueAsString(createStaffDTO)))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void addStaffDispatcherException() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        var employeeId = UUID.randomUUID();
        var newEmployee = Employee.builder().id(employeeId).userId(employeeId)
                .consent(true)
                .organizationId(employee.getOrganizationId()).build();
        newEmployee = employeeRepository.save(newEmployee);

        var createStaffDTO = new CreateStaffDto(
                StaffSpeciality.DISPATCHER,
                "firstname",
                "lastname",
                "patronymic",
                "+79999999999",
                "mail@mail.ru",
                employee.getOrganizationId(),
                newEmployee.getId(),
                UUID.randomUUID(),
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        Response response = Response.builder()
                .status(409)
                .reason("Conflict")
                .request(Request.create(Request.HttpMethod.POST, "/path/to/resource", new HashMap<>(), null, new RequestTemplate()))
                .build();

        doThrow(FeignException.errorStatus("POST", response))
                .when(staffClient)
                .addDispatcher(any(), any(), any(), any());

        mockMvc.perform(post("/internal-auto-park/staff/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt)
                        .header("Authorization", jwt)
                        .content(objectMapper.writeValueAsString(createStaffDTO)))
                .andExpect(status().isConflict())

                .andReturn();
    }

    @Test
    void addStaffPassengerDriver() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        var employeeId = UUID.randomUUID();
        var newEmployee = Employee.builder().id(employeeId).userId(employeeId)
                .consent(true)
                .organizationId(employee.getOrganizationId()).build();
        newEmployee = employeeRepository.save(newEmployee);

        var createStaffDTO = new CreateStaffDto(
                StaffSpeciality.PASSENGER_DRIVER,
                "firstname",
                "lastname",
                "patronymic",
                "+79999999999",
                "mail@mail.ru",
                employee.getOrganizationId(),
                newEmployee.getId(),
                UUID.randomUUID(),
                false,
                null,
                null,
                "01.01.2025",
                "01.01.2025",
                null,
                "55555555555",
                "123",
                "4315",
                Set.of("A", "C")
        );
        mockMvc.perform(post("/internal-auto-park/staff/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt)
                        .header("Authorization", jwt)
                        .content(objectMapper.writeValueAsString(createStaffDTO)))
                .andExpect(status().isOk()).andReturn();

    }

    @Test
    void addStaffPassengerDriverException() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        var employeeId = UUID.randomUUID();
        var newEmployee = Employee.builder().id(employeeId).userId(employeeId)
                .consent(true)
                .organizationId(employee.getOrganizationId()).build();
        newEmployee = employeeRepository.save(newEmployee);

        var createStaffDTO = new CreateStaffDto(
                StaffSpeciality.PASSENGER_DRIVER,
                "firstname",
                "lastname",
                "patronymic",
                "+79999999999",
                "mail@mail.ru",
                employee.getOrganizationId(),
                newEmployee.getId(),
                UUID.randomUUID(),
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        Response response = Response.builder()
                .status(409)
                .reason("Conflict")
                .request(Request.create(Request.HttpMethod.POST, "/path/to/resource", new HashMap<>(), null, new RequestTemplate()))
                .build();

        doThrow(FeignException.errorStatus("POST", response))
                .when(staffClient)
                .addDriver(any(), any(), any(), any());

        mockMvc.perform(post("/internal-auto-park/staff/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt)
                        .header("Authorization", jwt)
                        .content(objectMapper.writeValueAsString(createStaffDTO)))
                .andExpect(status().isConflict());

    }

    @Test
    void addStaffCargoDriver() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        var employeeId = UUID.randomUUID();
        var newEmployee = Employee.builder().id(employeeId).userId(employeeId)
                .consent(true)
                .organizationId(employee.getOrganizationId()).build();
        newEmployee = employeeRepository.save(newEmployee);

        var createStaffDTO = new CreateStaffDto(
                CARGO_DRIVER,
                "firstname",
                "lastname",
                "patronymic",
                "+79999999999",
                "mail@mail.ru",
                employee.getOrganizationId(),
                newEmployee.getId(),
                UUID.randomUUID(),
                false,
                null,
                null,
                "01.01.2025",
                "01.01.2025",
                null,
                "123123",
                "123123",
                "1321",
                Set.of("A")
        );
        mockMvc.perform(post("/internal-auto-park/staff/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt)
                        .header("Authorization", jwt)
                        .content(objectMapper.writeValueAsString(createStaffDTO)))
                .andExpect(status().isOk()).andReturn();

    }

    @Test
    void addStaffConflict() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        var employeeId = UUID.randomUUID();
        var newEmployee = Employee.builder().id(employeeId).userId(employeeId)
                .consent(true)
                .organizationId(UUID.randomUUID()).build();
        newEmployee = employeeRepository.save(newEmployee);

        var createStaffDTO = new CreateStaffDto(
                CARGO_DRIVER,
                "firstname",
                "lastname",
                "patronymic",
                "+79999999999",
                "mail@mail.ru",
                employee.getOrganizationId(),
                newEmployee.getId(),
                UUID.randomUUID(),
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        mockMvc.perform(post("/internal-auto-park/staff/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt)
                        .header("Authorization", jwt)
                        .content(objectMapper.writeValueAsString(createStaffDTO)))
                .andExpect(status().isConflict()).andReturn();

    }

    @Test
    void getStaffDispatcher() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        mockMvc.perform(get("/internal-auto-park/staff/")
                        .with(jwt)
                        .header("Authorization", jwt)
                        .param("organizationId", employee.getOrganizationId().toString())
                        .param("speciality", GetStaffDto.Speciality.DISPATCHER.name()))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void getStaffDispatcherData() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));

        var attorneyNumber = "2";
        var personnelNumber = "1";
        var ewbCreationPossibility = true;
        var issueDate = LocalDate.now();
        var expiryDate = LocalDate.now().plusDays(10);
        var branchName = "branchName";
        var oauthId = UUID.randomUUID();
        StaffDto staffDto = new StaffDto();
        staffDto.setAttorneyNumber(attorneyNumber);
        staffDto.setPersonnelNumber(personnelNumber);
        staffDto.setEwbCreationPossibility(ewbCreationPossibility);
        staffDto.setIssueDate(LocalDate.now());
        staffDto.setExpiryDate(LocalDate.now().plusDays(10));
        staffDto.setBranchName(branchName);
        staffDto.setOauthId(oauthId);
        when(staffClient.getDispatchers(any(), any(), anyInt(), anyInt(), anyBoolean(), any())).thenReturn(new PageImpl<StaffDto>(List.of(staffDto)));

        mockMvc.perform(get("/internal-auto-park/staff/")
                        .with(jwt)
                        .header("Authorization", jwt)
                        .param("organizationId", employee.getOrganizationId().toString())
                        .param("speciality", GetStaffDto.Speciality.DISPATCHER.name()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.content[0].attorneyNumber").value(attorneyNumber))
                .andExpect(jsonPath("$.content[0].branchName").value(branchName))
                .andExpect(jsonPath("$.content[0].personnelNumber").value(personnelNumber))
                .andExpect(jsonPath("$.content[0].ewbCreationPossibility").value(ewbCreationPossibility))
                .andExpect(jsonPath("$.content[0].issueDate").value(issueDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))))
                .andExpect(jsonPath("$.content[0].expiryDate").value(expiryDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))))
                .andExpect(jsonPath("$.content[0].oauthId").value(oauthId.toString()));
    }

    @Test
    void getStaffDriverData() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));

        var oauthId = UUID.randomUUID();
        StaffDto staffDto = new StaffDto();
        staffDto.setPersonnelNumber("123");
        staffDto.setDriverLicenseNumber("1010");
        staffDto.setIssueDate(LocalDate.now());
        staffDto.setDriverLicenses(Set.of("A"));
        staffDto.setExpiryDate(LocalDate.now().plusDays(10));
        staffDto.setDriverSpeciality("CARGO_DRIVER");
        staffDto.setOauthId(oauthId);
        when(staffClient.getDrivers(any(), any(), anyInt(), anyInt(), anyBoolean(), any())).thenReturn(new PageImpl<StaffDto>(List.of(staffDto)));

        mockMvc.perform(get("/internal-auto-park/staff/")
                        .with(jwt)
                        .header("Authorization", jwt)
                        .param("organizationId", employee.getOrganizationId().toString())
                        .param("speciality", GetStaffDto.Speciality.DRIVER.name()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.content[0].personnelNumber").value(staffDto.getPersonnelNumber()))
                .andExpect(jsonPath("$.content[0].driverLicenseNumber").value(staffDto.getDriverLicenseNumber()))
                .andExpect(jsonPath("$.content[0].driverLicenses[0]").value("A"))
                .andExpect(jsonPath("$.content[0].driverSpeciality").value("CARGO_DRIVER"))
                .andExpect(jsonPath("$.content[0].issueDate").value(staffDto.getIssueDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))))
                .andExpect(jsonPath("$.content[0].expiryDate").value(staffDto.getExpiryDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))))
                .andExpect(jsonPath("$.content[0].oauthId").value(oauthId.toString()));
    }

    @Test
    void getStaffDriver() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        mockMvc.perform(get("/internal-auto-park/staff/")
                        .with(jwt)
                        .header("Authorization", jwt)
                        .param("organizationId", employee.getOrganizationId().toString())
                        .param("speciality", GetStaffDto.Speciality.DRIVER.name()))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void deleteStaffDriver() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        mockMvc.perform(delete("/internal-auto-park/staff/"+UUID.randomUUID()+"/")
                        .with(jwt)
                        .header("Authorization", jwt)
                        .param("organizationId", employee.getOrganizationId().toString())
                        .param("speciality", GetStaffDto.Speciality.DRIVER.name()))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void deleteStaffDispatcher() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        mockMvc.perform(delete("/internal-auto-park/staff/"+UUID.randomUUID()+"/")
                        .with(jwt)
                        .header("Authorization", jwt)
                        .param("organizationId", employee.getOrganizationId().toString())
                        .param("speciality", GetStaffDto.Speciality.DISPATCHER.name()))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void addBranch() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        var createBranchDto = new CreateBranchDto(
                "Test",
                UUID.randomUUID(),
                employee.getOrganizationId(),
                300
        );
        mockMvc.perform(post("/internal-auto-park/branches/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt)
                        .header("Authorization", jwt)
                        .content(objectMapper.writeValueAsString(createBranchDto)))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void getBranches() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        mockMvc.perform(get("/internal-auto-park/branches/")
                        .with(jwt)
                        .header("Authorization", jwt)
                        .param("organizationId", employee.getOrganizationId().toString()))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void editBranch() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        var createBranchDto = new CreateBranchDto(
                "Test",
                UUID.randomUUID(),
                employee.getOrganizationId(),
                300
        );
        mockMvc.perform(put("/internal-auto-park/branches/"+UUID.randomUUID()+"/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt)
                        .header("Authorization", jwt)
                        .content(objectMapper.writeValueAsString(createBranchDto)))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void editBranchException() throws Exception {
        FeignException.FeignClientException exception = new FeignException.BadRequest("", Mockito.mock(Request.class), new byte[0], Map.of());
        doThrow(exception).when(branchesClient).updateBranch(any(), any(), any(), any(), any());
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        var createBranchDto = new CreateBranchDto(
                "Test",
                UUID.randomUUID(),
                employee.getOrganizationId(),
                300
        );
        mockMvc.perform(put("/internal-auto-park/branches/"+UUID.randomUUID()+"/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt)
                        .header("Authorization", jwt)
                        .content(objectMapper.writeValueAsString(createBranchDto)))
                .andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    void patchDispatcher() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        var employeeId = UUID.randomUUID();
        var newEmployee = Employee.builder().id(employeeId).userId(employeeId)
                .consent(true)
                .organizationId(employee.getOrganizationId()).build();
        employeeRepository.save(newEmployee);
        doNothing().when(staffClient).patchDispatcher(any(), any(), any(), any(), any());

        List<PatchData> patchData = List.of(new PatchData("organizationId", employee.getOrganizationId()),
                new PatchData("branchId", UUID.randomUUID()),
                new PatchData("test", ""));
        mockMvc.perform(patch("/internal-auto-park/staff/" + employeeId + "/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt)
                        .param("speciality", GetStaffDto.Speciality.DISPATCHER.name())
                        .header("Authorization", jwt)
                        .content(objectMapper.writeValueAsString(patchData)))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void patchPassengerDriver() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        var employeeId = UUID.randomUUID();
        var newEmployee = Employee.builder().id(employeeId).userId(employeeId)
                .consent(true)
                .organizationId(employee.getOrganizationId()).build();
        employeeRepository.save(newEmployee);
        doNothing().when(staffClient).patchDriver(any(), any(), any(), any(), any());

        List<PatchData> patchData = List.of(new PatchData("organizationId", employee.getOrganizationId()),
                new PatchData("branchId", UUID.randomUUID()),
                new PatchData("firstName", "test"),
                new PatchData("test", ""));
        mockMvc.perform(patch("/internal-auto-park/staff/" + employeeId + "/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt)
                        .param("speciality", PASSENGER_DRIVER.name())
                        .header("Authorization", jwt)
                        .content(objectMapper.writeValueAsString(patchData)))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void patchCargoDriver() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        var employeeId = UUID.randomUUID();
        var newEmployee = Employee.builder().id(employeeId).userId(employeeId)
                .consent(true)
                .organizationId(employee.getOrganizationId()).build();
        employeeRepository.save(newEmployee);
        doNothing().when(staffClient).patchDriver(any(), any(), any(), any(), any());

        List<PatchData> patchData = List.of(new PatchData("organizationId", employee.getOrganizationId()),
                new PatchData("branchId", UUID.randomUUID()),
                new PatchData("firstName", "test"),
                new PatchData("test", ""));
        mockMvc.perform(patch("/internal-auto-park/staff/" + employeeId + "/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt)
                        .param("speciality", CARGO_DRIVER.name())
                        .header("Authorization", jwt)
                        .content(objectMapper.writeValueAsString(patchData)))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void patchStaffException() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        var employeeId = UUID.randomUUID();
        var newEmployee = Employee.builder().id(employeeId).userId(employeeId)
                .consent(true)
                .organizationId(employee.getOrganizationId()).build();
        employeeRepository.save(newEmployee);
        FeignException.FeignClientException exception = new FeignException.BadRequest("", Mockito.mock(Request.class), new byte[0], Map.of());
        doThrow(exception).when(staffClient).patchDispatcher(any(), any(), any(), any(), any());

        List<PatchData> patchData = List.of(new PatchData("organizationId", employee.getOrganizationId()));
        mockMvc.perform(patch("/internal-auto-park/staff/" + employeeId + "/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt)
                        .header("Authorization", jwt)
                        .content(objectMapper.writeValueAsString(patchData)))
                .andExpect(status().isBadRequest()).andReturn();
    }


    @Test
    void deleteBranch() throws Exception {
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        mockMvc.perform(delete("/internal-auto-park/branches/"+UUID.randomUUID()+"/")
                        .with(jwt)
                        .header("Authorization", jwt)
                        .param("organizationId", employee.getOrganizationId().toString()))
                .andExpect(status().isOk()).andReturn();
    }

}
