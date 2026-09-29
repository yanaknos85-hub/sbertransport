package ru.sber.transport.trip.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.trip.business.model.Dispatcher;
import ru.sber.transport.trip.business.model.Driver;
import ru.sber.transport.trip.business.model.IntegrationClient;
import ru.sber.transport.trip.messaging.providers.DispatcherProvider;
import ru.sber.transport.trip.messaging.providers.DriverProvider;
import ru.sber.transport.trip.messaging.providers.IntegrationClientProvider;

@Configuration
public class CheckAccessConfiguration {

    @Bean
    EmployeeOrganizationFunction checkAccessOrganization(
            DispatcherProvider dispatcherProvider, DriverProvider driverProvider, IntegrationClientProvider integrationClientProvider
    ) {
        return authenticated -> dispatcherProvider.get(authenticated)
                .or(() -> dispatcherProvider.getByOauthId(authenticated))
                .map(Dispatcher::getContractorId)
                .orElse(driverProvider.get(authenticated)
                        .or(() -> driverProvider.getByOauthId(authenticated))
                        .map(Driver::getContractorId)
                        .orElse(integrationClientProvider.get(authenticated)
                                .map(IntegrationClient::getContractorId).orElse(null)));
    }
}
