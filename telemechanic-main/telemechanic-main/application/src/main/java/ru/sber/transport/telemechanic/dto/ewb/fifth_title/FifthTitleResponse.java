package ru.sber.transport.telemechanic.dto.ewb.fifth_title;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

@Schema(name = "FifthTitleResponse", description = "Ответ на запрос формирования пятого титула")
public record FifthTitleResponse(
    @Schema(description = "Наименование сформированного файла")
    String fileName,
    @Schema(description = "Контент сформированного файла")
    byte[] content,
    @Schema(description = "Дата и время создания файла")
    LocalDateTime creationTime
) {
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (FifthTitleResponse) o;
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
        return "FifthTitleResponse{fileName='" + fileName + "', content=" + Arrays.toString(content) + ", creationTime=" + creationTime.toString() + "}";
    }
}
