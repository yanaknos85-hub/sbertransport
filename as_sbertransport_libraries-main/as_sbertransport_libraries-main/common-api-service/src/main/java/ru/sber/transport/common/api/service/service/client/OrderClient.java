package ru.sber.transport.common.api.service.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.common.api.service.model.dto.*;

import jakarta.validation.Valid;
import ru.sber.transport.common.api.service.model.dto.cargo.CargoOrderRequest;

import java.net.URI;
import java.util.List;

@FeignClient(value = "order-client")
public interface OrderClient {

    /**
     * POST /orders/{orderParthnerID}/cancel : Отмена заказа у Контрагента
     *
     * @param contractorUrl   (required)
     * @param authorization   (required)
     * @param orderParthnerID (required)
     * @param body            (optional)
     * @return OK (status code 200)
     */
    @PostMapping(
            value = "/orders/{orderParthnerID}/cancel",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<CancelOrderResponse> cancel(
            URI contractorUrl,
            @RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "X-customer-url") String customerUrl,
            @PathVariable("orderParthnerID") String orderParthnerID,
            @Valid @RequestBody(required = false) String body
    );


    /**
     * POST /orders : Создание заказа
     *
     * @param contractorUrl (required)
     * @param authorization (required)
     * @param orderRequest  (required)
     * @return OK (status code 200)
     */
    @PostMapping(
            value = "/orders",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<OrderResponse> createOrder(
            @RequestHeader(value = "contractorUrl") URI contractorUrl,
            @RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "X-customer-url") String customerUrl,
            @Valid @RequestBody OrderRequest orderRequest
    );

    /**
     * POST /cargo/orders : Создание заказа на грузоперевозку
     *
     * @param contractorUrl (required)
     * @param authorization (required)
     * @param cargoOrderRequest  (required)
     * @return OK (status code 200)
     */
    @PostMapping(
            value = "/cargo/orders",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<List<OrderResponse>> createOrder(
            @RequestHeader(value = "contractorUrl") URI contractorUrl,
            @RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "X-customer-url") String customerUrl,
            @Valid @RequestBody List<CargoOrderRequest> cargoOrderRequest
    );


    /**
     * GET /orders/{orderParthnerID}/location : Получение местоположения водителя Контрагента
     *
     * @param contractorUrl   (required)
     * @param authorization   (required)
     * @param orderParthnerID (required)
     * @return OK (status code 200)
     */
    @GetMapping(
            value = "/orders/{orderParthnerID}/location",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<LocationResponse> location(
            @RequestHeader(value = "contractorUrl") URI contractorUrl,
            @RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "X-customer-url") String customerUrl,
            @PathVariable("orderParthnerID") String orderParthnerID
    );


    /**
     * GET /orders/{orderParthnerID} : Получение информации о заказе от Контрагента
     *
     * @param contractorUrl   (required)
     * @param authorization   (required)
     * @param orderParthnerID (required)
     * @return OK (status code 200)
     */
    @GetMapping(
            value = "/orders/{orderParthnerID}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<OrderInfoResponse> orderInfo(
            @RequestHeader(value = "contractorUrl") URI contractorUrl,
            @RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "X-customer-url") String customerUrl,
            @PathVariable("orderParthnerID") String orderParthnerID
    );


    /**
     * GET /orders : Получение списка заказов от Контрагента
     *
     * @param contractorUrl (required)
     * @param authorization (required)
     * @param requestParam  (optional)
     * @return OK (status code 200)
     */
    @GetMapping(
            value = "/orders",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<ShortOrderInfoResponse> ordersInfo(
            @RequestHeader(value = "contractorUrl") URI contractorUrl,
            @RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "X-customer-url") String customerUrl,
            @Valid @org.springframework.cloud.openfeign.SpringQueryMap OrderFilterRequest requestParam
    );


    /**
     * GET /orders/{orderParthnerID}/route : Получение маршрута поездки у Контрагента
     *
     * @param contractorUrl   (required)
     * @param authorization   (required)
     * @param orderParthnerID (required)
     * @return OK (status code 200)
     */
    @GetMapping(
            value = "/orders/{orderParthnerID}/route",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<RouteResponse> route(
            @RequestHeader(value = "contractorUrl") URI contractorUrl,
            @RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "X-customer-url") String customerUrl,
            @PathVariable("orderParthnerID") String orderParthnerID
    );


    /**
     * PUT /orders/{orderParthnerID} : Изменение заказа у Контрагента
     *
     * @param contractorUrl      (required)
     * @param authorization      (required)
     * @param orderParthnerID    (required)
     * @param updateOrderRequest (optional)
     * @return OK (status code 200)
     */
    @PutMapping(
            value = "/orders/{orderParthnerID}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<OrderResponse> updateOrder(
            @RequestHeader(value = "contractorUrl") URI contractorUrl,
            @RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "X-customer-url") String customerUrl,
            @PathVariable("orderParthnerID") String orderParthnerID,
            @Valid @RequestBody(required = false) UpdateOrderRequest updateOrderRequest
    );

}
