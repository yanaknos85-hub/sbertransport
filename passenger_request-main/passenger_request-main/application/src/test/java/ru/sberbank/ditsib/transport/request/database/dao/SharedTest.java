package ru.sberbank.ditsib.transport.request.database.dao;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.ResponseEntity;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.constants.*;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sberbank.ditsib.transport.exceptions.NotImplementedException;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingJoinRequest;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingJoinRequestText;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.database.model.corp.Position;
import ru.sberbank.ditsib.transport.request.database.model.deadline.DeadlineSettings;
import ru.sberbank.ditsib.transport.request.database.model.driversData.Driver;
import ru.sberbank.ditsib.transport.request.database.model.magenta.OrderKpi;
import ru.sberbank.ditsib.transport.request.database.model.magenta.SharedRideKPI;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.TransportCompensation;
import ru.sberbank.ditsib.transport.request.dto.CoordinatesDTO;
import ru.sberbank.ditsib.transport.request.dto.RouteSegmentDTO;
import ru.sberbank.ditsib.transport.request.dto.WaypointDTO;
import ru.sberbank.ditsib.transport.request.shared.DeadlineSettingsSharedData;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

import static ru.sberbank.ditsib.transport.constants.CarsharingClass.COMFORT;
import static ru.sberbank.ditsib.transport.constants.CarsharingClass.ECONOMY;
import static ru.sberbank.ditsib.transport.constants.CarsharingJoinRequestStatus.UNDER_CONSIDERATION;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.*;
import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.*;

public abstract class SharedTest extends KafkaTest {
    public static final String ADDRESS_STRING_1 = "Невский проспект, округ Лиговка-Ямская";
    public static final double LATITUDE_1 = 59.923854;
    public static final double LONGITUDE_1 = 30.384503;
    
    public static final String ADDRESS_STRING_2 = "Загородный проспект 3, Владимирский округ";
    public static final double LATITUDE_2 = 59.927761;
    public static final double LONGITUDE_2 = 30.345835;
    
    public static final double LATITUDE_3 = 59.925854;
    public static final double LONGITUDE_3 = 30.354503;
    
    //Данные для теста сотрудников
    public static final String EMPLOYEE_NAME1 = "Петр";
    public static final String EMPLOYEE_NAME2 = "BBB";
    public static final String EMPLOYEE_NAME3 = "Анатолий";
    public static final String EMPLOYEE_NAME4 = "Александр";
    public static final String EMPLOYEE_LASTNAME1 = "Семенов";
    public static final String EMPLOYEE_LASTNAME2 = "DDD";
    public static final String EMPLOYEE_LASTNAME3 = "ААААААА";
    public static final String EMPLOYEE_LASTNAME4 = "Пушкин";
    public static final String EMPLOYEE_PATRONYMIC1 = "Иванович";
    public static final String EMPLOYEE_PATRONYMIC2 = "FFF";
    public static final String EMPLOYEE_PATRONYMIC3 = "ВВВВВВ";
    public static final String EMPLOYEE_PATRONYMIC4 = "Сергеевич";
    public static final String PERSONNEL_NUMBER_1 = "0987654";
    public static final String PERSONNEL_NUMBER_2 = "9876543";
    public static final String PERSONNEL_NUMBER_3 = "98765111";
    public static final String PERSONNEL_NUMBER_4 = "0875648";
    public static final String EMPLOYEE_NAME6 = "Иван";
    public static final String EMPLOYEE_LASTNAME6 = "Крузенштерн";
    public static final String EMPLOYEE_PATRONYMIC6 = "Фёдорович";
    public static final String PERSONNEL_NUMBER_6 = "193544455";
    public static final String EMPLOYEE_NAME7 = "Антон";
    public static final String EMPLOYEE_LASTNAME7 = "Чехов";
    public static final String EMPLOYEE_PATRONYMIC7 = "Павлович";
    public static final String PERSONNEL_NUMBER_7 = "948316851";
    public static final String EMPLOYEE_NAME8 = "Михаил";
    public static final String EMPLOYEE_LASTNAME8 = "Лермонтов";
    public static final String EMPLOYEE_PATRONYMIC8 = "Юрьевич";
    public static final String PERSONNEL_NUMBER_8 = "6418684687";
    
    
    public static final String EMPLOYEE_ID_1 = "6f6e278a-b055-4359-8d17-09a3955e12a1";
    public static final String EMPLOYEE_ID_2 = "2798178f-27ca-4597-8d38-a3bcaaea71ed";
    public static final String EMPLOYEE_ID_3 = "3398178f-27ca-4597-8d38-a3bcaaea71ed";
    public static final String EMPLOYEE_ID_4 = "96eeb5e4-7360-45b0-889d-c96b866ee4f7";
    public static final String EMPLOYEE_ID_6 = "96eeb5e6-7366-45b0-889d-c96b866ee4f6";
    public static final String EMPLOYEE_ID_7 = "f975bada-b383-4be7-a5b7-1fbe3341c61c";
    public static final String EMPLOYEE_ID_8 = "aefd5607-878f-4fd7-89fe-377296aed33b";
    
