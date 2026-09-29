package ru.sberbank.ditsib.transport.request.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ru.sber.transport.humanreadableid.dao.AbstractRepository;
import ru.sber.transport.humanreadableid.service.CompanySQService;
import ru.sber.transport.humanreadableid.service.HumanReadbaleIdFormatter;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.humanreadableid.service.impl.SQGeneratorImpl;
import ru.sberbank.ditsib.transport.request.human_readable_id.model.CompanySQRequest;

@Configuration
public class SqConfig {
    @Bean
    @Primary
    public SQGenerator sQGenerator(
            CompanySQService sqService, AbstractRepository<CompanySQRequest> companySQRepositoryLimits,
            HumanReadbaleIdFormatter humanReadbaleIdFormatter
                                  ) {
        return new SQGeneratorImpl(sqService, companySQRepositoryLimits, humanReadbaleIdFormatter);
    }
}
