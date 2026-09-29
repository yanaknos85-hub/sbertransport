package ru.sber.transport.common.api.service.model.dto.cargo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Address
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Address implements Serializable {
  @JsonProperty("addressStringRepresentation")
  @Schema(description = "Строковое представление адреса")
  private String addressStringRepresentation;

  @JsonProperty("coordinates")
  @Schema(description = "Координаты")
  private Coordinates coordinates;
}

