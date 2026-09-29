package ru.sber.transport.telemechanic.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
                                       .easupId("1111000")
                                       .build();
        var actual = mapper.departmentMessageToDepartment(message);
        assertNotNull(actual);
        assertEquals(actual.getId(), message.getId());
        assertEquals(actual.getOrganization().getId(), message.getOrganizationId());
        assertEquals(actual.getDepartmentName(), message.getDepartmentName());
        assertEquals(actual.getParentId(), message.getParentId());
        assertEquals(actual.getHumanReadableId(), message.getHumanReadableId());
        assertEquals(actual.getEasupId(), message.getEasupId());
        
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
                                   .easupId("1111000")
                                   .build();
        actual = mapper.departmentMessageToDepartment(message);
        assertNotNull(actual);
        assertEquals(actual.getId(), message.getId());
        assertEquals(actual.getOrganization().getId(), message.getOrganizationId());
        assertEquals(actual.getDepartmentName(), message.getDepartmentName());
        assertNull(actual.getParentId());
        assertEquals(actual.getHumanReadableId(), message.getHumanReadableId());
        assertEquals(actual.getEasupId(), message.getEasupId());
    }
}