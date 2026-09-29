package ru.sberbank.ditsib.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sberbank.ditsib.dto.AggregatedMainLeadDto;

import java.util.List;

/**
 * Контроллер планировщика заявок
 */
@RequestMapping("/manager/planner")
@Tag(name = "Планировщик заявок", description = "Методы для отображения запланированных заявок")
public interface PlannerController {

    /**
     * Запрос на получение всех сохраненных заявок
     * @return сохраненные заявки
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    List<AggregatedMainLeadDto> getAllRequests();
}
