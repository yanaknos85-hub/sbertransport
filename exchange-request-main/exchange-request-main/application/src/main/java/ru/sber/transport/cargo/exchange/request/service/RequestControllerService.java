package ru.sber.transport.cargo.exchange.request.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.transport.cargo.exchange.request.dto.*;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;
import ru.sber.transport.cargo.exchange.request.enums.Role;

import java.util.UUID;

public interface RequestControllerService {
    RequestDto get(UUID requestId, JwtAuthenticationToken authentication);
    RequestValidationResponse newDraft(RequestDto requestDto, JwtAuthenticationToken authentication);
    RequestValidationResponse edit(UUID requestId, RequestDto requestDto, JwtAuthenticationToken authentication);
    StatusResponseDto changeStatus(UUID requestId, RequestStatus newStatus, JwtAuthenticationToken authentication, Role role);
    RequestValidationResponse publish(RequestDto requestDto, JwtAuthenticationToken authentication);
    Page<ShipperRequestDto> searchRequestsForShipper(@Valid ShipperRequestFilterDto requestDto, JwtAuthenticationToken authentication);
    Page<CarrierRequestDto> searchRequestsForCarrier(@Valid CarrierRequestFilterDto searchDto, JwtAuthenticationToken authentication);
}
