package ru.sber.transport.common.api.service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.sber.transport.common.api.service.model.dto.Authorization;

import java.net.URI;

@FeignClient(value = "auth-client")
public interface AuthorizationClient {

    /**
     * GET /auth : Аутентификация в системе Контрагента
     *
     * @param contractorUrl (required)
     * @param authorization (required)
     * @return OK (status code 200)
     */
    @GetMapping(
            value = "/auth",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Authorization> auth(
            URI contractorUrl,
            @RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "X-customer-url") String customerUrl
    );

}
