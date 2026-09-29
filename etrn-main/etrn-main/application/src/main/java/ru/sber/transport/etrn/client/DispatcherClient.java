package ru.sber.transport.etrn.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import ru.sber.transport.etrn.config.DispatcherClientConfiguration;
import ru.sber.transport.etrn.dto.DispatcherDto;

/**
 * Feign-клиент для взаимодействия с Dispatcher-сервисом.
 */
@FeignClient(
        name = "dispatcher-client",
        url = "${dispatcher.service.url}",
        configuration = DispatcherClientConfiguration.class
)
public interface DispatcherClient {

    /**
     * Получение личного профиля диспетчера (содержит информацию о доверенностях).
     */
    @GetMapping(value = "/self/dispatcher/", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<DispatcherDto> getSelfProfile();
}
