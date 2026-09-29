package ru.sberbank.ditsib.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.sberbank.ditsib.config.MainLeadFeignConfig;
import ru.sberbank.ditsib.dto.CreateMainLeadRequestDto;
import ru.sberbank.ditsib.dto.CreateMainLeadResponseDto;

/**
 * Черновик Feign клиента для взаимодействия с внешним сервисом
 */
@FeignClient(
        name = "main-lead-service",
        configuration = MainLeadFeignConfig.class
)
public interface MainLeadFeignClient {

    /**
     * Метод для предсказания маршрута такси
     */
    @PostMapping("/mass-taxi-route/predict")
    CreateMainLeadResponseDto predict(@RequestBody CreateMainLeadRequestDto request);
} 