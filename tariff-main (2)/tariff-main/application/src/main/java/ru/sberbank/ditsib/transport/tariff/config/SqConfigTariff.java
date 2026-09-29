package ru.sberbank.ditsib.transport.tariff.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.humanreadableid.dao.AbstractRepository;
import ru.sber.transport.humanreadableid.service.CompanySQService;
import ru.sber.transport.humanreadableid.service.HumanReadbaleIdFormatter;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.humanreadableid.service.impl.SQGeneratorImpl;
import ru.sberbank.ditsib.transport.tariff.humanReadableId.model.CompanySQRequest;

@Configuration
public class SqConfigTariff {
    
    @Bean
    @Qualifier("sQGeneratorTariff")
    public SQGenerator sQGenerator(
            CompanySQService sqService, AbstractRepository<CompanySQRequest> companySQRepositoryLimits,
            HumanReadbaleIdFormatter humanReadbaleIdFormatter
                                  ) {
        return new SQGeneratorImpl(sqService, companySQRepositoryLimits, humanReadbaleIdFormatter);
    }
    
}
