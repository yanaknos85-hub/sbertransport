package ru.sber.transport.dispatcher.dto.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.sber.transport.dispatcher.dto.PatchDataV2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Поля для изменнения.
 */
public enum PatchField {

    /**
     * Телефон.
     */
    @JsonProperty("phone")
    PHONE,

    /**
     * Согласие Пдн.
     */
    @JsonProperty("consent")
    CONSENT,

    /**
     * Флаг возможности создания ЭПЛ
     */
    @JsonProperty("ewbCreationPossibility")
    EWB_CREATION_POSSIBILITY,

    /**
     * Номер доверенности
     */
    @JsonProperty("attorneyNumber")
    ATTORNEY_NUMBER,

    /**
     * Дата выдачи МЧД
     */
    @JsonProperty("issueDate")
    ISSUE_DATE,

    /**
     * Дата окончания срока действия МЧД
     */
    @JsonProperty("expiryDate")
    EXPIRY_DATE,

    /**
     * Система создания
     */
    @JsonProperty("creationSystem")
    CREATION_SYSTEM,

    /**
     * Уникальный идентификатор автопарка
     */
    @JsonProperty("autoparkId")
    AUTOPARK_ID,

    /**
     * ИНН
     */
    @JsonProperty("tin")
    TIN,

    /**
     * СНИЛС
     */
    @JsonProperty("snils")
    SNILS,

    /**
     * Номер водительского удостоверения
     */
    @JsonProperty("driverLicenseNumber")
    DRIVER_LICENSE_NUMBER,

    /**
     * Лицензии драйверов
     */
    @JsonProperty("driverLicenses")
    DRIVER_LICENSES,

    /**
     * Специализация
     */
    @JsonProperty("speciality")
    SPECIALITY,
    ;

    /**
     * Ручное создание мапы для патча
     * Решение со стримами не подходит, тк кидает NPE при null value
     * @param data data
     * @return мапа
     */
    public static Map<PatchField, Serializable> getPatchDataMap(List<PatchDataV2> data) {
        Map<PatchField, Serializable> map = new HashMap<>();
        for (PatchDataV2 patchData : data) {
            map.put(patchData.field(), patchData.value());
        }
        return map;
    }
}
