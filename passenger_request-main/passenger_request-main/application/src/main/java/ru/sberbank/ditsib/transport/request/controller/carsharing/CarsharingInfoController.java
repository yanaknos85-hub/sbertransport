package ru.sberbank.ditsib.transport.request.controller.carsharing;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sberbank.ditsib.transport.request.dto.carsharing.CarsharingInfoRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.carsharing.CarsharingInfoResponseDTO;

/**
 * Контроллер для работы с доп информацией по пользователю каршеринга (ПНД и т.д.)
 */
@RequestMapping({ "/carsharing-info", "/carsharing-info/" })
@Validated
@Tag(name = "Контроллер для работы с доп информацией по пользователю каршеринга",
     description = "Контроллер для работы с доп информацией по пользователю каршеринга")
public interface CarsharingInfoController {
    
    @Operation(summary = "Получить доп информацию по пользователю каршеринга",
               description = "Получить доп информацию по пользователю каршеринга")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    CarsharingInfoResponseDTO get();
    
    @Operation(summary = "Обновить доп информацию по пользователю каршеринга",
               description = "Обновить доп информацию по пользователю каршеринга")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    CarsharingInfoResponseDTO update(@RequestBody CarsharingInfoRequestDTO dto);
    
}
