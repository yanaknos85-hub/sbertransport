package ru.sber.transport.dispatcher.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.dispatcher.database.model.Driver;
import ru.sber.transport.dispatcher.mappers.DriverMapper;
import ru.sber.transport.dispatcher.messages.Source;
import ru.sber.transport.dispatcher.messaging.senders.DriverSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;

import java.util.List;
import java.util.Map;

/**
 * Реализация отправителя данных.
 */
@Component
@RequiredArgsConstructor
public class DriverSenderImpl implements DriverSender {

    @Qualifier("usersOutput")
    private final ObjectProvider<OutputBridge> usersOutput;

    @Qualifier("usersOutputSsl")
    private final ObjectProvider<OutputBridge> usersOutputSsl;

    @Qualifier("driverOutput")
    private final ObjectProvider<OutputBridge> driverOutput;

    @Qualifier("driverOutputSsl")
    private final ObjectProvider<OutputBridge> driverOutputSsl;

    private final DriverMapper driverMapper;

    @Override
    public void send(Driver driver, Source source, boolean isUserChanging) {
        if(isUserChanging && driver.getOauthId() == null) {
            var active = driver.isActive();
            var message = UserMessage.builder()
                    .id(driver.getId())
                    .deleted(!active)
                    .active(active)
                    .orgStructureType("EXTERNAL")
                    .email(driver.getEmail())
                    .phone(driver.getContactPhone())
                    .lastName(driver.getLastName())
                    .firstName(driver.getFirstName())
                    .patronymic(driver.getPatronymic())
                    .consent(driver.isConsent())
                    .scope(UserMessage.Scope.DRIVER)
                    .build();

            usersOutput.ifAvailable(ob -> ob.send(message, Map.of(UserMessage.TYPE, message.orgStructureType())));
            usersOutputSsl.ifAvailable(ob -> ob.send(message, Map.of(UserMessage.TYPE, message.orgStructureType())));
        }

        var driverMessage = driverMapper.toDriverMessage(driver);
        driverOutput.ifAvailable(ob -> ob.send(driverMessage, Map.of("source", source.name())));
        driverOutputSsl.ifAvailable(ob -> ob.send(driverMessage, Map.of("source", source.name())));
    }

    @Override
    public void sendAll(List<Driver> drivers) {
        drivers.forEach(driver -> send(driver, Source.CONTRACTOR, true));
    }
}
