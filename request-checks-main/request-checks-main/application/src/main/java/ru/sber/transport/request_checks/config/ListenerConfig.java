package ru.sber.transport.request_checks.config;

import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sber.transport.request_checks.messaging.listeners.ExternalTripRequestListener;
import ru.sber.transport.request_checks.messaging.listeners.TripRequestListener;
import ru.sber.transport.request_checks.messaging.message.ExternalRequestMessage;
import ru.sber.transport.request_checks.service.TripRequestService;

/**
 * Конфигурация приложения для обмена сообщениями между микросервисами
 */
@Slf4j
@Configuration
@NoAuthorize("/ws/requests")
public class ListenerConfig {

    /**
     * Слушатель внешних заявок на поездки
     *
     * @return Слушатель внешних заявок на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    public Consumer<Message<ExternalRequestMessage>> externalRequestInput(TripRequestService tripRequestService) {
        log.info("Creating request external listener");
        return new ExternalTripRequestListener(tripRequestService);
    }

    /**
     * Слушатель внешних заявок на поездки (SSL)
     *
     * @return Слушатель внешних заявок на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    public Consumer<Message<ExternalRequestMessage>> externalRequestInputSsl(TripRequestService tripRequestService) {
        log.info("Creating request external SSL listener");
        return new ExternalTripRequestListener(tripRequestService);
    }

    /**
     * Слушатель заявок на поездки
     *
     * @return Слушатель заявок на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    public Consumer<Message<RequestMessage>> requests(TripRequestService tripRequestService) {
        log.info("Creating request listener");
        return new TripRequestListener(tripRequestService);
    }

    /**
     * Слушатель заявок на поездки
     *
     * @return Слушатель заявок на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    public Consumer<Message<RequestMessage>> requestsSsl(TripRequestService tripRequestsService) {
        log.info("Creating trip request SSL listener");
        return new TripRequestListener(tripRequestsService);
    }

}
