package ru.sberbank.ditsib.transport.request.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.dao.TypedRequestRepository;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static java.util.concurrent.ThreadPoolExecutor.*;

@Slf4j
@Configuration
public class BeanConfig {

    @Bean
    public Clock clock() {
        log.info("Clock configured to UTC");
        return Clock.systemUTC();
    }

    @Bean
    public Executor fraudAsyncExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("fraud-async-");
        executor.setRejectedExecutionHandler(new CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    public Map<TransportTypeEnum, RequestSender<Request>> requestSenderMap(List<RequestSender<? extends Request>> requestSenders) {
        log.info("{} request senders configured", requestSenders.size());
        return requestSenders.stream()
                .collect(Collectors.toMap(RequestSender::type, ReflectionUtils::cast));
    }

    @Bean
    public Map<TransportTypeEnum, TypedRequestRepository<Request>> requestRepositoriesMap(List<TypedRequestRepository<? extends Request>> repositories) {
        log.info("{} repositories configured", repositories.size());
        return repositories.stream()
                .collect(Collectors.toMap(TypedRequestRepository::type, ReflectionUtils::cast));
    }

}
