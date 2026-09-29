package ru.sber.transport.telemechanic.provider.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.telemechanic.messaging.listener.message.DriverMessage;
import ru.sber.transport.telemechanic.provider.DriverProvider;
import ru.sber.transport.telemechanic.service.DriverService;

@Component
@RequiredArgsConstructor
public class DriverProviderImpl implements DriverProvider {
    
    private final DriverService driverService;
    
    @Override
    public void save(DriverMessage message) {
        driverService.saveMessage(message);
    }
}
