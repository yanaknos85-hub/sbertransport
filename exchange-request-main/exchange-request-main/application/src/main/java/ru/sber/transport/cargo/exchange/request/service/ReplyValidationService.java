package ru.sber.transport.cargo.exchange.request.service;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.transport.cargo.exchange.request.database.model.CarrierReply;
import ru.sber.transport.cargo.exchange.request.database.model.Request;
import ru.sber.transport.cargo.exchange.request.database.model.User;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;

import java.util.UUID;

/**
 *
 */
public interface ReplyValidationService {
    void validateAdd(UUID requestId, JwtAuthenticationToken authentication);

    void validateGetAll(JwtAuthenticationToken authentication);

    void validateAccept(JwtAuthenticationToken authentication);

    void validateCancel(JwtAuthenticationToken authentication);

    void checkRequestStatus(Request request, RequestStatus requiredStatus);

    void checkRequestStatusIn(Request request, RequestStatus... requiredStatuses);

    void checkDuplucate(UUID requestId, UUID organizationId);

    void checkReplyOwner(CarrierReply reply, UUID userOrganizationId);

    void checkRequestOwner(Request request, User user);
}
