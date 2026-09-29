package ru.sberbank.ditsib.transport.request.database.model.publicTransport;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

import java.util.Optional;

/**
 * Допустимые для загрузки типы файлов
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum UploadFileFormats {
    
    JPG(MediaType.IMAGE_JPEG_VALUE, "jpg"),
    JPEG(MediaType.IMAGE_JPEG_VALUE, "jpeg"),
    GIF(MediaType.IMAGE_GIF_VALUE, "gif"),
    PNG(MediaType.IMAGE_PNG_VALUE, "png"),
    TIF("image/tiff", "tif"),
    TIFF("image/tiff", "tiff"),
    PDF(MediaType.APPLICATION_PDF_VALUE, "pdf"),
    DOC("application/msword", "doc"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"),
    HEIC("image/heic", "heic"),
    HEIF("image/heif", "heif");
    
    private final String mediaType;
    private final String fileFormat;
    
    public static Optional<UploadFileFormats> getByFileFormat(String fileFormat) {
        for (UploadFileFormats value : UploadFileFormats.values()) {
            if (value.getFileFormat().equals(fileFormat)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
    
    public static Optional<UploadFileFormats> getByMediaType(String mediaType) {
        for (UploadFileFormats value : UploadFileFormats.values()) {
            if (value.getMediaType().equals(mediaType)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
