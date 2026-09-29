package ru.sber.transport.cargo.exchange.request.controller.impl;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.cargo.exchange.request.controller.RequestController;
import ru.sber.transport.cargo.exchange.request.dto.*;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;
import ru.sber.transport.cargo.exchange.request.enums.Role;
import ru.sber.transport.cargo.exchange.request.exception.AccessDeniedException;
import ru.sber.transport.cargo.exchange.request.exception.RequestNotFoundException;
import ru.sber.transport.cargo.exchange.request.exception.StatusNotTransitionException;
import ru.sber.transport.cargo.exchange.request.exception.UserNotFoundException;
import ru.sber.transport.cargo.exchange.request.service.RequestControllerService;
import ru.sber.transport.cargo.exchange.request.service.RequestService;
import ru.sber.transport.cargo.exchange.request.service.UserService;

import java.util.UUID;

/**
 * Реализация REST-контроллера для управления заявками.
 * Обрабатывает HTTP-запросы, делегирует бизнес-логику RequestService.
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RequestControllerImpl implements RequestController {


    private final RequestControllerService requestControllerService;

    private final RequestService requestService;

    private final UserService userService;

    /**
     * Получение заявки по ID.
     * Доступно только владельцу заявки.
     *
     * @param requestId Уникальный идентификатор заявки
     * @return ResponseEntity с DTO заявки
     */
    @Override
    public ResponseEntity<?> get(UUID requestId, JwtAuthenticationToken authentication) {
        try {
             return ResponseEntity.ok(requestControllerService.get(requestId, authentication));
        } catch (RequestNotFoundException uex) {
            return ResponseEntity.status(404).body(new ErrorResponseDto("404", uex.getMessage()));
        } catch (UserNotFoundException iex) {
            return ResponseEntity.status(403).body(new ErrorResponseDto("403", iex.getMessage()));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(401).body(new ErrorResponseDto("401", ex.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?>  newDraft(
            RequestDto requestDto,
            JwtAuthenticationToken authentication) {
        try {
            return ResponseEntity.ok(requestControllerService.newDraft(requestDto, authentication));
        } catch (UserNotFoundException iex) {
            return ResponseEntity.status(403).body(new ErrorResponseDto("403", iex.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> edit(UUID requestId, RequestDto requestDto, JwtAuthenticationToken authentication) {
        try {
            return ResponseEntity.ok(requestControllerService.edit(requestId, requestDto, authentication));
        } catch (RequestNotFoundException uex) {
            return ResponseEntity.status(404).body(new ErrorResponseDto("404", uex.getMessage()));
        } catch (UserNotFoundException iex) {
            return ResponseEntity.status(403).body(new ErrorResponseDto("403", iex.getMessage()));
        }

    }

    @Override
    public ResponseEntity<?>  publish(RequestDto requestDto, JwtAuthenticationToken authentication) {
        try {
            return ResponseEntity.ok(requestControllerService.publish(requestDto, authentication));
        } catch (RequestNotFoundException uex) {
            return ResponseEntity.status(404).body(new ErrorResponseDto("404", uex.getMessage()));
        } catch (UserNotFoundException iex) {
            return ResponseEntity.status(403).body(new ErrorResponseDto("403", iex.getMessage()));
        }
    }

    @Override
    public Page<MarketplaceRequestDto> searchPublishedRequests(MarketplaceRequestFilterDto searchDto, JwtAuthenticationToken token) {
        return requestService.searchPublishedRequests(searchDto, userService.findOrganizationIdByToken(token));
    }

    @Override
    public Page<ShipperRequestDto> searchShipperRequests(ShipperRequestFilterDto searchDto, JwtAuthenticationToken authentication) {
        return requestControllerService.searchRequestsForShipper(searchDto, authentication);
    }

    /**
     * Смена статуса заявки для грузовладельца.
     *
     * @param requestId      уникальный идентификатор заявки
     * @param newStatus      новый статус
     * @param authentication авторизация
     * @return заявка с обновленным статусом
     */
    @Override
    public ResponseEntity<?> changeStatusShipper(UUID requestId, RequestStatus newStatus, JwtAuthenticationToken authentication) {
        return changeStatus(requestId, newStatus, authentication, Role.SHIPPER);
    }

    /**
     * Смена статуса заявки для грузоперевозчика.
     *
     * @param requestId      уникальный идентификатор заявки
     * @param newStatus      новый статус
     * @param authentication авторизация
     * @return заявка с обновленным статусом
     */
    @Override
    public ResponseEntity<?> changeStatusCarrier(UUID requestId, RequestStatus newStatus, JwtAuthenticationToken authentication) {
        return changeStatus(requestId, newStatus, authentication, Role.CARRIER);
    }

    private ResponseEntity<?> changeStatus(UUID requestId, RequestStatus newStatus, JwtAuthenticationToken authentication, Role role) {
        try {
            return ResponseEntity.ok(requestControllerService.changeStatus(requestId, newStatus, authentication, role));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(403).body(new ErrorResponseDto("403", ex.getMessage()));
        } catch (StatusNotTransitionException ex) {
            return ResponseEntity.status(400).body(new ErrorResponseDto("400", ex.getMessage()));
        } catch (RequestNotFoundException | UserNotFoundException uex) {
            return ResponseEntity.status(404).body(new ErrorResponseDto("404", uex.getMessage()));
        }
    }

    @Override
    public Page<CarrierRequestDto> searchCarrierRequests(
            @RequestBody @Valid CarrierRequestFilterDto searchDto,
            @Parameter(hidden = true) JwtAuthenticationToken authentication) {
        return requestControllerService.searchRequestsForCarrier(searchDto, authentication);
    }
}



