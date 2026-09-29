package ru.sber.transport.cargo.exchange.request.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.cargo.exchange.request.database.dao.ReplyRepository;
import ru.sber.transport.cargo.exchange.request.database.dao.RequestHistoryRepository;
import ru.sber.transport.cargo.exchange.request.database.dao.RequestRepository;
import ru.sber.transport.cargo.exchange.request.database.model.Request;
import ru.sber.transport.cargo.exchange.request.database.model.Request_;
import ru.sber.transport.cargo.exchange.request.database.model.VehicleRequirements;
import ru.sber.transport.cargo.exchange.request.database.model.Waypoint;
import ru.sber.transport.cargo.exchange.request.dto.ShipperRequestDto;
import ru.sber.transport.cargo.exchange.request.dto.ShipperRequestFilterDto;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;
import ru.sber.transport.cargo.exchange.request.exception.RequestNotFoundException;
import ru.sber.transport.cargo.exchange.request.mapper.RequestMapper;
import ru.sber.transport.cargo.exchange.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static ru.sber.transport.cargo.exchange.request.enums.RequestStatus.DRAFT;

@SpringBootTest(classes = RequestServiceImpl.class)
@DisplayName("RequestServiceImpl — Юнит-тест")
class RequestServiceImplTest {

    public static final LocalDateTime NOW = LocalDateTime.now();

    @MockitoBean
    private RequestRepository requestRepository;

    @MockitoBean
    private RequestHistoryRepository requestHistoryRepository;

    @MockitoBean
    private ReplyRepository replyRepository;

    @Autowired
    private RequestService requestService;

    @MockitoBean
    private RequestMapper requestMapper;

    @MockitoBean
    private ObjectMapper objectMapper;


    private final UUID ownerId = UUID.randomUUID();
    private final UUID requestId = UUID.randomUUID();

    private UUID organizationId;
    private Request requestDraft;
    private Request requestPublished;
    private ShipperRequestDto draftDto;
    private ShipperRequestDto publishedDto;

    private Request request;

    @BeforeEach
    void setUp() {

        request = Request.builder()
                .id(requestId)
                .ownerId(ownerId)
                .status(DRAFT)
                .humanReadableId("ОП-202602-0000001")
                .createdAt(NOW)
                .expiresAt(NOW.plusDays(2))
                .build();

        organizationId = UUID.randomUUID();

        // Создаем тестовые заявки
        requestDraft = new Request();
        requestDraft.setId(UUID.randomUUID());
        requestDraft.setStatus(DRAFT);
        requestDraft.setOrganizationId(organizationId);
        requestDraft.setCreatedAt(LocalDateTime.now());

        requestPublished = new Request();
        requestPublished.setId(UUID.randomUUID());
        requestPublished.setStatus(RequestStatus.PUBLISHED);
        requestPublished.setOrganizationId(organizationId);
        requestPublished.setCreatedAt(LocalDateTime.now());

        // DTO
        draftDto = new ShipperRequestDto(
                requestDraft.getId(),
                null,
                null,
                null,
                null,
                null,
                null,
                "Черновик",
                null
        );

        publishedDto = new ShipperRequestDto(
                requestPublished.getId(),
                null,
                null,
                null,
                null,
                null,
                null,
                "Опубликована",
                null
        );
    }

