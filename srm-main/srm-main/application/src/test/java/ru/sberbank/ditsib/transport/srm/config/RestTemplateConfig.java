package ru.sberbank.ditsib.transport.srm.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import ru.sber.transport.exchange.RestTemplateConfiguration;

@TestConfiguration
@EnableAutoConfiguration(exclude = RestTemplateConfiguration.class)
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate secureRestTemplate() {
        return new RestTemplate();
    }
    
}
