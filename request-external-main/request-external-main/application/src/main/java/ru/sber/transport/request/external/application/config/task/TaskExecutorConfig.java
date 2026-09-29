package ru.sber.transport.request.external.application.config.task;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.sber.transport.request.external.application.properties.TaskExecutorProperties;

@Configuration
public class TaskExecutorConfig {

    @Bean
    public Executor departmentConsistencyCheckTaskExecutor(
            @Qualifier("departmentConsistencyCheckTaskExecutorProperties") TaskExecutorProperties properties) {
        return createExecutor(properties);
    }

    private ThreadPoolTaskExecutor createExecutor(TaskExecutorProperties props) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(props.getCorePoolSize());
        executor.setMaxPoolSize(props.getMaxPoolSize());
        executor.setQueueCapacity(props.getQueueCapacity());
        executor.setThreadNamePrefix(props.getThreadNamePrefix());
        executor.setRejectedExecutionHandler(getHandler(props.getRejectedExecutionHandler()));
        executor.initialize();
        return executor;
    }

    private RejectedExecutionHandler getHandler(TaskExecutorProperties.RejectedExecutionHandlerType type) {
        return switch (type) {
            case CALLER_RUNS -> new ThreadPoolExecutor.CallerRunsPolicy();
            case ABORT -> new ThreadPoolExecutor.AbortPolicy();
            case DISCARD -> new ThreadPoolExecutor.DiscardPolicy();
            case DISCARD_OLDEST -> new ThreadPoolExecutor.DiscardOldestPolicy();
        };
    }
}
