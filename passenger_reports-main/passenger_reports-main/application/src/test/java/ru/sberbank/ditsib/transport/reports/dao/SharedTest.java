package ru.sberbank.ditsib.transport.reports.dao;

import org.junit.jupiter.api.BeforeEach;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.*;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sberbank.ditsib.transport.reports.dto.NewTaxiTripRegistryDTO;
import ru.sberbank.ditsib.transport.reports.model.*;
import ru.sberbank.ditsib.transport.reports.model.magenta.OrderKpi;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRide;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRideKPI;
import ru.sberbank.ditsib.transport.reports.model.tariff.*;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.SingleTaxiTrip;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class SharedTest extends KafkaTest {
    
    public static final String EMPLOYEE_NAME1 = "Петр";
    public static final String EMPLOYEE_LASTNAME1 = "Семенов";
    public static final String EMPLOYEE_PATRONYMIC1 = "Иванович";
    public static final String EMPLOYEE_PERSONALNUM = "PN00001";
    
    public static final String EMPLOYEE_NAME2 = "Иванов";
    public static final String EMPLOYEE_LASTNAME2 = "Иванов";
    public static final String EMPLOYEE_PATRONYMIC2 = "Иванович";
    public static final String EMPLOYEE_PERSONALNUM2 = "PN00002";
    
    public static final String EMPLOYEE_NAME3 = "Константин";
    public static final String EMPLOYEE_LASTNAME3 = "Юон";
    public static final String EMPLOYEE_PATRONYMIC3 = "Федорович";
    public static final String EMPLOYEE_PERSONALNUM3 = "PN00003";
    
    public static final String EMPLOYEE_NAME4 = "Сергей";
    public static final String EMPLOYEE_LASTNAME4 = "Есенин";
    public static final String EMPLOYEE_PATRONYMIC4 = "Александрович";
    public static final String EMPLOYEE_PERSONALNUM4 = "PN00004";
    
    public static final String EMPLOYEE_NAME5 = "Юрий";
    public static final String EMPLOYEE_LASTNAME5 = "Долгорукий";
    public static final String EMPLOYEE_PATRONYMIC5 = "Владимирович";
    public static final String EMPLOYEE_PERSONALNUM5 = "PN00005";
    
    public static final UUID REQUEST_ID_1 = UUID.fromString("08aba2f1-f931-460b-90bc-2a6e9633d8c8");
    public static final UUID REQUEST_ID_2 = UUID.fromString("bf46cb7f-3fb6-4f6d-9294-5941734d87d6");
    public static final UUID REQUEST_ID_3 = UUID.fromString("ec93e83f-69c4-4ba6-9578-0ba9164d7906");
    public static final UUID REQUEST_ID_4 = UUID.fromString("f96b8126-0a3a-457c-878d-49cb8024922e");
    public static final UUID REQUEST_ID_5 = UUID.fromString("c872f1e6-530a-4883-af89-2e6262689636");
    public static final UUID REQUEST_ID_6 = UUID.randomUUID();
    public static final String USER1_ID = "f10bcc5b-51db-4e1c-a747-2a229604f974";
    public final static String USER1_ID_STR = "f10bcc5b-51db-4e1c-a747-2a229604f974";
    public final static String USER2_ID_STR = "a26a3382-674d-4497-9411-815303250ee1";
    public final static String USER3_ID_STR = "a0408b2c-2334-11eb-9b73-305a3a7d9fe6";
    public final static String USER4_ID_STR = "c2055154-8d02-4e1a-ac57-eadbaca44026";
    public final static String USER5_ID_STR = "f62d200a-1720-4c76-bee9-047c26766c1f";
    public final static String ROLE_STR = "GUEST";
    
    public static final UUID EMPLOYEE_ID_1 = UUID.fromString("cd39f8c3-0574-4409-9f0d-0bc68a0e3796");
    public static final UUID EMPLOYEE_ID_2 = UUID.fromString("ec93e83f-3fb6-460b-9578-49cb8024922e");
    public static final UUID EMPLOYEE_ID_3 = UUID.fromString("604e460c-74d3-43ab-9b77-df65fd68a977");
    public static final UUID EMPLOYEE_ID_4 = UUID.fromString("86bc31ca-2e26-4aa4-8b8e-fa3641b0772b");
    public static final UUID EMPLOYEE_ID_5 = UUID.fromString("4e000597-b208-4903-9fc6-10167935b4ca");
    
    public static final String HUMAN_READABLE_EMPLOYEE_ID_1 = "US-0001-1";
    public static final String HUMAN_READABLE_EMPLOYEE_ID_2 = "US-0002-2";
    public static final String HUMAN_READABLE_EMPLOYEE_ID_3 = "US-0003-3";
    public static final String HUMAN_READABLE_EMPLOYEE_ID_4 = "US-0004-4";
    public static final String HUMAN_READABLE_EMPLOYEE_ID_5 = "US-0005-5";
    
    private static final UUID TARIFF_ID_1 = UUID.fromString("f2459553-9e91-4e32-8e9c-efe4e90c74da");
    private static final UUID TARIFF_ID_2 = UUID.fromString("d74ae8db-5834-4eef-a452-4b90720ead43");
    private static final UUID TARIFF_ID_3 = UUID.fromString("6a133710-e5af-4f2a-a820-ba5cac3765a7");
    private static final UUID TARIFF_ID_4 = UUID.fromString("f2edddfe-bca6-4afa-a949-d5da33cc095a");
    private static final UUID TARIFF_ID_5 = UUID.fromString("3984dcce-385c-4e06-8308-668221093e95");
    
    private static final UUID LIMIT_ID_1 = UUID.fromString("d5559553-9e41-5e34-8ccc-efe4ed0c74d1");
    private static final UUID LIMIT_ID_2 = UUID.fromString("03c87c3a-5d64-47ea-9a74-e06c241606c5");
    
    private static final UUID WAYPOINT1_ID_1 = UUID.fromString("4e49340d-cdde-4a52-ace0-fe721a0b9738");
    private static final UUID WAYPOINT1_ID_2 = UUID.fromString("a0baed27-b0a6-4005-805f-c79bd00962a2");
    
    private static final UUID WAYPOINT2_ID_1 = UUID.fromString("408d33f2-7378-4439-8253-a9ceef8af506");
    private static final UUID WAYPOINT2_ID_2 = UUID.fromString("ff88b374-14c0-4ebc-813a-511565bd7ab8");
    private static final UUID WAYPOINT2_ID_3 = UUID.fromString("85125429-bed7-4b9e-b16a-3336bfcfe26a");
    
    private static final UUID WAYPOINT3_ID_1 = UUID.fromString("d9986b3f-7371-4da2-89d6-73eb645145e1");
    private static final UUID WAYPOINT3_ID_2 = UUID.fromString("44f81bbc-530e-415b-ac4f-eeec002eb91d");
    private static final UUID WAYPOINT3_ID_3 = UUID.fromString("6ab7bfab-8f8a-4964-90c5-0f5582af4d13");
    
    private static final UUID WAYPOINT4_ID_1 = UUID.fromString("ae4b5092-1888-4bb9-97ed-4779d1e56cb7");
    private static final UUID WAYPOINT4_ID_2 = UUID.fromString("416f9c4a-9de1-4a2b-b56f-33bd567829f6");
    private static final UUID WAYPOINT4_ID_3 = UUID.fromString("5f2dea08-241c-45fb-8d16-21bc7b8d591e");
    private static final UUID WAYPOINT4_ID_4 = UUID.fromString("4074fc3e-c092-4592-aa11-14350bbb31ca");
    
    private static final UUID WAYPOINT5_ID_1 = UUID.fromString("468e0dd1-f215-4d19-a15b-4733c08227a7");
    private static final UUID WAYPOINT5_ID_2 = UUID.fromString("728c28bb-beb4-4521-86f9-002a79d74151");
    
    private static final UUID CONTRACTOR_ID_1 = UUID.fromString("6b076a9a-9acf-4adb-be4e-3f198a6f6723");
    private static final UUID CONTRACTOR_ID_2 = UUID.fromString("7677b4dd-b7cc-4bda-909b-5144dce3b3bd");
    private static final UUID CARSHARING_CONTRACTOR_ID_1 = UUID.fromString("82a9e079-ef3a-47df-97e4-6d2a97dccb53");

    protected static final UUID EXECUTOR_GROUP_ID_1 = UUID.fromString("3f198a6f-9acf-4adb-be4e-5144dce36723");
    protected static final UUID EXECUTOR_GROUP_ID_2 = UUID.fromString("7677b4dd-ef3a-4bda-47df-5144dce3b3bd");
    protected static final String EXECUTOR_GROUP_NAME_1 = "ДЦБ/Медвежий переулок №1066/Сбербанк";
    protected static final String EXECUTOR_GROUP_NAME_2 = "КИБ/Уральское озеро №1067/Сбербанк";
    
    protected List<Address> addresses;
    
    protected final List<Waypoint> waypoints = new ArrayList<>();
    protected final List<Waypoint> waypoints1 = new ArrayList<>();
    protected final List<Waypoint> waypoints2 = new ArrayList<>();
    protected final List<Waypoint> waypoints3 = new ArrayList<>();
    protected final List<Waypoint> waypoints4 = new ArrayList<>();
    protected final List<Waypoint> waypoints5 = new ArrayList<>();
    
    protected final List<OrderKpi> ordersKPI = new ArrayList<>();
    
    protected Employee testEmployee1;
    protected Employee testEmployee2;
    protected Employee testEmployee3;
    protected Employee testEmployee4;
    protected Employee testEmployee5;
    
    protected Request request1; //personal
    protected Request request2; //public
    protected Request request3; //taxi singleTrip
    protected Request request4; //taxi coopTrip
    protected Request request5; //carsharing
    protected Request request6; //group transfer
    
    protected SingleTaxiTrip singleTaxiTrip1;
    protected CoopTaxiTrip coopTaxiTrip1;
    protected SharedRide sharedRide1;
    
    protected List<TransportCompensation> cityTripCompensationList;
    
    protected Address address1;
    protected Address address2;
    protected Address address3;
    protected Address address4;
    
    public static final String HUMAN_READABLE_REQUEST_ID_1 = "ЛT-0001-1";
    public static final String HUMAN_READABLE_REQUEST_ID_2 = "OT-0002-2";
    public static final String HUMAN_READABLE_REQUEST_ID_3 = "ТT-0003-3";
    public static final String HUMAN_READABLE_REQUEST_ID_4 = "ТT-0004-4";
    public static final String HUMAN_READABLE_REQUEST_ID_5 = "КT-0005-5";
    public static final String HUMAN_READABLE_REQUEST_ID_6 = "GT-0005-5";
    
    protected TripPurpose tripPurpose1;
    protected TripPurpose tripPurpose3;
    protected TripPurpose tripPurpose4;
    protected TripPurpose tripPurpose5;
    
    public static final UUID TAXI_TRIP_ID_1 = UUID.fromString("1179cdd3-6e57-4789-922f-6c92308abf4b");
    public static final UUID TAXI_TRIP_ID_2 = UUID.fromString("94bbaa43-7263-441b-8760-274c4c29fe2f");
    
    public static final UUID MAGENTA_ID_1 = UUID.fromString("9999aa43-7263-441b-8760-274c4c29eeee");
    
    protected SharedRideKPI sharedRideKPI1;
    
    protected OrderKpi orderKpi1;
    
    public static final UUID KPI_ID_1 = UUID.fromString("13072573-ce03-442c-9b76-06e9ceeb7f6a");
    public static final UUID ORDER_KPI_ID_1 = UUID.fromString("13072573-ce03-442c-9b76-06e9ceeb7f6a");
    
    protected TaxiTariff taxiTariff1;
    protected CarSharingTariff carsharingTariff1;
    protected PersonalTariff personalTariff;
    protected PublicTariff publicTariff;
    
    protected PersonalCar employee1PersonalCar;
    
    protected Contract contract1;
    protected Contract contract2;
    protected Contract contract3;
    
    public static final UUID CONTRACT_ID_1 = UUID.fromString("00981355-3a58-461c-8651-43a8f3d2076e");
    public static final UUID CONTRACT_ID_2 = UUID.fromString("918046f6-0c9b-498b-89cd-4c232356cb6b");
    public static final UUID CONTRACT_ID_3 = UUID.fromString("f5cfb6b7-28fb-431b-b41b-bde2e491df99");
    
    protected Contractor contractor1;
    protected Contractor contractor2;
    protected Contractor carsharingContractor;
    
    protected Organization organization1;
    protected Organization organization2;
    protected Organization organization3;
    protected Organization organization5;
    protected Department department1;
    protected Department department2;
    protected Department department3;
    protected Department department5;
    protected Department departmentHead;
    protected Department departmentLocal;
    
    protected Department dep1;
    protected Department dep2;
    protected Department dep3;
    protected Department dep4;
    protected Department dep5;
    protected Department dep6;
    
    protected Position testPosition1;
    protected Position testPosition2;
    protected Position testPosition3;
    public static final String POSITION1_NAME = "Клерк";
    public static final String POSITION2_NAME = "Босс";
    public static final String POSITION3_NAME = "Админ";
    
    protected Limit limit1;
    protected Limit limit2;
    protected Limit limit3;
    protected Limit limit4;
    protected Limit limit5;
    
    public static final String HUMAN_READABLE_LIMIT_ID_3 = "LIM-0003-11"; // taxi
    public static final String HUMAN_READABLE_LIMIT_ID_4 = "LIM-0003-02"; // taxi
    
    protected NewTaxiTripRegistryDTO taxiTripRegistryDTO1;
    protected NewTaxiTripRegistryDTO taxiTripRegistryDTO2;
    
    @BeforeEach
    public void initValues() {
        address1 = new Address(UUID.nameUUIDFromBytes("Москва".getBytes()), "Россия", "Москва", "Москва", "Улица", "1",
                               null, null, null);
        address2 = new Address(UUID.nameUUIDFromBytes("Новосибирск".getBytes()), "Россия", "Новосибирская",
                               "Новосибирск", "Улица", "2", null, null, true);
        address3 = new Address(UUID.nameUUIDFromBytes("Екатеринбург".getBytes()), "Россия", "Свердловская",
                               "Екатеринбург", "Улица", null, "3", null, false);
        address4 = new Address(UUID.nameUUIDFromBytes("Нижний Новгород".getBytes()), "Россия",
                               "Нижегородская", "Нижний Новгород", "Улица", null,
                               null, "24 к", true);
        
        addresses = generateAddress();
        testPosition1 = Position.builder().id(UUID.randomUUID()).name(POSITION1_NAME).build();
        testPosition2 = Position.builder().id(UUID.randomUUID()).name(POSITION2_NAME).build();
        testPosition3 = Position.builder().id(UUID.randomUUID()).name(POSITION3_NAME).build();
        
        sharedRideKPI1 = new SharedRideKPI();
        sharedRideKPI1.setId(KPI_ID_1);
        sharedRideKPI1.setTotalCost(600.0);
        sharedRideKPI1.setTotalDistanceKm(3.0);
        sharedRideKPI1.setTotalTimeMin(60);
        
        orderKpi1 = new OrderKpi();
        orderKpi1.setRequestId(REQUEST_ID_4);
        orderKpi1.setCostSharePart(20.0);
        orderKpi1.setId(ORDER_KPI_ID_1);
        orderKpi1.setKpiId(sharedRideKPI1.getId());
        orderKpi1.setOrderDistanceKm(10);
        orderKpi1.setRideTimeMin(50);
        orderKpi1.setSavings(100.0);
        orderKpi1.setSavingsPct(20.0);
        
        organization1 = new Organization();
        organization1.setId(UUID.randomUUID());
        organization1.setOfficialName("Организация 1");
        
        organization2 = new Organization();
        organization2.setId(UUID.randomUUID());
        organization2.setOfficialName("Организация 2");
        
        organization3 = new Organization();
        organization3.setId(UUID.randomUUID());
        organization3.setOfficialName("Организация 3");
        
        organization5 = new Organization();
        organization5.setId(UUID.randomUUID());
        organization5.setOfficialName("Организация 5");
        
        sharedRide1 = new SharedRide();
        sharedRide1.setId(MAGENTA_ID_1);
        sharedRide1.setActive(true);
        sharedRide1.setKpi(sharedRideKPI1);
        sharedRide1.setTariffId(TARIFF_ID_4);
        sharedRide1.setPassengers(2);
        
        departmentHead = new Department();
        departmentHead = departmentHead.toBuilder().
                                       id(UUID.randomUUID()).
                                       departmentName("Центральное отделение").
                                       organizationId(organization1.getId()).
                                       build();
        
        
        departmentLocal = new Department();
        departmentLocal = departmentLocal.toBuilder().
                                         id(UUID.randomUUID()).
                                         departmentName("Региональное отделение").
                                         parentId(departmentHead.getId()).
                                         organizationId(organization1.getId()).
                                         build();
        
        department1 = new Department();
        department1 = department1.toBuilder().
                                 id(UUID.randomUUID()).
                                 departmentName("Department 1").
                                 parentId(departmentLocal.getId()).
                                 organizationId(organization1.getId()).
                                 build();
        
        department2 = new Department();
        department2 = department2.toBuilder().
                                 id(UUID.randomUUID()).
                                 departmentName("Department 2").
                                 parentId(departmentLocal.getId()).
                                 organizationId(organization2.getId()).
                                 build();
        
        department3 = new Department();
        department3 = department3.toBuilder().
                                 id(UUID.randomUUID()).
                                 departmentName("Department 3").
                                 parentId(departmentLocal.getId()).
                                 organizationId(organization3.getId()).
                                 build();
        
        department5 = new Department();
        department5 = department5.toBuilder().
                                 id(UUID.randomUUID()).
                                 departmentName("Department 5").
                                 parentId(departmentLocal.getId()).
                                 organizationId(organization5.getId()).
                                 build();
        
        dep1 = Department.builder()
                         .id(UUID.randomUUID())
                         .departmentName("level 1")
                         .organizationId(organization1.getId())
                         .build();
        
        dep2 = Department.builder()
                         .id(UUID.randomUUID())
                         .departmentName("level 2")
                         .parentId(dep1.getId())
                         .organizationId(organization1.getId())
                         .build();
        
        dep3 = Department.builder()
                         .id(UUID.randomUUID())
                         .parentId(dep2.getId())
                         .departmentName("level 3")
                         .organizationId(organization1.getId())
                         .build();
        dep4 = Department.builder()
                         .id(UUID.randomUUID())
                         .parentId(dep3.getId())
                         .departmentName("level 4")
                         .organizationId(organization1.getId())
                         .build();
        dep5 = Department.builder()
                         .id(UUID.randomUUID())
                         .parentId(dep4.getId())
                         .departmentName("level 5")
                         .organizationId(organization1.getId())
                         .build();
        dep6 = Department.builder()
                         .id(UUID.randomUUID())
                         .parentId(dep5.getId())
                         .departmentName("level 6")
                         .organizationId(organization1.getId())
                         .build();
        
        testEmployee1 = new Employee();
        testEmployee1.setId(EMPLOYEE_ID_1);
        testEmployee1.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_1);
        testEmployee1.setFirstName(EMPLOYEE_NAME1);
        testEmployee1.setLastName(EMPLOYEE_LASTNAME1);
        testEmployee1.setPatronymic(EMPLOYEE_PATRONYMIC1);
        testEmployee1.setPersonnelNumber(EMPLOYEE_PERSONALNUM);
        testEmployee1.setItinerantType(ItinerantType.FULL);
        testEmployee1.setOrganization(organization1);
        testEmployee1.setDepartment(department1);
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setCostCenter("9900L11040");
        testEmployee1.setUserId(UUID.fromString(USER1_ID_STR));
        
        testEmployee2 = new Employee();
        testEmployee2.setId(EMPLOYEE_ID_2);
        testEmployee2.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_2);
        testEmployee2.setFirstName(EMPLOYEE_NAME2);
        testEmployee2.setLastName(EMPLOYEE_LASTNAME2);
        testEmployee2.setPatronymic(EMPLOYEE_PATRONYMIC2);
        testEmployee2.setPersonnelNumber(EMPLOYEE_PERSONALNUM2);
        testEmployee2.setOrganization(organization2);
        testEmployee2.setDepartment(department2);
        testEmployee2.setPosition(testPosition2);
        testEmployee2.setCostCenter("9900L11040");
        testEmployee2.setUserId(UUID.fromString(USER2_ID_STR));
        
        testEmployee3 = new Employee();
        testEmployee3.setId(EMPLOYEE_ID_3);
        testEmployee3.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_3);
        testEmployee3.setFirstName(EMPLOYEE_NAME3);
        testEmployee3.setLastName(EMPLOYEE_LASTNAME3);
        testEmployee3.setPatronymic(EMPLOYEE_PATRONYMIC3);
        testEmployee3.setPersonnelNumber(EMPLOYEE_PERSONALNUM3);
        testEmployee3.setItinerantType(ItinerantType.FULL);
        testEmployee3.setOrganization(organization3);
        testEmployee3.setDepartment(department3);
        testEmployee3.setCostCenter("123456789");
        testEmployee3.setPosition(testPosition3);
        testEmployee3.setUserId(UUID.fromString(USER3_ID_STR));
        
        testEmployee4 = new Employee();
        testEmployee4.setId(EMPLOYEE_ID_4);
        testEmployee4.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_4);
        testEmployee4.setFirstName(EMPLOYEE_NAME4);
        testEmployee4.setLastName(EMPLOYEE_LASTNAME4);
        testEmployee4.setPatronymic(EMPLOYEE_PATRONYMIC4);
        testEmployee4.setPersonnelNumber(EMPLOYEE_PERSONALNUM4);
        testEmployee4.setItinerantType(null);
        testEmployee4.setDepartment(department3);
        testEmployee4.setCostCenter("987654321");
        testEmployee4.setUserId(UUID.fromString(USER4_ID_STR));
        
        testEmployee5 = new Employee();
        testEmployee5.setId(EMPLOYEE_ID_5);
        testEmployee5.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_5);
        testEmployee5.setFirstName(EMPLOYEE_NAME5);
        testEmployee5.setLastName(EMPLOYEE_LASTNAME5);
        testEmployee5.setPatronymic(EMPLOYEE_PATRONYMIC5);
        testEmployee5.setPersonnelNumber(EMPLOYEE_PERSONALNUM5);
        testEmployee5.setItinerantType(null);
        testEmployee5.setOrganization(organization5);
        testEmployee5.setDepartment(department5);
        testEmployee5.setUserId(UUID.fromString(USER5_ID_STR));
        
        tripPurpose1 = TripPurpose.builder().id(UUID.randomUUID()).organization(UUID.randomUUID())
                                  .active(true).purpose("Test purpose").build();
        tripPurpose3 = TripPurpose.builder().id(UUID.randomUUID()).organization(UUID.randomUUID())
                                  .active(true).purpose("Test purpose 3").build();
        tripPurpose4 = TripPurpose.builder().id(UUID.randomUUID()).organization(UUID.randomUUID())
                                  .active(true).purpose("Test purpose 4").build();
        tripPurpose5 = TripPurpose.builder().id(UUID.randomUUID()).organization(UUID.randomUUID())
                                  .active(true).purpose("Test purpose 5").build();
        
        contractor1 = new Contractor();
        contractor1 = contractor1.toBuilder().id(CONTRACTOR_ID_1).name("Контрактор 1").build();
        
        contractor2 = new Contractor();
        contractor2 = contractor2.toBuilder().id(CONTRACTOR_ID_2).name("Контрактор 2").build();
        
        carsharingContractor = new Contractor();
        carsharingContractor =
                carsharingContractor.toBuilder().id(CARSHARING_CONTRACTOR_ID_1).name("Каршеринговая компания").build();
        
        contract1 =
                Contract.builder()
                        .contractor(contractor1)
                        .id(CONTRACT_ID_1)
                        .uvhd("test")
                        .contractNumber("test")
                        .includeVat(true)
                        .sum(1000L)
                        .endDate(LocalDate.now())
                        .startDate(LocalDate.now().minus(2, ChronoUnit.DAYS))
                        .transportType(TransportTypeEnum.TAXI)
                        .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                        .creationTime(LocalDateTime.now().minus(2, ChronoUnit.DAYS))
                        .active(true)
                        .userId(UUID.randomUUID())
                        .vatValue(1000)
                        .build();
        contract2 =
                Contract.builder()
                        .contractor(contractor2)
                        .id(CONTRACT_ID_2)
                        .uvhd("test")
                        .contractNumber("test")
                        .includeVat(true)
                        .sum(1000L)
                        .endDate(LocalDate.now())
                        .startDate(LocalDate.now().minus(2, ChronoUnit.DAYS))
                        .transportType(TransportTypeEnum.TAXI)
                        .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                        .creationTime(LocalDateTime.now().minus(2, ChronoUnit.DAYS))
                        .active(true)
                        .userId(UUID.randomUUID())
                        .vatValue(1000)
                        .build();
        contract3 =
                Contract.builder()
                        .contractor(carsharingContractor)
                        .uvhd("test")
                        .contractNumber("test")
                        .includeVat(true)
                        .sum(1000L)
                        .endDate(LocalDate.now())
                        .startDate(LocalDate.now().minus(2, ChronoUnit.DAYS))
                        .transportType(TransportTypeEnum.TAXI)
                        .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                        .creationTime(LocalDateTime.now().minus(2, ChronoUnit.DAYS))
                        .active(true)
                        .userId(UUID.randomUUID())
                        .vatValue(1000)
                        .id(CONTRACT_ID_3)
                        .build();
        
        employee1PersonalCar = PersonalCar.builder().
                                          id(UUID.randomUUID()).
                                          brandName("brand").
                                          isNew(true).
                                          employee(testEmployee1).
                                          engineVolume(2000).
                                          build();
        
        personalTariff = new PersonalTariff();
        personalTariff.setActive(true);
        personalTariff.setId(TARIFF_ID_1);
        personalTariff.setHumanReadableId("TF-0001-00001234");
        personalTariff.setTransportType(TransportTypeEnum.PERSONAL);
        personalTariff = personalTariff.toBuilder().trustIdx(0.0).build();
        
        publicTariff = new PublicTariff();
        publicTariff.setActive(true);
        publicTariff.setId(TARIFF_ID_2);
        publicTariff.setHumanReadableId("TF-0001-00301234");
        publicTariff.setTransportType(TransportTypeEnum.PUBLIC);
        
        taxiTariff1 = new TaxiTariff();
        taxiTariff1.setActive(true);
        taxiTariff1.setContract(contract1);
        taxiTariff1.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_3);
        taxiTariff1.setId(TARIFF_ID_3);
        taxiTariff1.setOrganizationId(organization3.getId());
        taxiTariff1.setRegion("Регион");
        taxiTariff1.setTransportType(TransportTypeEnum.TAXI);
        taxiTariff1 = taxiTariff1.toBuilder().waitCostPerMinIntermediate(1)
                                 .waitCostPerMin(1)
                                 .rideCostPerMin(1)
                                 .rideCostPerKm(1)
                                 .minRideTimeCost(1)
                                 .carServiceCost(1)
                                 .minRideDistanceCost(1)
                                 .contractorDeviationParams(ContractorDeviationsTariffParams.builder()
                                                                                            .maxDiffComputedCostPercent(1)
                                                                                            .maxDiffComputedDistancePercent(1)
                                                                                            .maxDiffComputedWaitingPercent(1)
                                                                                            .maxDiffContractorCostPercent(1)
                                                                                            .maxDiffFactDistancePercent(1).build())
                                 .build();
        
        carsharingTariff1 = new CarSharingTariff();
        carsharingTariff1.setActive(true);
        carsharingTariff1.setContract(contract3);
        carsharingTariff1.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_5);
        carsharingTariff1.setId(TARIFF_ID_5);
        carsharingTariff1.setOrganizationId(organization5.getId());
        carsharingTariff1.setRegion("Регион");
        carsharingTariff1.setTransportType(TransportTypeEnum.CARSHARING);
        carsharingTariff1 = carsharingTariff1.toBuilder()
                                             .waitCostPerMin(1)
                                             .rideCostPerMin(1)
                                             .rideCostPerKm(1)
                                             .coefCasko(1.0)
                                             .coefChildSeat(1.0)
                                             .coefPetTransport(1.0)
                                             .coefTraffic(1.0)
                                             .timedTariffParams(TimedTariffParams.builder().coefDayOff(1.0).coefWorkDayEvening(1.0)
                                                                                 .coefWorkDayMorning(1.0).coefWorkDayNight(1.0)
                                                                                 .coefWorkDayNoon(1.0).build())
                                             .build();
        
        cityTripCompensationList = List.of(
                TransportCompensation
                        .builder()
                        .id(UUID.randomUUID())
                        .compensationType(PublicCompensationType.CITY_TRIP_COMPENSATION)
                        .transportType(PublicTransportType.CITY_BUS)
                        .ticketsCost(350)
                        .ticketsCount(1)
                        .request(request2)
                        .build()
                                          );
        
        request1 = new Request();
        request1.setId(REQUEST_ID_1);
        request1.setTariff(personalTariff);
        //TODO убрать следы limitId и добавить его в реальное место
