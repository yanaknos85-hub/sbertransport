package ru.sberbank.ditsib.transport.srm.feign;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URI;

/**
 * Черновик Feign клиента для взаимодействия с внешним сервисом
 */
@FeignClient(
        name = "sowa-feign-client"
)
public interface SowaFeignClient {

    @Headers({
            "accept:application/json",
            "content-type:application/json"
    })
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> getDistMatrix(URI uri);
} 