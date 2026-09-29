package ru.sber.transport.telemechanic;

import lombok.experimental.UtilityClass;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.UUID;

@UtilityClass
public class TestData {
    public static final String OFFICIAL_NAME_1 = "officialName1";
    public static final String OFFICIAL_NAME_2 = "officialName2";
    public static final String OFFICIAL_NAME_3 = "officialName3";
    public static final String POSITION_NAME_1 = "positionName1";
    public static final String POSITION_NAME_2 = "positionName2";
    public static final String POSITION_NAME_3 = "positionName3";
    public static final String DEPARTMENT_NAME_1 = "departmentName1";
    public static final String DEPARTMENT_NAME_2 = "departmentName2";
    public static final UUID ORGANIZATION_1_ID = UUID.randomUUID();
    public static final UUID ORGANIZATION_2_ID = UUID.randomUUID();
    public static final UUID ORGANIZATION_3_ID = UUID.randomUUID();
    public static final Organization ORGANIZATION_1 = createOrganization1();
    public static final Organization ORGANIZATION_2 = createOrganization2();
    public static final UUID POSITION_1_ID = UUID.randomUUID();
    public static final UUID POSITION_2_ID = UUID.randomUUID();
    public static final UUID POSITION_3_ID = UUID.randomUUID();
    public static final UUID DEPARTMENT_1_ID = UUID.randomUUID();
    public static final UUID DEPARTMENT_2_ID = UUID.randomUUID();
    public static final UUID EMPLOYEE_1_ID = UUID.randomUUID();
    public static final UUID EMPLOYEE_2_ID = UUID.randomUUID();
    public static final UUID EMPLOYEE_3_ID = UUID.randomUUID();
    public static final String DEPARTMENT_HUMAN_READABLE_ID_1 = "DT-0001-00000001";
    public static final String DEPARTMENT_HUMAN_READABLE_ID_2 = "DT-0001-00000002";
    public static final String EMPLOYEE_HUMAN_READABLE_ID_1 = "US-0001-00000001";
    public static final String EMPLOYEE_HUMAN_READABLE_ID_2 = "US-0001-00000002";
    public static final String EMPLOYEE_HUMAN_READABLE_ID_3 = "US-0001-00000003";
    public static final String EMPLOYEE_1_PERSONNEL_NUMBER = "0000001";
    public static final String EMPLOYEE_2_PERSONNEL_NUMBER = "0000002";
    public static final String EMPLOYEE_3_PERSONNEL_NUMBER = "0000003";
    
    public static Organization createOrganization1() {
        return Organization.builder()
                           .id(ORGANIZATION_1_ID)
                           .officialName(OFFICIAL_NAME_1)
                           .digitId(1L)
                           .msrn("11111111")
                           .tin("111111")
                           .build();
    }
    
    public static Organization createOrganization2() {
        return Organization.builder()
                           .id(ORGANIZATION_2_ID)
                           .officialName(OFFICIAL_NAME_2)
                           .digitId(2L)
                           .msrn("22222222")
                           .tin("222222")
                           .build();
    }
    
    public static Organization createOrganization3() {
        return Organization.builder()
                           .id(ORGANIZATION_3_ID)
                           .officialName(OFFICIAL_NAME_3)
                           .digitId(3L)
                           .msrn("33333333")
                           .tin("333333")
                           .build();
    }
    
    public static Position createPosition1(Organization organization) {
        return Position.builder()
                       .id(POSITION_1_ID)
                       .positionName(POSITION_NAME_1)
                       .organization(organization)
                       .build();
    }
    
    public static Position createPosition2(Organization organization) {
        return Position.builder()
                       .id(POSITION_2_ID)
                       .positionName(POSITION_NAME_2)
                       .organization(organization)
                       .build();
    }
    
    public static Position createPosition3(Organization organization) {
        return Position.builder()
                       .id(POSITION_3_ID)
                       .positionName(POSITION_NAME_3)
                       .organization(organization)
                       .build();
    }
    
    public static Department createDepartment1(Organization organization, UUID parentId) {
        return Department.builder()
                         .id(DEPARTMENT_1_ID)
                         .humanReadableId(DEPARTMENT_HUMAN_READABLE_ID_1)
                         .organization(organization)
                         .departmentName(DEPARTMENT_NAME_1)
                         .parentId(parentId)
                         .build();
    }
    
    public static Department createDepartment2(Organization organization, UUID parentId) {
        return Department.builder()
                         .id(DEPARTMENT_2_ID)
                         .humanReadableId(DEPARTMENT_HUMAN_READABLE_ID_2)
                         .organization(organization)
                         .departmentName(DEPARTMENT_NAME_2)
                         .parentId(parentId)
                         .build();
    }
    
    public static Employee createEmployee1(Department department, Position position) {
        return Employee.builder()
                       .id(EMPLOYEE_1_ID)
                       .personnelNumber(EMPLOYEE_1_PERSONNEL_NUMBER)
                       .patronymic("Александровна")
                       .lastName("Гришина")
                       .firstName("Светлана")
                       .mobilePhone("+792356811")
                       .organization(department.getOrganization())
                       .department(department)
                       .userId(EMPLOYEE_1_ID)
                       .humanReadableId(EMPLOYEE_HUMAN_READABLE_ID_1)
                       .position(position)
                       .build();
    }
    
    public static Employee createEmployee2(Department department, Position position) {
        return Employee.builder()
                       .id(EMPLOYEE_2_ID)
                       .personnelNumber(EMPLOYEE_2_PERSONNEL_NUMBER)
                       .patronymic("Олегович")
                       .lastName("Горбач")
                       .firstName("Константин")
                       .mobilePhone("+792356812")
                       .organization(department.getOrganization())
                       .department(department)
                       .userId(EMPLOYEE_2_ID)
                       .humanReadableId(EMPLOYEE_HUMAN_READABLE_ID_2)
                       .position(position)
                       .build();
    }
    
    public static Employee createEmployee3(Department department, Position position) {
        return Employee.builder()
                       .id(EMPLOYEE_3_ID)
                       .personnelNumber(EMPLOYEE_3_PERSONNEL_NUMBER)
                       .patronymic("Григорьевич")
                       .lastName("Колесниковенко")
                       .firstName("Андрей")
                       .mobilePhone("+792356813")
                       .organization(department.getOrganization())
                       .department(department)
                       .userId(EMPLOYEE_3_ID)
                       .humanReadableId(EMPLOYEE_HUMAN_READABLE_ID_3)
                       .position(position)
                       .build();
    }
    
    public static Request createRequest(Employee employee, Transport transport, UUID organizationId) {
        var request = Request.builder()
                             .humanReadableId("TM-" + UUID.randomUUID())
                             .author(employee)
                             .transport(transport)
                             .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                             .status(RequestStatus.IN_PROGRESS)
                             .organizationId(organizationId)
                             .build();
        var checks = new ArrayList<Check>();
        for (var type : CheckType.values()) {
            checks.add(new Check(null, type, CheckStatus.IN_PROGRESS, 0, request, null));
        }
        request.getChecks().addAll(checks);
        return request;
    }
}
