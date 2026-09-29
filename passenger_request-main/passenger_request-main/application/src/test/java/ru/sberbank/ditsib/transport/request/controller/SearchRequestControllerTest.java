package ru.sberbank.ditsib.transport.request.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.Data;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.*;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;
import ru.sberbank.ditsib.transport.request.database.model.taxi.TaxiTariff;
import ru.sberbank.ditsib.transport.request.dto.GetRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.RegionDto;
import ru.sberbank.ditsib.transport.request.dto.RequestDTO;
import ru.sberbank.ditsib.transport.request.dto.RequestPublicSearchDTO;
import ru.sberbank.ditsib.transport.request.service.RegionDataResolver;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@Transactional
@EmbeddedPostgres
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера поиска заявок")
@SpringBootTest(classes = { RequestApplication.class })
@MockitoBean(types = JwtDecoder.class)
class SearchRequestControllerTest extends SharedTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private TripPurposeRepository tripPurposeRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private ContractorRepository contractorRepository;
    @Autowired
    private TaxiTariffRepository taxiTariffRepository;
    @Autowired
    private TaxiTripRepository taxiTripRepository;
    
    
    @MockitoBean
    private RegionDataResolver regionDataResolver;
    
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    @Data
    public static class PublicContent {
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
        request10.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_10);
        
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
        
        Contractor contractor = Contractor.builder()
                                          .id(UUID.randomUUID())
                                          .name("name")
                                          .contractorName("name")
                                          .contractorRusName("rusname")
                                          .integrationEmail("aaa@bbb.ru")
                                          .build();
        contractorRepository.save(contractor);
        
        TaxiTariff taxiTariff = TaxiTariff.builder()
                                          .id(TARIFF_ID_1)
                                          .contractorId(contractor.getId())
                                          .workGroup("Work group")
                                          .regionId(UUID.randomUUID())
                                          .humanReadableId("TT-123-23")
                                          .taxiClass(TaxiClass.ECONOMY)
                                          .rideCostPerKm(1)
                                          .rideCostPerMin(2)
                                          .waitCostPerMin(3)
                                          .departmentId(UUID.randomUUID())
                                          .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                                          .transportType(TransportTypeEnum.TAXI)
                                          .build();
        taxiTariffRepository.save(taxiTariff);
        
        var regionDto = RegionDto.builder().id(UUID.randomUUID()).name("moscow").build();
        when(regionDataResolver.getRegion(any())).thenReturn(regionDto);
    }
    
    @Test
    @DisplayName("Поиск заявок по типу компенсации")
    void test_getRequestByTransportCompensation() throws Exception {
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        var contractor = contractorRepository.save(Instancio.create(Contractor.class));
        request10.getTransportCompensation().forEach(it -> it.setRequest(request10));
        request1.setContractorId(contractor.getId());
        request2.setContractorId(contractor.getId());
        requestRepository.save(request1);
        requestRepository.save(request2);
        requestRepository.save(request4);
        requestRepository.save(request10);
        
        var requestPublicSearchDTO = new RequestPublicSearchDTO();
        requestPublicSearchDTO.setCompensationType(PublicCompensationType.CITY_TRIP_COMPENSATION);
        
        var response = mockMvc.perform(post("/public_search")
                                               .contentType(MediaType.APPLICATION_JSON_VALUE)
                                               .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                               .content(objectMapper.writeValueAsBytes(requestPublicSearchDTO)))
                              .andExpect(status().isOk()).andReturn();
        
        var actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                      new TypeReference<PublicContent>() {
                                                      });
        
        assertEquals(request10.getId(), actual.content.get(0).getId());
    }
    
    
    @Test
    @DisplayName("Поиск заявок по типу транспорта")
    void test_getRequestByIdAndTransportType() throws Exception {
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        var contractor = contractorRepository.save(Instancio.create(Contractor.class));
        request1.setContractorId(contractor.getId());
        request2.setContractorId(contractor.getId());
        requestRepository.save(request1);
        requestRepository.save(request2);
        requestRepository.save(request4);

        requestRepository.save(request6);
        
        var response = mockMvc.perform(
                                      get(String.format("/%s/%s", request4.getTransportType().getName(), request4.getId()))
                                              .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
        
                              .andExpect(status().isOk()).andReturn();
        var actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                      new TypeReference<GetRequestDTO>() {
                                                      });
        
        assertEquals(request4.getId(), actual.getId());
        assertNotNull(actual.getPassenger());
        assertEquals(actual.getPassenger().id(), request4.getPassenger().getId());
    }
    
    @Test
    @DisplayName("Поиск заявок по автору")
    void test_getRequestByAuthorId() throws Exception {
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        var contractor = contractorRepository.save(Instancio.create(Contractor.class));
        taxiTripRepository.saveAndFlush(taxiTrip1);
        taxiTripRepository.saveAndFlush(taxiTrip2);
        request1.setTaxiTrip(taxiTrip1);
        request1.setContractorId(contractor.getId());
        request2.setTaxiTrip(taxiTrip2);
        request2.setContractorId(contractor.getId());
        requestRepository.save(request1);
        requestRepository.save(request2);
        
        var response = mockMvc.perform(
                                      get("/search?authorId=" + request1.getAuthor().getId())
                                              .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
        
                              .andExpect(status().isOk()).andReturn();
        var actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                         new TypeReference<List<RequestDTO>>() {
                                                         });
        
        assertEquals(1, actual.size());
        assertEquals(request1.getId(), actual.getFirst().getId());
        assertEquals(request1.getAuthor().getId(), testEmployee1.getId());
    }
    
    @Test
    @DisplayName("Поиск заявок по статусам поездки")
    void test_getRequestByStatuses() throws Exception {
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        var contractor = contractorRepository.save(Instancio.create(Contractor.class));
        request1.setStatus(TripRequestStatus.TAXI_APPROVED);
        request1.setContractorId(contractor.getId());
        request2.setStatus(TripRequestStatus.TAXI_TRIP_FINISHED);
        request2.setContractorId(contractor.getId());
        request3.setStatus(TripRequestStatus.TAXI_CANCELLED);
        request3.setContractorId(contractor.getId());
        requestRepository.save(request1);
        requestRepository.save(request2);
        requestRepository.save(request3);
        taxiTripRepository.saveAndFlush(taxiTrip1);
        taxiTripRepository.saveAndFlush(taxiTrip2);
        taxiTripRepository.saveAndFlush(taxiTrip3);
        request1.setTaxiTrip(taxiTrip1);
        request2.setTaxiTrip(taxiTrip2);
        request3.setTaxiTrip(taxiTrip3);
        requestRepository.save(request1);
        requestRepository.save(request2);
        requestRepository.save(request3);
        
        var response = mockMvc.perform(
                                      get("/search?statuses=" + request1.getStatus().ordinal()
                                          + "&statuses=" + request2.getStatus().ordinal()
                                          + "&statuses=" + request3.getStatus().ordinal())
                                              .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk()).andReturn();
        List<RequestDTO> actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                         new TypeReference<>() {
                                                         });
        
        assertEquals(3, actual.size());
        assertThat(actual.get(0).getStatus()).isIn(TripRequestStatus.TAXI_APPROVED,
                                                   TripRequestStatus.TAXI_TRIP_FINISHED,
                                                   TripRequestStatus.TAXI_CANCELLED);
        assertThat(actual.get(1).getStatus()).isIn(TripRequestStatus.TAXI_APPROVED,
                                                   TripRequestStatus.TAXI_TRIP_FINISHED,
                                                   TripRequestStatus.TAXI_CANCELLED);
        assertThat(actual.get(2).getStatus()).isIn(TripRequestStatus.TAXI_APPROVED,
                                                   TripRequestStatus.TAXI_TRIP_FINISHED,
                                                   TripRequestStatus.TAXI_CANCELLED);
    }
    
    @Test
    @DisplayName("Поиск заявок по пассажиру")
    void test_getRequestByPassengerId() throws Exception {
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
    
        taxiTripRepository.saveAndFlush(taxiTrip1);
        taxiTripRepository.saveAndFlush(taxiTrip2);
        taxiTripRepository.saveAndFlush(taxiTrip3);
        var contractor = contractorRepository.save(Instancio.create(Contractor.class));
        request1.setCoopTrip(true);
        request1.setTaxiTrip(taxiTrip1);
        request1.setContractorId(contractor.getId());
        requestRepository.save(request1);
        request2.setPassenger(request1.getPassenger());
        request2.setDesiredDate(request1.getDesiredDate().minusHours(1));
        request2.setCoopTrip(true);
        request2.setTaxiTrip(taxiTrip2);
        request2.setContractorId(contractor.getId());
        requestRepository.save(request2);
        request3.setPassenger(request1.getPassenger());
        request3.setDesiredDate(request1.getDesiredDate().plusHours(1));
        request3.setCoopTrip(false);
        request3.setTaxiTrip(taxiTrip3);
        request3.setContractorId(contractor.getId());
        requestRepository.save(request3);
        
        var response = mockMvc.perform(
                                      get("/search?passengerId=" + request1.getPassenger().getId())
                                              .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk()).andReturn();
        List<RequestDTO> actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                         new TypeReference<>() {
                                                         });
        
        assertEquals(3, actual.size());
        assertEquals(request1.getId(), actual.get(1).getId());
        assertEquals(request2.getId(), actual.get(2).getId());
        assertEquals(request1.getPassenger().getId(), actual.get(1).getPassenger().id());
        assertEquals(request1.getPassenger().getId(), actual.get(2).getPassenger().id());
    }
    
    
}
