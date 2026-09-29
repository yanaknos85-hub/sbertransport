package ru.sberbank.ditsib.transport.vehicle;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LoggingExtension implements AfterEachCallback, BeforeEachCallback {

    private Logger logger;
    private ListAppender<ILoggingEvent> appender;
    private final Class<?> clazz;

    public LoggingExtension(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        appender = new ListAppender<>();
        logger = (Logger) LoggerFactory.getLogger(clazz);
        logger.addAppender(appender);
        appender.start();
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        logger.detachAppender(appender);
    }

    public List<ILoggingEvent> getEvents() {
        return appender.list;
    }
}