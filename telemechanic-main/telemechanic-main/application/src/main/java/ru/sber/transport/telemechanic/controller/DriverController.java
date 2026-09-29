package ru.sber.transport.telemechanic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.telemechanic.dto.driver.*;

import java.util.UUID;

@Validated
@RequestMapping("drivers")
@Tag(name = "Водители", description = "Набор методов для работы с водителями")
public interface DriverController {
    
    /**
     * Добавление нового водителя
     *
     * @param request {@link AddDriverRequest} запрос с данными о водителе
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление нового водителя", description = "Добавление нового водителя")
    void addDriver(
            @Valid
            @RequestBody
            AddDriverRequest request
                  );
    
    /**
     * Редактирование водителя
     *
     * @param id идентификатор водителя
     * @param request {@link EditDriverRequest} запрос с данными о водителе
     * @param authentication данные аутентификации инициатора запроса
     */
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Редактирование водителя", description = "Редактирование водителя")
    void editDriver(
            @PathVariable UUID id,
            @Valid
            @RequestBody
            EditDriverRequest request,
            @Parameter(hidden = true)
            Authentication authentication
                   );
    
    /**
     * Деактивация водителя
     *
     * @param id идентификатор водителя
     */
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Деактивация водителя", description = "Деактивация водителя")
    void deactivateDriver(
            @PathVariable UUID id
                         );
    
    /**
     * Получение водителя по идентификатору
     *
     * @param id идентификатор водителя
     *
     * @return {@link DriverSearchResponse} данные водителя
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение водителя по идентификатору", description = "Получение водителя по идентификатору")
    GetDriverResponse getDriverById(
            @PathVariable UUID id
                                      );
    
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение водителей по фильтрам", description = "Получение водителей по фильтрам")
    Page<DriverByFioResponse> getDrivers(DriverFilters filters);
    
    /**
     * Получение водителя по ФИО
     *
     * @param request запрос с данными для поиска водителя {@link DriverByFioRequest}
     *
     * @return {@link DriverByFioResponse} данные водителя
     */
    @PostMapping(value = "fio", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение водителя по ФИО", description = "Получение водителя по ФИО")
    Page<DriverByFioResponse> getDriversByFio(
            @Valid @RequestBody DriverByFioRequest request,
            @Parameter(hidden = true) Authentication authentication
                                             );
    
    /**
     * Поиск водителей
     *
     * @param request запрос с данными для поиска водителей {@link DriverSearchRequest}
     *
     * @return {@link DriverSearchResponse} с результатами поиска водителей
     */
    @PostMapping(value = "search", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Поиск водителей", description = "Поиск водителей с фильтрацией и сортировкой")
    Page<DriverSearchResponse> search(@RequestBody DriverSearchRequest request);
}
