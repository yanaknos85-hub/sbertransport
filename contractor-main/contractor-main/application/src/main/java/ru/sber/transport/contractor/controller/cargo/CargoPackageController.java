package ru.sber.transport.contractor.controller.cargo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.contractor.dto.cargo.CargoPackageDto;
import ru.sber.transport.contractor.dto.cargo.NewCargoPackageDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.UUID;

/**
 * Controller for working with cargo packages.
 */
@RequestMapping("/{contractorId}/cargo")
@Tag(name = "Грузы. Справочник упаковок", description = "Набор операций для работы со справочников упаковочных " +
                                                        "материалов")
public interface CargoPackageController {
    
    /**
     * Get package with ID.
     *
     * @param contractorId id of contractor of package.
     * @param packageId ID of package to get.
     *
     * @return purpose.
     */
    @GetMapping(value = "/package/{packageId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение упаковки")
    CargoPackageDto getPackage(@PathVariable("contractorId") UUID contractorId,
                               @PathVariable("packageId") @NotNull UUID packageId);

    /**
     * Add a new package.
     *
     * @param contractorId id of contractor of package.
     * @param newData new data of package.
     *
     * @return package.
     */
    @PostMapping(value = "/package/", consumes = MediaType.APPLICATION_JSON_VALUE, produces =
            MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление", description = "Добавление упаковки")
    @ResponseBody
    CargoPackageDto addPackage(
            @PathVariable("contractorId") UUID contractorId,
            @Valid @RequestBody NewCargoPackageDto newData
                              );

    /**
     * Edit a new package.
     * @param contractorId id of contractor of package.
     * @param packageId ID package to edit.
     * @param newData new data of package.
     *
     */
    @PutMapping(value = "/package/{packageId}/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение упаковки")
    void updatePackage(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("packageId") UUID packageId,
            @Valid @RequestBody NewCargoPackageDto newData
                      );

    /**
     * Get all packages.
     * @param contractorId id of contractor of packages.
     *
     * @return list of packages.
     */
    @GetMapping(value = "/packages/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение всех", description = "Получение всех упаковок контрагента")
    Collection<CargoPackageDto> getPackages(@PathVariable("contractorId") UUID contractorId);

    /**
     * Delete package.
     *
     * @param contractorId id of contractor of package.
     * @param packageId ID package to delete.
     */
    @DeleteMapping(value = "/package/{packageId}/")
    @Operation(summary = "Удаление", description = "Удаление упаковки")
    void deletePackage(@PathVariable("contractorId") UUID contractorId,
                       @PathVariable("packageId") UUID packageId);
}
