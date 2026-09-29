package ru.sber.transport.contractor.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ru.sber.transport.messaging.*;

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
 * @param organizations организации.
 * @param externalId внешний ID.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
        long digitId,
        boolean autoassign,
        String serviceType,
        String contractorType,
        String contactPersonEmail,
        List<UUID> organizations,
        UUID externalId
) implements Message<UUID> {
    @Override
    public UUID getId() {
        return id;
    }

}