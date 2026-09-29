package ru.sber.transport.telemechanic.enumerate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(title = "Статус загрузки файла в хранилище s3", description = "Статус загрузки файла в хранилище s3")
public enum FileStatus {
    
    NOT_UPLOADED,
    UPLOADED,
    DELETED
}
