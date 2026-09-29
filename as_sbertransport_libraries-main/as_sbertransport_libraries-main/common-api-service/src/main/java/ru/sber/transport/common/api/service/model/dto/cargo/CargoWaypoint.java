package ru.sber.transport.common.api.service.model.dto.cargo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * CargoWaypoint
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CargoWaypoint implements Serializable {
  @JsonProperty("orderingIndex")
  @Schema(description = "Порядковый номер точки")
  private Integer orderingIndex;

  @JsonProperty("id")
  @Schema(description = "внутренний id точки в АС Сбертранспорт")
  private UUID id;

  @JsonProperty("type")
  @Schema(description = "Тип точки (сбор/доставка/сбор-доставка)", example = "LOAD | UNLOAD | LOAD_UNLOAD")
  private String type;

  @JsonProperty("address")
  @Schema(description = "Адрес")
  private Address address;

  @JsonProperty("contacts")
  @Schema(description = "Уникальные контакты на точке")
  private List<CargoWaypointContact> contacts;
}

