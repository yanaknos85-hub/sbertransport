package ru.sberbank.ditsib.transport.tariff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;

import java.util.List;

/**
 * Контроллер тарифов общественного транспорта
 */
@RequestMapping({"public/compensation/type","public/compensation/type/"})
@Tag(
        name = "Типы компенсации за общественный транспорт",
        description ="Контроллер для получения типов компенсации за общественный транспорт"
)
public interface PublicCompensationTypeController {
    
    /**
     * Получение всех типов компенсации за общественный транспорт
     * @return коллекция типов компенсации
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение всех типов компенсации за общественный транспорт")
    List<PublicCompensationType> getAll();
}
