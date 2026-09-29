package ru.sber.transport.dispatcher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.ditsib.encription.PasswordEncryption;
import ru.sber.transport.authorization.service.ConsentFunction;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.dispatcher.database.dao.DispatcherRepository;
import ru.sber.transport.dispatcher.database.dao.DriverRepository;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.database.model.Dispatcher;
import ru.sber.transport.dispatcher.database.model.Driver;
import ru.sber.transport.scripting.reactive.ScriptUtils;

@Configuration
public class CommonConfig {

    @Value("${dispatcher-room.admin.role:ROLE_DISPATCHER_ROOM_ADMIN}")
    private String dispatcherRoomAdminRole;

    @Bean
    public ScriptUtils scriptUtils() {
        return new ScriptUtils();
    }

    @Bean
    public PasswordEncryption passwordEncryption() {
        return new PasswordEncryption();
    }

    @Bean
    EmployeeOrganizationFunction checkAccessOrganization(
            DispatcherRepository dispatcherRepository, DriverRepository driverRepository
    ) {
        return authenticated -> dispatcherRepository.findById(authenticated)
                .or(() -> dispatcherRepository.findByOauthId(authenticated))
                .map(Dispatcher::getContractor)
                .map(Contractor::getId)
                .orElse(driverRepository.findById(authenticated)
                        .or(() -> driverRepository.findByOauthId(authenticated))
                        .map(Driver::getContractor)
                        .map(Contractor::getId).orElse(null));
    }

    @Bean
    public ConsentFunction getConsentFunction(DriverRepository driverProvider, DispatcherRepository dispatcherProvider) {
        return model -> {
            if(!model.getRoles().isEmpty() && model.getRoles().contains(dispatcherRoomAdminRole)){
                return true;
            }
            var driver = driverProvider.findById(model.getId())
                    .orElseGet(() -> driverProvider.findByOauthId(model.getId()).orElse(null));
            var dispatcher = dispatcherProvider.findById(model.getId())
                    .orElseGet(() -> dispatcherProvider.findByOauthId(model.getId()).orElse(null));
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