    public static final String HUMAN_READABLE_EMPLOYEE_ID_1 = "US-0001-1";
    public static final String HUMAN_READABLE_EMPLOYEE_ID_2 = "US-0001-2";
    public static final String HUMAN_READABLE_EMPLOYEE_ID_3 = "US-0002-1";
    public static final String HUMAN_READABLE_EMPLOYEE_ID_4 = "US-0002-2";
    public static final String HUMAN_READABLE_EMPLOYEE_ID_6 = "US-0002-6";
    public static final String HUMAN_READABLE_EMPLOYEE_ID_7 = "US-0003-7";
    public static final String HUMAN_READABLE_EMPLOYEE_ID_8 = "US-0003-8";
    
    //Данные для теста должностей
    public static final String POSITION1_NAME = "Клерк";
    public static final String POSITION2_NAME = "Босс";
    public static final String POSITION3_NAME = "Админ";
    
    
    public static final String USER1_ID = "f10bcc5b-51db-4e1c-a747-2a229604f974";
    public static final String USER2_ID = "558d39f2-c638-490f-90e9-94d896a65b4c";
    public static final String USER3_ID = "558d39f2-c638-4911-90e9-94d896a65b4c";
    public static final String USER4_ID = "18cedb32-04d7-4c36-ad2a-8145e1619fe7";
    public static final String USER6_ID = "18cedb36-04d6-4c36-ad2a-8145e1619fe6";
    public static final String USER7_ID = "2b601ee1-f03a-4bed-abfe-edb7160f62f4";
    public static final String USER8_ID = "e8d5309d-2c05-4b46-b88a-f62db13e70d3";
    
    protected static final UUID userId = UUID.fromString(USER1_ID);
    protected static final UUID userId2 = UUID.fromString(USER2_ID);
    protected static final UUID userId3 = UUID.fromString(USER3_ID);
    protected static final UUID userId4 = UUID.fromString(USER4_ID);
    protected static final UUID userId6 = UUID.fromString(USER6_ID);
    protected static final UUID userId7 = UUID.fromString(USER7_ID);
    protected static final UUID userId8 = UUID.fromString(USER8_ID);
    
    public static final String REQUEST_COMMENT_1 = "Comment AAA";
    public static final String REQUEST_COMMENT_2 = "Comment AAA BBB";
    public static final String REQUEST_COMMENT_3 = "Comment bbb CCC";
    public static final String REQUEST_COMMENT_4 = "Comment CCC ddd";
    
    public static final Integer MAGENTA_ID_1 = 123456;
    
    protected static final UUID TARIFF_ID_1 = UUID.fromString("f2459553-9e91-4e32-8e9c-efe4e90c74da");
    protected static final UUID TARIFF_ID_2 = UUID.fromString("ea808b1d-953c-4c46-ae4f-d54d8c28dfc0");
    protected static final UUID PERSONAL_CAR_ID = UUID.fromString("f2459553-8e70-3e21-8e9c-efe4e90c74da");
    protected static final Integer TRIGGER_TIME = 1000;
    
    protected static final UUID CONTRACTOR_ID_1 = UUID.fromString("ad31b825-7f20-4277-8a15-33ec1ce1ad44");
    protected static final UUID CONTRACTOR_ID_2 = UUID.fromString("77336484-7dcd-45f1-aa90-35219a175d5b");
    protected static final UUID CONTRACTOR_ID_3 = UUID.randomUUID();
    protected static final UUID CONTRACTOR_ID_4 = UUID.randomUUID();
    
    protected static final UUID DISPATCHER_ID_1 = UUID.randomUUID();
    
    protected static final UUID DRIVER_ID_1 = UUID.randomUUID();
    
    protected ExpectedData expectedData1;
    protected ExpectedData expectedData2;
    protected Address address1;
    protected Address address2;
    protected Address address3;
    
    protected final List<Waypoint> waypoints1 = new ArrayList<>();
    protected final List<Waypoint> waypoints2 = new ArrayList<>();
    protected final List<Waypoint> waypoints3 = new ArrayList<>();
    protected final List<Waypoint> waypoints4 = new ArrayList<>();
    protected final List<Waypoint> waypoints5 = new ArrayList<>();
    protected final List<Waypoint> waypoints6 = new ArrayList<>();
    protected final List<Waypoint> waypoints7 = new ArrayList<>();
    protected final List<Waypoint> waypoints8 = new ArrayList<>();
    protected final List<Waypoint> waypoints9 = new ArrayList<>();
    protected final List<Waypoint> waypoints10 = new ArrayList<>();
    protected final List<Waypoint> waypoints11 = new ArrayList<>();
    
    protected final List<RouteSegmentDTO> routeSegments1 = new ArrayList<>();
    protected final List<RouteSegmentDTO> routeSegments2 = new ArrayList<>();
    protected final List<RouteSegmentDTO> routeSegments3 = new ArrayList<>();
    protected final List<RouteSegmentDTO> routeSegments4 = new ArrayList<>();
    protected final List<RouteSegmentDTO> routeSegments5 = new ArrayList<>();
    protected final List<RouteSegmentDTO> routeSegments6 = new ArrayList<>();
    protected final List<RouteSegmentDTO> routeSegments7 = new ArrayList<>();
    protected final List<RouteSegmentDTO> routeSegments8 = new ArrayList<>();
    protected final List<RouteSegmentDTO> routeSegments9 = new ArrayList<>();
    protected final List<RouteSegmentDTO> routeSegments10 = new ArrayList<>();
    protected final List<RouteSegmentDTO> routeSegments11 = new ArrayList<>();
    protected final List<RouteSegmentDTO> routeSegments12 = new ArrayList<>();
    
