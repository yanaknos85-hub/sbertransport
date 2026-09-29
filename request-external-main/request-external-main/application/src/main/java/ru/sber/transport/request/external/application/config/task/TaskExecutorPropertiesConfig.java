package ru.sber.transport.request.external.application.config.task;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.request.external.application.properties.TaskExecutorProperties;

@Configuration
public class TaskExecutorPropertiesConfig {

    @Bean("departmentConsistencyCheckTaskExecutorProperties")
    @ConfigurationProperties("task-executor.department-consistency-check")
    public TaskExecutorProperties departmentConsistencyCheckProperties() {
        return new TaskExecutorProperties();
    }
}