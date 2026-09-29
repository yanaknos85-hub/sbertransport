package ru.sber.transport.address.business.use_cases;

import lombok.NonNull;
import ru.sber.transport.address.business.model.Address;

import java.math.BigDecimal;
import java.util.*;

/**
 * Бизнес-логика работы с адресами.
 */
public interface Search {

    /**
     * Поиск по строке.
     *
     * @param request строка запроса.
     * @return список адресов.
     */
    List<Address> search(String request);

    /**
     * Поиск по координатам.
     *
     * @param latitude  широта.
     * @param longitude долгота.
     * @return адрес.
     */
    Address search(@NonNull BigDecimal latitude, @NonNull BigDecimal longitude);
}
