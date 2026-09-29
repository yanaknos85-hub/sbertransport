package ru.sber.transport.cargo.exchange.request.controller.impl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.cargo.exchange.request.controller.ReplyController;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyDto;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyShortDto;
import ru.sber.transport.cargo.exchange.request.service.ReplyService;
import ru.sber.transport.cargo.exchange.request.service.ReplyValidationService;
import ru.sber.transport.cargo.exchange.request.service.UserService;

import java.util.List;
import java.util.UUID;

/**
 * Реализация REST-контроллера для управления откликами на заявки.
 * Обрабатывает HTTP-запросы, делегирует бизнес-логику ReplyService.
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ReplyControllerImpl implements ReplyController {
    private final ReplyService replyService;
    private final UserService userService;
    private final ReplyValidationService replyValidationService;

    @Override
    public void add(UUID requestId, CarrierReplyDto carrierReplyDto, JwtAuthenticationToken authentication) {
        replyValidationService.validateAdd(requestId, authentication);
        replyService.add(requestId, carrierReplyDto, userService.findUserByToken(authentication));
    }

    @Override
    public List<CarrierReplyShortDto> getAllByRequest(UUID requestId, JwtAuthenticationToken authentication) {
        replyValidationService.validateGetAll(authentication);
        return replyService.getAllByRequest(requestId);
    }

    @Override
    public void accept(UUID replyId, JwtAuthenticationToken authentication) {
        replyValidationService.validateAccept(authentication);
        replyService.accept(replyId);
    }

    @Override
    public void cancelByCarrier(UUID requestId, JwtAuthenticationToken authentication) {
        replyValidationService.validateCancel(authentication);
        replyService.cancelByCarrier(requestId, userService.findOrganizationIdByToken(authentication));
    }

    @Override
    public void cancelByShipper(UUID replyId, JwtAuthenticationToken authentication) {
        replyValidationService.validateCancel(authentication);
        replyService.cancelByShipper(replyId, userService.findUserByToken(authentication));
    }
}
