package ru.sber.transport.cargo.exchange.request.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.cargo.exchange.request.config.RequestServiceProperties;
import ru.sber.transport.cargo.exchange.request.database.dao.ReplyRepository;
import ru.sber.transport.cargo.exchange.request.database.model.Organization;
import ru.sber.transport.cargo.exchange.request.database.model.Request;
import ru.sber.transport.cargo.exchange.request.database.model.User;
import ru.sber.transport.cargo.exchange.request.dto.RequestDto;
import ru.sber.transport.cargo.exchange.request.dto.RequestShortDto;
import ru.sber.transport.cargo.exchange.request.dto.RequestValidationResponse;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;
import ru.sber.transport.cargo.exchange.request.exception.RequestNotFoundException;
import ru.sber.transport.cargo.exchange.request.mapper.RequestMapper;
import ru.sber.transport.cargo.exchange.request.service.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = RequestControllerServiceImpl.class)
@DisplayName("RequestControllerServiceImpl — Юнит-тест")
class RequestControllerServiceImplTest {

    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final UUID REQUEST_ID = UUID.randomUUID();
    private static final UUID ORG_UUIID = UUID.randomUUID();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RequestService requestService;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private RequestValidationService requestValidationService;

    @MockitoBean
    private RequestMapper requestMapper;

    @MockitoBean
    private RequestServiceProperties requestServiceProperties;

    @Autowired
    private RequestControllerServiceImpl controllerService;

    @MockitoBean
    private ReplyRepository replyRepository;

    @MockitoBean
    private HumanReadableIdGenerator humanReadableIdGenerator;

    private JwtAuthenticationToken authentication;

    private Organization organization;

    private User user;

    private final Logger logger = LoggerFactory.getLogger(RequestControllerServiceImplTest.class);

    @BeforeEach
    void setUp() {
        // Мок аутентификации
        authentication = mock(JwtAuthenticationToken.class);
        var token = mock(Jwt.class);
        when(authentication.getToken()).thenReturn(token);
        when(token.getClaim("sub")).thenReturn("auth-sub-123");
        when(userService.findUserIdByToken(authentication)).thenReturn(OWNER_ID);

        // Настройки сервиса
        when(requestServiceProperties.getPrefix()).thenReturn("ОП");
        when(requestServiceProperties.getExpiredDays()).thenReturn(2);

        organization = Organization.builder()
                .id(ORG_UUIID)
                .name("testOrg")
                .build();

        user = User.builder()
                .id(OWNER_ID)
                .organization(organization)
                .build();

        // Подменяем логгер для проверки вызовов
        try (MockedStatic<LoggerFactory> mockedLogger = mockStatic(LoggerFactory.class)) {
            mockedLogger.when(() -> LoggerFactory.getLogger(RequestControllerServiceImpl.class)).thenReturn(logger);
        }
    }

    @Test
    @DisplayName("Должен вернуть DTO заявки, если она найдена")
    void shouldReturnRequestDto_WhenFound() {
        // Given
        Request request = createRequest();
        RequestDto dto = createRequestDto();
        when(userService.findUserByToken(any())).thenReturn(user);
        when(requestService.getById(REQUEST_ID)).thenReturn(request);
        when(requestMapper.toDto(request, user.getOrganization().getId())).thenReturn(dto);

        // When
        RequestDto result = controllerService.get(REQUEST_ID, authentication);

        // Then
        assertThat(result).isSameAs(dto);
        verify(requestService).getById(REQUEST_ID);
    }

    @Test
    @DisplayName("Должен выбросить исключение, если заявка не найдена")
    void shouldThrow_WhenRequestNotFound() {
        // Given
        when(requestService.getById(REQUEST_ID))
                .thenThrow(new RequestNotFoundException("Not found"));

        // When & Then
        assertThatThrownBy(() -> controllerService.get(REQUEST_ID, authentication))
                .isInstanceOf(RequestNotFoundException.class);
    }

