package ru.sberbank.ditsib.transport.request.controller.carsharing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.RestTemplateConfig;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.request.dto.GetRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.RegionDto;
import ru.sberbank.ditsib.transport.request.dto.RequestCarsharingSearchDTO;
import ru.sberbank.ditsib.transport.request.service.RegionDataResolver;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@Slf4j
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка контроллера заявок для каршеринга")
@SpringBootTest(classes = RequestApplication.class)
@MockitoBean(types = JwtDecoder.class)
@Import(RestTemplateConfig.class)
class CarsharingRequestControllerTest extends SharedTest {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private RequestRepository requestRepository;
    
    @Autowired
    private TripPurposeRepository tripPurposeRepository;
    
    @Autowired
    private PositionRepository positionRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @MockitoBean
    private RestTemplate restTemplate;
    
    @MockitoBean
    private RegionDataResolver regionDataResolver;
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    @Data
    public static class Content {
        public List<GetRequestDTO> content;
    }
    
    @SneakyThrows
    @BeforeEach
    public void setUp() {
        
        testEmployee1.setUserId(userId);
        testEmployee2.setUserId(userId2);
        testEmployee3.setUserId(userId3);
        testEmployee4.setUserId(userId4);
        testEmployee6.setUserId(userId6);
        testEmployee7.setUserId(userId7);
        testEmployee8.setUserId(userId8);
        
        testEmployee1.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_1);
        testEmployee2.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_2);
        testEmployee3.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_3);
        testEmployee4.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_4);
        testEmployee6.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_6);
        testEmployee7.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_7);
        testEmployee8.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_8);
        
        request1.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_1);
        request2.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_2);
        request3.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_3);
        request4.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_4);
        publicRequest.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_5);
        request6.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_6);
        request7.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_7);
        request8.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_8);
        
        tripPurposeRepository.save(TripPurpose.builder().id(purposeId1).purpose(PURPOSE_DESCRIPTION_1).build());
        tripPurposeRepository.save(TripPurpose.builder().id(purposeId2).purpose(PURPOSE_DESCRIPTION_2).build());
        
        organizationRepository.saveAndFlush(organization1);
        departmentRepository.saveAndFlush(department1);
        positionRepository.save(testPosition1);
        positionRepository.save(testPosition2);
        positionRepository.save(testPosition3);
        employeeRepository.saveAndFlush(testEmployee1);
        testEmployee2.setSupervisorId(testEmployee1.getId());
        employeeRepository.save(testEmployee2);
        testEmployee3.setSupervisorId(testEmployee2.getId());
        employeeRepository.saveAndFlush(testEmployee3);
        department1.setDepartmentHead(testEmployee2.getId());
        departmentRepository.saveAndFlush(department1);
        employeeRepository.save(testEmployee4);
        employeeRepository.save(testEmployee6);
        employeeRepository.save(testEmployee7);
        employeeRepository.save(testEmployee8);
        
        RegionDto regionDto = RegionDto.builder().code("1").id(UUID.randomUUID()).name("moscow").build();
        when(regionDataResolver.getRegion(any())).thenReturn(regionDto);
        
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(String.class)))
                .thenReturn(deletedAnswer());
    }
    
    @Test
    @DisplayName("Получение заявки для каршеринга по идентификатору")
    void test_getCarsharingRequest() throws Exception {
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        requestRepository.save(request7);
        
        var response = mockMvc.perform(get("/CARSHARING/" + request7.getId())
                                               .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                               .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk()).andReturn();
        
        Map<String, Object> actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                            new TypeReference<>() {
                                                            });
        
        assertEquals(request7.getId().toString(), actual.get("id"));
        
        Map<String, Object> expected = (Map<String, Object>) actual.get("expected");
        List<Map<String, Object>> waypoints = (List<Map<String, Object>>) expected.get("waypoints");
        
        assertEquals(address1.getLatitude(), waypoints.get(0).get("latitude"));
        assertEquals(address1.getLongitude(), waypoints.get(0).get("longitude"));
        assertEquals(address2.getLatitude(), waypoints.get(1).get("latitude"));
        assertEquals(address2.getLongitude(), waypoints.get(1).get("longitude"));
        assertEquals(address3.getLatitude(), waypoints.get(2).get("latitude"));
        assertEquals(address3.getLongitude(), waypoints.get(2).get("longitude"));
        
        Map<String, Object> author = (Map<String, Object>) actual.get("author");
        assertEquals(request7.getAuthor().getId().toString(), author.get("id"));
        assertEquals(request7.getAuthor().getDepartment().getId().toString(), author.get("departmentId"));
        Map<String, Object> passenger = (Map<String, Object>) actual.get("passenger");
        assertEquals(request7.getPassenger().getId().toString(), passenger.get("id"));
        assertEquals(request7.getPassenger().getDepartment().getId().toString(), passenger.get("departmentId"));
        assertEquals(request7.getContractorId().toString(), actual.get("contractorId"));
        assertNull(passenger.get("approvedBy"));
    }
    
    @Test
    @DisplayName("Поиск заявок на каршеринге по coopTrip")
    @WithMockUser(username = USER1_ID, roles = "GUEST")
    void test_getCarsharingRequestByCoopTrip() throws Exception {
        addressRepository.save(address1);
        addressRepository.save(address2);
        employeeRepository.save(testEmployee7);
        employeeRepository.save(testEmployee8);
        addressRepository.saveAndFlush(address3);
        requestRepository.save(request7);
        requestRepository.save(request8);
        
        RequestCarsharingSearchDTO requestSearchDTO = new RequestCarsharingSearchDTO();
        requestSearchDTO.setCoopTrip(true);
        
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post("/carsharing_search").content(request)
                                                                           .header("Authorization", UUID.randomUUID())
                                                                           .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        assertEquals(2, requestRepository.findAll().size());
        assertEquals(1, actual.content.size());
        GetRequestDTO getRequestDTO = actual.content.get(0);
        assertEquals(request7.getId(), getRequestDTO.getId());
        assertEquals(request7.isCoopTrip(), getRequestDTO.isCoopTrip());
    }
    
    private ResponseEntity<String> deletedAnswer() {
        return loadMagentaAnswerMock("cancel.json");
    }
    
    private ResponseEntity<String> suitableRidesAnswer() {
        return loadMagentaAnswerMock("suitableRides.json");
    }
}