//        request1.setLimitId(LIMIT_ID_1);
        request1.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_1);
        request1.setAuthor(testEmployee1);
        request1.setEmployeeDriverId(testEmployee1.getId());
        request1.setPassenger(testEmployee1);
        RequestRating requestRating1 = new RequestRating();
        requestRating1.setRating(4);
        request1.setRequestRating(requestRating1);
        request1.setTransportType(TransportTypeEnum.PERSONAL.name());
        request1.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request1.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request1.setApprovedBy(testEmployee1);
        request1.setApprovalDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request1.setPurpose(tripPurpose1);
        request1.setCoopTrip(false);
        request1.setStatus(TripRequestStatus.TAXI_APPROVED.name());
        request1.setPersonalCar(employee1PersonalCar);
        request1.setExpected(ExpectedData.builder().cost(290000.0).distance(1000.0).time(Duration.of(10, ChronoUnit.MINUTES)).build());
        request1.setOrganizationId(organization1.getId());
        request1.setCostCenter(testEmployee1.getCostCenter());
        request1.setExecutorGroupId(EXECUTOR_GROUP_ID_1);
        request1.setExecutorGroupName(EXECUTOR_GROUP_NAME_1);
        
        request2 = new Request();
        request2.setId(REQUEST_ID_2);
        request2.setTariff(publicTariff);
