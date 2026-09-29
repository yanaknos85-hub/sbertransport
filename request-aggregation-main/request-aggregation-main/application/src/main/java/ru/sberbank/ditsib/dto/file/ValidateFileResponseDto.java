package ru.sberbank.ditsib.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sberbank.ditsib.dto.lead.ValidatedLeadDto;

import java.util.List;

@Schema(description = "Ответ по валидации файла с заявками")
public record ValidateFileResponseDto(
        @Schema(description = "Результаты валидации строк файла")
        List<ValidatedLeadDto> content
) {
}
