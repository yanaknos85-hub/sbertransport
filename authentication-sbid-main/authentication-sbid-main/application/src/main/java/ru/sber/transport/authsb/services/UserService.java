package ru.sber.transport.authsb.services;

import ru.sber.transport.authsb.database.model.User;
import java.util.Optional;


/**
 * Сервис для управления пользователями.
 */
public interface UserService {

    /**
     * Найти пользователя по sub (уникальный идентификатор SBID).
     */
    Optional<User> findBySub(String sub);


    /**
     * Сохранить нового или обновить существующего пользователя.
     */
    User save(User user);

}