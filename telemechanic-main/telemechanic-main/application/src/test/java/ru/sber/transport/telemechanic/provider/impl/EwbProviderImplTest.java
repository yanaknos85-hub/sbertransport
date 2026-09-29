package ru.sber.transport.telemechanic.provider.impl;

import ch.qos.logback.classic.Level;
import org.assertj.core.groups.Tuple;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.commons.JUnitException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.FirstTitleCreateRequestMessage;
import ru.sber.transport.telemechanic.FirstTitleCreateResponseMessage;
import ru.sber.transport.telemechanic.LoggingExtension;
import ru.sber.transport.telemechanic.database.model.Driver;
import ru.sber.transport.telemechanic.dto.ewb.FirstTitleDto;
import ru.sber.transport.telemechanic.messaging.sender.FirstTitleCreateRequestSender;
import ru.sber.transport.telemechanic.service.DriverService;
import ru.sber.transport.telemechanic.service.EwbService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EwbProviderImplTest {
    
    @InjectMocks
    private EwbProviderImpl ewbProvider;
    @Mock
    private EwbService ewbService;
    @Mock
    private DriverService driverService;
    @Mock
    private FirstTitleCreateRequestSender firstTitleCreateRequestSender;
    @Captor
    private ArgumentCaptor<FirstTitleCreateResponseMessage> firstTitleCreateResponseMessageArgumentCaptor;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(EwbProviderImpl.class);
    
    @Test
    void save_success() {
        var message = Instancio.create(FirstTitleCreateRequestMessage.class);
        var driver = Instancio.create(Driver.class);
        var expectedResponse = new FirstTitleCreateResponseMessage(message.id(), null);
        
        doReturn(driver).when(driverService).getByEmployeeId(message.driverEmployeeId());
        doNothing().when(ewbService).sendAndSaveFirstTitle(any(), any(UUID.class));
        doNothing().when(firstTitleCreateRequestSender).send(firstTitleCreateResponseMessageArgumentCaptor.capture());
        
        ewbProvider.save(message);
        
        assertThat(firstTitleCreateResponseMessageArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);
        
        verify(driverService).getByEmployeeId(any(UUID.class));
        verify(ewbService).sendAndSaveFirstTitle(any(FirstTitleDto.class), any(UUID.class));
        verify(firstTitleCreateRequestSender).send(any(FirstTitleCreateResponseMessage.class));
        
        LOGGING_EXTENSION.assertLogEvents(2, new Tuple[] {
                tuple(
                        Level.INFO,
                        "Start save first title, message id:" + message.id(),
                        false
                     ),
                tuple(
                        Level.INFO,
                        "Finish save first title, message id:" + message.id(),
                        false
                     )
        });
    }
    
    @Test
    void save_exception() {
        var message = Instancio.create(FirstTitleCreateRequestMessage.class);
        var driver = Instancio.create(Driver.class);
        var errorText = "Test error";
        var expectedResponse = new FirstTitleCreateResponseMessage(message.id(), errorText);
        
        doReturn(driver).when(driverService).getByEmployeeId(message.driverEmployeeId());
        doThrow(new JUnitException(errorText)).when(ewbService).sendAndSaveFirstTitle(any(), any(UUID.class));
        doNothing().when(firstTitleCreateRequestSender).send(firstTitleCreateResponseMessageArgumentCaptor.capture());
        
        ewbProvider.save(message);
        
        assertThat(firstTitleCreateResponseMessageArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);
        
        verify(driverService).getByEmployeeId(any(UUID.class));
        verify(ewbService).sendAndSaveFirstTitle(any(FirstTitleDto.class), any(UUID.class));
        verify(firstTitleCreateRequestSender).send(any(FirstTitleCreateResponseMessage.class));
        
        LOGGING_EXTENSION.assertLogEvents(3, new Tuple[] {
                tuple(
                        Level.INFO,
                        "Start save first title, message id:" + message.id(),
                        false
                     ),
                tuple(
                        Level.ERROR,
                        "Test error",
                        true
                     ),
                tuple(
                        Level.INFO,
                        "Finish save first title, message id:" + message.id(),
                        false
                     )
        });
    }
}