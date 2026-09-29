package ru.sber.transport.contractor.messages;

import ru.sber.transport.messaging.*;

import java.time.*;
import java.util.*;

/**
 * Сообщение договора.
 *
 * @param id идентификатор.
 * @param serviceType тип услуги.
 * @param regionIds список регионов, покрываемых договором.
 * @param transportType тип транспорта.
 * @param contractorId идентификатор контрагента.
 * @param sum сумма договора.
 * @param startDate начальная дата.
 * @param endDate конечная дата.
 * @param userId идентификатор пользователя.
 * @param creationTime дата создания.
 * @param organizations список организаций договора.
 * @param active признак активности.
 * @param deleted признак удаления.
 */
public record ContractMessage(
        UUID id,
        String serviceType,
        Set<UUID> regionIds,
        String transportType,
        UUID contractorId,
        Long sum,
        LocalDate startDate,
        LocalDate endDate,
        UUID userId,
        LocalDateTime creationTime,
        Set<UUID> organizations,
        boolean active,
        boolean deleted
) implements Message<UUID> {

    @Override
    public UUID getId() {
        return id();
    }
}
