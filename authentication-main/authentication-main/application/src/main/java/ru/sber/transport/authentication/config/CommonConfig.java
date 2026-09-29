package ru.sber.transport.authentication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.scripting.reactive.ScriptUtils;

@Configuration
class CommonConfig {

    @Bean
    public ScriptUtils scriptUtils() {
        return new ScriptUtils();
    }

}