//        request2.setLimitId(LIMIT_ID_2);
        request2.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_2);
        request2.setStatus(TripRequestStatus.TAXI_APPROVED.name());
        request2.setApprovalDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request2.setAuthor(testEmployee2);
        request2.setPurpose(tripPurpose1);
        request2.setPassenger(testEmployee2);
        request2.setTransportType(TransportTypeEnum.PUBLIC.name());
        request2.setPurpose(tripPurpose1);
        request2.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request2.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request2.getTransportCompensation().addAll(cityTripCompensationList);
        request2.setOrganizationId(organization2.getId());
        request2.setCostCenter(testEmployee2.getCostCenter());
        request2.setExecutorGroupId(EXECUTOR_GROUP_ID_2);
        request2.setExecutorGroupName(EXECUTOR_GROUP_NAME_2);
        
        request3 = new Request();
        request3.setId(REQUEST_ID_3);
        request3.setTariff(taxiTariff1);
        request3.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_3);
        request3.setAuthor(testEmployee3);
        request3.setPassenger(testEmployee3);
        RequestRating requestRating3 = new RequestRating();
        requestRating3.setRating(3);
        request3.setRequestRating(requestRating3);
        request3.setStatus(TripRequestStatus.TAXI_APPROVED.name());
        request3.setCoopTrip(false);
        request3.setTransportType(TransportTypeEnum.TAXI.name());
        request3.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request3.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request3.setApprovalDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request3.setApprovedBy(testEmployee3);
        request3.setPurpose(tripPurpose3);
        request3.setExpected(ExpectedData.builder().cost(1.0).distance(1.0).time(Duration.of(10, ChronoUnit.MINUTES)).build());
        request3.setOrganizationId(organization3.getId());
        request3.setCostCenter(testEmployee3.getCostCenter());
        request3.setExecutorGroupId(EXECUTOR_GROUP_ID_1);
        request3.setExecutorGroupName(EXECUTOR_GROUP_NAME_1);
        
        request4 = new Request();
        request4.setId(REQUEST_ID_4);
        request4.setTariff(taxiTariff1);
        request4.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_4);
        request4.setAuthor(testEmployee4);
        request4.setPassenger(testEmployee4);
        RequestRating requestRating4 = new RequestRating();
        requestRating4.setRating(4);
        request4.setRequestRating(requestRating4);
        request4.setTransportType(TransportTypeEnum.TAXI.name());
        request4.setStatus(TripRequestStatus.TAXI_APPROVED.name());
        request4.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request4.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request4.setApprovalDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request4.setApprovedBy(testEmployee4);
        request4.setPurpose(tripPurpose4);
        request4.setCoopTrip(true);
        request4.setRideId(MAGENTA_ID_1);
        request4.setSharedRide(sharedRide1);
        request4.setExpected(ExpectedData.builder().cost(1.0).distance(1.0).time(Duration.of(10, ChronoUnit.MINUTES)).build());
        request4.setOrganizationId(organization3.getId());
        request4.setCostCenter(testEmployee4.getCostCenter());
        
        request5 = new Request();
        request5.setId(REQUEST_ID_5);
        request5.setTariff(carsharingTariff1);
        request5.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_5);
        request5.setAuthor(testEmployee5);
        request5.setPassenger(testEmployee5);
        request5.setTransportType(TransportTypeEnum.CARSHARING.name());
        request5.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request5.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request5.setApprovalDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request5.setApprovedBy(testEmployee5);
        request5.setStatus(TripRequestStatus.CARSHARING_APPROVED.name());
        request5.setPurpose(tripPurpose5);
        request5.setCoopTrip(false);
        request5.setExpected(ExpectedData.builder().cost(1.0).distance(1.0).time(Duration.of(10, ChronoUnit.MINUTES)).build());
        request5.setContractor(carsharingContractor);
        request5.setCarsharingClass(CarsharingClass.ECONOMY.name());
        request5.setOrganizationId(organization5.getId());
        request5.setCostCenter(testEmployee5.getCostCenter());
        
        request6 = new Request();
        request6.setId(REQUEST_ID_6);
        request6.setTariff(carsharingTariff1);
        request6.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_6);
        request6.setAuthor(testEmployee5);
        request6.setPassenger(testEmployee5);
        request6.setTransportType(TransportTypeEnum.GROUP_TRANSFER.name());
        request6.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request6.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request6.setApprovalDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        request6.setApprovedBy(testEmployee5);
        request6.setStatus(TripRequestStatus.GROUP_TRANSFER_APPROVED.name());
        request6.setPurpose(tripPurpose5);
        request6.setCoopTrip(false);
        request6.setExpected(ExpectedData.builder().cost(1.0).distance(1.0).time(Duration.of(10, ChronoUnit.MINUTES)).build());
        request6.setTransportClass(GroupTransferClass.TRANSFER.name());
        request6.setOrganizationId(organization5.getId());
        request6.setCostCenter(testEmployee5.getCostCenter());
        request6.setPassengerDepartment1(dep1.getDepartmentName());
        request6.setPassengerDepartment2(dep2.getDepartmentName());
        request6.setPassengerDepartment3(dep3.getDepartmentName());
        request6.setPassengerDepartment4(dep4.getDepartmentName());
        request6.setPassengerDepartment5(dep5.getDepartmentName());
        request6.setPassengerDepartment6(dep6.getDepartmentName());
        
        waypoints1.add(Waypoint.builder().id(WAYPOINT1_ID_1).address(address1).waitTime(Duration.ofMinutes(5L))
                               .request(request1).build());
        waypoints1.add(Waypoint.builder().id(WAYPOINT1_ID_2).address(address2).waitTime(Duration.ofMinutes(2L))
                               .request(request1).build());
        waypoints2.add(Waypoint.builder().id(WAYPOINT2_ID_1).address(address1).waitTime(Duration.ofMinutes(5L))
                               .request(request2).build());
        waypoints2.add(Waypoint.builder().id(WAYPOINT2_ID_2).address(address2).waitTime(Duration.ofMinutes(2L))
                               .request(request2).build());
        waypoints2.add(Waypoint.builder().id(WAYPOINT2_ID_3).address(address3).waitTime(Duration.ofMinutes(2L))
                               .request(request2).build());
        waypoints3.add(Waypoint.builder().id(WAYPOINT3_ID_2).address(address2).waitTime(Duration.ofMinutes(2L))
                               .request(request3).build());
        waypoints3.add(Waypoint.builder().id(WAYPOINT3_ID_3).address(address3).waitTime(Duration.ofMinutes(2L))
                               .request(request3).build());
        waypoints4.add(Waypoint.builder().id(WAYPOINT4_ID_1).address(address1).waitTime(Duration.ofMinutes(5L))
                               .request(request4).build());
        waypoints4.add(Waypoint.builder().id(WAYPOINT4_ID_2).address(address2).waitTime(Duration.ofMinutes(2L))
                               .request(request4).build());
        waypoints4.add(Waypoint.builder().id(WAYPOINT4_ID_3).address(address3).waitTime(Duration.ofMinutes(5L))
                               .request(request4).build());
        waypoints4.add(Waypoint.builder().id(WAYPOINT4_ID_4).address(address4).waitTime(Duration.ofMinutes(2L))
                               .request(request4).build());
        waypoints5.add(Waypoint.builder().id(WAYPOINT5_ID_1).address(address1).waitTime(Duration.ofMinutes(2L))
                               .request(request5).build());
        waypoints5.add(Waypoint.builder().id(WAYPOINT5_ID_2).address(address2).waitTime(Duration.ofMinutes(2L))
                               .request(request5).build());
        request1.getWaypoints().addAll(waypoints1);
        request2.getWaypoints().addAll(waypoints2);
        request3.getWaypoints().addAll(waypoints3);
        request4.getWaypoints().addAll(waypoints4);
        request5.getWaypoints().addAll(waypoints5);
        waypoints.addAll(waypoints1);
        waypoints.addAll(waypoints2);
        waypoints.addAll(waypoints3);
        waypoints.addAll(waypoints4);
        waypoints.addAll(waypoints5);
        taxiTripRegistryDTO1 =
                NewTaxiTripRegistryDTO
                        .builder()
                        .contractorId(contractor1.getId())
                        .date(request3.getCreationTime().minusDays(1).toLocalDate())
                        .build();
        
        taxiTripRegistryDTO2 =
                NewTaxiTripRegistryDTO
                        .builder()
                        .contractorId(contractor2.getId())
                        .date(request4.getCreationTime().minusDays(1).toLocalDate())
                        .build();
        
        singleTaxiTrip1 = new SingleTaxiTrip();
        singleTaxiTrip1.setId(TAXI_TRIP_ID_1);
        singleTaxiTrip1.setDateTimeRegistered(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        singleTaxiTrip1.setFactParametersSettingTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        singleTaxiTrip1.setOrganizationId(organization3.getId());
        singleTaxiTrip1.setStatus(InboundTaxiTripStatus.ORDER_FINISHED.name());
        singleTaxiTrip1.setTariff(taxiTariff1);
        singleTaxiTrip1.setTaxiId(400 + "");
        singleTaxiTrip1.setTripFactDistance(400.0);
        singleTaxiTrip1.setTripFactDuration(Duration.ofHours(1));
        singleTaxiTrip1.setTripFactPrice(400);
        singleTaxiTrip1.setLastXmlReceivedDateTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        singleTaxiTrip1.setTripFactWaitTime(Duration.ofMinutes(15));
        singleTaxiTrip1.setTripFinishTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(1));
        singleTaxiTrip1.setTripStartTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusMinutes(30));
        singleTaxiTrip1.setTripType(TripType.SINGLE);
        singleTaxiTrip1.setRequest(request3);
        
        coopTaxiTrip1 = new CoopTaxiTrip();
        coopTaxiTrip1.setId(TAXI_TRIP_ID_2);
        coopTaxiTrip1.setDateTimeRegistered(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        coopTaxiTrip1.setFactParametersSettingTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        coopTaxiTrip1.setOrganizationId(organization3.getId());
        coopTaxiTrip1.setStatus(InboundTaxiTripStatus.ORDER_FINISHED.name());
        coopTaxiTrip1.setTariff(taxiTariff1);
        coopTaxiTrip1.setTaxiId(500 + "");
        coopTaxiTrip1.setTripFactDistance(500.0);
        coopTaxiTrip1.setTripFactDuration(Duration.ofHours(1));
        coopTaxiTrip1.setTripFactPrice(500);
        coopTaxiTrip1.setTripFactWaitTime(Duration.ofHours(1));
        coopTaxiTrip1.setLastXmlReceivedDateTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        coopTaxiTrip1.setTripFinishTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(3));
        coopTaxiTrip1.setTripStartTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(2));
        coopTaxiTrip1.setTripType(TripType.COOP);
        coopTaxiTrip1.setRideId(MAGENTA_ID_1);
        coopTaxiTrip1.setSharedRide(sharedRide1);
        
        limit1 =
                Limit.builder().balance(1000L).departmentId(UUID.randomUUID()).id(UUID.randomUUID()).build();
        request1.setLimit(limit1);
        
        limit2 =
                Limit.builder().balance(1500L).departmentId(UUID.randomUUID()).id(UUID.randomUUID()).build();
        request2.setLimit(limit2);
        
        limit3 = Limit.builder().balance(2000L).departmentId(UUID.randomUUID()).id(UUID.randomUUID()).humanReadableId(HUMAN_READABLE_LIMIT_ID_3)
                      .build();
        request3.setLimit(limit3);
        
        limit4 = Limit.builder().balance(3000L)
                      .departmentId(UUID.randomUUID())
                      .id(UUID.randomUUID())
                      .humanReadableId(HUMAN_READABLE_LIMIT_ID_4).build();
        request4.setLimit(limit4);
        limit5 =
                Limit.builder().balance(3000L).departmentId(UUID.randomUUID()).id(UUID.randomUUID()).build();
        request5.setLimit(limit5);
    }
    
    private List<Address> generateAddress() {
        var result = new ArrayList<Address>();
        result.add(address1);
        result.add(address2);
        result.add(address3);
        result.add(address4);
        result.add(new Address(UUID.nameUUIDFromBytes("Казань".getBytes()), "Россия", "Татарстан", "Казань", "Улица",
                               "5", null, null, true));
        result.add(new Address(UUID.nameUUIDFromBytes("Самара".getBytes()), "Россия", "Самарская", "Самара", "Улица", "6", null, null, true));
        result.add(new Address(UUID.nameUUIDFromBytes("Челябинск".getBytes()), "Россия", "Челябинская", "Челябинск", "Улица", "7", null, null, true));
        result.add(new Address(UUID.nameUUIDFromBytes("Омск".getBytes()), "Россия", "Омская", "Омск", "Улица", "8", null, null, true));
        result.add(
                new Address(UUID.nameUUIDFromBytes("Ростов-на-Дону".getBytes()), "Россия", "Ростовская", "Ростов-на-Дону", "Улица", "9", null, null,
                            true));
        result.add(new Address(UUID.nameUUIDFromBytes("Санкт-Петербург".getBytes()), "Россия", null, "Санкт-Петербург", "Улица", "10", null, null,
                               true));
        return result;
    }
    
    
}
