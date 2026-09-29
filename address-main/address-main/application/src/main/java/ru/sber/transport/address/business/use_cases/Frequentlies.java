package ru.sber.transport.address.business.use_cases;

import ru.sber.transport.address.business.model.GeoAddress;

import java.util.UUID;

/**
 * Интерфейс бизнес-действия, специфичных для частых адресов.
 */
public interface Frequentlies {

    /**
     * Увеличить счетчик использований.
     *
     * @param address адрес.
     * @param first признак того, что адрес использовался в заявке и долеж быть первым в списке.
     * @param employeeId идентификатор владельца.
     */
    void increaseUsage(GeoAddress address, boolean first, UUID employeeId);

}
