package ru.sber.transport.telemechanic.dto.predict;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "Ошибки приборной панели", description = "Данные об ошибках приборной панели")
public record ErrorPredictDto(@JsonProperty("errors_list")
                              @Schema(description = "Список ошибок приборной панели", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                              List<String> errors) {
}
    