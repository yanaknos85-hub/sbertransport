package ru.sber.transport.common.api.service.model.dto.cargo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * CargoDetail
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CargoDetail implements Serializable {
  @JsonProperty("orderingIndex")
  @Schema(description = "Порядковый номер груза (уникальный в разрезе заявки)")
  private Integer orderingIndex;

  @JsonProperty("cargoName")
  @Schema(description = "Наименование груза")
  private String cargoName;

  @JsonProperty("weight")
  @Schema(description = "Масса, кг")
  private Double weight;

  @JsonProperty("volume")
  @Schema(description = "Объем, м3")
  private Double volume;

  @JsonProperty("occupiedPlacesCount")
  @Schema(description = "Количество мест")
  private Integer occupiedPlacesCount;

  @JsonProperty("height")
  @Schema(description = "Высота, см")
  private Double height;

  @JsonProperty("length")
  @Schema(description = "Длина, см")
  private Double length;

  @JsonProperty("width")
  @Schema(description = "Ширина, см")
  private Double width;

  @JsonProperty("fragile")
  @Schema(description = "Бьющийся")
  private boolean fragile;
}

