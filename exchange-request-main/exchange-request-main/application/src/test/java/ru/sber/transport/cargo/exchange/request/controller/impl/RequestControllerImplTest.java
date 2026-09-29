package ru.sber.transport.cargo.exchange.request.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.instancio.Instancio;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.cargo.exchange.request.RequestApplication;
import ru.sber.transport.cargo.exchange.request.database.dao.ReplyRepository;
import ru.sber.transport.cargo.exchange.request.database.model.CarrierReply;
import ru.sber.transport.cargo.exchange.request.database.model.Organization;
import ru.sber.transport.cargo.exchange.request.database.model.Request;
import ru.sber.transport.cargo.exchange.request.dto.*;
import ru.sber.transport.cargo.exchange.request.enums.PaymentForm;
import ru.sber.transport.cargo.exchange.request.enums.PaymentTerms;
import ru.sber.transport.cargo.exchange.request.enums.ViewType;
import ru.sber.transport.cargo.exchange.request.service.RequestService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.cargo.exchange.request.enums.RequestStatus.*;
import static ru.sber.transport.cargo.exchange.request.enums.Role.CARRIER;
import static ru.sber.transport.cargo.exchange.request.enums.Role.SHIPPER;

@DisplayName("Проверка котроллера запросов")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = RequestApplication.class)
@EmbeddedPostgres
@Slf4j
class RequestControllerImplTest {
    public static final String VALID_OWNER_ID = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380005";
    private static final UUID VALID_REQUEST_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001");
    private static final UUID NOT_EXISTS_REQUEST_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380011");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RequestService requestService;

    @Autowired
    private ReplyRepository replyRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private AuthorizationManager<RequestAuthorizationContext> roleCheckService;

    @BeforeEach
    void init() {
        when(roleCheckService.authorize(any(), any())).thenReturn(new AuthorizationDecision(true));
    }

