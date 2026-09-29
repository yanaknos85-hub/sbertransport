package ru.sber.transport.common.api.service.service;

import ru.sber.transport.common.api.service.model.dto.*;

/**
 * Сервис для связи с контрагентами.
 */
public interface ClientService {

    /**
     * Проверка состояния API системы Контрагента
     *
     * @return статус состояния API системы Контрагента
     */
    StatusResponse health(CredentialClient auth);

    /**
     * Получение информации о Контрагенте
     *
     * @param auth аутентификация.
     * @return Информация о Контрагенте
     */
    ClientInfoResponse clientInfo(CredentialClient auth);

    /**
     * Получение тарифов.
     *
     * @param auth аутентификация.
     * @param filterRequest фильтр запроса.
     * @return ответ с тарифом.
     */
    TariffsResponse getTariffs(CredentialClient auth, TariffFilterRequest filterRequest);

    /**
     * Получение городов.
     *
     * @param auth аутентификация.
     * @return ответ с городами.
     */
    CitiesResponse getCities(CredentialClient auth);

}
