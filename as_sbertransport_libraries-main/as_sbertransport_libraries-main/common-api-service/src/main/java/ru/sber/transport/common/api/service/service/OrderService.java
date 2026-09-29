package ru.sber.transport.common.api.service.service;

import ru.sber.transport.common.api.service.model.dto.*;
import ru.sber.transport.common.api.service.model.dto.cargo.CargoOrderRequest;

import java.util.List;

public interface OrderService {

    /**
     * Отправка заявки контрагенту на создание заказа
     *
     * @param request
     * @param auth
     * @return
     */
    OrderResponse create(CredentialClient auth, OrderRequest request);

    /**
     * Отправка заявки контрагенту на создание заказа на грузоперевозку
     *
     * @param request
     * @param auth
     * @return
     */
    List<OrderResponse> create(CredentialClient auth, List<CargoOrderRequest> request);

    /**
     * Получение информации о заказе от Контрагента
     *
     * @param auth
     * @param orderPartnerID Идентификатор созданного заказа
     * @return
     */
    OrderInfoResponse info(CredentialClient auth, String orderPartnerID);

    /**
     * Отмена заказа у Контрагента
     *
     * @param auth
     * @param orderPartnerID Идентификатор созданного заказа
     * @return
     */
    CancelOrderResponse cancel(CredentialClient auth, String orderPartnerID);

    /**
     * @param auth
     * @param orderPartnerID
     * @return
     */
    LocationResponse location(CredentialClient auth, String orderPartnerID);

    /**
     * @param auth
     * @param filterRequest
     * @return
     */
    ShortOrderInfoResponse getAll(CredentialClient auth, OrderFilterRequest filterRequest);

    /**
     * @param auth
     * @param orderPartnerID
     * @param updateOrderRequest
     * @return
     */
    OrderResponse updateOrder(CredentialClient auth, String orderPartnerID, UpdateOrderRequest updateOrderRequest);

    /**
     * @param auth
     * @param orderPartnerID
     * @return
     */
    RouteResponse routeOrder(CredentialClient auth, String orderPartnerID);
}
