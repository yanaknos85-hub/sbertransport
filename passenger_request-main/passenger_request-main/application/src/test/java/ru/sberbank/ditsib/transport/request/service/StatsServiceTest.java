package ru.sberbank.ditsib.transport.request.service;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.model.StatsDTO;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;
import ru.sberbank.ditsib.transport.request.database.model.taxi.TaxiTariff;
import ru.sberbank.ditsib.transport.request.dto.RegionDto;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@Transactional
@SpringBootTest(classes = RequestApplication.class)
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка сервиса статистики")
class StatsServiceTest  extends SharedTest {
    
    @Autowired
    private StatsService statsService;
    
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
    private ContractorRepository contractorRepository;
    
    @Autowired
    private TaxiTariffRepository taxiTariffRepository;
    
    @MockitoBean
    private RegionDataResolver regionDataResolver;
    
    @MockitoSpyBean
    private ReservationService reservationService;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @BeforeEach
    public void init() {
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
    
        var contractor = Contractor.builder()
                                          .id(CONTRACTOR_ID_1)
                                          .name("name")
                                          .contractorName("name")
                                          .contractorRusName("rusname")
                                          .integrationEmail("aaa@bbb.ru")
                                          .build();
        contractorRepository.save(contractor);
    
        var taxiTariff = TaxiTariff.builder()
                                          .id(TARIFF_ID_1)
                                          .contractorId(contractor.getId())
                                          .workGroup("Work group")
                                          .regionId(UUID.randomUUID())
                                          .humanReadableId("TT-123-23")
                                          .triggerTime(TRIGGER_TIME)
                                          .taxiClass(TaxiClass.ECONOMY)
                                          .rideCostPerKm(1)
                                          .rideCostPerMin(2)
                                          .waitCostPerMin(3)
                                          .departmentId(UUID.randomUUID())
                                          .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                                          .transportType(TransportTypeEnum.TAXI)
                                          .build();
        taxiTariffRepository.save(taxiTariff);

        var regionDto = RegionDto.builder().code("1").id(UUID.randomUUID()).name("moscow").build();
        when(regionDataResolver.getRegion(any())).thenReturn(regionDto);
        doNothing().when(reservationService).cancel(any());
        
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        request2.setStatus(TripRequestStatus.TAXI_TRIP_FINISHED);
        request2.setOrganizationId(organization1.getId());
        request2.setContractorId(contractor.getId());
        request4.setOrganizationId(organization1.getId());
        requestRepository.save(request2);
        requestRepository.save(request4);
    }
    
    @Test
    @DisplayName("Вызов сервиса статистики")
    void testStats() {
        List<StatsDTO> listStats = statsService.processStats();
        assertThat(listStats.getFirst().getTotalExecuted()).isEqualTo(1);
    }
}
