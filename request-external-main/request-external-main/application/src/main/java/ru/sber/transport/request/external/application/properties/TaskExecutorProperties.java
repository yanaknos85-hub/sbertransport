package ru.sber.transport.request.external.application.properties;


import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class TaskExecutorProperties {

    private Integer corePoolSize = 2;
    private Integer maxPoolSize = 4;
    private Integer queueCapacity = 20;
    private String threadNamePrefix = "CustomTaskExecutor-";
    private RejectedExecutionHandlerType rejectedExecutionHandler = RejectedExecutionHandlerType.CALLER_RUNS;

    public enum RejectedExecutionHandlerType {
        CALLER_RUNS,
        ABORT,
        DISCARD,
        DISCARD_OLDEST
    }
}