package ru.sberbank.ditsib.transport.tariff.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.sberbank.ditsib.converters.*;

/**
 * Web configuration.
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    
    @Override
    public void addFormatters(FormatterRegistry formatterRegistry) {
        formatterRegistry.addConverter(new MillisDurationConverter());
        formatterRegistry.addConverter(new DurationMillisConverter());
        formatterRegistry.addConverter(new LocalDateSerializer());
        formatterRegistry.addConverter(new LocalDateDeserializer());
        formatterRegistry.addConverter(new MillisLocalDateTimeConverter());
    }
    
}
