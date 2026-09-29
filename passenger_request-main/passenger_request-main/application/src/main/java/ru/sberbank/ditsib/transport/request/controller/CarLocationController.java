package ru.sberbank.ditsib.transport.request.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@RequestMapping("car-location")
@Tag(
        name = "Контроллер для взаимодействия с местоположением автомобиля",
        description = "Набор операция для работы с местоположением автомобиля"
)
public interface CarLocationController {

    @GetMapping
    @Operation(
            summary = "Получение местоположения автомобиля через Web-Socket",
            description = "Регистрация запроса на получение местоположения автомобиля через Web-Socket"
    )
    void getCarLocation(@RequestParam UUID requestId);
}
