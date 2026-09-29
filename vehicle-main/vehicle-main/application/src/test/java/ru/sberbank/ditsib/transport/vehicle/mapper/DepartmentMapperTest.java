package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.vehicle.database.model.Department;
import ru.sberbank.ditsib.transport.vehicle.dto.DepartmentDto;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Тест маппера подразделений")
class DepartmentMapperTest {

    private final DepartmentMapper mapper = Mappers.getMapper(DepartmentMapper.class);

    @Test
    void fromMessage() {
        var message = DepartmentMessage.builder()
                .id(UUID.randomUUID())
                .code("code")
                .deleted(false)
                .departmentHeadId(UUID.randomUUID())
                .departmentName("departmentName")
                .humanReadableId("humanReadableId")
                .location("location")
                .organizationId(UUID.randomUUID())
                .parentId(UUID.randomUUID())
                .build();
        var actual = mapper.departmentMessageToDepartment(message);
        assertNotNull(actual);
        assertEquals(actual.getId(), message.getId());
        assertEquals(actual.getOrganization().getId(), message.getOrganizationId());
        assertEquals(actual.getDepartmentName(), message.getDepartmentName());
        assertEquals(actual.getParent().getId(), message.getParentId());
        assertEquals(actual.getHumanReadableId(), message.getHumanReadableId());

        message = DepartmentMessage.builder()
                .id(UUID.randomUUID())
                .code("code")
                .deleted(false)
                .departmentHeadId(UUID.randomUUID())
                .departmentName("departmentName")
                .humanReadableId("humanReadableId")
                .location("location")
                .organizationId(UUID.randomUUID())
                .parentId(null)
                .build();
        actual = mapper.departmentMessageToDepartment(message);
        assertNotNull(actual);
        assertEquals(actual.getId(), message.getId());
        assertEquals(actual.getOrganization().getId(), message.getOrganizationId());
        assertEquals(actual.getDepartmentName(), message.getDepartmentName());
        assertNull(actual.getParent());
        assertEquals(actual.getHumanReadableId(), message.getHumanReadableId());
    }

    @Test
    void departmentToDepartmentDto() {
        var department = Instancio.of(Department.class)
                .set(Select.field(Department::getParent), Instancio.create(Department.class))
                .create();
        var expected = new DepartmentDto(department.getId(), department.getDepartmentName(), department.getParent().getId());
        var actual = mapper.departmentToDepartmentDto(department);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void departmentToDepartmentIds() {
        var departments = Instancio.ofSet(Department.class).create();
        var result = mapper.departmentsToDepartmentUUIDs(departments);

        assertEquals(result, departments.stream().map(Department::getId).collect(Collectors.toSet()));
    }

    @ParameterizedTest
    @MethodSource("departmentProvider")
    void departmentToDepartmentUUID(Department department) {
        var result = mapper.departmentToDepartmentUUID(department);

        if (department == null) {
            assertNull(result);
        } else assertEquals(result, department.getId());
    }

    @ParameterizedTest
    @MethodSource("uuidProvider")
    void uuidToDepartment(UUID uuid) {
        var result = mapper.uuidToDepartment(uuid);

        if (uuid == null) {
            assertNull(result);
        } else assertEquals(result.getId(), uuid);
    }

    static Stream<UUID> uuidProvider() {
        return Stream.of(UUID.randomUUID(), null);
    }

    static Stream<Department> departmentProvider() {
        return Stream.of(Instancio.create(Department.class), null);
    }

}
