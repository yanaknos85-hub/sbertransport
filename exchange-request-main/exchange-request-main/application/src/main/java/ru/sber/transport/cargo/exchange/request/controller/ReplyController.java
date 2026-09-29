package ru.sber.transport.cargo.exchange.request.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyDto;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyShortDto;

import java.util.List;
import java.util.UUID;

/**
 * REST-контроллер для управления откликами на заявки.
 * Предоставляет API для получения информации об откликах.
 */
@RequestMapping("/reply")
public interface ReplyController {
    @PostMapping(value = "/{requestId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление отклика", description = "Добавление отклика для заявки")
    void add(@PathVariable("requestId") @NotNull UUID requestId,
             @RequestBody @Valid @NotNull CarrierReplyDto carrierReplyDto,
             @Parameter(hidden = true) JwtAuthenticationToken authentication);

    @GetMapping(value = "/list/{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение откликов", description = "Получение всех откликов по заявке")
    List<CarrierReplyShortDto> getAllByRequest(@PathVariable("requestId") @NotNull UUID requestId,
                                               @Parameter(hidden = true) JwtAuthenticationToken authentication);

    @PutMapping(value = "/{replyId}")
    @Operation(summary = "Подтверждение отклика", description = "Подтверждение отклика грузовладельцем")
    void accept(@PathVariable("replyId") @NotNull UUID replyId,
             @Parameter(hidden = true) JwtAuthenticationToken authentication);

    @DeleteMapping(value = "/{requestId}/carrier")
    @Operation(summary = "Отзыв отклика", description = "Отзыв отклика грузоперевозчиком")
    void cancelByCarrier(@PathVariable("requestId") @NotNull UUID requestId,
                         @Parameter(hidden = true) JwtAuthenticationToken authentication);

    @DeleteMapping(value = "/{replyId}/shipper")
    @Operation(summary = "Отзыв подтверждения отклика", description = "Отзыв подтверждения отклика грузовладельцем")
    void cancelByShipper(@PathVariable("replyId") @NotNull UUID replyId,
                         @Parameter(hidden = true) JwtAuthenticationToken authentication);
}