    protected CoordinatesDTO coordinates1;
    protected CoordinatesDTO coordinates2;
    protected CoordinatesDTO coordinates3;
    protected Organization organization1;
    protected Department department1;
    protected Position testPosition1;
    protected Position testPosition2;
    protected Position testPosition3;
    
    protected Employee testEmployee1;
    protected Employee testEmployee2;
    protected Employee testEmployee3;
    protected Employee testEmployee4;
    protected Employee testEmployee5;
    protected Employee testEmployee6;
    protected Employee testEmployee7;
    protected Employee testEmployee8;
    
    protected Driver driver1;
    
    protected RequestForTaxi request1;
    protected RequestForTaxi request2;
    protected RequestForTaxi request3;
    protected RequestForPersonal request4;
    protected RequestForPublic publicRequest;
    protected RequestForPersonal request6;
    protected RequestForCarsharing request7;
    protected RequestForCarsharing request8;
    protected RequestForPublic request9;
    protected RequestForPublic request10;
    protected RequestForPublic request11;
    protected CarsharingJoinRequest request12;
    
    protected SingleTaxiTrip taxiTrip1;
    
    protected SingleTaxiTrip taxiTrip2;
    
    protected SingleTaxiTrip taxiTrip3;
    
    protected CarsharingJoinRequestText requestText1;
    
    //protected MagentaSharedRequest magentaSharedRequest;
    protected UUID rideId = UUID.randomUUID();
    protected SharedRideKPI sharedRideKPI;
    protected OrderKpi orderKpi1;
    protected OrderKpi orderKpi2;
    
    public static final String HUMAN_READABLE_REQUEST_ID_1 = "TT-0001-1";
    public static final String HUMAN_READABLE_REQUEST_ID_2 = "TT-0001-2";
    public static final String HUMAN_READABLE_REQUEST_ID_3 = "TT-0001-3";
    public static final String HUMAN_READABLE_REQUEST_ID_4 = "OT-0002-2";
    public static final String HUMAN_READABLE_REQUEST_ID_5 = "OT-0002-5";
    public static final String HUMAN_READABLE_REQUEST_ID_6 = "OT-0002-6";
    public static final String HUMAN_READABLE_REQUEST_ID_7 = "OT-0003-7";
    public static final String HUMAN_READABLE_REQUEST_ID_8 = "OT-0003-8";
    public static final String HUMAN_READABLE_REQUEST_ID_9 = "OT-0003-9";
    public static final String HUMAN_READABLE_REQUEST_ID_10 = "OT-0003-10";
    public static final String HUMAN_READABLE_REQUEST_ID_11 = "OT-0003-11";
    
    protected static final UUID purposeId1 = UUID.randomUUID();
    protected static final UUID purposeId2 = UUID.randomUUID();
    protected static final String PURPOSE_DESCRIPTION_1 = "new purpose 1";
    protected static final String PURPOSE_DESCRIPTION_2 = "new purpose 2";
    
    private List<TransportCompensation> cityTripCompensationList;
    
    protected DeadlineSettingsSharedData deadlineSettingsSharedData;
    protected DeadlineSettings deadlineSettings;
    protected ZoneId zoneId;
    
