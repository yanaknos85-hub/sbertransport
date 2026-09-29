package ru.sber.transport.telemechanic.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.telemechanic.database.model.Transport;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransportMapperTest {
    
    private final TransportMapper mapper = Mappers.getMapper(TransportMapper.class);
    
    @Test
    void transportToGetTransportResponse() {
        var transport = Instancio.of(Transport.class).create();
        
        var actual = mapper.transportToGetTransportResponse(transport);
        
        assertEquals(transport.getId(), actual.id());
        assertEquals(transport.getStateNumber(), actual.stateNumber());
        assertEquals(transport.getBrand(), actual.brand());
        assertEquals(transport.getModel(), actual.model());
        assertEquals(transport.getType(), actual.transportType());
    }
    
}