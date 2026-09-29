package ru.sber.transport.cargo.exchange.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.sber.transport.cargo.exchange.request.config.RequestServiceProperties;
import ru.sber.transport.cargo.exchange.request.database.dao.ReplyRepository;
import ru.sber.transport.cargo.exchange.request.database.model.Request;
import ru.sber.transport.cargo.exchange.request.database.model.User;
import ru.sber.transport.cargo.exchange.request.dto.*;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;
import ru.sber.transport.cargo.exchange.request.enums.Role;
import ru.sber.transport.cargo.exchange.request.exception.AccessDeniedException;
import ru.sber.transport.cargo.exchange.request.exception.RequestNotFoundException;
import ru.sber.transport.cargo.exchange.request.mapper.RequestMapper;
import ru.sber.transport.cargo.exchange.request.service.*;

import java.time.LocalDateTime;
import java.util.UUID;

import static ru.sber.transport.cargo.exchange.request.util.ContextHelper.hasRole;

/**
 * Реализация сервиса контроллера для управления заявками.
 * Содержит бизнес-логику по работе контроллера.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RequestControllerServiceImpl implements RequestControllerService {

    public static final String REQUEST_IS_NULL = "Request is null";

    private final HumanReadableIdGenerator humanReadableIdGenerator;
    private final UserService userService;
    private final RequestService requestService;
    private final ReplyRepository replyRepository;
    private final RequestValidationService requestValidationService;
    private final RequestMapper requestMapper;
    private final RequestServiceProperties requestServiceProperties;

    @Override
    public RequestDto get(UUID requestId, JwtAuthenticationToken authentication) {
        // Для MVP нет проверки прав доступа к заявке на просмотр
        var user = userService.findUserByToken(authentication);
        // Получаем заявку
        var request = requestService.getById(requestId);
        var organizationId = user.getOrganization().getId();

        if (!user.getId().equals(request.getOwnerId())
                && !organizationId.equals(request.getOrganizationId())
                && !organizationId.equals(request.getCarrierOrganizationId())
                && !replyRepository.existsByRequestIdAndOrganizationId(request.getId(), organizationId)) {
            log.warn("Попытка получить заявку с id: {}, organization_id: {} пользователем ownerId: {}",
                    requestId, organizationId, user.getId());
            throw new RequestNotFoundException("Доступ запрещен");
        }

        if (request.getStatus() == RequestStatus.DRAFT && request.getDraftInfo() != null) {
            return request.getCopyOfDraftInfoWithId();
        }

        return requestMapper.toDto(request, organizationId);
    }

    @Override
    public RequestValidationResponse newDraft(RequestDto requestDto, JwtAuthenticationToken authentication) {
        if (requestDto == null) {
            throw new IllegalArgumentException(REQUEST_IS_NULL);
        }

        var user = userService.findUserByToken(authentication);
        var request = buildNewRequest(RequestStatus.DRAFT, user);
        requestMapper.updateDtoFromEntity(request, requestDto);
        request.setDraftInfo(requestDto);

        request = requestService.save(request);
        var response = requestValidationService.validate(requestDto);
        response.setRequest(requestMapper.toShortDto(request));

        return response;
    }

    @Override
    public RequestValidationResponse edit(UUID requestId, RequestDto requestDto, JwtAuthenticationToken authentication) {
        checkRequestId(requestId, requestDto);
        var request = requestService.getByIdAndUserId(requestId, userService.findUserIdByToken(authentication));
        // Копируем поля, которые не должны изменяться из фронта
        requestMapper.updateDtoFromEntity(request, requestDto);

        if (request.getStatus() == RequestStatus.DRAFT) {
            request.setDraftInfo(requestDto);
        } else {
            var response = requestValidationService.validate(requestDto);
            if (Boolean.FALSE.equals(response.getValidation().getIsValidForPublication())) {
                response.setRequest(requestMapper.toShortDto(request));
                return response;
            }
            rewriteDtoFields(requestDto, request);
            request = requestMapper.toEntity(requestDto);
        }

        request = requestService.save(request);
        var response = requestValidationService.validate(requestDto);
        response.setRequest(requestMapper.toShortDto(request));

        return response;
    }

    @Override
    public RequestValidationResponse publish(RequestDto requestDto, JwtAuthenticationToken authentication) {
        var response = requestValidationService.validate(requestDto);
        if (Boolean.FALSE.equals(response.getValidation().getIsValidForPublication())) {
            response.setRequest(requestMapper.toShortDto(requestDto));
            return response;
        }

        var user = userService.findUserByToken(authentication);
        var requestId = requestDto.getId();

        if (requestId == null) {
            var request = buildNewRequest(RequestStatus.PUBLISHED, user);
            requestMapper.updateDtoFromEntity(request, requestDto);
        } else {
            var oldRequest = requestService.getByIdAndUserId(requestId, user.getId());
            // Копируем поля, которые не должны изменяться из фронта
            rewriteDtoFields(requestDto, oldRequest);
            requestDto.setStatus(RequestStatus.PUBLISHED.name());
        }

        requestDto.setPublishedAt(LocalDateTime.now());
        var request = requestMapper.toEntity(requestDto);
        request.setDraftInfo(null);

        request = requestService.save(request);
        response.setRequest(requestMapper.toShortDto(request));

        return response;
    }

    @Override
    public Page<ShipperRequestDto> searchRequestsForShipper(ShipperRequestFilterDto requestDto, JwtAuthenticationToken authentication) {
        var user = userService.findUserByToken(authentication);
        var org = user.getOrganization();

        requestDto.setOrganizationId(org.getId());

        return requestService.searchShipperRequestsByOrganizationId(requestDto);
    }

    @Override
    public Page<CarrierRequestDto> searchRequestsForCarrier(CarrierRequestFilterDto searchDto, JwtAuthenticationToken authentication) {
        var orgId = userService.findOrganizationIdByToken(authentication);
        return requestService.searchRequestsCarrierOrganizationId(searchDto, orgId);
    }

    @Override
    public StatusResponseDto changeStatus(UUID requestId, RequestStatus newStatus, JwtAuthenticationToken authentication, Role role) {
        // Проверяем, что пользователь имеет права на изменение статуса
        if (!hasRole(authentication, role.name())) {
            throw new AccessDeniedException("Пользователь не имеет права на изменение статуса заявки (не " + role.getDisplayName() + ")");
        }

        var user = userService.findUserByToken(authentication);

        return requestService.changeStatus(requestId, newStatus, user, role);
    }

    private Request buildNewRequest(RequestStatus status, User user) {
        return Request.builder()
                .status(status)
                .ownerId(user.getId())
                .organizationId(user.getOrganization().getId())
                .humanReadableId(
                        humanReadableIdGenerator.generateHumanReadableId(
                                requestServiceProperties.getPrefix()))
                .createdAt(LocalDateTime.now())
                .expiresAt(
                        LocalDateTime.now().plusDays(
                                requestServiceProperties.getExpiredDays()))
                .build();
    }

    private static void checkRequestId(UUID requestId, RequestDto requestDto) {
        if (requestDto == null) {
            throw new IllegalArgumentException(REQUEST_IS_NULL);
        }
        if (requestDto.getId() != null && !requestDto.getId().equals(requestId)) {
            throw new IllegalArgumentException(REQUEST_IS_NULL);
        }
    }

    private static void rewriteDtoFields(RequestDto requestDto, Request oldRequest) {
        requestDto.setId(oldRequest.getId());
        requestDto.setCreatedAt(oldRequest.getCreatedAt());
        requestDto.setHumanReadableId(oldRequest.getHumanReadableId());
        requestDto.setStatus(oldRequest.getStatus().name());
        requestDto.setUpdatedAt(LocalDateTime.now());
        requestDto.setOwnerId(oldRequest.getOwnerId());
    }
}
