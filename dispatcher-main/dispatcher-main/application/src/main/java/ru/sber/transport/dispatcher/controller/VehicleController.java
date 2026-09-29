package ru.sber.transport.dispatcher.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.dispatcher.dto.NewVehicleDTO;
import ru.sber.transport.dispatcher.dto.VehicleDTO;
import ru.sber.transport.dispatcher.dto.search.VehicleSearchDTO;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * Контроллер для работы с Транспортными средствами.
 */
@RequestMapping("/{contractorId}/autopark/{autoparkId}/vehicle/")
@Tag(name = "Транспортные средства", description = "Набор операций для работы с транспортом контрагентов")
public interface VehicleController {
    
    /**
     * Добавление ТС.
     *
     * @param vehicleDTO данные для нового ТС.
     *
     * @return Добавленный ТС.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление нового транспорта контрагента")
    VehicleDTO add(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("autoparkId") UUID autoparkId,
            @RequestBody @Valid NewVehicleDTO vehicleDTO
                  );
    
    /**
     * Редактирование ТС.
     *
     * @param autoparkId id автопарка к которому принадлежит ТС..
     * @param vehicleId id ТС.
     * @param vehicleDTO ДТО с ТС.
     */
    @PutMapping(value = "/{vehicleID}/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных транспорта контрагента")
    void edit(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("autoparkId") UUID autoparkId,
            @PathVariable("vehicleID") UUID vehicleId,
            @RequestBody @Valid NewVehicleDTO vehicleDTO
             );
    
    /**
     * Удалить ТС по id.
     *
     * @param autoparkId id автопарка к которому принадлежит ТС.
     * @param vehicleId id ТС.
     */
    @DeleteMapping("/{vehicleId}/")
    @Operation(summary = "Удаление", description = "Удаление данных транспорта контрагента")
    void delete(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("autoparkId") UUID autoparkId,
            @PathVariable("vehicleId") UUID vehicleId
               );
    
    /**
     * Получить ТС по ID.
     *
     * @param autoparkId id автопарка.
     * @param vehicleId id ТС.
     *
     * @return ТС.
     */
    @GetMapping(value = "/{vehicleId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных транспорта автопарка")
    VehicleDTO get(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("autoparkId") UUID autoparkId,
            @PathVariable("vehicleId") UUID vehicleId
                    );
    
    /**
     * Получить все транспортные средства автопарка контрагента.
     *
     * @param autoparkId id автопарка.
     *
     * @return коллекция ТС.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных транспорта автопарка контрагента")
    Page<VehicleDTO> getAll(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("autoparkId") UUID autoparkId,
            VehicleSearchDTO searchDTO);
}
