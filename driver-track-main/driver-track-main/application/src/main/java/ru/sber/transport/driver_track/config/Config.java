package ru.sber.transport.driver_track.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.authorization.service.ConsentFunction;
import ru.sber.transport.driver_track.repository.DriverRepository;
import ru.sber.transport.scripting.reactive.ScriptUtils;

@Configuration
public class Config {

    @Bean
    public ConsentFunction getConsentFunction(DriverRepository driverRepository) {
        return uuid -> driverRepository.getByIdNotNull(uuid.getId()).getConsent();
    }

    @Bean
    public ScriptUtils scriptUtils() {
        return new ScriptUtils();
    }
}
