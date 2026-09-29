package ru.sber.transport.dispatcher.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.dispatcher.database.dao.DispatcherRepository;
import ru.sber.transport.dispatcher.database.dao.DriverRepository;
import ru.sber.transport.dispatcher.messages.Source;
import ru.sber.transport.dispatcher.messaging.senders.DispatcherSender;
import ru.sber.transport.dispatcher.messaging.senders.DriverSender;
import ru.sber.transport.dispatcher.service.ContactConfirmationService;
import ru.sber.transport.user_data_confirmation.message.UserDataConfirmationMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContactConfirmationServiceImpl implements ContactConfirmationService {

    private final DriverRepository driverRepository;

    private final DispatcherRepository dispatcherRepository;

    private final DispatcherSender dispatcherSender;
    private final DriverSender driverSender;

    @Override
    public void confirm(UserDataConfirmationMessage message) {
        var dispatcher = dispatcherRepository.findById(message.getId()).orElse(null);
        if (dispatcher != null) {
            if (dispatcher.getPhone().equals(message.getPhone())) {
                dispatcher.setPhoneConfirmed(true);
                var saved = dispatcherRepository.save(dispatcher);
                dispatcherSender.send(saved);
            } else {
                logWarn(dispatcher.getPhone(), message.getPhone());
            }
        } else {
            var driver = driverRepository.findById(message.getId()).orElse(null);
            if (driver != null) {
                if (driver.getContactPhone().equals(message.getPhone())) {
                    driver.setPhoneConfirmed(true);
                    var saved = driverRepository.save(driver);
                    driverSender.send(saved, Source.CONTRACTOR, false);
                } else {
                    logWarn(driver.getContactPhone(), message.getPhone());
                }
            } else {
                log.warn("Employee not found by id {}", message.getId());
            }
        }
    }

    private void logWarn(String actualPhone, String receivedPhone) {
        log.warn("Phone can not be confirmed, actual phone number is different. Actual: {}, received: {}",
                actualPhone, receivedPhone);
    }
}
