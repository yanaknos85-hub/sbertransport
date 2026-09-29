package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.reports.dao.SharedRideRepository;
import ru.sberbank.ditsib.transport.reports.dao.TransportCompensationRepository;
import ru.sberbank.ditsib.transport.reports.dao.WaypointRepository;
import ru.sberbank.ditsib.transport.reports.mappers.DriverMapper;
import ru.sberbank.ditsib.transport.reports.mappers.EntityDTOMapper;
import ru.sberbank.ditsib.transport.reports.mappers.VehicleMapper;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.service.*;

import java.util.Optional;

import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@Slf4j
@DisplayName("Проверка нового поиска заявок")
@MockitoBean(types = JwtDecoder.class)
@ExtendWith(MockitoExtension.class)
class TripRequestServiceUnitTest {
    
    @Mock
    private RequestService requestService;
    @Mock
    private AddressService addressService;
    @Mock
    private TripPurposeService purposeService;
    @Mock
    private PersonalCarService personalCarService;
    @Mock
    private EntityDTOMapper mapper;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private WaypointRepository waypointRepository;
    @Mock
    private TransportCompensationRepository transportCompensationRepository;
    @Mock
    private ContractorService contractorService;
    @Mock
    private SharedRideRepository sharedRideRepository;
    @Mock
    private VehicleMapper vehicleMapper;
    @Mock
    private DriverMapper driverMapper;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private PaymentService paymentService;
    
    @InjectMocks
    private TripRequestServiceImpl tripRequestService;
    
    @Test
    @Disabled("Необходимо подобрать тестовые данные")
    void shoulBe_success() {
        RequestMessage message = Instancio.of(RequestMessage.class)
                                          .set(field("deleted"), false)
                                          .set(field("transportType"), "TAXI")
                                          .set(field("status"), "TAXI_APPROVED")
                                          .ignore(field("transportCompensation"))
                                          .ignore(field("tariffId"))
                                          .ignore(field("passenger"))
                                          .create();
        Request request = Instancio.of(Request.class)
                                   .set(field("transportType"), "TAXI")
                                   .set(field("status"), "TAXI_AWAITING_APPROVAL")
                                   .create();
        
        when(requestService.findById(message.getId())).thenReturn(Optional.ofNullable(request));
        when(paymentService.fillPaymentData(request)).thenReturn(request);
        tripRequestService.processMessage(message);
        verify(requestService).findById(message.getId());
    }
    
    @Test
    void shouldBe_noInteractions_whenRequestMessageisDeleted_true() {
        RequestMessage message = Instancio.of(RequestMessage.class)
                                          .set(field("deleted"), true)
                                          .create();
        tripRequestService.processMessage(message);
        verifyNoInteractions(requestService);
    }
    
    @Test
    void shouldBe_noInteractions_whenRequestMessageTransportType_unknown() {
        RequestMessage message = Instancio.of(RequestMessage.class)
                                          .set(field("deleted"), false)
                                          .set(field("transportType"), "boo")
                                          .create();
        tripRequestService.processMessage(message);
        verifyNoInteractions(requestService);
    }
    
    @Test
    void shouldBe_noInteractions_whenRequestMessageStatus_isBeforeCurrentStatus() {
        RequestMessage message = Instancio.of(RequestMessage.class)
                                          .set(field("deleted"), false)
                                          .set(field("transportType"), "TAXI")
                                          .set(field("status"), "TAXI_AWAITING_APPROVAL")
                                          .ignore(field("transportCompensation"))
                                          .create();
        Request request = Instancio.of(Request.class)
                                   .set(field("transportType"), "TAXI")
                                   .set(field("status"), "TAXI_TRIP_FINISHED")
                                   .create();
    
        when(requestService.findById(message.getId())).thenReturn(Optional.ofNullable(request));
        tripRequestService.processMessage(message);
        verifyNoInteractions(employeeService);
    }
    
}
