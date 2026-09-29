package ru.sber.transport.common.api.service.model.dto.cargo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Auto
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Auto implements Serializable {
  @JsonProperty("weight")
  @Schema(description = "Грузоподъемность, кг")
  private Double weight;

  @JsonProperty("volume")
  @Schema(description = "Объем, м3")
  private Double volume;
}

