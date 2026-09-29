package ru.sberbank.ditsib.transport.request.client;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sber.transport.tariff.model.BaseTariffDataDto;

import java.util.UUID;

@FeignClient(name = "tariff", url = "${feign.url.tariff:}")
public interface TariffService {
    /**
     * Получение данных базового тарифа
     *
     * @param transportTypeId идентификатор типа транспорта
     * @param tariffId ID тарифа
     *
     * @return calculated data.
     */
    @GetMapping(value = "{transportTypeId}/{tariffId}", consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение данных базового тарифа", description = "Получение данных базового тарифа по идентификатору")
    BaseTariffDataDto getBaseTariff(
            @PathVariable("transportTypeId") UUID transportTypeId,
            @PathVariable("tariffId") UUID tariffId,
            @RequestHeader("Authorization") String token);
}
