package ru.sberbank.ditsib.transport.vehicle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.vehicle.dto.StateNumberSearchRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transmissiontype.TransportInfoDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.DeactivationDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.TransportSearchWithStructureRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.TransportSearchingRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.TransportSelfSearchingRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.create.TransportCreateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.update.TransportUpdateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.AccessiblePositionDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.TransportResponseDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.TransportSearchResponseDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.TransportSearchResponseDtoV2;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.TransportSearchWithStructureResponseDto;

import java.util.List;
import java.util.UUID;

@Validated
@RequestMapping("transport")
@Tag(name = "Справочник: Транспорт", description = "Контроллер для работы с Транспортом")
public interface TransportController {

    @Operation(summary = "Получение по ИД", description = "Получение Транспортом по уникальному идентификатору")
    @GetMapping("{transportId}")
    TransportResponseDto getById(
            @PathVariable("transportId") @Parameter(description = "ИД Транспорта", required = true) UUID transportId,
            @Parameter(hidden = true) Authentication authentication);

    @Operation(summary = "Добавление", description = "Добавление Транспорта")
    @PostMapping
    void add(
            @RequestBody @Valid @Parameter(description = "Данные Транспорта", required = true) TransportCreateDto requestDto,
            @Parameter(hidden = true) Authentication authentication);

    @Operation(summary = "Изменение", description = "Обновление Транспорта")
    @PatchMapping("/{transportId}")
    TransportResponseDto update(
            @PathVariable("transportId") @Parameter(description = "ИД Транспорта", required = true) UUID transportId,
            @RequestBody @Valid @Parameter(description = "Данные Транспорта", required = true) TransportUpdateDto requestDto,
            @Parameter(hidden = true) Authentication authentication);

    @Operation(summary = "Вывод из эксплуатации", description = "Вывод из эксплуатации Транспорта")
    @PatchMapping("/deactivate/{transportId}")
    void deactivate(
            @PathVariable("transportId") @Parameter(description = "ИД Транспорта", required = true) UUID transportId,
            @RequestBody @Valid @Parameter(description = "Данные о дате вывода из эксплуатации", required = true) DeactivationDto deactivationDto,
            @Parameter(hidden = true) Authentication authentication);

    @Operation(summary = "Поиск транспортных средств", description = "Поиск транспортных средств по всем организациям")
    @PostMapping("all-organizations")
    Page<TransportSearchResponseDto> searchAllOrganizations(
            @RequestBody @Valid TransportSearchingRequestDto searchingRequestDto,
            @Parameter(hidden = true) Authentication authentication);

    @Operation(summary = "Поиск транспортных средств", description = "Поиск транспортных средств по организации пользователя")
    @PostMapping("self-organizations")
    Page<TransportSearchResponseDto> searchSelfOrganizations(
            @RequestBody @Valid TransportSelfSearchingRequestDto selfSearchingRequestDto,
            @Parameter(hidden = true) Authentication authentication);

    @Operation(summary = "Поиск транспортных средств", description = "Поиск транспортных средств по vin-коду или гос. номеру")
    @PostMapping("search")
    @Deprecated(since = "5.14", forRemoval = true)
    Page<TransportSearchResponseDto> search(
            @RequestBody @Valid TransportSearchingRequestDto searchingRequestDto,
            @Parameter(hidden = true) Authentication authentication);

    @Operation(summary = "Поиск транспортных средств с указанием марки и модели авто", description = "Поиск транспортных средств по vin-коду или гос. номеру")
    @PostMapping(value = "search", headers = "x-version=2")
    @Deprecated(since = "5.14", forRemoval = true)
    Page<TransportSearchResponseDtoV2> searchWithBrandAndModel(
            @RequestBody @Valid TransportSearchingRequestDto searchingRequestDto,
            @Parameter(hidden = true) Authentication authentication);

    @PostMapping("statenumber")
    Page<TransportInfoDto> getTransportByStateNumber(@RequestBody @Valid StateNumberSearchRequestDto requestDto,
                                                     @Parameter(hidden = true) Authentication authentication);

    @Operation(summary = "Поиск транспортного средства сотрудника, с добавлением транспортных средств по его штатной структуре",
               description = "Поиск транспортного средства сотрудника, с добавлением транспортных средств по его штатной структуре")
    @PostMapping("search/structure")
    Page<TransportSearchWithStructureResponseDto> searchWithStructure(
            @RequestBody @Valid TransportSearchWithStructureRequestDto requestDto,
            @Parameter(hidden = true) Authentication authentication);

    @Operation(summary = "Получение должностей для закрепления", description = "Получение должностей для закрепления")
    @GetMapping("accessible-positions")
    List<AccessiblePositionDto> getAccessiblePositions();
}
