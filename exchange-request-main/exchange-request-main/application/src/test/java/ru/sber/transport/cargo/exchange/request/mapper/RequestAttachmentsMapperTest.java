package ru.sber.transport.cargo.exchange.request.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sber.transport.cargo.exchange.request.database.model.Attachment;
import ru.sber.transport.cargo.exchange.request.dto.AttachmentDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RequestMapper — Тест маппинга Attachment ↔ AttachmentDto")
class RequestAttachmentsMapperTest {

    @Autowired
    private RequestMapper requestMapper = new RequestMapperImpl();;

    private final UUID ATTACHMENT_ID = UUID.fromString("b0eebc99-9c0b-4ef8-bb6d-6bb9bd380011");
    private final UUID REQUEST_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001");

    private Attachment attachment;
    private AttachmentDto attachmentDto;

    @BeforeEach
    void setUp() {
        // Исходный объект Attachment
        attachment = Attachment.builder()
                .id(ATTACHMENT_ID)
                .requestId(REQUEST_ID)
                .originalName("invoice.pdf")
                .fileType("DOCUMENT")
                .storagePath("/files/invoice.pdf")
                .uploadAt(LocalDateTime.of(2026, 2, 1, 12, 0))
                .build();

        // Исходный объект AttachmentDto
        attachmentDto = AttachmentDto.builder()
                .id(ATTACHMENT_ID)
                .requestId(REQUEST_ID)
                .originalName("photo.jpg")
                .fileType("CARGO_PHOTO")
                .storagePath("/files/photo.jpg")
                .uploadAt(LocalDateTime.of(2026, 2, 2, 14, 30))
                .build();
    }

    @Test
    @DisplayName("Должен корректно маппить Attachment в AttachmentDto")
    void shouldMap_Attachment_To_AttachmentDto() {
        // When
        AttachmentDto result = requestMapper.toAttachmentDto(attachment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ATTACHMENT_ID);
        assertThat(result.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(result.getOriginalName()).isEqualTo("invoice.pdf");
        assertThat(result.getFileType()).isEqualTo("DOCUMENT");
        assertThat(result.getStoragePath()).isEqualTo("/files/invoice.pdf");
        assertThat(result.getUploadAt()).isEqualTo(LocalDateTime.of(2026, 2, 1, 12, 0));
    }


    @DisplayName("Mapping AttachmentDto to Attachment")
    void shouldMap_AttachmentDto_To_Attachment() {
        // When
        Attachment result = requestMapper.toAttachmentEntity(attachmentDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ATTACHMENT_ID);
        assertThat(result.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(result.getOriginalName()).isEqualTo("photo.jpg");
        assertThat(result.getFileType()).isEqualTo("CARGO_PHOTO");
        assertThat(result.getStoragePath()).isEqualTo("/files/photo.jpg");
        assertThat(result.getUploadAt()).isEqualTo(LocalDateTime.of(2026, 2, 2, 14, 30));
    }

    @Test
    @DisplayName("Должен маппить список вложений")
    void shouldMap_AttachmentList() {
        // Given
        List<Attachment> attachments = List.of(
                attachment,
                Attachment.builder()
                        .id(UUID.randomUUID())
                        .requestId(REQUEST_ID)
                        .originalName("doc2.pdf")
                        .fileType("DOCUMENT")
                        .storagePath("/files/doc2.pdf")
                        .uploadAt(LocalDateTime.now())
                        .build()
        );

        // When
        List<AttachmentDto> dtos = requestMapper.toAttachmentDtoList(attachments);
        List<Attachment> entities = requestMapper.toAttachmentEntityList(dtos);

        // Then
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getOriginalName()).isEqualTo("invoice.pdf");
        assertThat(dtos.get(1).getOriginalName()).isEqualTo("doc2.pdf");

        assertThat(entities).hasSize(2);
        assertThat(entities.get(0).getOriginalName()).isEqualTo("invoice.pdf");
        assertThat(entities.get(1).getOriginalName()).isEqualTo("doc2.pdf");
    }

    @Test
    @DisplayName("Должен возвращать null при маппинге null")
    void shouldReturnNull_WhenMappingNull() {
        // When
        AttachmentDto dto = requestMapper.toAttachmentDto(null);
        Attachment entity = requestMapper.toAttachmentEntity(null);

        // Then
        assertThat(dto).isNull();
        assertThat(entity).isNull();
    }
}




