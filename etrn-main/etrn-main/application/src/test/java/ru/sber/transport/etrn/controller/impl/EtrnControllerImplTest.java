package ru.sber.transport.etrn.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.etrn.TestCommon;
import ru.sber.transport.etrn.dto.*;
import ru.sber.transport.etrn.exceptions.EtrnNotFoundException;
import ru.sber.transport.etrn.service.AttorneyCheckService;
import ru.sber.transport.etrn.service.EtrnService;
import ru.sber.transport.etrn.service.LockService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ru.sber.transport.etrn.schedulers.EtrnLockSchedulingProcessor;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@EmbeddedPostgres
@DisplayName("Интеграционные тесты контроллера EtrnControllerImpl")
class EtrnControllerImplTest extends TestCommon {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AttorneyCheckService attorneyCheckService;

    @InjectMocks
    private EtrnControllerImpl etrnController;


    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EtrnService etrnService;

    @MockitoBean
    private LockService lockService;

    @MockitoBean("etrnLockSchedulingProcessorImpl")
    private EtrnLockSchedulingProcessor etrnLockSchedulingProcessor;

    private UUID testEtrnId;
    private EtrnDetailDto detailDto;

    @BeforeEach
    void setUp() {
        testEtrnId = UUID.randomUUID();
        var signedAt = LocalDateTime.of(2025, 1, 15, 11, 0, 0);

        detailDto = new EtrnDetailDto(
                testEtrnId,
                ETRN_HRID_1,
                "APP-001",
                "RT-001",
                STATUS_WAIT_KORUS_CONFIRMATION,
                null,
                TIME_ZONE,
                "T3",
                ORGANIZATION_NAME_1,
                ORGANIZATION_NAME_2,
                "ООО «Перевозчик»",
                "Груз — оборудование",
                10,
                new BigDecimal("1500.50"),
                "Москва — Санкт-Петербург",
                null,
                new BigDecimal("12.00"),
                new BigDecimal("2.50"),
                new BigDecimal("3.00"),
                "Иванов И.И.",
                "DRIVER",
                LocalDateTime.of(2025, 1, 15, 10, 30, 0),
                "EVT-001",
                List.of(
                        new TitleEntryDto("T1", null, null),
                        new TitleEntryDto("T3", signedAt, EMPLOYEE_HRID_1)
                ),
                null,
                null,
                true,
                LocalDateTime.of(2025, 1, 15, 10, 0, 0),
                LocalDateTime.of(2025, 1, 15, 12, 0, 0),
                1L
        );
    }

    @Test
    @DisplayName("Получение детальной информации по ЭТрН")
    @WithMockUser(username = USER_ID_1_STR, roles = {"GUEST"})
    void getById_shouldReturnDetailDto() throws Exception {
        // Arrange
        when(etrnService.getById(testEtrnId)).thenReturn(detailDto);

        // Act & Assert
        mockMvc.perform(get("/" + testEtrnId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEtrnId.toString()))
                .andExpect(jsonPath("$.humanReadableId").value(ETRN_HRID_1))
                .andExpect(jsonPath("$.status").value(STATUS_WAIT_KORUS_CONFIRMATION))
                .andExpect(jsonPath("$.currentTitle").value("T3"))
                .andExpect(jsonPath("$.titleChain").isArray())
                .andExpect(jsonPath("$.titleChain.length()").value(2));
    }

