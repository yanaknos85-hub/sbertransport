package ru.sberbank.ditsib.transport.tariff.dto;

import ru.sber.transport.messaging.Message;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO для контракта для отправки в Kafka
 * @param id идентификатор контракта
 * @param serviceType тип
 * @param regionIds идентификаторы регионов
 * @param transportType тип транспорта
 * @param contractorId идентификатор контрагента
 * @param sum сумма
 * @param startDate дата начала
 * @param endDate дата окончания
 * @param userId идентификатор пользователя
 * @param creationTime время создания
 * @param organizations идентификаторы организаций
 * @param uvhd увхд
 * @param active признак активности
 * @param deleted признак удаления
 * @param includeVat включен ли НДС
 * @param contractNumber номер контракта
 * @param contractType тип контракта
 * @param vatValue значение НДС
 * @param responsibleEmployeeId идентификатор ответственного сотрудника
 * @param driverLatePickupPenalty штраф за опоздание водителя
 * @param poorServiceQualityPenalty штраф за плохое качество услуги
 * @param driverOrderCancellationPenalty штраф за отмену заказа водителем
 * @param paymentOrganizationId идентификатор организации, осуществляющей оплату
 */
public record ContractForKafkaMessage(
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
        String uvhd,
        boolean active,
        boolean deleted,
        boolean includeVat,
        String contractNumber,
        String contractType,
        Integer vatValue,
        UUID responsibleEmployeeId,
        BigDecimal driverLatePickupPenalty,
        BigDecimal poorServiceQualityPenalty,
        BigDecimal driverOrderCancellationPenalty,
        UUID paymentOrganizationId
) implements Message<UUID> {

    @Override
    public UUID getId() {
        return this.id();
    }
}
