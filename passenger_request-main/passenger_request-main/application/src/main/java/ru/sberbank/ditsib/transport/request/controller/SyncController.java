package ru.sberbank.ditsib.transport.request.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequestMapping({"synchronize","synchronize/"})
@Tag(name = "Синхронизация заявки", description = "Набор операций для синхронизации данных по заявкам")
public interface SyncController {
    /**
     * @param creationDateFrom  Начальная дата периода отбора заявок по времени создания
     * @param creationDateTo    Конечная дата периода отбора заявок по времени создания
     *
     * @return                  Список id заявок для синхронизации
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение списка id заявок для синхронизации",
               description = "Получение списка id заявок, данные по которым необходимо повторно отправить в топик service.request")
    List<String> getRequestIdForSynchronize(
            @RequestParam("creationDateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate creationDateFrom,
            @RequestParam("creationDateTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate creationDateTo);
    
    /**
     * @param creationDateFrom  Начальная дата периода отбора заявок по времени создания
     * @param creationDateTo    Конечная дата периода отбора заявок по времени создания
     *
     * @return                  Список id заявок для синхронизации
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение списка id заявок для синхронизации",
               description = "Получение списка id заявок, данные по которым необходимо повторно отправить в топик service.request")
    List<String> postRequestIdForSynchronize(
            @RequestParam("creationDateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate creationDateFrom,
            @RequestParam("creationDateTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate creationDateTo);
}