    @Test
    @DisplayName("Получение детальной информации — ЭТрН не найдена")
    @WithMockUser(username = USER_ID_1_STR, roles = {"GUEST"})
    void getById_shouldReturn404WhenNotFound() throws Exception {
        // Arrange
        when(etrnService.getById(testEtrnId)).thenThrow(
                new EtrnNotFoundException(testEtrnId.toString()));

        // Act & Assert
        mockMvc.perform(get("/" + testEtrnId))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // POST / (create)
    // =========================================================================

    @Test
    @DisplayName("Создание ЭТрН")
    @WithMockUser(username = USER_ID_1_STR, roles = {"GUEST"})
    void create_shouldReturnCreated() throws Exception {
        // Arrange
        var createRequest = new EtrnCreateRequest(
                "ETRN-NEW-00000001",
                "ООО «Отправитель»",
                "ООО «Получатель»",
                "ООО «Перевозчик»",
                "Europe/Moscow"
        );

        var createdDto = new EtrnDto(
                UUID.randomUUID(),
                createRequest.humanReadableId(),
                null,
                STATUS_IDENTIFIED,
                null,
                createRequest.senderName(),
                createRequest.receiverName(),
                createRequest.carrierName(),
                LocalDateTime.of(2025, 1, 15, 10, 0, 0),
                LocalDateTime.of(2025, 1, 15, 10, 0, 0)
        );

        when(etrnService.create(any())).thenReturn(createdDto);

        // Act & Assert
        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.humanReadableId").value(createRequest.humanReadableId()))
                .andExpect(jsonPath("$.status").value(STATUS_IDENTIFIED));
    }

    @Test
    @DisplayName("Создание ЭТрН — невалидный запрос")
    @WithMockUser(username = USER_ID_1_STR, roles = {"GUEST"})
    void create_shouldReturn400WhenInvalid() throws Exception {
        // Arrange — humanReadableId пустой (не проходит @NotBlank)
        var invalidRequest = new EtrnCreateRequest(
                "",
                "ООО «Отправитель»",
                "ООО «Получатель»",
                "ООО «Перевозчик»",
                "Europe/Moscow"
        );

        // Act & Assert
        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // POST /list
    // =========================================================================

    @Test
    @DisplayName("Журнал ЭТрН — пагинация и поиск")
    @WithMockUser(username = USER_ID_1_STR, roles = {"GUEST"})
    void list_shouldReturnPage() throws Exception {
        // Arrange
        var pageSetting = new SearchEtrnDto.PageSetting(0, 10);
        var sortSetting = new SearchEtrnDto.SortSetting(true, "createdAt");
        var searchRequest = new SearchEtrnDto(pageSetting, sortSetting, null, null, null);

        var journalDto = new EtrnJournalDto(
                testEtrnId,
                ETRN_HRID_1,
                null,
                STATUS_WAIT_KORUS_CONFIRMATION,
                "T3",
                ORGANIZATION_NAME_1,
                ORGANIZATION_NAME_2,
                "ООО «Перевозчик»",
                LocalDateTime.of(2025, 1, 15, 10, 0, 0),
                LocalDateTime.of(2025, 1, 15, 12, 0, 0)
        );

        var page = new PageImpl<>(List.of(journalDto), Pageable.unpaged(), 1);
        when(etrnService.list(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(post("/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].humanReadableId").value(ETRN_HRID_1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // =========================================================================
    // PUT /{id}/lock
    // =========================================================================

    @Test
    @DisplayName("Установка блокировки")
    @WithMockUser(username = USER_ID_1_STR, roles = {"GUEST"})
    void lock_shouldReturnOk() throws Exception {
        // Arrange
        doNothing().when(lockService).lock(any(), any());

        // Act & Assert
        mockMvc.perform(put("/" + testEtrnId + "/lock"))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // DELETE /{id}/lock
    // =========================================================================

    @Test
    @DisplayName("Снятие блокировки")
    @WithMockUser(username = USER_ID_1_STR, roles = {"GUEST"})
    void unlock_shouldReturnOk() throws Exception {
        // Arrange
        doNothing().when(lockService).unlock(any(), any());

        // Act & Assert
        mockMvc.perform(delete("/" + testEtrnId + "/lock"))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // Authorization
    // =========================================================================

    @Test
    @DisplayName("Запрос без авторизации — security отключён, возвращает 200")
    void withoutAuthorization_shouldReturn200() throws Exception {
        // Arrange
        when(etrnService.getById(any(UUID.class))).thenReturn(detailDto);

        // Act & Assert
        mockMvc.perform(get("/" + testEtrnId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Проверка доверенности успешна — возвращает 200 с данными")
    void attorneyCheck_success_returnsDto() {
        // Arrange
        var attorneyDto = new AttorneyCheckResponseDto("TRN-001", LocalDate.of(2024, 1, 1), LocalDate.of(2027, 12, 31));
        ResponseEntity<AttorneyCheckResponseDto> responseEntity = ResponseEntity.ok(attorneyDto);

        when(attorneyCheckService.checkAttorney()).thenReturn(responseEntity);

        // Act
        ResponseEntity<AttorneyCheckResponseDto> response = etrnController.attorneyCheck(null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().attorneyNumber()).isEqualTo("TRN-001");
        assertThat(response.getBody().issueDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(response.getBody().expiryDate()).isEqualTo(LocalDate.of(2027, 12, 31));
    }

    @Test
    @DisplayName("Проверка доверенности — ошибка сервиса, возвращает 500")
    void attorneyCheck_serviceError_returns500() {
        // Arrange
        when(attorneyCheckService.checkAttorney())
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_GATEWAY).build());

        // Act
        ResponseEntity<AttorneyCheckResponseDto> response = etrnController.attorneyCheck(null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    }
}
