package ru.sber.transport.dispatcher.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.database.dao.DispatcherRepository;
import ru.sber.transport.dispatcher.database.dao.DriverRepository;
import ru.sber.transport.dispatcher.database.model.Dispatcher;
import ru.sber.transport.dispatcher.database.model.Driver;
import ru.sber.transport.dispatcher.messaging.senders.DispatcherSender;
import ru.sber.transport.dispatcher.messaging.senders.DriverSender;
import ru.sber.transport.user_data_confirmation.message.UserDataConfirmationMessage;

import java.util.Optional;

import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@ExtendWith(MockitoExtension.class)
@Feature("app_platform_dispatcher")
class ContactConfirmationServiceImplTest {

    @InjectMocks
    private ContactConfirmationServiceImpl contactConfirmationService;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private DispatcherRepository dispatcherRepository;

    @Mock
    private DispatcherSender dispatcherSender;

    @Mock
    private DriverSender driverSender;

    @Test
    void testConfirm_WhenDispatcherIsNotNull() {
        // given
        UserDataConfirmationMessage message = Instancio.create(UserDataConfirmationMessage.class);
        Dispatcher dispatcher = Instancio.of(Dispatcher.class)
                .set(field(Dispatcher::getId), message.getId())
                .set(field(Dispatcher::getPhone), message.getPhone())
                .set(field(Dispatcher::isPhoneConfirmed), false)
                .create();

        when(dispatcherRepository.findById(message.getId()))
                .thenReturn(Optional.of(dispatcher));

        when(dispatcherRepository.save(any()))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        // when
        contactConfirmationService.confirm(message);

        // then
        verify(dispatcherRepository, times(1))
                .save(argThat(Dispatcher::isPhoneConfirmed));

        verify(dispatcherSender, times(1))
                .send(argThat(Dispatcher::isPhoneConfirmed));
    }

    @Test
    void testConfirm_WhenDispatcherIsNotNullAndPhoneNotMatches() {
        // given
        UserDataConfirmationMessage message = Instancio.create(UserDataConfirmationMessage.class);
        Dispatcher dispatcher = Instancio.of(Dispatcher.class)
                .set(field(Dispatcher::getId), message.getId())
                .set(field(Dispatcher::getPhone), message.getPhone() + "1")
                .set(field(Dispatcher::isPhoneConfirmed), false)
                .create();

        when(dispatcherRepository.findById(message.getId()))
                .thenReturn(Optional.of(dispatcher));

        // when
        contactConfirmationService.confirm(message);

        // then
        verify(dispatcherRepository, never())
                .save(any());

        verify(dispatcherSender, never())
                .send(any());
    }

    @Test
    void testConfirm_WhenDispatcherIsNull() {
        // given
        UserDataConfirmationMessage message = Instancio.create(UserDataConfirmationMessage.class);
        Driver driver = Instancio.of(Driver.class)
                .set(field(Driver::getId), message.getId())
                .set(field(Driver::getContactPhone), message.getPhone())
                .set(field(Driver::isPhoneConfirmed), false)
                .create();

        when(dispatcherRepository.findById(message.getId()))
                .thenReturn(Optional.empty());

        when(driverRepository.findById(message.getId()))
                .thenReturn(Optional.of(driver));

        when(driverRepository.save(any()))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        // when
        contactConfirmationService.confirm(message);

        // then
        verify(driverSender, times(1))
                .send(argThat(Driver::isPhoneConfirmed), any(), anyBoolean());
    }

    @Test
    void testConfirm_WhenDispatcherIsNullAndDriverIsNotNullAndPhoneNotMatches() {
        // given
        UserDataConfirmationMessage message = Instancio.create(UserDataConfirmationMessage.class);
        Driver driver = Instancio.of(Driver.class)
                .set(field(Driver::getId), message.getId())
                .set(field(Driver::getContactPhone), message.getPhone() + "1")
                .set(field(Driver::isPhoneConfirmed), false)
                .create();

        when(dispatcherRepository.findById(message.getId()))
                .thenReturn(Optional.empty());

        when(driverRepository.findById(message.getId()))
                .thenReturn(Optional.of(driver));

        // when
        contactConfirmationService.confirm(message);

        // then
        verify(driverRepository, never())
                .save(any());

        verify(driverSender, never())
                .send(any(), any(), anyBoolean());
    }

    @Test
    void testConfirm_WhenDispatcherIsNullAndDriverIsNull() {
        // given
        UserDataConfirmationMessage message = Instancio.create(UserDataConfirmationMessage.class);

        when(dispatcherRepository.findById(message.getId()))
                .thenReturn(Optional.empty());

        when(driverRepository.findById(message.getId()))
                .thenReturn(Optional.empty());

        // when
        contactConfirmationService.confirm(message);

        // then
        verify(dispatcherRepository, never())
                .save(any());

        verify(driverRepository, never())
                .save(any());

        verify(driverSender, never())
                .send(any(), any(), anyBoolean());

        verify(dispatcherSender, never())
                .send(any());
    }

}