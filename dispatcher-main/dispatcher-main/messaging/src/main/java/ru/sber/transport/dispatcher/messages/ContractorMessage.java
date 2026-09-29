package ru.sber.transport.dispatcher.messages;

import ru.sber.transport.messaging.Message;

import java.util.*;

/**
 * Сообщение контрагента.
 *
 * @param id идентификатор.
 * @param name название.
 * @param msrn ОГРН.
 * @param tin ИНН.
 * @param rating рейтинг.
 * @param contactPersonInfo информация о контактном лице.
 * @param contactPersonPhone номер телефона контактного лица.
 * @param regionIds список обслуживаемых регионов.
 * @param contractorName название контрагента.
 * @param contractorRusName русское название контрагента.
 * @param integrationEmail интеграционный E-Mail.
 * @param deleted признак удаления.
 * @param integrationType тип интеграции.
 * @param url урл.
 * @param login логин для авторизации.
 * @param password пароль для авторизации.
 * @param mainDispatcher идентификатор основного диспетчера.
 * @param internal признак внутреннего автопарка.
 * @deprecated предпочтителен использование avro.
 */
@Deprecated(since = "2023-11-24")
public record ContractorMessage(
        UUID id,
        String name,
        String msrn,
        String tin,
        Integer rating,
        String contactPersonInfo,
        String contactPersonPhone,
        List<UUID> regionIds,
        String contractorName,
        String contractorRusName,
        String integrationEmail,
        boolean deleted,
        String integrationType,
        String url,
        String login,
        String password,
        Dispatcher mainDispatcher,
        long digitId,
        boolean autoassign,
        Integer vehicleCountNorm,
        boolean internal
) implements Message<UUID> {
    @Override
    public UUID getId() {
        return id;
    }

    public record Dispatcher(
            UUID id,
            String humanReadableId,
            String lastName,
            String firstName,
            String patronymic,
            String phone,
            String email,
            UUID contractorId,
            boolean active,
            boolean consent
    ) implements Message<UUID> {

        @Override
        public UUID getId() {
            return id;
        }

    }

}
