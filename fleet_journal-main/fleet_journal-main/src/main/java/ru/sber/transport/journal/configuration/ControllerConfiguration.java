package ru.sber.transport.journal.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.sber.transport.journal.controller.impl.JournalControllerImpl;

/**
 * Конфигурация контроллеров.
 */
@Configuration
@Import(JournalControllerImpl.class)
public class ControllerConfiguration {
}