    @BeforeEach
    public void initValues() {
        organization1 = Organization.builder().id(UUID.randomUUID()).digitId(1L).build();
        department1 = Department.builder()
                                .id(UUID.randomUUID())
                                .organization(organization1)
                                .location("Location")
                                .departmentName("Department name")
                                .build();
        
        testPosition1 = Position.builder()
                                .id(UUID.randomUUID())
                                .organizationId(organization1.getId())
                                .availableClasses(Set.of(TaxiClass.ECONOMY, TaxiClass.COMFORT))
                                .positionName(POSITION1_NAME)
                                .selfApproved(false).build();
        
        testPosition2 = Position.builder()
                                .id(UUID.randomUUID())
                                .organizationId(organization1.getId())
                                .availableClasses(Set.of(TaxiClass.ECONOMY, TaxiClass.COMFORT, TaxiClass.BUSINESS))
                                .positionName(POSITION2_NAME)
                                .selfApproved(true).build();
        
        testPosition3 = Position.builder()
                                .id(UUID.randomUUID())
                                .organizationId(organization1.getId())
                                .availableClasses(Set.of(TaxiClass.ECONOMY, TaxiClass.COMFORT))
                                .positionName(POSITION3_NAME)
                                .selfApproved(false).build();
        
        request6 = (RequestForPersonal) createRequest(expectedData1, testEmployee6, testEmployee6, PERSONAL, null, null,
                                                      LocalDateTime.now(ZoneOffset.UTC), null,
                                                      LocalDateTime.now(ZoneOffset.UTC).plusHours(3), REQUEST_COMMENT_4,
                                                      TripPurpose.builder().id(purposeId2).purpose(PURPOSE_DESCRIPTION_2).build(), TARIFF_ID_1,
                                                      PERSONAL_AWAITING_APPROVAL, false, PERSONAL_CAR_ID, rideId, null, null);
        
        address1 = Address.builder().latitude(LATITUDE_1).longitude(LONGITUDE_1)
                          .structure("Structure 1")
                          .street("Улица  ** Мещерякова rdd")
                          .region("Region 1")
                          .house("14/9")
                          .country("Country 1")
                          .city("Москва")
                          .building("Building 1")
                          .existInVspGosbTbRegistry(false)
                          .build();
        address2 = Address.builder().latitude(LATITUDE_2).longitude(LONGITUDE_2)
                          .structure("Structure 2")
                          .street("Street 2")
                          .region("Region 2")
                          .house("24 k1")
                          .country("Country 2")
                          .city("City 2")
                          .building("Building 2")
                          .existInVspGosbTbRegistry(false).build();
        address3 = Address.builder().latitude(LATITUDE_3).longitude(LONGITUDE_3)
                          .structure("Structure 3")
                          .street("Черногорская улица")
                          .region("Region 3")
                          .house("8")
                          .country("Country 3")
                          .city("Кострома")
                          .building("Building 3")
                          .existInVspGosbTbRegistry(false)
                          .build();
        
        coordinates1 = CoordinatesDTO.builder().latitude(LATITUDE_1).longitude(LONGITUDE_1).build();
        coordinates2 = CoordinatesDTO.builder().latitude(LATITUDE_2).longitude(LONGITUDE_3).build();
        coordinates3 = CoordinatesDTO.builder().latitude(LATITUDE_3).longitude(LONGITUDE_3).build();
        
        expectedData1 = new ExpectedData();
        expectedData1.setTime(Duration.ofMinutes(30));
        expectedData1.setCost(200d);
        expectedData1.setDistance(30d);
        
        expectedData2 = new ExpectedData();
        expectedData2.setTime(Duration.ofMinutes(15));
        expectedData2.setCost(150d);
        expectedData2.setDistance(25d);
        
        testEmployee1 = new Employee();
        testEmployee1.setId(UUID.fromString(EMPLOYEE_ID_1));
        testEmployee1.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_1);
        testEmployee1.setUserId(UUID.fromString(USER1_ID));
        testEmployee1.setFirstName(EMPLOYEE_NAME1);
        testEmployee1.setLastName(EMPLOYEE_LASTNAME1);
        testEmployee1.setPatronymic(EMPLOYEE_PATRONYMIC1);
        testEmployee1.setPersonnelNumber(PERSONNEL_NUMBER_1);
        testEmployee1.setDepartment(department1);
        testEmployee1.setMobilePhone("+78005553535");
        testEmployee1.setPositionId(testPosition1.getId());
        
