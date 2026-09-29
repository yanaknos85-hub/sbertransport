package ru.sber.transport.journal.helper;

import lombok.experimental.UtilityClass;
import ru.sberbank.ditsib.transport.exceptions.IllegalCallerResponseException;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

/**
 * Проверка возможность видеть заявку
 */
@UtilityClass
public class CanViewHelper {
    public static final String ILLEGAL_CALLER_MESSAGE = "Только создатель или коллега, указанный в заявке, может ее изменять";
    
    /**
     * Проверяем, может ли пользователь просматривать заявку
     *
     * @param userId Идентификатор записи с таблицы corporate.user авторизованного пользователя
     * @param authorUserId Идентификатор записи с таблицы corporate.user автора заявки
     * @param colleagueUserId Идентификатор записи с таблицы corporate.user коллеги
     */
    public static void checkCanView(UUID userId, UUID authorUserId, UUID colleagueUserId) {
        var ids = new ArrayList<>();
        ids.add(authorUserId);
        if (Objects.nonNull(colleagueUserId)) {
            ids.add(colleagueUserId);
        }
        if (!ids.contains(userId)) {
            throw new IllegalCallerResponseException(ILLEGAL_CALLER_MESSAGE);
        }
    }
}
