package ru.sberbank.ditsib.transport.srm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.srm.model.SrmRequestDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;

import java.util.List;
import java.util.UUID;

/**
 * Controller interface for checkin settingss.
 */
@RequestMapping(value = {"srm","srm/"})
@Tag(name = "АПИ совместных поездок", description = "АПИ совместных поездок")
public interface SrmController {
    
    /**
     * Добавление новой совместной поездки
     *
     * @param requestDTO данные новой поездки
     *
     * @return DTO с данными созданной поездки
     */
    @PostMapping(value = {"addNew","addNew/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление новой совместной поездки",
               description = "Добавление новой совместной поездки")
    SrmSharedRideDTO addNew(@Valid @RequestBody SrmRequestDTO requestDTO);


    /**
     * Запрос на присоединение к совместной поездке
     *
     * @param rideId идентификатор существующей совместной поездки
     * @param requestDTO данные нового заказа
     *
     * @return DTO с данными созданной поездки
     */
    @PostMapping(value = {"joinRequest/{rideId}","joinRequest/{rideId}/"},
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Присоединение к совместной поездке",
               description = "Присоединение к совместной поездке")
    SrmSharedRideDTO joinRequest(@PathVariable("rideId") @NotNull UUID rideId,
                                 @Valid @RequestBody SrmRequestDTO requestDTO);
    
    /**
     * Отмена заявки из совместной поездки
     *
     * @param requestId номер заявки для отмены
     *
     * @return DTO с данными созданной поездки
     */
    @DeleteMapping(value = {"{requestId}","{requestId}/"},
                   produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Отмена заявки из совместной поездки",
               description = "Отмена заявки из совместной поездки")
    List<SrmSharedRideDTO> cancelRequest(@PathVariable("requestId") @NotNull UUID requestId);
    
    /**
     * Окончание совместной поездки
     *
     * @param rideId номер совместной поездки
     *
     * @return DTO с данными поездки
     */
    @PutMapping(value = {"{rideId}","{rideId}/"},
                   produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Окончание совместной поездки",
               description = "Окончание совместной поездки")
    SrmSharedRideDTO finishSharedRide(@PathVariable("rideId") @NotNull UUID rideId);
    
    /**
     * Получение списка подходящих совместных поездок
     *
     * @param requestDTO данные поездки-кандидата на совмещение
     *
     * @return DTO с предварительными данными подходящих объединенных поездок
     */
    @PostMapping(value = {"findMatch","findMatch/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск подходящих совместных поездок",
               description = "Поиск подходящих совместных поездок")
    List<SrmSharedRideDTO> findMatch(@Valid @RequestBody SrmRequestDTO requestDTO);
    
    /**
     * Получение списка всех совместных поездок
     *
     * @return DTO с предварительными данными подходящих объединенных поездок
     */
    @GetMapping(value = {"{rideId}","{rideId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение совместной поездки по id",
               description = "Получение совместной поездки по id")
    SrmSharedRideDTO get(@PathVariable("rideId") @NotNull UUID rideId);
    
    /**
     * Получение списка всех совместных поездок
     *
     * @return DTO с предварительными данными подходящих объединенных поездок
     */
    @GetMapping(value = {"getByRequestId/{requestId}","getByRequestId/{requestId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение совместной поездки по requestId",
               description = "Получение совместной поездки по requestId")
    List<SrmSharedRideDTO> getSharedRideByRequestId(@PathVariable("requestId") @NotNull UUID requestId);
    
    /**
     * Получение списка всех совместных поездок
     *
     * @return DTO с предварительными данными подходящих объединенных поездок
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение списка совместных поездок",
               description = "Получение списка совместных поездок")
    List<SrmSharedRideDTO> getAll();
}
