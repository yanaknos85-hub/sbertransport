package ru.sber.transport.cargo.exchange.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.sber.transport.cargo.exchange.request.database.dao.ReplyRepository;
import ru.sber.transport.cargo.exchange.request.database.model.CarrierReply;
import ru.sber.transport.cargo.exchange.request.database.model.Request;
import ru.sber.transport.cargo.exchange.request.database.model.User;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;
import ru.sber.transport.cargo.exchange.request.enums.Role;
import ru.sber.transport.cargo.exchange.request.exception.AccessDeniedException;
import ru.sber.transport.cargo.exchange.request.exception.ReplyDuplicatedException;
import ru.sber.transport.cargo.exchange.request.exception.ReplyForbiddenException;
import ru.sber.transport.cargo.exchange.request.exception.ReplyRequestIncorrectStatusException;
import ru.sber.transport.cargo.exchange.request.service.ReplyValidationService;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.sber.transport.cargo.exchange.request.enums.Role.CARRIER;
import static ru.sber.transport.cargo.exchange.request.enums.Role.SHIPPER;
import static ru.sber.transport.cargo.exchange.request.util.ContextHelper.hasRole;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReplyValidationServiceImpl implements ReplyValidationService {
    private final ReplyRepository replyRepository;

    @Override
    public void validateAdd(UUID requestId, JwtAuthenticationToken authentication) {
        checkRole(authentication, CARRIER);
    }

    @Override
    public void validateGetAll(JwtAuthenticationToken authentication) {
        checkRole(authentication, SHIPPER);
    }

    @Override
    public void validateAccept(JwtAuthenticationToken authentication) {
        checkRole(authentication, SHIPPER);
    }

    @Override
    public void validateCancel(JwtAuthenticationToken authentication) {
        checkRole(authentication, CARRIER);
    }

    @Override
    public void checkRequestStatus(Request request, RequestStatus requiredStatus) {
        if (requiredStatus != request.getStatus()) {
            throw new ReplyRequestIncorrectStatusException(
                    "Заявка должна быть в статусе %s".formatted(requiredStatus.getDisplayName()));
        }
    }

    @Override
    public void checkRequestStatusIn(Request request, RequestStatus...requiredStatuses) {
        if (Arrays.stream(requiredStatuses).noneMatch(status ->status == request.getStatus())) {
            throw new ReplyRequestIncorrectStatusException(
                    "Заявка должна быть в одном из статусов %s".formatted(
                            Arrays.stream(requiredStatuses)
                                    .map(RequestStatus::getDisplayName)
                                    .collect(Collectors.joining(", "))));
        }
    }

    @Override
    public void checkDuplucate(UUID requestId, UUID organizationId) {
        if (replyRepository.existsByRequestIdAndOrganizationId(requestId, organizationId)) {
            throw new ReplyDuplicatedException("Вы уже откликнулись на данную заявку");
        }
    }

    @Override
    public void checkReplyOwner(CarrierReply reply, UUID userOrganizationId) {
        if (!reply.getOrganization().getId().equals(userOrganizationId)) {
            throw new ReplyForbiddenException("Работа с откликом запрещена");
        }
    }

    @Override
    public void checkRequestOwner(Request request, User user) {
        if (!user.getId().equals(request.getOwnerId()) &&
                !user.getOrganization().getId().equals(request.getOrganizationId())) {
            throw new ReplyForbiddenException("Работа с откликом запрещена");
        }
    }

    private void checkRole(JwtAuthenticationToken authentication, Role role) {
        if (!hasRole(authentication, role.name())) {
            throw new AccessDeniedException("У пользователя отсутствует роль %s".formatted(role.getDisplayName()));
        }
    }
}
