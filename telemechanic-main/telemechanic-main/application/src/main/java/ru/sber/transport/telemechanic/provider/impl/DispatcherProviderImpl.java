package ru.sber.transport.telemechanic.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.telemechanic.messaging.listener.message.DispatcherMessage;
import ru.sber.transport.telemechanic.provider.DispatcherProvider;
import ru.sber.transport.telemechanic.service.DispatcherService;

@Component
@RequiredArgsConstructor
@Slf4j
public class DispatcherProviderImpl implements DispatcherProvider {
    
    public static final String NO_EWB_CREATION_POSSIBILITY_MESSAGE = "Cant' save dispatcher id: {}, phone: {}, " +
                                                                     "EWB creation possibility is {}";
    private final DispatcherService dispatcherService;
    
    @Override
    public void save(DispatcherMessage message) {
        if (!Boolean.TRUE.equals(message.ewbCreationPossibility())) {
            log.info(NO_EWB_CREATION_POSSIBILITY_MESSAGE, message.getId(), message.phone(), message.ewbCreationPossibility());
            return;
        }
        dispatcherService.saveMessage(message);
    }
}