    @Test
    @DisplayName("Успешное получение заявки")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/test_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGetByOwnerIdRequest() throws Exception {
        var response = mockMvc.perform(
                get("/%s".formatted(VALID_REQUEST_ID))
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001"))
                .andExpect(jsonPath("$.humanreadableId").value("ОР-202602-0000001"))
                .andExpect(jsonPath("$.internalId").value("INT-REQ-001"))
                .andExpect(jsonPath("$.ownerId").value(VALID_OWNER_ID))
                .andExpect(jsonPath("$.status").value(PUBLISHED.name()))
                .andExpect(jsonPath("$.useEtrn").value(true))
                .andExpect(jsonPath("$.viewType").value("FIXED"))
                .andExpect(jsonPath("$.paymentForm").value("NON_CASH"))
                .andExpect(jsonPath("$.paymentTerms").value("PREPAYMENT"))
                .andExpect(jsonPath("$.paymentDays").doesNotExist()) // или .value((Integer) null)
                .andExpect(jsonPath("$.requestCreated").value(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .andExpect(jsonPath("$.senderFio").value("Алексей Кузнецов"))
                .andExpect(jsonPath("$.senderPhone").value("+7 916 123-45-67"))
                .andExpect(jsonPath("$.recipientFio").value("Марина Петрова"))
                .andExpect(jsonPath("$.recipientPhone").value("8-903-987-65-43"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.expiresAt").exists())
                .andExpect(jsonPath("$.publishedAt").exists())
                .andExpect(jsonPath("$.updatedAt").doesNotExist())
                .andExpect(jsonPath("$.completedAt").doesNotExist())

                // === Waypoints: проверяем массив из 2 элементов ===
                .andExpect(jsonPath("$.waypoints").isArray())
                .andExpect(jsonPath("$.waypoints").isNotEmpty())
                .andExpect(jsonPath("$.waypoints[0].type").value("LOAD"))
                .andExpect(jsonPath("$.waypoints[0].orderingIndex").value(0))
                .andExpect(jsonPath("$.waypoints[0].radius").value(500))
                .andExpect(jsonPath("$.waypoints[0].addressInfo.city").value("Москва"))
                .andExpect(jsonPath("$.waypoints[0].addressInfo.street").value("ул. Ленина, 10"))
                .andExpect(jsonPath("$.waypoints[0].addressInfo.longitude").value(37.6173))
                .andExpect(jsonPath("$.waypoints[0].addressInfo.latitude").value(55.7558))
                .andExpect(jsonPath("$.waypoints[0].date").exists())
                .andExpect(jsonPath("$.waypoints[0].from").exists())
                .andExpect(jsonPath("$.waypoints[0].to").exists())

                .andExpect(jsonPath("$.waypoints[1].type").value("UNLOAD"))
                .andExpect(jsonPath("$.waypoints[1].orderingIndex").value(1))
                .andExpect(jsonPath("$.waypoints[1].radius").value(300))
                .andExpect(jsonPath("$.waypoints[1].addressInfo.city").value("Санкт-Петербург"))
                .andExpect(jsonPath("$.waypoints[1].addressInfo.street").value("Невский проспект, 50"))
                .andExpect(jsonPath("$.waypoints[1].addressInfo.longitude").value(30.3165))
                .andExpect(jsonPath("$.waypoints[1].addressInfo.latitude").value(59.9388))
                .andExpect(jsonPath("$.waypoints[1].date").exists())
                .andExpect(jsonPath("$.waypoints[1].from").exists())
                .andExpect(jsonPath("$.waypoints[1].to").exists())

                .andReturn()
                .getResponse();

        log.info("Response content: {}", response.getContentAsString());
        // Дополнительная проверка через десериализацию (как у вас)
        RequestDto requestDto = mapper.readValue(response.getContentAsString(), new TypeReference<>() {
        });

        assertThat(requestDto).isNotNull();
        assertThat(requestDto.getId()).isEqualTo(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001"));
        assertThat(requestDto.getHumanReadableId()).isEqualTo("ОР-202602-0000001");
        assertThat(requestDto.getInternalId()).isEqualTo("INT-REQ-001");
        assertThat(requestDto.getOwnerId()).isEqualTo((UUID.fromString(VALID_OWNER_ID)));
        assertThat(requestDto.getStatus()).isEqualTo(PUBLISHED.name());
        assertThat(requestDto.getUseEtrn()).isTrue();
        assertThat(requestDto.getViewType()).isEqualTo("FIXED");
        assertThat(requestDto.getPaymentForm()).isEqualTo("NON_CASH");
        assertThat(requestDto.getPaymentTerms()).isEqualTo("PREPAYMENT");
        assertThat(requestDto.getPaymentDays()).isNull();
        assertThat(requestDto.getRequestCreated()).isEqualTo(java.time.LocalDate.now());
        assertThat(requestDto.getSenderFio()).isEqualTo("Алексей Кузнецов");
        assertThat(requestDto.getSenderPhone()).isEqualTo("+7 916 123-45-67");
        assertThat(requestDto.getRecipientFio()).isEqualTo("Марина Петрова");
        assertThat(requestDto.getRecipientPhone()).isEqualTo("8-903-987-65-43");
        assertThat(requestDto.getPublishedAt()).isNotNull();
        assertThat(requestDto.getExpiresAt()).isNotNull();
        assertThat(requestDto.getCreatedAt()).isNotNull();
        assertThat(requestDto.getUpdatedAt()).isNull();
        assertThat(requestDto.getCompletedAt()).isNull();

    }


    /**
     * Сценарий: Заявка не найдена
     * Ожидание: 404 Not Found
     */
    @Test
    @DisplayName("Заявка не найдена")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/test_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldReturnNotFound_WhenRequestDoesNotExist() throws Exception {

        String url = "/%s".formatted(NOT_EXISTS_REQUEST_ID);

        mockMvc.perform(get(url)
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isNotFound());
    }

    /**
     * Сценарий: Доступ к чужой заявке
     * Ожидание: 404 (не раскрываем существование ресурса)
     */
    @Test
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/test_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldReturnNotFound_WhenAccessingOtherUsersRequest() throws Exception {

        String url = "/%s".formatted(VALID_REQUEST_ID);

        mockMvc.perform(get(url)
                        .with(jwt().jwt(builder -> builder.jti("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380023"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isNotFound());
    }

    /**
     * Сценарий: Неверный формат ID заявки
     * Ожидание: 400 Bad Request
     */
    @Test
    void shouldReturnBadRequest_WhenInvalidRequestId() throws Exception {
        String invalidUuid = "invalid-uuid";
        String url = "/%s".formatted(invalidUuid);

        mockMvc.perform(get(url)
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isBadRequest());
    }

    /**
     * Сценарий: Не передан токен
     * Ожидание: 403 Forbidden
     */
    @Test
    void shouldReturnBadRequest_WhenOwnerIdIsMissing() throws Exception {
        String url = "/%s".formatted(VALID_REQUEST_ID);

        mockMvc.perform(get(url))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Создание черновика заявки")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/test_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createDraft() throws Exception {

        String url = "/draft";

        var response = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(getContentAsString("/json/requestDraft.json"))
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        log.info("Response content: {}", response.getContentAsString());

        var requests = requestService.getByOwnerId(UUID.fromString(VALID_OWNER_ID));
        assertThat(requests).hasSize(2);
    }

    @Test
    @DisplayName("Создание черновика заявки")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createMinimalDraft() throws Exception {

        String url = "/draft";
        RequestDto requestDto = RequestDto.builder()
                .costRequest(333.)
                .vatInclude(false)
                .build();

        String content = mapper.writeValueAsString(requestDto);
        log.info("[create minimal draft] Content to send:\r\n{} ", content);
        var response = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content)
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        log.info("Response content: {}", response.getContentAsString());

        var requests = requestService.getByOwnerId(UUID.fromString(VALID_OWNER_ID));
        assertThat(requests).hasSize(1);
    }

    @Test
    @DisplayName("Сохранение и получение черновика заявки")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void saveAndGetByOwnerId() throws Exception {
        String url = "/draft";

        var response = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(getContentAsString("/json/requestDraft.json"))
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        log.info("Response content: {}", response.getContentAsString());
        var responseDto = mapper.readValue(response.getContentAsString(), RequestValidationResponse.class);

        assertThat(responseDto.getValidation().getIsValidForPublication()).isTrue();
        response = mockMvc.perform(
                        get("/%s".formatted(responseDto.getRequest().getId()))
                                .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId").value(responseDto.getRequest().getOwnerId().toString()))
                .andExpect(jsonPath("$.humanreadableId").value(responseDto.getRequest().getHumanReadableId()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.internalId").isNotEmpty())
                .andExpect(jsonPath("$.ownerId").value(VALID_OWNER_ID))
                .andExpect(jsonPath("$.status").value(DRAFT.name()))
                .andExpect(jsonPath("$.useEtrn").value(false))
                .andExpect(jsonPath("$.viewType").value("FIXED"))
                .andExpect(jsonPath("$.paymentForm").value("NON_CASH"))
                .andExpect(jsonPath("$.paymentTerms").value("PREPAYMENT"))
                .andExpect(jsonPath("$.paymentDays").doesNotExist())

                // === Waypoints: проверяем массив из 2 элементов ===
                .andExpect(jsonPath("$.waypoints").isArray())
                .andExpect(jsonPath("$.waypoints[0].type").value("LOAD"))
                .andExpect(jsonPath("$.waypoints[0].orderingIndex").value(0))
                .andExpect(jsonPath("$.waypoints[0].radius").value(50))
                .andExpect(jsonPath("$.waypoints[0].addressInfo.city").value("Москва"))
                .andExpect(jsonPath("$.waypoints[0].addressInfo.street").value("Ленина, 1"))
                .andExpect(jsonPath("$.waypoints[0].addressInfo.building").value("корпус 2"))
                .andExpect(jsonPath("$.waypoints[0].addressInfo.latitude").value(55.7558))
                .andExpect(jsonPath("$.waypoints[0].addressInfo.longitude").value(37.6176))

                .andExpect(jsonPath("$.waypoints[1].type").value("UNLOAD"))
                .andExpect(jsonPath("$.waypoints[1].orderingIndex").value(1))
                .andExpect(jsonPath("$.waypoints[1].radius").value(100))
                .andExpect(jsonPath("$.waypoints[1].addressInfo.city").value("Санкт-Петербург"))
                .andExpect(jsonPath("$.waypoints[1].addressInfo.street").value("Невский проспект, 50"))
                .andExpect(jsonPath("$.waypoints[1].addressInfo.latitude").value(55.3161))
                .andExpect(jsonPath("$.waypoints[1].addressInfo.longitude").value(37.9343))

                // === cargoDetails ===
                .andExpect(jsonPath("$.cargoDetails.weightKg").value(500.0))
                .andExpect(jsonPath("$.cargoDetails.volumeM3").value(15.5))
                .andExpect(jsonPath("$.cargoDetails.declaredValue").value(100000.0))
                .andExpect(jsonPath("$.cargoDetails.length").value(2.5))
                .andExpect(jsonPath("$.cargoDetails.width").value(1.8))
                .andExpect(jsonPath("$.cargoDetails.height").value(1.7))
                .andExpect(jsonPath("$.cargoDetails.cargoType").isArray())
                .andExpect(jsonPath("$.cargoDetails.cargoType[0]").value("bulk_cargo"))
                .andExpect(jsonPath("$.cargoDetails.cargoType").isNotEmpty())

                // Проверка поля methodDeterminingMass
                .andExpect(jsonPath("$.cargoDetails.methodDeterminingMass").isArray())
                .andExpect(jsonPath("$.cargoDetails.methodDeterminingMass").isNotEmpty())
                .andExpect(jsonPath("$.cargoDetails.methodDeterminingMass[0]").value("Код 02 Взвешивание поосно"))

                .andExpect(jsonPath("$.cargoDetails.cargoPackage").isArray())
                .andExpect(jsonPath("$.cargoDetails.cargoPackage[0]").value("rigid_and_semirigid"))
                .andExpect(jsonPath("$.cargoDetails.cargoPackage").isNotEmpty())

                .andExpect(jsonPath("$.vehicleRequirements.vehicleBodyType").isArray())
                .andExpect(jsonPath("$.vehicleRequirements.vehicleBodyType[0]").value("temperature_controlled"))
                .andExpect(jsonPath("$.vehicleRequirements.vehicleBodyType").isNotEmpty())

                // === specialCondition ===
                .andExpect(jsonPath("$.specialConditions.isDangerous").value(false))
                .andExpect(jsonPath("$.specialConditions.hasTemperature").value(true))
                .andExpect(jsonPath("$.specialConditions.tempMin").value(2))
                .andExpect(jsonPath("$.specialConditions.tempMax").value(8))
                .andExpect(jsonPath("$.specialConditions.otherConditions").value("Хранить и перевозить при температуре +2..+8°C"))

                // === vehicleRequirements ===
                .andExpect(jsonPath("$.vehicleRequirements.loadType").value("rear"))
                .andExpect(jsonPath("$.vehicleRequirements.unloadType").value("rear"))
                .andExpect(jsonPath("$.vehicleRequirements.capacityM3").value(30.0))
                .andExpect(jsonPath("$.vehicleRequirements.loadCapacity").value(10.0))
                .andExpect(jsonPath("$.vehicleRequirements.noAdditionalLoad").value(true))
                .andExpect(jsonPath("$.vehicleRequirements.vehicleExtraFeatures[0]").value("palletJack"))
                .andExpect(jsonPath("$.vehicleRequirements.vehicleExtraFeatures[1]").value("thermalCover"))
                .andExpect(jsonPath("$.vehicleRequirements.comment").value("Требуется манипулятор на разгрузке"))

                .andReturn()
                .getResponse();
        log.info("Get Response content: {}", response.getContentAsString());
        assertThat(mapper.readValue(response.getContentAsString(), RequestDto.class).getHumanReadableId()).isNotNull();
    }

    @Test
    @DisplayName("Публикация  заявки")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void publish() throws Exception {
        var response = mockMvc.perform(post("/draft")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(getContentAsString("/json/requestDraft.json"))
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        log.info("Response content: {}", response.getContentAsString());
        var responseDto = mapper.readValue(response.getContentAsString(), RequestValidationResponse.class);

        assertThat(responseDto.getValidation().getIsValidForPublication()).isTrue();
        response = mockMvc.perform(
                        get("/%s".formatted(responseDto.getRequest().getId()))
                                .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var requestDto = mapper.readValue(response.getContentAsString(), RequestDto.class);
        assertThat(requestDto.getHumanReadableId()).isNotNull();

        response = mockMvc.perform(post("/publish")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(requestDto))
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        responseDto = mapper.readValue(response.getContentAsString(), RequestValidationResponse.class);

        responseDto.getValidation().getErrors()
                .forEach(error -> log.error("Ошибка валидации {} - {} ", error.getField(), error.getMessage()));

        assertThat(responseDto.getValidation().getIsValidForPublication()).isTrue();
        log.info("Response content: {}", response.getContentAsString());

        var requests = requestService.getByOwnerId(UUID.fromString(VALID_OWNER_ID));
        assertThat(requests).hasSize(1);
        var result = requests.getFirst();
        assertThat(result.getDraftInfo()).isNull();
        assertThat(result.getPublishedAt()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PUBLISHED);

        mockMvc.perform(
                        get("/%s".formatted(responseDto.getRequest().getId()))
                                .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId").value(result.getOwnerId().toString()))
                .andExpect(jsonPath("$.humanreadableId").value(result.getHumanReadableId()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.internalId").value(result.getInternalId()))
                .andExpect(jsonPath("$.ownerId").value(VALID_OWNER_ID))
                .andExpect(jsonPath("$.status").value(PUBLISHED.name()))
                .andExpect(jsonPath("$.useEtrn").value(result.getUseEtrn()))
                .andExpect(jsonPath("$.viewType").value("FIXED"))
                .andExpect(jsonPath("$.paymentForm").value("NON_CASH"))
                .andExpect(jsonPath("$.paymentTerms").value("PREPAYMENT"))
                .andExpect(jsonPath("$.paymentDays").doesNotExist())
                .andExpect(jsonPath("$.senderForwarder").value(true))

                // === Waypoints: проверяем массив из 2 элементов ===
                .andExpect(jsonPath("$.waypoints").isArray())
                .andExpect(jsonPath("$.waypoints[0].type").value("LOAD"))
                .andExpect(jsonPath("$.waypoints[0].orderingIndex").value(0))
                .andExpect(jsonPath("$.waypoints[0].radius").value(50))
                .andExpect(jsonPath("$.waypoints[0].addressInfo.city").value("Москва"))
                .andExpect(jsonPath("$.waypoints[0].addressInfo.street").value("Ленина, 1"))
                .andExpect(jsonPath("$.waypoints[0].addressInfo.building").value("корпус 2"))
                .andExpect(jsonPath("$.waypoints[0].addressInfo.latitude").value(55.7558))
                .andExpect(jsonPath("$.waypoints[0].addressInfo.longitude").value(37.6176))

                .andExpect(jsonPath("$.waypoints[1].type").value("UNLOAD"))
                .andExpect(jsonPath("$.waypoints[1].orderingIndex").value(1))
                .andExpect(jsonPath("$.waypoints[1].radius").value(100))
                .andExpect(jsonPath("$.waypoints[1].addressInfo.city").value("Санкт-Петербург"))
                .andExpect(jsonPath("$.waypoints[1].addressInfo.street").value("Невский проспект, 50"))
                .andExpect(jsonPath("$.waypoints[1].addressInfo.latitude").value(55.3161))
                .andExpect(jsonPath("$.waypoints[1].addressInfo.longitude").value(37.9343))

                // === cargoDetails ===
                .andExpect(jsonPath("$.cargoDetails.weightKg").value(500.0))
                .andExpect(jsonPath("$.cargoDetails.volumeM3").value(15.5))
                .andExpect(jsonPath("$.cargoDetails.declaredValue").value(100000.0))
                .andExpect(jsonPath("$.cargoDetails.length").value(2.5))
                .andExpect(jsonPath("$.cargoDetails.width").value(1.8))

                .andExpect(jsonPath("$.cargoDetails.cargoType").isArray())
                .andExpect(jsonPath("$.cargoDetails.cargoType[0]").value("bulk_cargo"))
                .andExpect(jsonPath("$.cargoDetails.cargoType").isNotEmpty())

                .andExpect(jsonPath("$.cargoDetails.cargoPackage").isArray())
                .andExpect(jsonPath("$.cargoDetails.cargoPackage[0]").value("rigid_and_semirigid"))
                .andExpect(jsonPath("$.cargoDetails.cargoPackage").isNotEmpty())

                .andExpect(jsonPath("$.vehicleRequirements.vehicleBodyType").isArray())
                .andExpect(jsonPath("$.vehicleRequirements.vehicleBodyType[0]").value("temperature_controlled"))
                .andExpect(jsonPath("$.vehicleRequirements.vehicleBodyType").isNotEmpty())
                .andExpect(jsonPath("$.vehicleRequirements.vehicleExtraFeatures[0]").value("palletJack"))
                .andExpect(jsonPath("$.vehicleRequirements.vehicleExtraFeatures[1]").value("thermalCover"))

                // === vehicleRequirements ===
                .andExpect(jsonPath("$.vehicleRequirements.loadType").value("rear"))
                .andExpect(jsonPath("$.vehicleRequirements.unloadType").value("rear"))
                .andExpect(jsonPath("$.vehicleRequirements.capacityM3").value(30.0))
                .andExpect(jsonPath("$.vehicleRequirements.loadCapacity").value(10.0))
                .andExpect(jsonPath("$.vehicleRequirements.noAdditionalLoad").value(true))
                .andExpect(jsonPath("$.vehicleRequirements.comment").value("Требуется манипулятор на разгрузке"))

                .andReturn()
                .getResponse();
    }

    @Test
    @DisplayName("Создание черновика заявки и редактирование")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createUncompletedEdit() throws Exception {
        var contentRequest = getContentAsString("/json/requestDraft.json");
        var requestDto = mapper.readValue(contentRequest, RequestDto.class);

        var tempWaypoints = requestDto.getWaypoints();
        requestDto.setWaypoints(List.of());
        requestDto.setPaymentTerms("");
        requestDto.setPaymentForm("");
        requestDto.setViewType("");

        var response = mockMvc.perform(post("/draft")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(requestDto))
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        log.info("Response content: {}", response.getContentAsString());
        var responseDto = mapper.readValue(response.getContentAsString(), RequestValidationResponse.class);
        assertThat(responseDto.getValidation().getIsValidForPublication()).isFalse();

        response =  mockMvc.perform(
                        get("/%s".formatted(responseDto.getRequest().getId()))
                                .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        requestDto = mapper.readValue(response.getContentAsString(), RequestDto.class);
        assertThat(requestDto.getHumanReadableId()).isNotNull();

        requestDto.setWaypoints(tempWaypoints);

        response = mockMvc.perform(
                put("/%s".formatted(responseDto.getRequest().getId()))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(contentRequest)
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        responseDto = mapper.readValue(response.getContentAsString(), RequestValidationResponse.class);

        assertThat(responseDto.getValidation().getIsValidForPublication()).isTrue();

        requestDto.setPaymentTerms(PaymentTerms.PREPAYMENT.toString());
        requestDto.setPaymentForm(PaymentForm.NON_CASH.toString());
        requestDto.setViewType(ViewType.FIXED.toString());

        mockMvc.perform(post("/publish")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(requestDto))
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn();

        response =  mockMvc.perform(
                        get("/%s".formatted(responseDto.getRequest().getId()))
                                .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        requestDto = mapper.readValue(response.getContentAsString(), RequestDto.class);
        assertThat(requestDto.getStatus()).isEqualTo(PUBLISHED.name());

        requestDto.setPaymentTerms("");

        response = mockMvc.perform(
                        put("/%s".formatted(responseDto.getRequest().getId()))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(mapper.writeValueAsString(requestDto))
                                .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        responseDto = mapper.readValue(response.getContentAsString(), RequestValidationResponse.class);
        assertThat(responseDto.getValidation().getIsValidForPublication()).isFalse();

        var requests = requestService.getByOwnerId(UUID.fromString(VALID_OWNER_ID));
        assertThat(requests).hasSize(1);
        assertThat(requests.getFirst().getPaymentTerms()).isEqualTo(PaymentTerms.PREPAYMENT);

        response =  mockMvc.perform(
                        get("/%s".formatted(responseDto.getRequest().getId()))
                                .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        requestDto = mapper.readValue(response.getContentAsString(), RequestDto.class);
        requestDto.setPaymentTerms(PaymentTerms.ON_DELIVERY.name());

        response = mockMvc.perform(
                        put("/%s".formatted(responseDto.getRequest().getId()))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(mapper.writeValueAsString(requestDto))
                                .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        responseDto = mapper.readValue(response.getContentAsString(), RequestValidationResponse.class);
        assertThat(responseDto.getValidation().getIsValidForPublication()).isTrue();

        requests = requestService.getByOwnerId(UUID.fromString(VALID_OWNER_ID));
        assertThat(requests).hasSize(1);
        assertThat(requests.getFirst().getPaymentTerms()).isEqualTo(PaymentTerms.ON_DELIVERY);

    }

    @ParameterizedTest
    @ValueSource(strings = {"a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004", "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380088"})
    @DisplayName("Поиск опубликованных заявок без фильтров")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void searchPublishedRequestsWithoutFilters(String organizationId) throws Exception {
        // Создаем черновик
        var requestJson = getContentAsString("/json/requestDraft.json");
        var draftResponse = mockMvc.perform(post("/draft")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID)))
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        log.info("Response content: {}", draftResponse.getContentAsString());
        var responseDto = mapper.readValue(draftResponse.getContentAsString(), RequestValidationResponse.class);
        assertThat(responseDto.getValidation().getIsValidForPublication()).isTrue();
        // Получаем созданный запрос
        var getRequest = mockMvc.perform(
                        get("/%s".formatted(responseDto.getRequest().getId()))
                                .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var requestDto = mapper.readValue(getRequest.getContentAsString(), RequestDto.class);
        // Публикуем
        mockMvc.perform(post("/publish")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(requestDto))
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        replyRepository.save(CarrierReply.builder()
                .request(Request.builder().id(responseDto.getRequest().getId()).build())
                .organization(Organization.builder().id(UUID.fromString(organizationId)).build())
                .reply(new CarrierReplyDto(
                        new CarrierReplyDto.Auto("Volvo FH16", "Volvo", "FH16", "2020", "16","A123AA777"),
                        new CarrierReplyDto.Trailer("КрАЗ Т-2020", "КрАЗ", "Т-2020", "B456BB777"),
                        new CarrierReplyDto.Driver("Иван Иванов", "+79161234567", "1234 567890"),
                        50000.0,
                        null,
                        "Готов к отправке"))
                .build());

        // поиск без фильтров
        mockMvc.perform(post("/list/published")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{}")
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                // Проверяем, что результат — массив
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                // Проверяем поля первого (и единственного) элемента
                .andExpect(jsonPath("$.content[0].humanreadableId").value(requestDto.getHumanReadableId()))
                .andExpect(jsonPath("$.content[0].addressFrom").value(requestDto.getWaypoints().getFirst().getAddressInfo().toFullAddressString()))
                .andExpect(jsonPath("$.content[0].addressTo").value(requestDto.getWaypoints().getLast().getAddressInfo().toFullAddressString()))
                .andExpect(jsonPath("$.content[0].loadingDate").value(requestDto.getWaypoints().getFirst().getDate().toString()))
                .andExpect(jsonPath("$.content[0].deliveryDate").value(requestDto.getWaypoints().getLast().getDate().toString()))
                .andExpect(jsonPath("$.content[0].vehicleBodyType").value(requestDto.getVehicleRequirements().getVehicleBodyType()))
                .andExpect(jsonPath("$.content[0].weight").value((requestDto.getCargoDetails().getWeightKg().divide(BigDecimal.valueOf(1000))).toString()))
                .andExpect(jsonPath("$.content[0].volume").value(requestDto.getCargoDetails().getVolumeM3().toString()))
                .andExpect(jsonPath("$.content[0].costRequest").value(requestDto.getCostRequest()))
                .andExpect(jsonPath("$.content[0].vatInclude").value(requestDto.getVatInclude()))
                .andExpect(jsonPath("$.content[0].selfReplied").value(organizationId.equals("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004")));

        // поиск с фильтрацией
        mockMvc.perform(post("/list/published")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(getContentAsString("/json/searchAvailableRequests.json"))
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                // Проверяем, что результат — массив
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                // Проверяем поля первого (и единственного) элемента
                .andExpect(jsonPath("$.content[0].humanreadableId").value(requestDto.getHumanReadableId()))
                .andExpect(jsonPath("$.content[0].addressFrom").value(requestDto.getWaypoints().getFirst().getAddressInfo().toFullAddressString()))
                .andExpect(jsonPath("$.content[0].addressTo").value(requestDto.getWaypoints().getLast().getAddressInfo().toFullAddressString()))
                .andExpect(jsonPath("$.content[0].loadingDate").value(requestDto.getWaypoints().getFirst().getDate().toString()))
                .andExpect(jsonPath("$.content[0].deliveryDate").value(requestDto.getWaypoints().getLast().getDate().toString()))
                .andExpect(jsonPath("$.content[0].vehicleBodyType").value(requestDto.getVehicleRequirements().getVehicleBodyType()))
                .andExpect(jsonPath("$.content[0].weight").value((requestDto.getCargoDetails().getWeightKg().divide(BigDecimal.valueOf(1000))).toString()))
                .andExpect(jsonPath("$.content[0].volume").value(requestDto.getCargoDetails().getVolumeM3().toString()))
                .andExpect(jsonPath("$.content[0].costRequest").value(requestDto.getCostRequest()))
                .andExpect(jsonPath("$.content[0].vatInclude").value(requestDto.getVatInclude()));
    }

    @Test
    @DisplayName("Поиск заявок грузовладельца с максимальной фильтрацией")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/test_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void searchShipperRequestsWithFullFilters() throws Exception {
        // Создаем черновик
        var requestJson = getContentAsString("/json/requestDraft.json");
        var draftResponse = mockMvc.perform(post("/draft")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestJson)
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        log.info("Response content: {}", draftResponse.getContentAsString());
        var responseDto = mapper.readValue(draftResponse.getContentAsString(), RequestValidationResponse.class);
        assertThat(responseDto.getValidation().getIsValidForPublication()).isTrue();
        // Получаем созданный запрос
        var getRequest = mockMvc.perform(
                        get("/%s".formatted(responseDto.getRequest().getId()))
                                .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var requestDto = mapper.readValue(getRequest.getContentAsString(), RequestDto.class);
        // Публикуем
        mockMvc.perform(post("/publish")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(requestDto))
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var filterPattern = """
                {
                  "humanreadableId": "%s",
                  "statusSet": ["DRAFT", "PUBLISHED", "CANCELLED_BY_CUSTOMER"],
                  "creationDateRange": {
                    "start": %s,
                    "end": %s
                  },
                  "loadingDateRange": {
                    "start": "2025-04-06",
                    "end": "2025-04-06"
                  },
                  "deliveryDateRange": {
                    "start": "2025-04-08",
                    "end": "2025-04-08"
                  },
                  "sortSetting": {
                    "property": "DELIVERY_DATE",
                    "directionAsc": false
                  },
                  "pageSetting": {
                    "page": 0,
                    "size": 10
                  }
                }
                """;
        var filterString = String.format(filterPattern,
                requestDto.getHumanReadableId(),
                "\"%s\"".formatted(LocalDateTime.now()),
                "\"%s\"".formatted(LocalDateTime.now()));

        // Поиск с максимальной фильтрацией
        mockMvc.perform(post("/list/shipper")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(filterString)
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                // Проверяем, что результат — массив
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                // Проверяем поля первого (и единственного) элемента
                .andExpect(jsonPath("$.content[0].humanreadableId").value(requestDto.getHumanReadableId()))
                .andExpect(jsonPath("$.content[0].addressFrom").value(requestDto.getWaypoints().getFirst().getAddressInfo().toFullAddressString()))
                .andExpect(jsonPath("$.content[0].addressTo").value(requestDto.getWaypoints().getLast().getAddressInfo().toFullAddressString()))
                .andExpect(jsonPath("$.content[0].loadingDate").value(requestDto.getWaypoints().getFirst().getDate().toString()))
                .andExpect(jsonPath("$.content[0].deliveryDate").value(requestDto.getWaypoints().getLast().getDate().toString()))
                .andExpect(jsonPath("$.content[0].status").value("Опубликована"))
                .andExpect(jsonPath("$.content[0].internalId").value(requestDto.getInternalId()));

        filterString = String.format(filterPattern,
                requestDto.getHumanReadableId(),
                "\"%s\"".formatted(LocalDateTime.now()),
                null);

        mockMvc.perform(post("/list/shipper")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(filterString)
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                // Проверяем, что результат — массив
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                // Проверяем поля первого (и единственного) элемента
                .andExpect(jsonPath("$.content[0].humanreadableId").value(requestDto.getHumanReadableId()));

        filterString = String.format(filterPattern,
                requestDto.getHumanReadableId(),
                null,
                "\"%s\"".formatted(LocalDateTime.now()));

        mockMvc.perform(post("/list/shipper")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(filterString)
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                // Проверяем, что результат — массив
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                // Проверяем поля первого (и единственного) элемента
                .andExpect(jsonPath("$.content[0].humanreadableId").value(requestDto.getHumanReadableId()));
    }

    @Test
    @DisplayName("Поиск заявок грузовладельца без фильтров")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/test_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void searchShipperRequestsWithoutFilters() throws Exception {
        // Создаем черновик
        var requestJson = getContentAsString("/json/requestDraft.json");
        var draftResponse = mockMvc.perform(post("/draft")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestJson)
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        log.info("Response content: {}", draftResponse.getContentAsString());
        var responseDto = mapper.readValue(draftResponse.getContentAsString(), RequestValidationResponse.class);
        assertThat(responseDto.getValidation().getIsValidForPublication()).isTrue();
        // Получаем созданный запрос
        var getRequest = mockMvc.perform(
                        get("/%s".formatted(responseDto.getRequest().getId()))
                                .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var requestDto = mapper.readValue(getRequest.getContentAsString(), RequestDto.class);
        // Публикуем
        var pubResp = mockMvc.perform(post("/publish")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(requestDto))
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        mapper.readValue(pubResp.getContentAsString(), RequestValidationResponse.class);

        // Поиск без фильтров
        mockMvc.perform(post("/list/shipper")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{}")
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                // Проверяем, что результат — массив
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content").value(Matchers.hasSize(2)));
    }

    @Test
    @DisplayName("Быстрая публикация без создания черновика")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void fastPublish() throws Exception {

        var response =  mockMvc.perform(post("/publish")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(getContentAsString("/json/requestDraft.json"))
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        log.info("Response content: {}", response.getContentAsString());
        var responseDto = mapper.readValue(response.getContentAsString(), RequestValidationResponse.class);
        // Получаем созданный и опубликованный запрос
        var getRequest = mockMvc.perform(
                        get("/%s".formatted(responseDto.getRequest().getId()))
                                .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var requestDto = mapper.readValue(getRequest.getContentAsString(), RequestDto.class);
        assertThat(requestDto.getStatus()).isEqualTo(PUBLISHED.name());

        mockMvc.perform(
                        put("/%s".formatted(responseDto.getRequest().getId()))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(mapper.writeValueAsString(requestDto))
                                .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_CARGO CARRIER"))))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @DisplayName("Успешная смена статуса заявки грузовладельцем")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/test_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testStatus() throws Exception {
        var url = "/changeStatus/shipper/%s/%s".formatted(VALID_REQUEST_ID, CARRIER_SELECTED.name());
        var response = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID).claim("roles", List.of(SHIPPER.name())))
                                .authorities(new SimpleGrantedAuthority(SHIPPER.name()))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        mapper.readValue(response.getContentAsString(), StatusResponseDto.class);
    }

    @Test
    @DisplayName("Успешная отмена заявки грузовладельцем")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/status_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testCancel() throws Exception {
        // Проверка, что CarrierReply существуют до вызова
        var repliesBefore = replyRepository.findAllByRequestId(VALID_REQUEST_ID);
        assertThat(repliesBefore).isNotEmpty().hasSize(2);

        var url = "/changeStatus/shipper/%s/%s".formatted(VALID_REQUEST_ID, CANCELLED_BY_CUSTOMER.name());
        var response = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID).claim("roles", List.of(SHIPPER.name())))
                                .authorities(new SimpleGrantedAuthority(SHIPPER.name()))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        mapper.readValue(response.getContentAsString(), StatusResponseDto.class);

        // Проверка заявки
        var request = requestService.getById(VALID_REQUEST_ID);
        assertThat(request.getStatus()).isEqualTo(CANCELLED_BY_CUSTOMER);
        assertThat(request.getCarrierOrganizationId()).isNull();
        assertThat(request.getCarrierInfo()).isNull();

        // Проверка, что CarrierReply удалены после вызова
        var repliesAfter = replyRepository.findAllByRequestId(VALID_REQUEST_ID);
        assertThat(repliesAfter).isEmpty();
    }

    @Test
    @DisplayName("Успешная смена статуса заявки грузоперевозчиком")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/status_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testStatus2() throws Exception {
        // Проверка, что CarrierReply существуют до вызова
        var repliesBefore = replyRepository.findAllByRequestId(VALID_REQUEST_ID);
        assertThat(repliesBefore).isNotEmpty().hasSize(2);

        var request = requestService.getById(VALID_REQUEST_ID);
        request.setCarrierInfo(Instancio.create(CarrierReplyDto.class));
        request.setCarrierOrganizationId(UUID.randomUUID());
        requestService.save(request);

        var url = "/changeStatus/carrier/%s/%s".formatted(VALID_REQUEST_ID, CONFIRMED.name());
        var response = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID).claim("roles", List.of(CARRIER.name())))
                                .authorities(new SimpleGrantedAuthority(CARRIER.name()))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        mapper.readValue(response.getContentAsString(), StatusResponseDto.class);

        // Проверка заявки
        request = requestService.getById(VALID_REQUEST_ID);
        assertThat(request.getStatus()).isEqualTo(CONFIRMED);
        assertThat(request.getCarrierOrganizationId()).isNotNull();
        assertThat(request.getCarrierInfo()).isNotNull();

        // Проверка, что CarrierReply удалены после вызова
        var repliesAfter = replyRepository.findAllByRequestId(VALID_REQUEST_ID);
        assertThat(repliesAfter).isEmpty();
    }

    @Test
    @DisplayName("Неуспешная смена статуса заявки грузовладельцем")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/status_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testStatus3() throws Exception {
        var url = "/changeStatus/carrier/%s/%s".formatted(VALID_REQUEST_ID, CONFIRMED.name());
        var response = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority(SHIPPER.name()))))
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse();
        mapper.readValue(response.getContentAsString(), ErrorResponseDto.class);
    }

    @Test
    @DisplayName("Неуспешная смена статуса заявки грузовладельцем")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/status_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testStatus4() throws Exception {
        var url = "/changeStatus/carrier/%s/%s".formatted(VALID_REQUEST_ID, DRAFT.name());
        var response = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID).claim("roles", List.of(SHIPPER.name())))
                                .authorities(new SimpleGrantedAuthority(SHIPPER.name()))))
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse();
        mapper.readValue(response.getContentAsString(), ErrorResponseDto.class);
    }

    @Test
    @DisplayName("Неуспешная смена статуса заявки грузоперевозчиком")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/status_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testStatus5() throws Exception {
        var url = "/changeStatus/carrier/%s/%s".formatted(UUID.randomUUID(), DRAFT.name());
        var response = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID).claim("roles", List.of(CARRIER.name())))
                                .authorities(new SimpleGrantedAuthority(CARRIER.name()))))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse();
        mapper.readValue(response.getContentAsString(), ErrorResponseDto.class);
    }

    @Test
    @DisplayName("Неуспешная смена статуса заявки грузоперевозчиком")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/status_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testStatus6() throws Exception {
        var url = "/changeStatus/carrier/%s/%s".formatted(VALID_REQUEST_ID, DRAFT.name());
        var response = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID)
                                        .claim("roles", List.of(CARRIER.name())))
                                .authorities(new SimpleGrantedAuthority(CARRIER.name()))))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
        mapper.readValue(response.getContentAsString(), ErrorResponseDto.class);
    }

    public static String getContentAsString(String filePath) {
        try {
            URL resource = RequestControllerImplTest.class.getResource(filePath);
            if (resource == null) {
                throw new IllegalArgumentException("Resource not found: " + filePath);
            }
            File file = new File(resource.getFile());
            if (!file.exists()) {
                throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());
            }
            String result = FileUtils.readFileToString(file, "UTF-8");
            log.info("File content: {}", result);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}