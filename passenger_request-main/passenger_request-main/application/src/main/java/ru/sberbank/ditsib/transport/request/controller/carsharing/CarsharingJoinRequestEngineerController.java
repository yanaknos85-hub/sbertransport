package ru.sberbank.ditsib.transport.request.controller.carsharing;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.request.dto.carsharing.GetCarsharingJoinRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.carsharing.GetCarsharingJoinRequestShortDTO;
import ru.sberbank.ditsib.transport.request.dto.carsharing.ProcessedContractorAndJoinStatusDTO;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Контроллер обработки Заявок на подключение к корп.каршерингу Инженером ОТО
 */
@RequestMapping({"carsharing-join-request/process","carsharing-join-request/process/"})
@Validated
@Tag(name = "Заявки на подключение к корп.каршерингу (модуль инженера ОТО)",
     description = "Просмотр, обработка заявок Инженером ОТО")
public interface CarsharingJoinRequestEngineerController {
    
    /**
     * Получить все обработанные заявки
     * @return все обработанные заявки
     */
    @GetMapping(path = {"done","done/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получить все обработанные заявки", description = "Получить все обработанные заявки")
    List<GetCarsharingJoinRequestShortDTO> getDoneRequests();
    
    /**
     * Получить все необработанные заявки
     * @return все необработанные заявки
     */
    @GetMapping(path = {"awaiting","awaiting/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получить все необработанные заявки", description = "Получить все необработанные заявки")
    List<GetCarsharingJoinRequestShortDTO> getAwaitingRequests();
    
    /**
     * Получить все отмененные заявки
     * @return все отмененные заявки
     */
    @GetMapping(path = {"cancelled","cancelled/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получить все отмененные заявки", description = "Получить все отмененные заявки")
    List<GetCarsharingJoinRequestShortDTO> getCancelledRequests();
    
    /**
     * Получить заявку
     * @param joinRequestId ID заявки
     * @return заявка на подключение
     */
    @GetMapping(path = {"{joinRequestId}","{joinRequestId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получить заявку", description = "Получить заявку")
    GetCarsharingJoinRequestDTO get(@PathVariable @NotNull UUID joinRequestId);
    
    /**
     * Обработать заявку
     * @param processedDtos данные по подключению
     * @param joinRequestId ID заявки
     * @param organizationId ID корп.клиента
     * @return заявка на подключение
     */
    @PostMapping(path = {"{organizationId}/{joinRequestId}","{organizationId}/{joinRequestId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Обработать заявку", description = "Обработать заявку")
    GetCarsharingJoinRequestDTO process(
            @RequestBody @NotNull @Valid Set<ProcessedContractorAndJoinStatusDTO> processedDtos,
            @PathVariable @NotNull UUID joinRequestId,
            @PathVariable @NotNull UUID organizationId);
}
