package ru.sber.transport.journal.configuration;

import org.springframework.context.annotation.Import;
import ru.sber.transport.journal.service.impl.JournalServiceImpl;

/**
 * Конфигурация сервисов.
 */
@Import(JournalServiceImpl.class)
public class ServiceConfiguration {
}
