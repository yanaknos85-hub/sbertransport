package ru.sberbank.ditsib.transport.helper;


import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Утильный класс для подселения контекста в бины, которые инициализирует hibernate
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {
    
    private static final AtomicReference<ApplicationContext> CONTEXT = new AtomicReference<>();
    
    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        ApplicationContextProvider.CONTEXT.set(context); // NOSONAR
    }

    /**
     * Получение бина.
     *
     * @param clazz класс бина.
     * @param <T> тип бина.
     * @return бин.
     */
    public static <T> T getBean(Class<T> clazz) {
        return ApplicationContextProvider.CONTEXT.get().getBean(clazz);
    }

    /**
     * Получение бина.
     *
     * @param qualifier имя бина.
     * @param clazz класс бина.
     * @param <T> тип бина.
     * @return бин.
     */
    public static <T> T getBean(String qualifier, Class<T> clazz) {
        return ApplicationContextProvider.CONTEXT.get().getBean(qualifier , clazz);
    }
}

