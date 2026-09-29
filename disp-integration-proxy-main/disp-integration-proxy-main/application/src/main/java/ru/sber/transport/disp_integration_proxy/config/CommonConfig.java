package ru.sber.transport.disp_integration_proxy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.authorization.service.ConsentFunction;
import ru.sber.transport.scripting.reactive.ScriptUtils;

@Configuration
class CommonConfig {

    @Bean
    ObjectMapper objectMapper() {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return mapper;
    }

    @Bean
    public ConsentFunction getConsentFunction() {
        return uuid -> true;
    }

    @Bean
    public ScriptUtils scriptUtils() {
        return new ScriptUtils();
    }
}
