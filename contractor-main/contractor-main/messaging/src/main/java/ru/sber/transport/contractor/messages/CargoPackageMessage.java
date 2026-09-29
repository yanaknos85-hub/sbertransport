package ru.sber.transport.contractor.messages;

import ru.sber.transport.messaging.*;

import java.util.*;

/**
 * Сообщение грузового пакета.
 *
 * @param id идентификатор.
 * @param label метка.
 * @param cost стоимость.
 * @param unit единицы измерения.
 * @param deleted флаг удаления.
 * @param organization организация.
 */
public record CargoPackageMessage(
        UUID id,
        String label,
        Double cost,
        String unit,
        boolean deleted,
        UUID organization
) implements Message<UUID> {

    @Override
    public UUID getId() {
        return id;
    }
}
