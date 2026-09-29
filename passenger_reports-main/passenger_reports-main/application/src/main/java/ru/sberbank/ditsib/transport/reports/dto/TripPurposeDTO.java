package ru.sberbank.ditsib.transport.reports.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Schema(title = "Цель поездки", description = "Описание цели поездки по заявке")
public class TripPurposeDTO {

    @Schema(description = "Идентификатор")
    private UUID id;

    @NotNull
    @Schema(description = "Описание цели поездки", maxLength = 128)
    private String purpose;
}
