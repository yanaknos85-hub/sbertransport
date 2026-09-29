package ru.sber.transport.common.api.service.config;

import com.fasterxml.jackson.databind.Module;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.openfeign.support.PageJacksonModule;
import org.springframework.cloud.openfeign.support.SortJacksonModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.ditsib.encription.PasswordEncryption;

@Configuration
@RequiredArgsConstructor
public class Config {

    @Bean
    public PasswordEncryption passwordEncryption() {
        return new PasswordEncryption();
    }

    @Bean
    public Module pageJacksonModule() {
        return new PageJacksonModule();
    }

    @Bean
    public Module sortJacksonModule() {
        return new SortJacksonModule();
    }

}
