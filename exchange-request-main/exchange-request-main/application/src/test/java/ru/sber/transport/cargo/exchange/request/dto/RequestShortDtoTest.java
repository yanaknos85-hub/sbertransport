package ru.sber.transport.cargo.exchange.request.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sber.transport.cargo.exchange.request.enums.RequestStatus.DRAFT;

class RequestShortDtoTest {

    private static final UUID REQUEST_ID = UUID.randomUUID();
    private static final String HUMANREADABLE_ID = "ОП-202602-0000001";
    private static final UUID ORGANIZATION_ID = UUID.randomUUID();
    private static final String INTERNAL_ID = "ORD-12345";
    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final String STATUS = "PUBLISHED";
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 2, 1, 10, 0);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 2, 1, 11, 30);
    private static final LocalDateTime EXPIRES_AT = LocalDateTime.of(2026, 2, 3, 10, 0);
    private static final LocalDateTime PUBLISHED_AT = LocalDateTime.of(2026, 2, 1, 12, 0);
    private static final LocalDateTime COMPLETED_AT = LocalDateTime.of(2026, 2, 2, 18, 0);

    @Test
    void noArgsConstructor_shouldCreateInstance_WithNullFields() {
        // When
        RequestShortDto dto = new RequestShortDto();

        // Then
        assertThat(dto.getId()).isNull();
        assertThat(dto.getHumanReadableId()).isNull();
        assertThat(dto.getInternalId()).isNull();
        assertThat(dto.getOwnerId()).isNull();
        assertThat(dto.getStatus()).isNull();
        assertThat(dto.getCreatedAt()).isNull();
        assertThat(dto.getUpdatedAt()).isNull();
        assertThat(dto.getExpiresAt()).isNull();
        assertThat(dto.getPublishedAt()).isNull();
        assertThat(dto.getCompletedAt()).isNull();
    }

    @Test
    void allArgsConstructor_shouldInitializeAllFields() {
        // When
        RequestShortDto dto = new RequestShortDto(
                REQUEST_ID,
                HUMANREADABLE_ID,
                ORGANIZATION_ID,
                INTERNAL_ID,
                OWNER_ID,
                STATUS,
                CREATED_AT,
                UPDATED_AT,
                EXPIRES_AT,
                PUBLISHED_AT,
                COMPLETED_AT
        );

        // Then
        assertThat(dto.getId()).isEqualTo(REQUEST_ID);
        assertThat(dto.getHumanReadableId()).isEqualTo(HUMANREADABLE_ID);
        assertThat(dto.getInternalId()).isEqualTo(INTERNAL_ID);
        assertThat(dto.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(dto.getStatus()).isEqualTo(STATUS);
        assertThat(dto.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(dto.getUpdatedAt()).isEqualTo(UPDATED_AT);
        assertThat(dto.getExpiresAt()).isEqualTo(EXPIRES_AT);
        assertThat(dto.getPublishedAt()).isEqualTo(PUBLISHED_AT);
        assertThat(dto.getCompletedAt()).isEqualTo(COMPLETED_AT);
    }

    @Test
    void builder_shouldCreateInstance_WithAllFields() {
        // When
        RequestShortDto dto = RequestShortDto.builder()
                .id(REQUEST_ID)
                .humanReadableId(HUMANREADABLE_ID)
                .internalId(INTERNAL_ID)
                .ownerId(OWNER_ID)
                .status(STATUS)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .expiresAt(EXPIRES_AT)
                .publishedAt(PUBLISHED_AT)
                .completedAt(COMPLETED_AT)
                .build();

        // Then
        assertThat(dto.getId()).isEqualTo(REQUEST_ID);
        assertThat(dto.getHumanReadableId()).isEqualTo(HUMANREADABLE_ID);
        assertThat(dto.getInternalId()).isEqualTo(INTERNAL_ID);
        assertThat(dto.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(dto.getStatus()).isEqualTo(STATUS);
        assertThat(dto.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(dto.getUpdatedAt()).isEqualTo(UPDATED_AT);
        assertThat(dto.getExpiresAt()).isEqualTo(EXPIRES_AT);
        assertThat(dto.getPublishedAt()).isEqualTo(PUBLISHED_AT);
        assertThat(dto.getCompletedAt()).isEqualTo(COMPLETED_AT);
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        // Given
        RequestShortDto dto = new RequestShortDto();

        // When
        dto.setId(UUID.randomUUID());
        dto.setHumanReadableId("ОП-202602-0000002");
        dto.setInternalId("ORD-67890");
        dto.setOwnerId(UUID.randomUUID());
        dto.setStatus(DRAFT.name());
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now().plusHours(1));
        dto.setExpiresAt(LocalDateTime.now().plusDays(2));
        dto.setPublishedAt(LocalDateTime.now().plusHours(2));
        dto.setCompletedAt(LocalDateTime.now().plusDays(1));

        // Then
        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getHumanReadableId()).isEqualTo("ОП-202602-0000002");
        assertThat(dto.getInternalId()).isEqualTo("ORD-67890");
        assertThat(dto.getOwnerId()).isNotNull();
        assertThat(dto.getStatus()).isEqualTo(DRAFT.name());
        assertThat(dto.getCreatedAt()).isNotNull();
        assertThat(dto.getUpdatedAt()).isNotNull();
        assertThat(dto.getExpiresAt()).isNotNull();
        assertThat(dto.getPublishedAt()).isNotNull();
        assertThat(dto.getCompletedAt()).isNotNull();
    }

    @Test
    void toString_shouldContainKeyFields() {
        // Given
        RequestShortDto dto = RequestShortDto.builder()
                .id(REQUEST_ID)
                .humanReadableId(HUMANREADABLE_ID)
                .status(STATUS)
                .build();

        // When
        String result = dto.toString();

        // Then
        assertThat(result).contains(REQUEST_ID.toString());
        assertThat(result).contains(HUMANREADABLE_ID);
        assertThat(result).contains(STATUS);
    }
}


