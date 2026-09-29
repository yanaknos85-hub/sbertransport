package ru.sber.transport.telemechanic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ru.sber.transport.humanreadableid.service.CompanySQService;
import ru.sber.transport.humanreadableid.service.HumanReadbaleIdFormatter;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.humanreadableid.service.impl.SQGeneratorImpl;
import ru.sber.transport.telemechanic.database.human_readable_id.dao.CompanySQRepository;

/**
 * Конфиг необходим для того чтобы ApplicationContextProvider попал в контекст и получил ApplicationContext, который
 * нужен при инициализации валидатора
 */
@Configuration
public class SqConfig {
    @Bean
    @Primary
    SQGenerator sQGenerator(
            CompanySQService sqService, CompanySQRepository companySQRepository,
            HumanReadbaleIdFormatter humanReadbaleIdFormatter) {
        return new SQGeneratorImpl(sqService, companySQRepository, humanReadbaleIdFormatter);
    }
}