    @Test
    @DisplayName("Должен создать новый черновик")
    void shouldCreateNewDraft() {
        // Given
        RequestDto inputDto = createRequestDto();
        RequestShortDto shortDto = createRequestShortDto();
        RequestValidationResponse validationResponse = createValidationResponse(true);
        Request requestEntity = createRequest();

        when(requestMapper.toEntity(inputDto)).thenReturn(requestEntity);
        when(requestMapper.toShortDto(any(Request.class))).thenReturn(shortDto);
        when(requestValidationService.validate(inputDto)).thenReturn(validationResponse);
        when(requestService.save(any(Request.class))).thenAnswer(invocation -> {
            Request req = invocation.getArgument(0);
            req.setId(UUID.randomUUID()); // имитация генерации ID
            return req;
        });
        when(organizationService.findById(any())).thenReturn(Optional.ofNullable(organization));
        when(userService.findUserByToken(any())).thenReturn(user);

        // When
        RequestValidationResponse result = controllerService.newDraft(inputDto, authentication);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRequest()).isSameAs(shortDto);
        assertThat(result.getValidation().getIsValidForPublication()).isTrue();
        verify(requestMapper).updateDtoFromEntity(any(), eq(inputDto));
        verify(requestService).save(any(Request.class));
    }

    @Test
    @DisplayName("Должен редактировать существующий черновик")
    void shouldEdit() {
        // Given
        RequestDto inputDto = createRequestDto();
        Request requestFromDb = createRequest();
        requestFromDb.setStatus(RequestStatus.DRAFT);
        RequestShortDto shortDto = createRequestShortDto();
        RequestValidationResponse validationResponse = createValidationResponse(true);

        when(requestService.getByIdAndUserId(REQUEST_ID, OWNER_ID)).thenReturn(requestFromDb);
        when(requestMapper.toShortDto(any(Request.class))).thenReturn(shortDto);
        when(requestValidationService.validate(inputDto)).thenReturn(validationResponse);
        when(requestService.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(organizationService.findById(any())).thenReturn(Optional.ofNullable(organization));

        // When
        RequestValidationResponse result = controllerService.edit(REQUEST_ID, inputDto, authentication);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRequest()).isEqualTo(shortDto); // ✅ Сравниваем по значению, а не по ссылке
        verify(requestMapper).updateDtoFromEntity(requestFromDb, inputDto);
        verify(requestService).save(requestFromDb);
    }

    @Test
    @DisplayName("Должен опубликовать заявку, если валидна")
    void shouldPublish_WhenValid() {
        // Given
        RequestDto inputDto = createRequestDto();
        Request oldRequest = createRequest();
        Request savedRequest = createRequest();
        savedRequest.setStatus(RequestStatus.PUBLISHED);
        savedRequest.setPublishedAt(LocalDateTime.now());

        RequestShortDto shortDto = createRequestShortDto();
        RequestValidationResponse validationResponse = createValidationResponse(true);

        when(requestValidationService.validate(inputDto)).thenReturn(validationResponse);
        when(requestService.getByIdAndUserId(REQUEST_ID, OWNER_ID)).thenReturn(oldRequest);
        when(requestMapper.toEntity(inputDto)).thenReturn(savedRequest);
        when(requestMapper.toShortDto(savedRequest)).thenReturn(shortDto);
        when(requestService.save(any(Request.class))).thenReturn(savedRequest);
        when(userService.findUserByToken(any())).thenReturn(user);

        // When
        RequestValidationResponse result = controllerService.publish(inputDto, authentication);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRequest()).isSameAs(shortDto);
        assertThat(result.getRequest().getStatus()).isEqualTo(RequestStatus.PUBLISHED.name());
        verify(requestService).save(any(Request.class));
    }

    @Test
    @DisplayName("Не должен публиковать, если валидация не прошла")
    void shouldNotPublish_WhenInvalid() {
        // Given
        RequestDto inputDto = createRequestDto();
        RequestShortDto shortDto = createRequestShortDto();
        RequestValidationResponse invalidResponse = createValidationResponse(false);

        when(requestValidationService.validate(inputDto)).thenReturn(invalidResponse);
        when(requestMapper.toShortDto(inputDto)).thenReturn(shortDto);

        // When
        RequestValidationResponse result = controllerService.publish(inputDto, authentication);

        // Then
        assertThat(result).isSameAs(invalidResponse);
        assertThat(result.getRequest()).isSameAs(shortDto);
        verify(requestService, never()).save(any());
    }

    @Test
    @DisplayName("Должен выбросить исключение, если requestDto == null")
    void shouldThrow_WhenRequestDtoIsNull() {
        assertThatThrownBy(() -> controllerService.newDraft(null, authentication))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Должен выбросить исключение, если requestDto.getId() не совпадает с requestId")
    void shouldThrow_WhenRequestIdMismatch() {
        // Given
        RequestDto inputDto = createRequestDto();
        UUID differentId = UUID.randomUUID();
        inputDto.setId(differentId);

        // When & Then
        assertThatThrownBy(() -> controllerService.edit(REQUEST_ID, inputDto, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Request is null");
    }

    @Test
    @DisplayName("Должен выбросить исключение, если requestDto == null в edit")
    void shouldThrow_WhenRequestDtoIsNullInEdit() {
        assertThatThrownBy(() -> controllerService.edit(REQUEST_ID, null, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Request is null");
    }

    // === Вспомогательные методы ===

    private Request createRequest() {
        return Request.builder()
                .id(REQUEST_ID)
                .ownerId(OWNER_ID)
                .organizationId(organization.getId())
                .status(RequestStatus.PUBLISHED)
                .humanReadableId("ОП-202602-0000001")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(2))
                .build();
    }

    private RequestDto createRequestDto() {
        return RequestDto.builder()
                .id(REQUEST_ID)
                .ownerId(OWNER_ID)
                .humanReadableId("ОП-202602-0000001")
                .status(RequestStatus.DRAFT.name())
                .useEtrn(true)
                .viewType("fixed")
                .paymentForm("non_cash")
                .paymentTerms("prepayment")
                .requestCreated(java.time.LocalDate.now())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(2))
                .build();
    }

    private RequestShortDto createRequestShortDto() {
        return RequestShortDto.builder()
                .id(REQUEST_ID)
                .humanReadableId("ОП-202602-0000001")
                .status(RequestStatus.PUBLISHED.name())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(2))
                .publishedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Должен выбросить исключение при отсутствии прав: не владелец, не перевозчик, нет отклика")
    void shouldThrowExceptionWhenNoAccessRights() {
        // Given
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        var usr = User.builder()
                .id(userId)
                .organization(Organization.builder().id(orgId).build())
                .build();

        Request request = Request.builder()
                .id(requestId)
                .ownerId(UUID.randomUUID()) // другой владелец
                .carrierOrganizationId(UUID.randomUUID()) // не назначена нашей организации
                .status(RequestStatus.PUBLISHED)
                .build();

        JwtAuthenticationToken auth = createMockJwtToken(userId);

        when(userService.findUserByToken(auth)).thenReturn(usr);
        when(requestService.getById(requestId)).thenReturn(request);
        when(replyRepository.existsByRequestIdAndOrganizationId(requestId, orgId))
                .thenReturn(false); // нет отклика

        // When & Then
        assertThatThrownBy(() -> controllerService.get(requestId, auth))
                .isInstanceOf(RequestNotFoundException.class)
                .hasMessage("Доступ запрещен");

        // Verify: Проверяем, что было обращение к репозиторию и вызов логгера
        verify(replyRepository).existsByRequestIdAndOrganizationId(requestId, orgId);
    }

    @Test
    @DisplayName("Должен разрешить доступ по отклику перевозчика")
    void shouldAllowAccessByCarrierReply() {
        // Given
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        var usr = User.builder()
                .id(userId)
                .organization(Organization.builder().id(orgId).build())
                .build();

        Request request = Request.builder()
                .id(requestId)
                .ownerId(UUID.randomUUID())
                .status(RequestStatus.PUBLISHED)
                .build();

        JwtAuthenticationToken auth = createMockJwtToken(userId);

        when(userService.findUserByToken(auth)).thenReturn(usr);
        when(requestService.getById(requestId)).thenReturn(request);
        when(replyRepository.existsByRequestIdAndOrganizationId(requestId, orgId))
                .thenReturn(true); // есть отклик → доступ разрешён

        // When
        controllerService.get(requestId, auth);

        // Then: Исключения нет → доступ разрешён
        verify(requestService).getById(requestId);
    }

    @Test
    @DisplayName("Должен разрешить доступ, если заявка назначена перевозчику")
    void shouldAllowAccessByAssignedCarrier() {
        // Given
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        var usr = User.builder()
                .id(userId)
                .organization(Organization.builder().id(orgId).build())
                .build();

        Request request = Request.builder()
                .id(requestId)
                .ownerId(UUID.randomUUID())
                .carrierOrganizationId(orgId) // назначена нашей организации
                .status(RequestStatus.CARRIER_SELECTED)
                .build();

        JwtAuthenticationToken auth = createMockJwtToken(userId);

        when(userService.findUserByToken(auth)).thenReturn(usr);
        when(requestService.getById(requestId)).thenReturn(request);
        // Даже если нет отклика — доступ есть, т.к. назначена
        when(replyRepository.existsByRequestIdAndOrganizationId(requestId, orgId))
                .thenReturn(false);

        // When
        controllerService.get(requestId, auth);

        // Then: Исключения нет
        verify(requestService).getById(requestId);
    }

    @Test
    @DisplayName("Должен разрешить доступ, если пользователь — владелец заявки")
    void shouldAllowAccessByOwner() {
        // Given
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        var usr = User.builder()
                .id(userId)
                .organization(Organization.builder().id(UUID.randomUUID()).build())
                .build();

        Request request = Request.builder()
                .id(requestId)
                .ownerId(userId) // тот же пользователь
                .status(RequestStatus.DRAFT)
                .build();

        JwtAuthenticationToken auth = createMockJwtToken(userId);

        when(userService.findUserByToken(auth)).thenReturn(usr);
        when(requestService.getById(requestId)).thenReturn(request);

        // When
        controllerService.get(requestId, auth);

        // Then: Исключения нет
        verify(replyRepository, never()).existsByRequestIdAndOrganizationId(any(), any());
    }

    @Test
    @DisplayName("Должен вернуть draftInfo, если статус DRAFT")
    void shouldReturnDraftInfoForDraftRequest() {
        // Given
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        var usr = User.builder()
                .id(userId)
                .organization(Organization.builder().id(UUID.randomUUID()).build())
                .build();

        RequestDto draftInfo = RequestDto.builder().id(requestId).build();

        Request request = Request.builder()
                .id(requestId)
                .ownerId(userId)
                .status(RequestStatus.DRAFT)
                .draftInfo(draftInfo)
                .build();

        JwtAuthenticationToken auth = createMockJwtToken(userId);

        when(userService.findUserByToken(auth)).thenReturn(usr);
        when(requestService.getById(requestId)).thenReturn(request);

        // When
        RequestDto result = controllerService.get(requestId, auth);

        // Then
        assertThat(result).usingRecursiveComparison().isEqualTo(draftInfo);
    }

    @Test
    @DisplayName("Должен вернуть маппинг через requestMapper для опубликованной заявки")
    void shouldMapToDtoForPublishedRequest() {
        // Given
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        var usr = User.builder()
                .id(userId)
                .organization(Organization.builder().id(UUID.randomUUID()).build())
                .build();

        Request request = Request.builder()
                .id(requestId)
                .ownerId(userId)
                .status(RequestStatus.PUBLISHED)
                .build();

        RequestDto mappedDto = RequestDto.builder().id(requestId).status("PUBLISHED").build();

        JwtAuthenticationToken auth = createMockJwtToken(userId);

        when(userService.findUserByToken(auth)).thenReturn(usr);
        when(requestService.getById(requestId)).thenReturn(request);
        when(requestMapper.toDto(request, usr.getOrganization().getId())).thenReturn(mappedDto);

        // When
        RequestDto result = controllerService.get(requestId, auth);

        // Then
        assertThat(result).isSameAs(mappedDto);
    }
    // Вспомогательный метод
    private JwtAuthenticationToken createMockJwtToken(UUID userId) {
        var jwt = Jwt.withTokenValue("token-123")
                .header("alg", "none")
                .claim("sub", "mock-sub-" + userId)
                .build();
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("CARRIER")));
    }
    private RequestValidationResponse createValidationResponse(boolean isValid) {
        return RequestValidationResponse.builder()
                .validation(RequestValidationResponse.ValidationInfo.builder()
                        .isValidForPublication(isValid)
                        .errors(isValid ? java.util.Collections.emptyList() : List.of(
                                RequestValidationResponse.ValidationInfo.ValidationError.builder()
                                        .field("weight")
                                        .message("Масса должна быть положительной")
                                        .build()
                        ))
                        .build())
                .build();
    }
}



