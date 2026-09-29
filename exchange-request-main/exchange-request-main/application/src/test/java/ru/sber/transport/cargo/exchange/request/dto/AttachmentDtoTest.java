package ru.sber.transport.cargo.exchange.request.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AttachmentDtoTest {

    private static final UUID ATTACHMENT_ID = UUID.randomUUID();
    private static final UUID REQUEST_ID = UUID.randomUUID();
    private static final String FILE_TYPE = "cargo_photo";
    private static final String ORIGINAL_NAME = "photo_001.jpg";
    private static final String STORAGE_PATH = "/uploads/2026/02/01/photo_abc123.jpg";
    private static final Long FILE_SIZE = 5_243_100L; // ~5.2 МБ
    private static final String MIME_TYPE = "image/jpeg";
    private static final LocalDateTime UPLOAD_AT = LocalDateTime.of(2026, 2, 1, 10, 30, 0);

    @Test
    void noArgsConstructor_shouldCreateInstance_WithNullFields() {
        // When
        AttachmentDto dto = new AttachmentDto();

        // Then
        assertThat(dto.getId()).isNull();
        assertThat(dto.getRequestId()).isNull();
        assertThat(dto.getFileType()).isNull();
        assertThat(dto.getOriginalName()).isNull();
        assertThat(dto.getStoragePath()).isNull();
        assertThat(dto.getFileSize()).isNull();
        assertThat(dto.getMimeType()).isNull();
        assertThat(dto.getUploadAt()).isNull();
    }

    @Test
    void allArgsConstructor_shouldInitializeAllFields() {
        // When
        AttachmentDto dto = new AttachmentDto(
                ATTACHMENT_ID,
                REQUEST_ID,
                FILE_TYPE,
                ORIGINAL_NAME,
                STORAGE_PATH,
                FILE_SIZE,
                MIME_TYPE,
                UPLOAD_AT
        );

        // Then
        assertThat(dto.getId()).isEqualTo(ATTACHMENT_ID);
        assertThat(dto.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(dto.getFileType()).isEqualTo(FILE_TYPE);
        assertThat(dto.getOriginalName()).isEqualTo(ORIGINAL_NAME);
        assertThat(dto.getStoragePath()).isEqualTo(STORAGE_PATH);
        assertThat(dto.getFileSize()).isEqualTo(FILE_SIZE);
        assertThat(dto.getMimeType()).isEqualTo(MIME_TYPE);
        assertThat(dto.getUploadAt()).isEqualTo(UPLOAD_AT);
    }

    @Test
    void builder_shouldCreateInstance_WithAllFields() {
        // When
        AttachmentDto dto = AttachmentDto.builder()
                .id(ATTACHMENT_ID)
                .requestId(REQUEST_ID)
                .fileType(FILE_TYPE)
                .originalName(ORIGINAL_NAME)
                .storagePath(STORAGE_PATH)
                .fileSize(FILE_SIZE)
                .mimeType(MIME_TYPE)
                .uploadAt(UPLOAD_AT)
                .build();

        // Then
        assertThat(dto.getId()).isEqualTo(ATTACHMENT_ID);
        assertThat(dto.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(dto.getFileType()).isEqualTo(FILE_TYPE);
        assertThat(dto.getOriginalName()).isEqualTo(ORIGINAL_NAME);
        assertThat(dto.getStoragePath()).isEqualTo(STORAGE_PATH);
        assertThat(dto.getFileSize()).isEqualTo(FILE_SIZE);
        assertThat(dto.getMimeType()).isEqualTo(MIME_TYPE);
        assertThat(dto.getUploadAt()).isEqualTo(UPLOAD_AT);
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        // Given
        AttachmentDto dto = new AttachmentDto();

        // When
        dto.setId(UUID.randomUUID());
        dto.setRequestId(UUID.randomUUID());
        dto.setFileType("document");
        dto.setOriginalName("contract.pdf");
        dto.setStoragePath("/docs/contract_xyz.pdf");
        dto.setFileSize(8_765_432L);
        dto.setMimeType("application/pdf");
        dto.setUploadAt(LocalDateTime.now());

        // Then
        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getRequestId()).isNotNull();
        assertThat(dto.getFileType()).isEqualTo("document");
        assertThat(dto.getOriginalName()).isEqualTo("contract.pdf");
        assertThat(dto.getStoragePath()).isEqualTo("/docs/contract_xyz.pdf");
        assertThat(dto.getFileSize()).isEqualTo(8_765_432L);
        assertThat(dto.getMimeType()).isEqualTo("application/pdf");
        assertThat(dto.getUploadAt()).isNotNull();
    }


    @Test
    void toString_shouldContainIdAndFileType() {
        // Given
        AttachmentDto dto = AttachmentDto.builder()
                .id(ATTACHMENT_ID)
                .fileType(FILE_TYPE)
                .originalName(ORIGINAL_NAME)
                .fileSize(FILE_SIZE)
                .build();

        // When
        String result = dto.toString();

        // Then
        assertThat(result).contains(ATTACHMENT_ID.toString());
        assertThat(result).contains(FILE_TYPE);
        assertThat(result).contains(ORIGINAL_NAME);
        assertThat(result).contains(FILE_SIZE.toString());
    }
}