        testEmployee2 = new Employee();
        testEmployee2.setId(UUID.fromString(EMPLOYEE_ID_2));
        testEmployee2.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_2);
        testEmployee2.setMobilePhone("+79996661313");
        testEmployee2.setUserId(UUID.fromString(USER2_ID));
        testEmployee2.setFirstName(EMPLOYEE_NAME2);
        testEmployee2.setLastName(EMPLOYEE_LASTNAME2);
        testEmployee2.setPatronymic(EMPLOYEE_PATRONYMIC2);
        testEmployee2.setPersonnelNumber(PERSONNEL_NUMBER_2);
        testEmployee2.setDepartment(department1);
        testEmployee2.setPositionId(testPosition2.getId());
        
        testEmployee3 = new Employee();
        testEmployee3.setId(UUID.fromString(EMPLOYEE_ID_3));
        testEmployee3.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_3);
        testEmployee3.setMobilePhone("+73333333333");
        testEmployee3.setUserId(UUID.fromString(USER3_ID));
        testEmployee3.setFirstName(EMPLOYEE_NAME3);
        testEmployee3.setLastName(EMPLOYEE_LASTNAME3);
        testEmployee3.setPatronymic(EMPLOYEE_PATRONYMIC3);
        testEmployee3.setPersonnelNumber(PERSONNEL_NUMBER_3);
        testEmployee3.setDepartment(department1);
        testEmployee3.setPositionId(testPosition3.getId());
        testEmployee3.setSupervisorId(testEmployee2.getId());
        
        testEmployee4 = new Employee();
        testEmployee4.setId(UUID.fromString(EMPLOYEE_ID_4));
        testEmployee4.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_4);
        testEmployee4.setUserId(UUID.fromString(USER4_ID));
        testEmployee4.setFirstName(EMPLOYEE_NAME4);
        testEmployee4.setLastName(EMPLOYEE_LASTNAME4);
        testEmployee4.setPatronymic(EMPLOYEE_PATRONYMIC4);
        testEmployee4.setPersonnelNumber(PERSONNEL_NUMBER_4);
        testEmployee4.setDepartment(department1);
        testEmployee4.setPositionId(testPosition3.getId());
        testEmployee4.setSupervisorId(testEmployee2.getId());
        testEmployee4.setItinerantType(ItinerantType.FULL);
        
        testEmployee5 = new Employee();
        testEmployee5.setId(UUID.fromString(EMPLOYEE_ID_4));
        testEmployee5.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_4);
        testEmployee5.setUserId(UUID.fromString(USER4_ID));
        testEmployee5.setFirstName(EMPLOYEE_NAME4);
        testEmployee5.setLastName(EMPLOYEE_LASTNAME4);
        testEmployee5.setPatronymic(EMPLOYEE_PATRONYMIC4);
        testEmployee5.setPersonnelNumber(PERSONNEL_NUMBER_4);
        testEmployee5.setDepartment(department1);
        testEmployee5.setPositionId(testPosition3.getId());
        testEmployee5.setSupervisorId(testEmployee2.getId());
        testEmployee5.setCostCenter("9900L11050");
        
        testEmployee6 = new Employee();
        testEmployee6.setId(UUID.fromString(EMPLOYEE_ID_6));
        testEmployee6.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_6);
        testEmployee6.setUserId(UUID.fromString(USER6_ID));
        testEmployee6.setFirstName(EMPLOYEE_NAME6);
        testEmployee6.setLastName(EMPLOYEE_LASTNAME6);
        testEmployee6.setPatronymic(EMPLOYEE_PATRONYMIC6);
        testEmployee6.setPersonnelNumber(PERSONNEL_NUMBER_6);
        testEmployee6.setDepartment(department1);
        testEmployee6.setPositionId(testPosition3.getId());
        testEmployee6.setSupervisorId(testEmployee6.getId());
        testEmployee6.setCostCenter("9900L11040");
        
        testEmployee7 = new Employee();
        testEmployee7.setId(UUID.fromString(EMPLOYEE_ID_7));
        testEmployee7.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_7);
        testEmployee7.setUserId(UUID.fromString(USER7_ID));
        testEmployee7.setFirstName(EMPLOYEE_NAME7);
        testEmployee7.setLastName(EMPLOYEE_LASTNAME7);
        testEmployee7.setPatronymic(EMPLOYEE_PATRONYMIC7);
        testEmployee7.setPersonnelNumber(PERSONNEL_NUMBER_7);
        testEmployee7.setDepartment(department1);
        testEmployee7.setPositionId(testPosition3.getId());
        testEmployee7.setSupervisorId(testEmployee7.getId());
        
        testEmployee8 = new Employee();
        testEmployee8.setId(UUID.fromString(EMPLOYEE_ID_8));
        testEmployee8.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_8);
        testEmployee8.setUserId(UUID.fromString(USER8_ID));
        testEmployee8.setFirstName(EMPLOYEE_NAME8);
        testEmployee8.setLastName(EMPLOYEE_LASTNAME8);
        testEmployee8.setPatronymic(EMPLOYEE_PATRONYMIC8);
        testEmployee8.setPersonnelNumber(PERSONNEL_NUMBER_8);
        testEmployee8.setDepartment(department1);
        testEmployee8.setPositionId(testPosition3.getId());
        testEmployee8.setSupervisorId(testEmployee8.getId());
        
        driver1 = new Driver();
        
        cityTripCompensationList = List.of(
                TransportCompensation
                        .builder()
                        .compensationType(PublicCompensationType.CITY_TRIP_COMPENSATION)
                        .transportType(PublicTransportType.CITY_BUS)
                        .ticketsCost(350)
                        .ticketsCount(1)
                        .build()
                                          );
        
        taxiTrip1 = SingleTaxiTrip.builder()
                                  .tripType(TripType.SINGLE)
                                  .dateTimeRegistered(LocalDateTime.now())
                                  .organizationId(organization1.getId())
                                  .tariffId(TARIFF_ID_1)
                                  .tripFinishTime(LocalDateTime.now().plusHours(2))
                                  .tripStartTime(LocalDateTime.now())
                                  .requests(Arrays.asList(request1))
                                  .status(InboundTaxiTripStatus.ORDER_FINISHED)
                                  .taxiId(UUID.randomUUID().toString())
                                  .tripFactDistance(1d)
                                  .tripFactDuration(Duration.ofHours(1))
                                  .tripFactPrice(100)
                                  .tripFactWaitTime(Duration.ofSeconds(100))
                                  .tripAssignmentDateTime(LocalDateTime.now())
                                  .active(true)
                                  .humanReadableId("US-0003-9")
                                  .contractorComment("comment")
                                  .resolution("Произвольное описание работ")
                                  .decisionCode(TaxiTripDecisionCode.FULLY_RESOLVED)
                                  .assignedCar(CarInfo.builder()
                                                      .brandName("BMW")
                                                      .model("X6")
                                                      .color("Black")
                                                      .registrationNumber("A888XY163RUS")
                                                      .build())
                                  .timeWorkStart(LocalDateTime.now())
                                  .timeWorkFinish(LocalDateTime.now().plusHours(2))
                                  .lastXmlReceivedDateTime(LocalDateTime.now())
                                  .build();
        
        taxiTrip2 = SingleTaxiTrip.builder()
                                  .tripType(TripType.SINGLE)
                                  .dateTimeRegistered(LocalDateTime.now())
                                  .organizationId(organization1.getId())
                                  .tariffId(TARIFF_ID_1)
                                  .tripFinishTime(LocalDateTime.now().plusHours(2))
                                  .tripStartTime(LocalDateTime.now())
                                  .requests(Arrays.asList(request2))
                                  .status(InboundTaxiTripStatus.ORDER_FINISHED)
                                  .taxiId(UUID.randomUUID().toString())
                                  .tripFactDistance(1d)
                                  .tripFactDuration(Duration.ofHours(1))
                                  .tripFactPrice(100)
                                  .tripFactWaitTime(Duration.ofSeconds(100))
                                  .tripAssignmentDateTime(LocalDateTime.now())
                                  .active(true)
                                  .humanReadableId("US-0003-10")
                                  .contractorComment("comment")
                                  .resolution("Произвольное описание работ")
                                  .decisionCode(TaxiTripDecisionCode.FULLY_RESOLVED)
                                  .assignedCar(CarInfo.builder()
                                                      .brandName("BMW")
                                                      .model("X6")
                                                      .color("Black")
                                                      .registrationNumber("A888XY163RUS")
                                                      .build())
                                  .timeWorkStart(LocalDateTime.now())
                                  .timeWorkFinish(LocalDateTime.now().plusHours(2))
                                  .lastXmlReceivedDateTime(LocalDateTime.now())
                                  .build();
        
        taxiTrip3 = SingleTaxiTrip.builder()
                                  .tripType(TripType.SINGLE)
                                  .dateTimeRegistered(LocalDateTime.now())
                                  .organizationId(organization1.getId())
                                  .tariffId(TARIFF_ID_1)
                                  .tripFinishTime(LocalDateTime.now().plusHours(2))
                                  .tripStartTime(LocalDateTime.now())
                                  .requests(Arrays.asList(request3))
                                  .status(InboundTaxiTripStatus.ORDER_FINISHED)
                                  .taxiId(UUID.randomUUID().toString())
                                  .tripFactDistance(1d)
                                  .tripFactDuration(Duration.ofHours(1))
                                  .tripFactPrice(100)
                                  .tripFactWaitTime(Duration.ofSeconds(100))
                                  .tripAssignmentDateTime(LocalDateTime.now())
                                  .active(true)
                                  .humanReadableId("US-0003-11")
                                  .contractorComment("comment")
                                  .resolution("Произвольное описание работ")
                                  .decisionCode(TaxiTripDecisionCode.FULLY_RESOLVED)
                                  .assignedCar(CarInfo.builder()
                                                      .brandName("BMW")
                                                      .model("X6")
                                                      .color("Black")
                                                      .registrationNumber("A888XY163RUS")
                                                      .build())
                                  .timeWorkStart(LocalDateTime.now())
                                  .timeWorkFinish(LocalDateTime.now().plusHours(2))
                                  .lastXmlReceivedDateTime(LocalDateTime.now())
                                  .build();
        
        request1 = (RequestForTaxi) createRequest(expectedData1, testEmployee1, testEmployee2, TAXI, TaxiClass.ECONOMY, null,
                                                  LocalDateTime.now(ZoneOffset.UTC), null,
                                                  LocalDateTime.now(ZoneOffset.UTC).plusHours(4), REQUEST_COMMENT_1,
                                                  TripPurpose.builder().id(purposeId1).purpose(PURPOSE_DESCRIPTION_1).build(), TARIFF_ID_1,
                                                  TAXI_AWAITING_APPROVAL, false, null, null, null, null);
        
        request2 = (RequestForTaxi) createRequest(expectedData1, testEmployee2, testEmployee1, TAXI, TaxiClass.COMFORT, null,
                                                  LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())), null,
                                                  LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(3), REQUEST_COMMENT_2,
                                                  TripPurpose.builder().id(purposeId1).purpose(PURPOSE_DESCRIPTION_1).build(), TARIFF_ID_1,
                                                  TAXI_AWAITING_APPROVAL, false, null, null, null, null);
        
        request3 = (RequestForTaxi) createRequest(expectedData2, testEmployee3, testEmployee3, TAXI, TaxiClass.COMFORT, null,
                                                  LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())), null,
                                                  LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(3), REQUEST_COMMENT_3,
                                                  TripPurpose.builder().id(purposeId2).purpose(PURPOSE_DESCRIPTION_2).build(), TARIFF_ID_1,
                                                  TAXI_AWAITING_APPROVAL, true, null, null, null, null);
        
        request4 = (RequestForPersonal) createRequest(expectedData1, testEmployee4, testEmployee4, PERSONAL, null, null,
                                                      LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())), null,
                                                      LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(3), REQUEST_COMMENT_4,
                                                      TripPurpose.builder().id(purposeId2).purpose(PURPOSE_DESCRIPTION_2).build(), TARIFF_ID_1,
                                                      PERSONAL_AWAITING_APPROVAL, false, PERSONAL_CAR_ID, null, null, null);
        
        request6 = (RequestForPersonal) createRequest(expectedData1, testEmployee6, testEmployee6, PERSONAL, null, null,
                                                      LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())), null,
                                                      LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(3), REQUEST_COMMENT_4,
                                                      TripPurpose.builder().id(purposeId2).purpose(PURPOSE_DESCRIPTION_2).build(), TARIFF_ID_1,
                                                      PERSONAL_AWAITING_APPROVAL, false, PERSONAL_CAR_ID, rideId, null, null);
        
        request7 = (RequestForCarsharing) createRequest(expectedData1, testEmployee7, testEmployee7, CARSHARING, null, ECONOMY,
                                                        LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())), null,
                                                        LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(3), null,
                                                        TripPurpose.builder().id(purposeId2).purpose(PURPOSE_DESCRIPTION_2).build(), TARIFF_ID_1,
                                                        CARSHARING_AWAITING_APPROVAL, true, null, null, CONTRACTOR_ID_1, null);
        
        request8 = (RequestForCarsharing) createRequest(expectedData2, testEmployee8, testEmployee8, CARSHARING, null, COMFORT,
                                                        LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())), null,
                                                        LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(3), null,
                                                        TripPurpose.builder().id(purposeId2).purpose(PURPOSE_DESCRIPTION_2).build(), TARIFF_ID_1,
                                                        CARSHARING_AWAITING_APPROVAL, false, null, null, CONTRACTOR_ID_2, null);
        
        request9 = (RequestForPublic) createRequest(expectedData1, testEmployee5, testEmployee5, PUBLIC, null, null,
                                                    LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())), null,
                                                    LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(3), null,
                                                    TripPurpose.builder().id(purposeId2).purpose(PURPOSE_DESCRIPTION_2).build(), TARIFF_ID_1,
                                                    PUBLIC_AWAITING_APPROVAL, false, null, null, null, cityTripCompensationList);
        
        request10 = (RequestForPublic) createRequest(expectedData1, testEmployee5, testEmployee5, PUBLIC, null, null,
                                                     LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                                                     LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(3),
                                                     LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(3), null,
                                                     TripPurpose.builder().id(purposeId2).purpose(PURPOSE_DESCRIPTION_2).build(), TARIFF_ID_1,
                                                     PUBLIC_TRIP_CONFIRMATION, false, null, null, null, cityTripCompensationList);
        
        request11 = (RequestForPublic) createRequest(expectedData1, testEmployee5, testEmployee5, PUBLIC, null, null,
                                                     LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())), null,
                                                     LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(3), null,
                                                     TripPurpose.builder().id(purposeId2).purpose(PURPOSE_DESCRIPTION_2).build(), TARIFF_ID_1,
                                                     PUBLIC_AWAITING_AFFIRMATIVE, false, null, null, null, cityTripCompensationList);
        
        request12 = new CarsharingJoinRequest();
        request12.setRequestStatus(UNDER_CONSIDERATION);
        request12.setEmployee(testEmployee1);
        request12.setPhone("8927");
        request12.setEmail("mail@mail.com");
        request12.setHumanReadableId("123");
        request12.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        requestText1 = new CarsharingJoinRequestText();
        requestText1.setOrganizationId(UUID.randomUUID());
        
        publicRequest = (RequestForPublic) createRequest(expectedData1, testEmployee5, testEmployee5, PUBLIC, null, null,
                                                         LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())), null,
                                                         LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(3), null,
                                                         TripPurpose.builder().id(purposeId2).purpose(PURPOSE_DESCRIPTION_2).build(), TARIFF_ID_1,
                                                         PUBLIC_AWAITING_APPROVAL, false, null, null, null, cityTripCompensationList);
        
        List<List<RouteSegmentDTO>> segments =
                Arrays.asList(routeSegments1, routeSegments2, routeSegments3, routeSegments4, routeSegments5,
                              routeSegments6, routeSegments7, routeSegments8, routeSegments9, routeSegments10,
                              routeSegments11);
        List<List<Waypoint>> waypoints =
                Arrays.asList(waypoints1, waypoints2, waypoints3, waypoints4, waypoints5, waypoints6, waypoints7,
                              waypoints8, waypoints9, waypoints10, waypoints11);
        List<Request> requests = Arrays.asList(request1, request2, request3, request4, publicRequest, request6,
                                               request7, request8, request9, request10, request11);
        
        for (int i = 0; i < requests.size(); i++) {
            waypoints.get(i)
                     .add(Waypoint.builder().address(address1).request(requests.get(i)).orderingIndex(i).build());
            waypoints.get(i).add(Waypoint.builder().address(address2).request(requests.get(i)).orderingIndex(i).
                                         waitTime(Duration.ofMinutes(10)).build());
            waypoints.get(i).add(Waypoint.builder().address(address3).request(requests.get(i)).orderingIndex(i).
                                         waitTime(Duration.ofMinutes(5)).build());
            waypoints.get(i)
                     .add(Waypoint.builder().address(address1).request(requests.get(i)).orderingIndex(i).build());

            /*
            waypoints.get(i).add(Waypoint.builder().address(address1).orderingIndex(0).request(requests.get(i)).build());
            waypoints.get(i).add(Waypoint.builder().address(address2).orderingIndex(1).request(requests.get(i)).build());
            waypoints.get(i).add(Waypoint.builder().address(address3).orderingIndex(2).request(requests.get(i)).build());
            waypoints.get(i).add(Waypoint.builder().address(address1).orderingIndex(3).request(requests.get(i)).build());
            * */
            requests.get(i).getWaypoints().addAll(waypoints.get(i));
            
            RouteSegmentDTO segment1 = new RouteSegmentDTO();
            segment1.getCoordinates().add(coordinates1);
            segment1.getCoordinates().add(coordinates2);
            segment1.setTime(Duration.ofMinutes(15));
            segment1.setCost(5d);
            segment1.setDistance(499d);
            
            RouteSegmentDTO segment2 = new RouteSegmentDTO();
            segment2.getCoordinates().add(coordinates2);
            segment2.getCoordinates().add(coordinates3);
            segment2.setTime(Duration.ofMinutes(15));
            
            segments.get(i).addAll(Arrays.asList(segment1, segment2));
            requests.get(i).getSegmentsJSON().addAll(segments.get(i));
            
            deadlineSettingsSharedData = new DeadlineSettingsSharedData();
        }
    }
    
    protected Request createRequest(
            ExpectedData expectedData, Employee author, Employee passenger, TransportTypeEnum transportType,
            TaxiClass taxiClass, CarsharingClass carsharingClass, LocalDateTime creationTime, LocalDateTime approvalDate,
            LocalDateTime desiredDate, String commentForDriver, TripPurpose purpose, UUID tariffId, TripRequestStatus status,
            boolean isCoopTrip, UUID personalCarId, UUID rideId, UUID contractorId,
            List<TransportCompensation> compensationList
                                   ) {
        Request request = null;
        switch (transportType) {
            case TAXI -> {
                request = new RequestForTaxi();
                request.setTransportType(TAXI);
                ((RequestForTaxi) request).setTaxiClass(taxiClass);
                ((RequestForTaxi) request).setCommentForDriver(commentForDriver);
                ((RequestForTaxi) request).setContractorId(contractorId);
                ((RequestForTaxi) request).setDeadlineState(DeadlineState.values()[new Random().nextInt(DeadlineState.values().length)]);
                ((RequestForTaxi) request).setCoopTrip(isCoopTrip);
            }
            case PERSONAL -> {
                request = new RequestForPersonal();
                request.setTransportType(PERSONAL);
                ((RequestForPersonal) request).setPersonalCarId(personalCarId);
                //((RequestForPersonal) request).setMagentaSharedRequest(magentaSharedRequest);
                ((RequestForPersonal) request).setCoopTrip(isCoopTrip);
            }
            case CARSHARING -> {
                request = new RequestForCarsharing();
                ((RequestForCarsharing) request).setCarsharingClass(carsharingClass);
                ((RequestForCarsharing) request).setContractorId(contractorId);
                ((RequestForCarsharing) request).setCoopTrip(isCoopTrip);
                request.setTransportType(CARSHARING);
            }
            case PUBLIC -> {
                request = new RequestForPublic();
                request.setTransportType(PUBLIC);
                ((RequestForPublic) request).getTransportCompensation().addAll(compensationList);
            }
        }
        if (request != null) {
            request.setExpected(expectedData);
            request.setAuthor(author);
            request.setPassenger(passenger);
            request.setCreationTime(creationTime);
            request.setApprovalDate(approvalDate);
            request.setDesiredDate(desiredDate);
            request.setTimeZone("GMT+3");
            request.setHumanReadableId("HRU-" + UUID.randomUUID());
            request.setPurpose(purpose);
            request.setTariffId(tariffId);
            request.setOutcomeTariffId(tariffId);
            request.setStatus(status);
        } else {
            throw new NotImplementedException();
        }
        return request;
    }
    
    protected RouteSegmentDTO createSegmentDTO(double baseCoordinate) {
        return RouteSegmentDTO.builder()
                              .coordinates(Collections.singletonList(createCoordinateDTO(baseCoordinate)))
                              .cost(20.0)
                              .distance(30.0)
                              .time(Duration.ofHours(1))
                              .build();
    }
    
    protected CoordinatesDTO createCoordinateDTO(double baseCoordinate) {
        return CoordinatesDTO.builder().latitude(baseCoordinate + 1.0).longitude(baseCoordinate + 2.0).build();
    }
    
    protected WaypointDTO createWaypointDTO(String postfix, double baseCoordinate) {
        return WaypointDTO.builder()
                          .latitude(baseCoordinate + 1.0)
                          .longitude(baseCoordinate + 2.0)
                          .country("test county " + postfix)
                          .region("test region " + postfix)
                          .city("test city " + postfix)
                          .street("test street " + postfix)
                          .house("test house " + postfix)
                          .building("test building " + postfix)
                          .structure("test structure " + postfix)
                          .existInVspGosbTbRegistry(false)
                          .build();
    }
    
    protected ResponseEntity<String> loadMagentaAnswerMock(String fileName) {
        var filePath = Optional.ofNullable(getClass().getClassLoader().getResource("answers/magenta/" + fileName))
                               .map(URL::getFile).orElseThrow();
        try {
            return ResponseEntity.ok(FileUtils.readFileToString(new File(filePath), "UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    
}
