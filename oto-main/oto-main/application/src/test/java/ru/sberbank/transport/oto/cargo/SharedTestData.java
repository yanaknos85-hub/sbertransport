package ru.sberbank.transport.oto.cargo;

import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.transport.oto.cargo.database.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SharedTestData extends KafkaTest {
    
    public static final String ORGANIZATION_ID = "f10b775b-51db-4e1c-a747-222296041234";
    public static final String ORGANIZATION2_ID = "f10b775b-51db-4e1c-a747-222296042222";
    public static final String ORGANIZATION3_ID = "f10b775b-51db-4e1c-a747-333396043333";
    public static final String ORGANIZATION_GROUP_ID = "f10b775b-51db-4e1c-a747-222296041234";
    public static final String ORGANIZATION_GROUP2_ID = "f10b775b-51db-4e1c-a747-222296042222";
    public static final String EXECUTOR_GROUP_ID_1 = "aaaaaaaa-1111-4e1c-a747-000000000001";
    public static final String EXECUTOR_GROUP_ID_2 = "aaaaaaaa-2222-4e1c-a747-000000000002";
    public static final String USER_ENGINEER_ID = "f10bcc5b-51db-4e1c-a747-000096040000";
    public static final String USER1_ID = "f10bcc5b-51db-4e1c-a747-2a229604f974";
    public static final String USER2_ID = "558d39f2-c638-490f-90e9-94d896a65b4c";
    public static final String PERSON_URL_TEMPLATE = "/person/" + ORGANIZATION_ID;
    public static final String CARGO_POST_URL_TEMPLATE = "/cargo/";
    public static final String AUTHORIZATION_HEADER_VALUE = "Basic login";
    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    
    protected Contractor createContractor(String name) {
        return Contractor.builder()
                         .id(UUID.randomUUID())
                         .name(name)
                         .build();
    }
    
    protected Position createPosition(String name) {
        return Position.builder()
                       .id(UUID.randomUUID())
                       .name(name)
                       .build();
    }
    
    protected Department createDepartment(UUID organizationId) {
        return Department.builder()
                         .id(UUID.randomUUID())
                         .departmentName("Department")
                         .organizationId(organizationId)
                         .build();
    }
    
    protected Employee createPassenger(
            UUID userId,
            String humanReadableId,
            String lastName,
            String firstName,
            String patronymic,
            String phone,
            Position position,
            Department department
                                      ) {
        return Employee.builder()
                       .id(userId)
                       .userId(userId)
                       .humanReadableId(humanReadableId)
                       .lastName(lastName)
                       .firstName(firstName)
                       .patronymic(patronymic)
                       .mobilePhone(phone)
                       .position(position)
                       .department(department)
                       .build();
    }
    
    protected Request createRequest(
            UUID requestId,
            String humanReadableId,
            TripRequestStatus status,
            TransportTypeEnum transportType,
            Contractor contractor,
            Employee passenger,
            ExpectedData expectedData,
            List<Waypoint> waypoints,
            LocalDateTime creationTime,
            LocalDateTime desiredDate,
            LocalDateTime deadline
                                   ) {
        return Request.builder()
                             .id(requestId)
                             .humanReadableId(humanReadableId)
                             .status(status.name())
                             .transportType(transportType.name())
                             .contractor(contractor)
                             .passenger(passenger)
                             .expected(expectedData)
                             .waypoints(waypoints)
                             .creationTime(creationTime)
                             .controlDate(deadline)
                             .desiredDate(desiredDate)
                             .deadline(deadline)
                             .deadlineState(DeadlineState.values()[new Random().nextInt(DeadlineState.values().length)])
                             .timeZone("UTC")
                             .build();
    }
}
