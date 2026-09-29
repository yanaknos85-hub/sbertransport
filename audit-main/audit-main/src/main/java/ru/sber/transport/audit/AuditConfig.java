package ru.sber.transport.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ru.sber.transport.audit.aspect.AuditedAspect;
import ru.sber.transport.audit.writer.AuditWriter;

/**
 * Конфиг аудита.
 */
@EnableAspectJAutoProxy
@Slf4j
public class AuditConfig {

    @Bean
    AuditedAspect auditedAspect(AuditWriter auditWriter) {
        return new AuditedAspect(auditWriter);
    }

}
