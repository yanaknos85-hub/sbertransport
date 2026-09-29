package ru.sberbank.ditsib.transport.vehicle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class TaskExecutorConfig {
    
    @Bean
    public TaskExecutor taskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1); //default: 1
        executor.setMaxPoolSize(1); //default: Integer.MAX_VALUE
        executor.setQueueCapacity(1); // default: Integer.MAX_VALUE
        executor.setKeepAliveSeconds(120); // default: 60 seconds
        executor.initialize();
        return executor;
    }
}
