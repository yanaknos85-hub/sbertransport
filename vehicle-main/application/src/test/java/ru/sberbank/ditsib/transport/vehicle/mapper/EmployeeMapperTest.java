package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.vehicle.database.model.Department;
import ru.sberbank.ditsib.transport.vehicle.database.model.Employee;
import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Тест маппера сотрудников")
class EmployeeMapperTest {
    private final EmployeeMapper mapper = Mappers.getMapper(EmployeeMapper.class);

    @Test
    void fromMessage() {
        var message = EmployeeMessage.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .firstName("firstName")
                .lastName("lastName")
                .patronymic("patronymic")
                .positionId(UUID.randomUUID())
                .deleted(false)
                .humanReadableId("humanReadableId")
                .organizationId(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .mobilePhone("mobilePhone")
                .personnelNumber("personnelNumber")
                .organizationId(UUID.randomUUID())
                .build();
        var actual = mapper.employeeMessageToEmployee(message);
        assertNotNull(actual);
        assertEquals(actual.getId(), message.getId());
        assertEquals(actual.getHumanReadableId(), message.getHumanReadableId());
        assertEquals(actual.getUserId(), message.getUserId());
        assertEquals(actual.getFirstName(), message.getFirstName());
        assertEquals(actual.getLastName(), message.getLastName());
        assertEquals(actual.getPatronymic(), message.getPatronymic());
        assertEquals(actual.getPosition().getId(), message.getPositionId());
        assertEquals(actual.getDepartment().getId(), message.getDepartmentId());
        assertEquals(actual.getMobilePhone(), message.getMobilePhone());
        assertEquals(actual.getPersonnelNumber(), message.getPersonnelNumber());
        assertEquals(actual.getOrganization().getId(), message.getOrganizationId());
    }


    @Test
    void employeeToTelemechanic() {
        var entity = Employee.builder().id(UUID.randomUUID())
                .firstName("Тест")
                .lastName("Мэппер")
                .patronymic(null)
                .personnelNumber("2016498")
                .organization(Organization.builder()
                        .id(UUID.randomUUID())
                        .officialName("ЦА")
                        .build())
                .department(Department.builder()
                        .id(UUID.randomUUID())
                        .departmentName("Департамент ЦА")
                        .build())
                .build();
        var actual = mapper.employeeToTelemechanicDto(entity);
        assertThat(entity.getPersonnelNumber()).isEqualTo(actual.personnelNumber());
        assertThat(entity.getDepartment().getId()).isEqualTo(actual.departmentId());
        assertThat(entity.getDepartment().getDepartmentName()).isEqualTo(actual.departmentName());
        assertThat(entity.getOrganization().getId()).isEqualTo(actual.organizationId());
        assertThat(entity.getOrganization().getOfficialName()).isEqualTo(actual.organizationName());
        assertThat(entity.getFIO()).isEqualTo(actual.fullName());
    }


    @Test
    void employeeToEmployeeDto() {
        var entity = Employee.builder().id(UUID.randomUUID())
                .firstName("Тест")
                .lastName("Мэппер")
                .patronymic(null)
                .personnelNumber("2016498")
                .organization(Organization.builder()
                        .id(UUID.randomUUID())
                        .officialName("ЦА")
                        .build())
                .department(Department.builder()
                        .id(UUID.randomUUID())
                        .departmentName("Департамент ЦА")
                        .build())
                .build();
        var actual = mapper.employeeToEmployeeDto(entity);
        assertThat(entity.getId()).isEqualTo(actual.id());
        assertThat(entity.getPersonnelNumber()).isEqualTo(actual.personnelNumber());
        assertThat(entity.getFIO()).isEqualTo(actual.fullName());
        assertThat(entity.getOrganization().getId()).isEqualTo(actual.organizationId());
        assertThat(entity.getOrganization().getOfficialName()).isEqualTo(actual.organizationName());
        assertThat(entity.getDepartment().getId()).isEqualTo(actual.departmentId());
        assertThat(entity.getDepartment().getDepartmentName()).isEqualTo(actual.departmentName());
    }
}