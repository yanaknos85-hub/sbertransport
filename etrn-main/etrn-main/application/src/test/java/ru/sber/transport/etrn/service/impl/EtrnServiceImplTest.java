package ru.sber.transport.etrn.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.sber.transport.etrn.database.dao.EtrnAuditRepository;
import ru.sber.transport.etrn.database.dao.EtrnRepository;
import ru.sber.transport.etrn.database.model.Etrn;
import ru.sber.transport.etrn.dto.*;
import ru.sber.transport.etrn.enums.EtrnCardStatus;
import ru.sber.transport.etrn.exceptions.EtrnNotFoundException;
import ru.sber.transport.etrn.mapper.EtrnMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EtrnServiceImplTest {

    private static final String HUMAN_READABLE_ID = "ETRn-2026-001234";
    private static final UUID ETRN_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Mock
    private EtrnRepository etrnRepository;

    @Mock
    private EtrnAuditRepository auditRepository;

    @Mock
    private EtrnMapper mapper;

    @InjectMocks
    private EtrnServiceImpl service;

    private EtrnCreateRequest createRequest;
    private Etrn etrnEntity;
    private EtrnDto etrnDto;

    @BeforeEach
    void setUp() {
        createRequest = new EtrnCreateRequest(
                HUMAN_READABLE_ID,
                "Отправитель",
                "Получатель",
                "Перевозчик",
                "Europe/Moscow"
        );

        LocalDateTime now = LocalDateTime.now();
        etrnEntity = Etrn.builder()
                .id(ETRN_ID)
                .humanReadableId(HUMAN_READABLE_ID)
                .status(EtrnCardStatus.IDENTIFIED.name())
                .timeZone("Europe/Moscow")
                .active(true)
                .build();

        etrnDto = new EtrnDto(
                ETRN_ID,
                HUMAN_READABLE_ID,
                null,
                EtrnCardStatus.IDENTIFIED.name(),
                null,
                "Отправитель",
                "Получатель",
                "Перевозчик",
                now,
                now
        );
    }

    // ==================== create ====================

    @Test
    @DisplayName("create — создание новой ЭТрН")
    void create_newEtrn_returnsDto() {
        when(etrnRepository.existsByHumanReadableId(HUMAN_READABLE_ID)).thenReturn(false);
        when(mapper.toEntity(createRequest)).thenReturn(etrnEntity);
        when(etrnRepository.save(etrnEntity)).thenReturn(etrnEntity);
        when(mapper.toDto(etrnEntity)).thenReturn(etrnDto);

        EtrnDto result = service.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.humanReadableId()).isEqualTo(HUMAN_READABLE_ID);
        verify(etrnRepository).save(etrnEntity);
        verify(auditRepository).save(any());
    }

    @Test
    @DisplayName("create — идемпотентный вызов по humanReadableId")
    void create_existingId_returnsExisting() {
        when(etrnRepository.existsByHumanReadableId(HUMAN_READABLE_ID)).thenReturn(true);
        when(etrnRepository.findByHumanReadableId(HUMAN_READABLE_ID)).thenReturn(Optional.of(etrnEntity));
        when(mapper.toDto(etrnEntity)).thenReturn(etrnDto);

        EtrnDto result = service.create(createRequest);

        assertThat(result.humanReadableId()).isEqualTo(HUMAN_READABLE_ID);
        verify(etrnRepository, never()).save(any());
        verify(auditRepository, never()).save(any());
    }

    // ==================== getById ====================

    @Test
    @DisplayName("getById — успешное получение по id")
    void getById_existingId_returnsDto() {
        LocalDateTime now = LocalDateTime.now();
        EtrnDetailDto detailDto = new EtrnDetailDto(
                ETRN_ID,
                HUMAN_READABLE_ID,
                null, // applicationNumber
                null, // routeNumber
                EtrnCardStatus.IDENTIFIED.name(),
                null, // sla
                "Europe/Moscow",
                null, // currentTitle
                "Отправитель",
                "Получатель",
                "Перевозчик",
                null, // cargoDescription
                null, // cargoPlaces
                null, // cargoWeightKg
                null, // route
                null, // mrpaExpiresAt
                null, // cargoLength
                null, // cargoWidth
                null, // cargoHeight
                null, // sesFullName
                null, // sesRole
                null, // sesEventDatetime
                null, // sesEventId
                List.of(),
                null, // verifications
                null, // lockInfo
                true,
                now,
                now,
                1L
        );

        when(etrnRepository.findById(ETRN_ID)).thenReturn(Optional.of(etrnEntity));
        when(mapper.toDetailDto(etrnEntity)).thenReturn(detailDto);

        EtrnDetailDto result = service.getById(ETRN_ID);

        assertThat(result).isNotNull();
        assertThat(result.humanReadableId()).isEqualTo(HUMAN_READABLE_ID);
    }

    @Test
    @DisplayName("getById — не найдена выбрасывает EtrnNotFoundException")
    void getById_notFound_throwsException() {
        when(etrnRepository.findById(ETRN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(ETRN_ID))
                .isInstanceOf(EtrnNotFoundException.class)
                .hasMessageContaining(ETRN_ID.toString());
    }

    // ==================== list ====================

    @Test
    @DisplayName("list — поиск без фильтров")
    void list_noFilters_returnsPage() {
        var pageSetting = new SearchEtrnDto.PageSetting(0, 10);
        var sortSetting = new SearchEtrnDto.SortSetting(true, "createdAt");
        var searchRequest = new SearchEtrnDto(
                pageSetting, sortSetting, null, null, null
        );

        Page<EtrnJournalDto> mockPage = new PageImpl<>(List.of());
        when(etrnRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);

        Page<EtrnJournalDto> result = service.list(searchRequest);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        verify(etrnRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("list — поиск с фильтром humanReadableId")
    void list_withHumanReadableId_filterApplied() {
        var pageSetting = new SearchEtrnDto.PageSetting(0, 10);
        var searchRequest = new SearchEtrnDto(
                pageSetting, null, HUMAN_READABLE_ID, null, null
        );

        Page<EtrnJournalDto> mockPage = new PageImpl<>(List.of());
        when(etrnRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);

        service.list(searchRequest);

        verify(etrnRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("list — поиск с фильтром statusFilter")
    void list_withStatusFilter_filterApplied() {
        var pageSetting = new SearchEtrnDto.PageSetting(0, 10);
        var searchRequest = new SearchEtrnDto(
                pageSetting, null, null, EtrnCardStatus.READY_FOR_BANK_ACTION.name(), null
        );

        Page<EtrnJournalDto> mockPage = new PageImpl<>(List.of());
        when(etrnRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);

        service.list(searchRequest);

        verify(etrnRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("list — поиск с сортировкой DESC")
    void list_withDescendingSort_sortApplied() {
        var pageSetting = new SearchEtrnDto.PageSetting(0, 10);
        var sortSetting = new SearchEtrnDto.SortSetting(false, "updatedAt");
        var searchRequest = new SearchEtrnDto(
                pageSetting, sortSetting, null, null, null
        );

        Page<EtrnJournalDto> mockPage = new PageImpl<>(List.of());
        when(etrnRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);

        Page<EtrnJournalDto> result = service.list(searchRequest);

        assertThat(result).isNotNull();
    }
}
