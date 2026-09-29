package ru.sber.transport.common.api.service.service;

import ru.sber.transport.common.api.service.model.dto.CredentialClient;

public interface AuthorizationService {

    /**
     * Аутентификация в системе Контрагента
     */
    void auth(CredentialClient auth);

    /**
     * Авторизация в системе Контрагента по refresh токену
     * 
     * @param auth
     */
    void refresh(CredentialClient auth);

}
