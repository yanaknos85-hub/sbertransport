package ru.sber.transport.telemechanic.provider.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.database.dao.OrganizationRepository;
import ru.sber.transport.telemechanic.database.model.Transport;
import ru.sber.transport.telemechanic.messaging.sender.message.TransportMessage;
import ru.sber.transport.telemechanic.service.TransportService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransportProviderImplTest {
    
    @Mock
    private TransportService transportService;
    @Mock
    private OrganizationRepository organizationRepository;
    @InjectMocks
    private TransportProviderImpl transportProvider;
    
    @Test
    void delete() {
        doReturn(Optional.of(Instancio.create(Transport.class))).when(transportService).get(any(UUID.class));
        doNothing().when(transportService).deactivate(any(Transport.class));
        transportProvider.delete(Instancio.create(TransportMessage.class));
        verify(transportService, times(1)).deactivate(any(Transport.class));
        
        doReturn(Optional.empty()).when(transportService).get(any(UUID.class));
        transportProvider.delete(Instancio.create(TransportMessage.class));
        verify(transportService, times(1)).deactivate(any(Transport.class));
    }
    
    @ParameterizedTest
    @MethodSource("saveSource")
    void save(List<UUID> orgIds, UUID contractorId, UUID autoparkId, boolean withSaving, int count) {
        var transportMessage = Instancio.of(TransportMessage.class)
                                    .set(field(TransportMessage::organizationIds), orgIds)
                                    .set(field(TransportMessage::autoparkId), autoparkId)
                                    .set(field(TransportMessage::contractorId), contractorId)
                                    .set(field(TransportMessage::deleted), false)
                                    .create();
        if(withSaving){
            doNothing().when(transportService).saveOrUpdate(any(Transport.class));
            if(!orgIds.isEmpty()){
                when(organizationRepository.findAllById(anyList())).thenReturn(anyList());
            }
        }
        transportProvider.save(transportMessage);
        verify(transportService, times(count)).saveOrUpdate(any(Transport.class));
    }
    
    static Stream<Arguments> saveSource() {
        return Stream.of(
                Arguments.of(List.of(UUID.randomUUID()), null, null, true, 1),
                Arguments.of(Collections.emptyList(), UUID.randomUUID(), UUID.randomUUID(), true, 1),
                Arguments.of(List.of(UUID.randomUUID()), UUID.randomUUID(), UUID.randomUUID(), false, 0),
                Arguments.of(Collections.emptyList(), null, null, false, 0)
                        );
    }
}