package ru.sberbank.ditsib.transport.reports.messaging;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.trip.TripRatingMessage;
import ru.sberbank.ditsib.transport.reports.dao.*;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.RequestRating;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка получения рейтинга поездок")
@MockitoBean(types = JwtDecoder.class)
class RequestRatingListenerTest  extends SharedTest {

    @Autowired
    private Consumer<Message<TripRatingMessage>> tripRatingInput;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private ContractorRepository contractorRepository;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private TaxiTariffRepository taxiTariffRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private TripPurposeRepository tripPurposeRepository;
    @Autowired
    private WaypointRepository waypointRepository;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private SingleTaxiTripRepository singleRepository;
    @Autowired
    private CoopTaxiTripRepository coopTaxiTripRepository;
    @Autowired
    private SharedRideRepository sharedRideRepository;

    @Autowired
    private TaxiTripRepository tripRepository;
    @Autowired
    private OrderKpiRepository orderKpiRepository;
    @Autowired
    private SharedRequestKpiRepository sharedRequestKpiRepository;
    @Autowired
    private TaxiTripRegistryRepository taxiTripRegistryRepository;

    @SneakyThrows
    @BeforeEach
    public void setUp() {
        testEmployee1.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_1);
        testEmployee2.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_2);
        testEmployee3.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_3);
        testEmployee4.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_4);
        testEmployee5.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_5);

        request1.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_1);
        request2.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_2);
        request3.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_3);
        request4.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_4);
        request5.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_5);

        organizationRepository.saveAndFlush(organization1);
        organizationRepository.saveAndFlush(organization2);
        organizationRepository.saveAndFlush(organization3);
        organizationRepository.saveAndFlush(organization5);

        departmentRepository.saveAndFlush(department1);
        departmentRepository.saveAndFlush(department2);
        departmentRepository.saveAndFlush(department3);
        departmentRepository.saveAndFlush(department5);

        positionRepository.save(testPosition1);
        positionRepository.save(testPosition2);
        positionRepository.save(testPosition3);

        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(testEmployee3);
        employeeRepository.save(testEmployee4);
        employeeRepository.save(testEmployee5);

        contractorRepository.save(contractor1);
        contractorRepository.save(contractor2);
        contractorRepository.save(carsharingContractor);
        contractRepository.save(contract1);
        contractRepository.save(contract2);
        taxiTariffRepository.save(taxiTariff1);

        addressRepository.saveAndFlush(address1);
        addressRepository.saveAndFlush(address2);
        addressRepository.saveAndFlush(address3);
        addressRepository.saveAndFlush(address4);

        tripPurposeRepository.save(tripPurpose1);
        tripPurposeRepository.save(tripPurpose3);
        tripPurposeRepository.save(tripPurpose4);
        tripPurposeRepository.save(tripPurpose5);

        waypointRepository.saveAll(waypoints1);
        waypointRepository.saveAll(waypoints2);
        waypointRepository.saveAll(waypoints3);
        waypointRepository.saveAll(waypoints4);
        waypointRepository.saveAll(waypoints5);

        sharedRideRepository.save(sharedRide1);

        requestRepository.save(request3);
        requestRepository.save(request4);

        singleRepository.save(singleTaxiTrip1);
        coopTaxiTripRepository.save(coopTaxiTrip1);
    }
    @AfterEach
    public void tearDown() {
        tripRepository.deleteAll();
        requestRepository.deleteAll();
        waypointRepository.deleteAll();
        addressRepository.deleteAll();
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        positionRepository.deleteAll();
        tripPurposeRepository.deleteAll();
        sharedRideRepository.deleteAll();
        taxiTariffRepository.deleteAll();
        organizationRepository.deleteAll();
        orderKpiRepository.deleteAll();
        sharedRequestKpiRepository.deleteAll();
        taxiTripRegistryRepository.deleteAll();
        contractRepository.deleteAll();
        contractorRepository.deleteAll();
    }

    @Test
    @DisplayName("Получение рейтинга поездки")
    void handleRequestMessageTest(){
        TripRatingMessage tripRatingMessage = new TripRatingMessage();
        tripRatingMessage.setRequestId(request3.getId());
        tripRatingMessage.setRating(10);
        tripRatingMessage.setRatingComment("You are amazing");
        
        tripRatingInput.accept(MessageBuilder.withPayload(tripRatingMessage).build());
        Request request = requestRepository.findById(tripRatingMessage.getRequestId()).get();
        RequestRating requestRating = request.getRequestRating();

        assertEquals(tripRatingMessage.getRating(), requestRating.getRating());
        assertEquals(tripRatingMessage.getRatingComment(), requestRating.getRatingComment());
    }

}
