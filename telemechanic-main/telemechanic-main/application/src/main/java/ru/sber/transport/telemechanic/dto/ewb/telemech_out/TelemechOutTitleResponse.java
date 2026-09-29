package ru.sber.transport.telemechanic.dto.ewb.telemech_out;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "TelemechOutTitleResponse", title = "Ответ на формирование титула")
public record TelemechOutTitleResponse(
        @NotBlank
        @Schema(description = "Имя файла", requiredMode = REQUIRED)
        String fileName,
        @Schema(description = "Сформированный файл", requiredMode = REQUIRED)
        byte[] content,
        @Schema(description = "Дата и время формирования титула", requiredMode = REQUIRED)
        LocalDateTime creationTime
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (TelemechOutTitleResponse) o;
        return fileName.equals(that.fileName) && Arrays.equals(content, that.content) && creationTime.equals(that.creationTime);
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(fileName, creationTime);
        result = 31 * result + Arrays.hashCode(content);
        return result;
    }
    
    @Override
    public String toString() {
        return "TelemechOutTitleResponse{fileName='" + fileName + "', content=" + Arrays.toString(content) + ", creationTime=" + creationTime.toString() + "}";
    }
}
