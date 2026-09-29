package ru.sber.transport.contractor.messages;

import ru.sber.transport.messaging.*;

import java.util.*;

/**
 * Сообщение автопарка.
 *
 * @param id идентификатор.
 * @param name название.
 * @param deleted флаг удаления.
 * @param contractorId контрагент.
 */
public record AutoparkMessage(

        UUID id,

        String name,

        boolean deleted,

        UUID contractorId

) implements Message<UUID> {

    @Override
    public UUID getId() {
        return id;
    }
}
