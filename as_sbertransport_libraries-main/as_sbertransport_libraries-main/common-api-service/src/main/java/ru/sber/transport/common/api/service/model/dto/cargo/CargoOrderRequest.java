package ru.sber.transport.common.api.service.model.dto.cargo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sber.transport.common.api.service.model.dto.Contact;

import java.io.Serializable;
import java.util.List;

/**
 * CargoOrderRequest
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CargoOrderRequest implements Serializable {
  @JsonProperty("routeId")
  @Schema(description = "Идентификатор маршрута в АС Сбертранспорт")
  private String routeId;

  @JsonProperty("humanReadableId")
  @Schema(description = "Человеко-читаемый идентификатор маршрута в АС Сбертранспорт")
  private String humanReadableId;

  @JsonProperty("author")
  @Schema(description = "Автор маршрута")
  private Contact author;

  @JsonProperty("desiredDate")
  @Schema(description = "Дата доставки в UTC")
  private String desiredDate;

  @JsonProperty("desiredAuto")
  @Schema(description = "Желаемый вид автомобиля")
  private Auto desiredAuto;

  @JsonProperty("workGroup")
  @Schema(description = "Рабочая группа")
  private String workGroup;

  @JsonProperty("contragentInn")
  @Schema(description = "ИНН контрагента")
  private String contragentInn;

  @JsonProperty("comment")
  @Schema(description = "Комментарий")
  private String comment;

  @JsonProperty("waypoints")
  @Schema(description = "Массив точек на Маршруте")
  private List<CargoWaypoint> waypoints;
}

