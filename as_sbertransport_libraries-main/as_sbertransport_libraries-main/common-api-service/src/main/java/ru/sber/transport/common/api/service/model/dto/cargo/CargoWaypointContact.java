package ru.sber.transport.common.api.service.model.dto.cargo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sber.transport.common.api.service.model.dto.Contact;

import java.io.Serializable;
import java.util.List;

/**
 * CargoWaypointContact
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CargoWaypointContact implements Serializable {
  @JsonProperty("contact")
  @Schema(description = "Контактное лицо")
  private Contact contact;

  @JsonProperty("requests")
  @Schema(description = "Массив заявок, в которых данный контакт является отправителем/получателем")
  private List<CargoRequest> requests;
}

