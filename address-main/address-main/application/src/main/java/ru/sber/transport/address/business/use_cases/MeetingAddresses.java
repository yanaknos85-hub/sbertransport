package ru.sber.transport.address.business.use_cases;

import ru.sber.transport.address.business.model.MeetingAddress;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс бизнес-функций работы с адресами встреч.
 */
public interface MeetingAddresses {

    /**
     * Получить список адресов.
     *
     * @return адреса.
     */
    List<MeetingAddress> get();

    MeetingAddress save(UUID organizationId, MeetingAddress source);
}
