package ru.sberbank.ditsib.transport.reports.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Допустимые для загрузки типы файлов
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum UploadFileFormats {
    
    XLS("application/vnd.ms-excel", "xls"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
    
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
}
