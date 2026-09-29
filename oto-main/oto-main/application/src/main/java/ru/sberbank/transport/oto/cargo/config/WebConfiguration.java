package ru.sberbank.transport.oto.cargo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.sberbank.ditsib.converters.DurationMillisConverter;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisDurationConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

/**
 * Web configuration.
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry formatterRegistry) {
        formatterRegistry.addConverter(new MillisDurationConverter());
        formatterRegistry.addConverter(new DurationMillisConverter());
        formatterRegistry.addConverter(new LocalDateTimeMillisConverter());
        formatterRegistry.addConverter(new MillisLocalDateTimeConverter());
    }

}
