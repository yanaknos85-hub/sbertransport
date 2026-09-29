package ru.sber.transport.request.external.provider.web;

import java.util.UUID;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sber.transport.business.providers.CurrentUser;

/**
 * Получение текущего пользователя из сессии.
 */
public class CurrentUserImpl implements CurrentUser {

    @Override
    public UUID get() {
        return ControllerUtils.currentUser();
    }

}
