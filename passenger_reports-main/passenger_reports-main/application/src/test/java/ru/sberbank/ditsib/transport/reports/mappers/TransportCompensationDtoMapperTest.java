package ru.sberbank.ditsib.transport.reports.mappers;

import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sberbank.ditsib.transport.reports.dto.publicTransport.TransportCompensationDTO;
import ru.sberbank.ditsib.transport.reports.model.TransportCompensation;

class TransportCompensationDtoMapperTest {
    
    private TransportCompensationDtoMapper transportCompensationDtoMapper = Mappers.getMapper(TransportCompensationDtoMapper.class);
 
    
    @Test
    void transportCompensationToTransportCompensationDto(){
        TransportCompensation expected = Instancio.of(TransportCompensation.class).create();
        TransportCompensationDTO actual = transportCompensationDtoMapper.mapToTransportCompensationDto(expected);
    
        Assertions.assertEquals(expected.getRequest().getId(), actual.getRequestId());
    }
}