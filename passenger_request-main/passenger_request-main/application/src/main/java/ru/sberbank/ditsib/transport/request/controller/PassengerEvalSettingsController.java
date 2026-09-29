package ru.sberbank.ditsib.transport.request.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sberbank.ditsib.transport.request.dto.PassengerEvalSettingsDTO;

import java.util.Collection;

@RequestMapping({"passengerEvalSettings","passengerEvalSettings/"})
public interface PassengerEvalSettingsController {
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение настройки оценки", description = "Получение настройки оценки сервисов пассажирских перевозок")
    Collection<? extends PassengerEvalSettingsDTO> get();
}
