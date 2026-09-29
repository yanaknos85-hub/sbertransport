package ru.sber.transport.telemechanic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.telemechanic.dto.dispatcher.*;

import java.util.UUID;

@RequestMapping("dispatchers")
@Tag(name = "Диспетчеры", description = "Набор методов для работы с диспетчерами")
public interface DispatcherController {
    
    /**
     * Добавление нового диспетчера
     *
     * @param request {@link AddDispatcherRequest} запрос с данными о диспетчере
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление нового диспетчера", description = "Добавление нового диспетчера")
    void addDispatcher(
            @Valid
            @RequestBody
            AddDispatcherRequest request
                      );
    
    /**
     * Редактирование диспетчера
     *
     * @param dispatcherId идентификатор диспетчера
     * @param request {@link EditDispatcherRequest} запрос с измененными данными диспетчера
     */
    @PatchMapping(path = "/{dispatcherId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Редактирование диспетчера", description = "Редактирование диспетчера")
    void editDispatcher(
            @PathVariable("dispatcherId")
            UUID dispatcherId,
            @Valid
            @RequestBody
            EditDispatcherRequest request
                       );
    
    /**
     * Получение информации о диспетчере
     *
     * @param dispatcherId идентификатор диспетчера
     *
     * @return {@link GetDispatcherResponse} информация о диспетчере
     */
    @GetMapping(path = "/{dispatcherId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение информации о диспетчере", description = "Получение информации о диспетчере")
    GetDispatcherResponse getDispatcher(
            @PathVariable("dispatcherId")
            UUID dispatcherId
                                       );
    
    /**
     * Деактивация диспетчера
     *
     * @param dispatcherId идентификатор диспетчера
     */
    @PatchMapping(path = "/{dispatcherId}/deactivate")
    @Operation(summary = "Деактивация диспетчера", description = "Деактивация диспетчера")
    void deactivateDispatcher(
            @PathVariable("dispatcherId")
            UUID dispatcherId
                             );
    
    /**
     * Получение списка диспетчеров с фильтрацией и сортировкой
     *
     * @param request {@link SearchDispatcherRequest} запрос с фильтрами и сортировкой
     *
     * @return {@link GetDispatcherResponse} список диспетчеров
     */
    @PostMapping(path = "/search")
    @Operation(summary = "Получение списка диспетчеров", description = "Получение списка диспетчеров с фильтрацией и сортировкой")
    Page<GetDispatcherResponse> search(
            @Valid
            @RequestBody
            SearchDispatcherRequest request
                                         );
    
    /**
     * Получение данных по организации диспетчера (с проверкой возможности создавать ЭПЛ)
     *
     * @param authentication {@link Authentication}
     *
     * @return {@link GetOrganizationDispatcherResponse}
     */
    @GetMapping(path = "self", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение данных по организации диспетчера", description = "Получение данных по организации диспетчера (с проверкой возможности создавать ЭПЛ)")
    GetOrganizationDispatcherResponse getSelfOrganizationInfo(@Parameter(hidden = true) Authentication authentication);
}
