package ru.sber.transport.notifications.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Конфигурация транзакций для обработки уведомлений.
 */
@Configuration
public class TransactionConfig {

    /**
     * Creates a TransactionTemplate with REQUIRES_NEW propagation for isolated notification processing.
     */
    @Bean
    public TransactionTemplate requiresNewTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
        return template;
    }
}
