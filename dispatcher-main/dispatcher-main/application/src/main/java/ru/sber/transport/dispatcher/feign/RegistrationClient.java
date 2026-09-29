package ru.sber.transport.dispatcher.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.sber.transport.dispatcher.dto.feign.RegistrationDataDto;
import ru.sber.transport.dispatcher.dto.feign.RegistrationResponseDto;

@FeignClient(name = "registration", url = "${feign.registration.url:http://authentication}")
public interface RegistrationClient {

    /**
     * Ручка для регистрации ТУЗ
     *
     * @param dto данные для регистрации
     */
    @PostMapping(value = "/registration", consumes = MediaType.APPLICATION_JSON_VALUE)
    RegistrationResponseDto register(@RequestBody RegistrationDataDto dto);

}
