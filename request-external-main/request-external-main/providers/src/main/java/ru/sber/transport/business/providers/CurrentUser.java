package ru.sber.transport.business.providers;

import java.io.Serializable;
import java.util.UUID;

/**
 * Провайдер доступа к информации о текущем пользователе системы.
 */
public interface CurrentUser extends Serializable {

    /**
     * Возвращает идентификатор пользователя системы.
     *
     * @return идентификатор пользователя системы
     */
    UUID get();

}
