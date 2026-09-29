package ru.sber.transport.trip.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.authorization.service.ConsentFunction;
import ru.sber.transport.trip.messaging.providers.DispatcherProvider;
import ru.sber.transport.trip.messaging.providers.DriverProvider;

@Configuration
public class ConsentFunctionConfig {

    @Value("${dispatcher-room.admin.role:ROLE_DISPATCHER_ROOM_ADMIN}")
    private String dispatcherRoomAdminRole;

    @Bean
    public ConsentFunction getConsentFunction(DriverProvider driverProvider, DispatcherProvider dispatcherProvider) {
        return model -> {
            if(!model.getRoles().isEmpty() && model.getRoles().contains(dispatcherRoomAdminRole)){
                return true;
            }
            var driver = driverProvider.get(model.getId())
                    .orElseGet(() -> driverProvider.getByOauthId(model.getId()).orElse(null));
            var dispatcher = dispatcherProvider.get(model.getId())
                    .orElseGet(() -> dispatcherProvider.getByOauthId(model.getId()).orElse(null));
            if(driver != null){
                return driver.getOauthId() != null || driver.isConsent();
            }
            if(dispatcher != null){
                return dispatcher.getOauthId() != null || dispatcher.isConsent();
            }
            return false;
        };
    }
}
