package ru.sber.transport.cargo.exchange.request.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.transport.cargo.exchange.request.RequestApplication;
import ru.sber.transport.cargo.exchange.request.database.dao.RequestRepository;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyDto;
import ru.sber.transport.cargo.exchange.request.dto.RequestDto;
import ru.sber.transport.cargo.exchange.request.dto.RequestValidationResponse;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.cargo.exchange.request.controller.impl.RequestControllerImplTest.getContentAsString;

@DisplayName("Проверка эндпоинтов контроллера запросов для перевозчика")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = RequestApplication.class, properties = {"logger.level.root=debug", "spring.main.cloud-platform=none"})
@EmbeddedPostgres
@ActiveProfiles("test")
@Slf4j
class RequestControllerImplCarrierTest {
    public static final String VALID_OWNER_ID = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380005";
    public static final String VALID_CARRIER_ID = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380023";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private AuthorizationManager<RequestAuthorizationContext> roleCheckService;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void init() {
        when(roleCheckService.authorize(any(), any())).thenReturn(new AuthorizationDecision(true));
    }

    @Test
    @DisplayName("Поиск опубликованных заявок без фильтров")
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void searchRequestsForCarrier() throws Exception {

        var response =  mockMvc.perform(post("/publish")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(getContentAsString("/json/requestDraft.json"))
                        .with(jwt().jwt(builder -> builder.jti(VALID_OWNER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO_OWNER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        log.info("Response content: {}", response.getContentAsString());
        var responseDto = mapper.readValue(response.getContentAsString(), RequestValidationResponse.class);

        transactionTemplate.executeWithoutResult(status ->
                requestRepository.findAll().forEach(request -> {
                    request.setCarrierOrganizationId(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380088"));
                    requestRepository.save(request);
                }));

        var getResponse = mockMvc.perform(
                        get("/%s".formatted(responseDto.getRequest().getId()))
                                .with(jwt().jwt(builder -> builder.jti(VALID_CARRIER_ID))
                                        .authorities(new SimpleGrantedAuthority("ROLE_CARGO_CARRIER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var requestDto =  mapper.readValue(getResponse.getContentAsString(), RequestDto.class);

        transactionTemplate.executeWithoutResult(status -> {
            var request =  requestRepository.findById(requestDto.getId()).orElseThrow();
            request.setCarrierOrganizationId(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380088"));

            try {
                request.setCarrierInfo(mapper.readValue("{\n" +
                        "       \"auto\": {\n" +
                        "         \"model\": \"MAZ-6440\",\n" +
                        "         \"mark\": \"МАЗ\",\n" +
                        "         \"regNumber\": \"М456СР178\"\n" +
                        "       },\n" +
                        "       \"driver\": {\n" +
                        "         \"fio\": \"Григорьев Станислав Михайлович\",\n" +
                        "         \"phone\": \"+7 921 444-55-66\",\n" +
                        "         \"licence\": \"1784567890\"\n" +
                        "       },\n" +
                        "       \"cost\": 700000.00,\n" +
                        "       \"vat\": \"VAT_20\",\n" +
                        "       \"comment\": \"Быстрая погрузка, опыт доставки одежды\"\n" +
                        "     }", CarrierReplyDto.class));
            } catch (JsonProcessingException e) {
                //
            }
            requestRepository.save(request);
        });

        // поиск без фильтров
        mockMvc.perform(post("/list/carrier")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{}")
                        .with(jwt().jwt(builder -> builder.jti(VALID_CARRIER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO_CARRIER"))))
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
                .andExpect(jsonPath("$.content[0].costRequest").value(requestDto.getCostRequest()))
                .andExpect(jsonPath("$.content[0].etrn").value(requestDto.getUseEtrn()));

        // поиск с фильтрацией
        mockMvc.perform(post("/list/carrier")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(getContentAsString("/json/searchCarrierRequests.json"))
                        .with(jwt().jwt(builder -> builder.jti(VALID_CARRIER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CARGO_CARRIER"))))
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
                .andExpect(jsonPath("$.content[0].costRequest").value(requestDto.getCostRequest()))
                .andExpect(jsonPath("$.content[0].etrn").value(requestDto.getUseEtrn()));
    }
}
