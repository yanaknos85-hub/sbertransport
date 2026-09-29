package ru.sber.transport.common.api.service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.sber.transport.common.api.service.model.dto.*;

import java.net.URI;

@FeignClient(value = "client-client", url = "http://localhost:8080")
public interface ClientClient {

    @GetMapping(
            value = "/info",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<ClientInfoResponse> clientInfo(
            @RequestHeader(value = "contractorUrl") URI contractorUrl,
            @RequestHeader(value = "Authorization") String authorization
    );


    /**
     * GET /cities : Получение справочника городов
     *
     * @param contractorUrl (required)
     * @param authorization (required)
     * @return OK (status code 200)
     */
    @GetMapping(
            value = "/cities",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<CitiesResponse> getCities(
            @RequestHeader(value = "contractorUrl") URI contractorUrl,
            @RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "X-customer-url") String customerUrl
            );


    /**
     * GET /tariffs : Получение справочника тарифов от Контрагента
     *
     * @param contractorUrl (required)
     * @param authorization (required)
     * @param requestParam  (required)
     * @return OK (status code 200)
     */
    @GetMapping(
            value = "/tariffs",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<TariffsResponse> getTariffs(
            @RequestHeader(value = "contractorUrl") URI contractorUrl,
            @RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "X-customer-url") String customerUrl,
            @org.springframework.cloud.openfeign.SpringQueryMap TariffFilterRequest requestParam
    );


    /**
     * GET /health : Проверка состояния API
     *
     * @param contractorUrl (required)
     * @param authorization (required)
     * @return OK (status code 200)
     */
    @GetMapping(
            value = "/health",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<StatusResponse> health(
            @RequestHeader(value = "contractorUrl") URI contractorUrl,
            @RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "X-customer-url") String customerUrl
            );

}
