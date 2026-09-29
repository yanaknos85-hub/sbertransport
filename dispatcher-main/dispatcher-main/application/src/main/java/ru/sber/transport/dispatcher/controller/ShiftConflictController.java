package ru.sber.transport.dispatcher.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import ru.sber.transport.dispatcher.dto.ShiftConflictResponseDTO;
import ru.sber.transport.dispatcher.dto.search.ShiftConflictSearchDTO;

/**
 * Контроллер для работы с конфликтами смен.
 */
@RequestMapping("/")
@Tag(name = "Конфликты смен", description = "Набор операций для работы с конфликтами смен")
public interface ShiftConflictController {

    /**
     * Получить все конфликты смен.
     *
     * @param searchDTO параметры поиска и фильтрации
     * @return страница с конфликтами смен
     */
    @GetMapping(value = "/shift-conflicts/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение конфликтов смен", description = "Получение списка конфликтов смен с пагинацией")
    Page<ShiftConflictResponseDTO> getAll(
            ShiftConflictSearchDTO searchDTO
    );

    /**
     * Удалить конфликт смен.
     *
     * @param routeId идентификатор колнфликтной записи
     */
    @DeleteMapping(value = "/shift-conflicts/{routeId}/")
    @Operation(summary = "Удаление конфликтов смен", description = "Удаление конфликтов смен")
    void delete(
            @PathVariable("routeId") String routeId
    );

}
