package ru.sberbank.ditsib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Данные тарифа транспорта для основного сервиса
 */
public record TariffRequestMainDto(
        UUID idTariff,
        @JsonProperty("maxPassenger")
        Integer maxPassanger,
        Boolean ispPersonal,
        @JsonProperty("minDstKm")
        Integer minDistKm,
        @JsonProperty("maxDstKm")
        Integer maxDistKm,
        Integer costPerKm,
        @JsonProperty("rewardForPassengerRub")
        Integer rewardForPassengerRub
) {
}