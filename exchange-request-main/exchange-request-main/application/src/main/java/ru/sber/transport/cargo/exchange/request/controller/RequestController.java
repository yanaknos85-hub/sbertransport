package ru.sber.transport.cargo.exchange.request.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.cargo.exchange.request.dto.*;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;
import ru.sber.transport.cargo.exchange.request.exception.RequestNotFoundException;

import java.util.UUID;

/**
 * REST-контроллер для управления заявками.
 * Предоставляет API для получения информации о заявках.
 */
public interface RequestController {

    /**
     * Получение заявки по идентификатору.
     * Возвращает полную информацию о заявке, если она существует.
     *
     * @param requestId Уникальный идентификатор заявки (UUID)
     * @return ResponseEntity с телом RequestDto
     * @throws RequestNotFoundException если заявка не найдена или нет прав на просмотр
     */
    @GetMapping("/{requestId}")
    @Operation(
            summary = "Получить заявку по ID",
            description = "Возвращает данные заявки по её уникальному идентификатору. ",
            tags = {"Requests"},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Заявка успешно найдена",
                            content = @Content(schema = @Schema(implementation = RequestDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Заявка не найдена или нет прав доступа",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Некорректный формат ID",
                            content = @Content
                    )
            }
    )
    @ResponseBody
    ResponseEntity<?> get(
            @Parameter(description = "Уникальный идентификатор заявки", required = true, example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
            @PathVariable UUID requestId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

    @PostMapping("/draft")
    @Operation(summary = "Создание черновика заявки", description = "Создание черновика заявки", tags = {"Requests"})
    ResponseEntity<?> newDraft(
            @RequestBody RequestDto requestDto,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);

    @PutMapping("/{requestId}")
    @Operation(summary = "Изменение черновика заявки", description = "Изменение данных черновика заявки", tags = {"Requests"})
    ResponseEntity<?> edit(
            @PathVariable UUID requestId,
            @RequestBody RequestDto requestDto,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);

    @PostMapping("/publish")
    @Operation(summary = "Публикация заявки", description = "Публикация заявки", tags = {"Requests"})
    ResponseEntity<?> publish(
            @RequestBody RequestDto requestDto,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Получение списка доступных (опубликованных) доставок.
     *
     * @param marketplaceRequestFilterDto параметры поиска
     * @return страница с доступными доставками
     */
    @PostMapping("/list/published")
    @ResponseBody
    @Operation(summary = "Получение списка доступных доставок",
            description = "Получение списка доступных доставок (Главная страница)",
            tags = {"Requests"})
    Page<MarketplaceRequestDto> searchPublishedRequests(
            @RequestBody @Valid MarketplaceRequestFilterDto marketplaceRequestFilterDto,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Получение списка доставок грузовладельца и его организации.
     *
     * @param marketplaceRequestFilterDto параметры поиска
     * @return страница с доступными доставками
     */
    @PostMapping("/list/shipper")
    @ResponseBody
    @Operation(summary = "Получение списка доставок грузовладельца",
            description = "Получение списка доставок грузовладельца и его организации",
            tags = {"Requests"}
    )
    Page<ShipperRequestDto> searchShipperRequests(
            @RequestBody @Valid ShipperRequestFilterDto marketplaceRequestFilterDto,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);


    /**
     * Получение списка доставок грузоперевозчикаи.
     *
     * @param carrierRequestFilterDto параметры поиска
     * @return страница с доступными доставками
     */
    @PostMapping("/list/carrier")
    @ResponseBody
    @Operation(summary = "Получение списка доставок грузоперевозчика",
            description = "Получение списка доставок грузоперевозчика",
            tags = {"Requests"})
    Page<CarrierRequestDto> searchCarrierRequests(
            @RequestBody @Valid CarrierRequestFilterDto carrierRequestFilterDto,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Смена статуса заявки для грузовладельца.
     *
     * @param requestId      уникальный идентификатор заявки
     * @param newStatus      новый статус
     * @param authentication авторизация
     * @return заявка с обновленным статусом
     */
    @PostMapping("changeStatus/shipper/{requestId}/{status}")
    @ResponseBody
    @Operation(
            summary = "Изменение статуса заявки грузовладельца", description = "Принудительное изменение статуса",
            tags = {"Requests"},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "статус заявки изменен",
                            content = @Content(schema = @Schema(implementation = StatusResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Заявка не найдена или нет прав доступа",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Переход по статусам невозможен",
                            content = @Content
                    )
            }
    )
    ResponseEntity<?> changeStatusShipper(@PathVariable("requestId") UUID requestId,
                                          @PathVariable("status") RequestStatus newStatus,
                                          @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Смена статуса заявки для перевозчика.
     *
     * @param requestId      уникальный идентификатор заявки
     * @param newStatus      новый статус
     * @param authentication авторизация
     * @return заявка с обновленным статусом
     */
    @PostMapping("changeStatus/carrier/{requestId}/{status}")
    @ResponseBody
    @Operation(
            summary = "Изменение статуса заявки перевозчика", description = "Принудительное изменение статуса",
            tags = {"Requests"},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "статус заявки изменен",
                            content = @Content(schema = @Schema(implementation = StatusResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Заявка не найдена или нет прав доступа",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Переход по статусам невозможен",
                            content = @Content
                    )
            }
    )
    ResponseEntity<?> changeStatusCarrier(@PathVariable("requestId") UUID requestId,
                                          @PathVariable("status") RequestStatus newStatus,
                                          @Parameter(hidden = true) JwtAuthenticationToken authentication);
}