    @Test
    @DisplayName("Должен вернуть список заявок по ownerId")
    void shouldReturnRequests_ByOwnerId() {
        // Given
        when(requestRepository.findByOwnerId(ownerId)).thenReturn(List.of(request));

        // When
        List<Request> result = requestService.getByOwnerId(ownerId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(requestId);
        verify(requestRepository).findByOwnerId(ownerId);
    }

    @Test
    @DisplayName("Должен выбросить исключение, если ownerId = null")
    void shouldThrow_WhenOwnerIdIsNull() {
        assertThatThrownBy(() -> requestService.getByOwnerId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Идентификатор владельца (ownerId) не может быть null");
    }

    @Test
    @DisplayName("Должен найти заявку по id и ownerId")
    void shouldFindByIdAndOwnerId() {
        // Given
        when(requestRepository.getByIdAndOwnerId(requestId, ownerId))
                .thenReturn(Optional.of(request));

        // When
        Request result = requestService.getByIdAndUserId(requestId, ownerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(requestId);
        verify(requestRepository).getByIdAndOwnerId(requestId, ownerId);
    }

    @Test
    @DisplayName("Должен выбросить исключение, если заявка не найдена")
    void shouldThrow_WhenRequestNotFound() {
        // Given
        when(requestRepository.getByIdAndOwnerId(requestId, ownerId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> requestService.getByIdAndUserId(requestId, ownerId))
                .isInstanceOf(RequestNotFoundException.class)
                .hasMessage("Заявка не найдена или у вас нет прав на её просмотр");
    }

    @Test
    @DisplayName("Должен выбросить исключение, если id = null")
    void shouldThrow_WhenIdIsNull() {

        assertThatThrownBy(() -> requestService.getByIdAndUserId(null, ownerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Идентификатор заявки (id) не может быть null");
    }

    @Test
    @DisplayName("Должен сохранить заявку и привязать vehicleRequirements")
    void shouldSaveRequest_WithVehicleRequirements() {
        // Given
        VehicleRequirements vr = VehicleRequirements.builder().build();
        request.setVehicleRequirements(vr);

        when(requestRepository.save(any(Request.class))).thenReturn(request);

        // When
        Request saved = requestService.save(request);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getVehicleRequirements()).isSameAs(vr);
        assertThat(vr.getRequest()).isSameAs(saved);
        verify(requestRepository).save(request);
    }

    @Test
    @DisplayName("Должен сохранить заявку и привязать waypoints")
    void shouldSaveRequest_WithWaypoints() {
        // Given
        Waypoint wp1 = Waypoint.builder().build();
        Waypoint wp2 = Waypoint.builder().build();

        request.setWaypoints(List.of(wp1, wp2));

        when(requestRepository.save(any(Request.class))).thenReturn(request);

        // When
        Request saved = requestService.save(request);

        // Then
        assertThat(saved.getWaypoints()).hasSize(2);
        assertThat(wp1.getRequest()).isSameAs(saved);
        assertThat(wp2.getRequest()).isSameAs(saved);
        verify(requestRepository).save(request);
    }

    @Test
    @DisplayName("Должен удалить просроченные черновики и вернуть количество удалённых записей")
    void shouldDeleteExpiredDrafts() {
        try (MockedStatic<LocalDateTime> mocked = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(LocalDateTime::now).thenReturn(NOW);

            when(requestRepository.deleteByStatusAndExpiresAtBefore(RequestStatus.DRAFT, NOW))
                    .thenReturn(5L);

            long deletedCount = requestService.deleteExpiredDrafts();

            assertThat(deletedCount).isEqualTo(5);
            verify(requestRepository).deleteByStatusAndExpiresAtBefore(RequestStatus.DRAFT, NOW);
        }
    }

    @Test
    void searchRequestsByOrganizationId_shouldReturnMappedResultsWithDraftHandling() {
        // Arrange
        ShipperRequestFilterDto filterDto = new ShipperRequestFilterDto();
        Pageable pageable = PageRequest.of(0, 10, Sort.by(DESC, Request_.HUMAN_READABLE_ID));

        filterDto.setOrganizationId(organizationId);

        Page<Request> requestPage = new PageImpl<>(List.of(requestDraft, requestPublished), pageable, 2);

        // Mock repository response (результат doSearchShippersRequests)
        when(requestRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(requestPage);

        // Mock mapper: toShipperRequestDto для обеих заявок
        when(requestMapper.toShipperRequestDto(requestDraft)).thenReturn(draftDto);
        when(requestMapper.toShipperRequestDto(requestPublished)).thenReturn(publishedDto);

        // Act
        Page<ShipperRequestDto> result = requestService.searchShipperRequestsByOrganizationId(filterDto);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals("Черновик", result.getContent().get(0).status());
        assertEquals("Опубликована", result.getContent().get(1).status());

        // Verify interactions
        verify(requestRepository).findAll(any(Specification.class), eq(pageable));
        verify(requestMapper, times(2)).toShipperRequestDto(any()); // для каждой заявки
    }

    @Test
    void searchRequestsByOrganizationId_shouldUseDefaultPageableWhenNull() {
        // Arrange
        ShipperRequestFilterDto filterDto = new ShipperRequestFilterDto(); // pageSetting == null

        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(DESC, Request_.HUMAN_READABLE_ID));
        Page<Request> requestPage = new PageImpl<>(List.of(requestPublished), expectedPageable, 1);

        when(requestRepository.findAll(any(Specification.class), eq(expectedPageable)))
                .thenReturn(requestPage);

        when(requestMapper.toShipperDto(any())).thenReturn(publishedDto);

        // Act
        Page<ShipperRequestDto> result = requestService.searchShipperRequestsByOrganizationId(filterDto);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(requestRepository).findAll(any(Specification.class), eq(expectedPageable));
    }
}



