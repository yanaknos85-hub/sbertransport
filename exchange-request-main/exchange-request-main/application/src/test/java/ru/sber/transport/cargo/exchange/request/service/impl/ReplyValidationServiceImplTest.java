package ru.sber.transport.cargo.exchange.request.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.transport.cargo.exchange.request.database.dao.ReplyRepository;
import ru.sber.transport.cargo.exchange.request.database.model.CarrierReply;
import ru.sber.transport.cargo.exchange.request.database.model.Organization;
import ru.sber.transport.cargo.exchange.request.database.model.Request;
import ru.sber.transport.cargo.exchange.request.database.model.User;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;
import ru.sber.transport.cargo.exchange.request.enums.Role;
import ru.sber.transport.cargo.exchange.request.exception.AccessDeniedException;
import ru.sber.transport.cargo.exchange.request.exception.ReplyDuplicatedException;
import ru.sber.transport.cargo.exchange.request.exception.ReplyForbiddenException;
import ru.sber.transport.cargo.exchange.request.exception.ReplyRequestIncorrectStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplyValidationServiceImplTest {

    @Mock private ReplyRepository replyRepository;
    @InjectMocks private ReplyValidationServiceImpl validationService;

    private UUID requestId;
    private UUID orgId;
    private UUID userId;
    private JwtAuthenticationToken carrierAuth;
    private JwtAuthenticationToken shipperAuth;
    private Request request;
    private CarrierReply reply;
    private Organization organization;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        orgId = UUID.randomUUID();
        userId = UUID.randomUUID();

        shipperAuth = new JwtAuthenticationToken(Jwt.withTokenValue(UUID.randomUUID().toString())
                .jti(UUID.randomUUID().toString())
                .header("test", "test")
                .claim("roles", Role.SHIPPER)
                .build());

        carrierAuth = new JwtAuthenticationToken(Jwt.withTokenValue(UUID.randomUUID().toString())
                .jti(UUID.randomUUID().toString())
                .header("test", "test")
                .claim("roles", Role.CARRIER)
                .build());

        // Организация и пользователь
        organization = new Organization();
        organization.setId(orgId);

        var user = new User();
        user.setId(userId);
        user.setOrganization(organization);

        // Заявка
        request = new Request();
        request.setId(requestId);
        request.setStatus(RequestStatus.PUBLISHED);
        request.setOrganizationId(orgId);
        request.setOwnerId(userId);

        // Отклик
        reply = new CarrierReply();
        reply.setOrganization(organization);
        reply.setRequest(request);
        reply.setSelected(false);
    }

    @Test
    void validateAdd_ShouldPass_WhenCarrierRole() {
        // when & then
        assertThatNoException().isThrownBy(() -> validationService.validateAdd(requestId, carrierAuth));
    }

    @Test
    void validateAdd_ShouldThrow_WhenNotCarrierRole() {
        // when & then
        assertThatThrownBy(() -> validationService.validateAdd(requestId, shipperAuth))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("отсутствует роль %s".formatted(Role.CARRIER.getDisplayName()));
    }

    @Test
    void validateGetAll_ShouldPass_WhenShipperRole() {
        // when & then
        assertThatNoException().isThrownBy(() -> validationService.validateGetAll(shipperAuth));
    }

    @Test
    void validateGetAll_ShouldThrow_WhenNotShipperRole() {
        // when & then
        assertThatThrownBy(() -> validationService.validateGetAll(carrierAuth))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("отсутствует роль %s".formatted(Role.SHIPPER.getDisplayName()));
    }

    @Test
    void validateAccept_ShouldPass_WhenShipperRole() {
        // when & then
        assertThatNoException().isThrownBy(() -> validationService.validateAccept(shipperAuth));
    }

    @Test
    void validateAccept_ShouldThrow_WhenNotShipperRole() {
        // when & then
        assertThatThrownBy(() -> validationService.validateAccept(carrierAuth))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("отсутствует роль %s".formatted(Role.SHIPPER.getDisplayName()));
    }

    @Test
    void validateCancel_ShouldPass_WhenCarrierRole() {
        // when & then
        assertThatNoException().isThrownBy(() -> validationService.validateCancel(carrierAuth));
    }

    @Test
    void validateCancel_ShouldThrow_WhenNotCarrierRole() {
        // when & then
        assertThatThrownBy(() -> validationService.validateCancel(shipperAuth))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("отсутствует роль %s".formatted(Role.CARRIER.getDisplayName()));
    }

    @Test
    void checkRequestStatus_ShouldPass_WhenStatusMatches() {
        // when & then
        assertThatNoException().isThrownBy(() -> validationService.checkRequestStatus(request, RequestStatus.PUBLISHED));
    }

    @Test
    void checkRequestStatus_ShouldThrow_WhenStatusMismatch() {
        // when & then
        assertThatThrownBy(() -> validationService.checkRequestStatus(request, RequestStatus.CARRIER_SELECTED))
                .isInstanceOf(ReplyRequestIncorrectStatusException.class)
                .hasMessage("Заявка должна быть в статусе %s".formatted(RequestStatus.CARRIER_SELECTED.getDisplayName()));
    }

    @Test
    void checkDuplucate_ShouldThrow_WhenDuplicateExists() {
        // given
        when(replyRepository.existsByRequestIdAndOrganizationId(requestId, orgId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> validationService.checkDuplucate(requestId, orgId))
                .isInstanceOf(ReplyDuplicatedException.class)
                .hasMessage("Вы уже откликнулись на данную заявку");
    }

    @Test
    void checkDuplucate_ShouldPass_WhenNoDuplicate() {
        // given
        when(replyRepository.existsByRequestIdAndOrganizationId(requestId, orgId)).thenReturn(false);

        // when & then
        assertThatNoException().isThrownBy(() -> validationService.checkDuplucate(requestId, orgId));
    }

    @Test
    void checkReplyOwner_ShouldPass_WhenOwnerMatches() {
        // when & then
        assertThatNoException()
                .isThrownBy(() -> validationService.checkReplyOwner(reply, orgId));
    }

    @Test
    void checkReplyOwner_ShouldThrow_WhenOwnerMismatch() {
        // given
        UUID otherOrgId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> validationService.checkReplyOwner(reply, otherOrgId))
                .isInstanceOf(ReplyForbiddenException.class)
                .hasMessage("Работа с откликом запрещена");
    }

    @Test
    void checkRequestOwner_ShouldPass_WhenUserIsOwner() {
        // given
        User ownerUser = new User();
        ownerUser.setId(userId); // тот же id

        // when & then
        assertThatNoException().isThrownBy(() -> validationService.checkRequestOwner(request, ownerUser));
    }

    @Test
    void checkRequestOwner_ShouldPass_WhenOrgIsOwner() {
        // given
        var orgUser = new User();
        orgUser.setId(UUID.randomUUID());
        orgUser.setOrganization(organization); // та же организация

        // when & then
        assertThatNoException().isThrownBy(() -> validationService.checkRequestOwner(request, orgUser));
    }

    @Test
    void checkRequestOwner_ShouldThrow_WhenNeitherOwnerNorOrg() {
        // given
        User foreignUser = new User();
        foreignUser.setId(UUID.randomUUID());
        foreignUser.setOrganization(new Organization());
        foreignUser.getOrganization().setId(UUID.randomUUID());

        // when & then
        assertThatThrownBy(() -> validationService.checkRequestOwner(request, foreignUser))
                .isInstanceOf(ReplyForbiddenException.class)
                .hasMessage("Работа с откликом запрещена");
    }

    @Test
    void checkRequestStatusIn_ShouldPass_WhenStatusInList() {
        // when & then
        assertThatNoException().isThrownBy(() -> validationService.checkRequestStatusIn(request, RequestStatus.PUBLISHED, RequestStatus.CARRIER_SELECTED, RequestStatus.CONFIRMED));
    }

    @Test
    void checkRequestStatusIn_ShouldThrow_WhenStatusNotInList() {
        // when & then
        assertThatThrownBy(() -> validationService.checkRequestStatusIn(request, RequestStatus.CARRIER_SELECTED, RequestStatus.CONFIRMED))
                .isInstanceOf(ReplyRequestIncorrectStatusException.class)
                .hasMessage("Заявка должна быть в одном из статусов %s".formatted("Перевозчик выбран, Перевозка подтверждена"));
    }
}