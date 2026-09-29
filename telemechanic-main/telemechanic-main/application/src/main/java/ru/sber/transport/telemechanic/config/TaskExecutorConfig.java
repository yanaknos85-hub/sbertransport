package ru.sber.transport.telemechanic.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ConditionalOnProperty(value = "task.executor.config.enabled", matchIfMissing = true)
public class TaskExecutorConfig {
    
    @Bean
    public TaskExecutor taskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1); // default: 1
        executor.setMaxPoolSize(1); // default: Integer.MAX_VALUE
        executor.setQueueCapacity(1); // default: Integer.MAX_VALUE
        executor.setKeepAliveSeconds(120); // default: 60
        executor.initialize();
        return executor;
    }
}
