package ru.sber.transport.telemechanic.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Тест маппера должностей")
class PositionMapperTest {
    
    private final PositionMapper mapper = Mappers.getMapper(PositionMapper.class);
    
    @Test
    void fromMessage() {
        var message = PositionMessage.builder()
                                     .id(UUID.randomUUID())
                                     .deleted(false)
                                     .organizationId(UUID.randomUUID())
                                     .positionName("positionName")
                                     .selfApproved(false)
                                     .build();
        var actual = mapper.positionMessageToPosition(message);
        assertNotNull(actual);
        assertEquals(actual.getId(), message.getId());
        assertEquals(actual.getOrganization().getId(), message.getOrganizationId());
        assertEquals(actual.getPositionName(), message.getPositionName());
    }
}