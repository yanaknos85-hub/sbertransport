package ru.sberbank.ditsib.transport.request.dto.publicTransport;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Schema(title = "Заявка на компенсацию за общественный транспорт", description = "Данные заявки")
@JsonPropertyOrder({ "id" })
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
public class TransportCompensationDTO extends NewTransportCompensationDTO{
    /**
     * Идентификатор
     */
    @NotNull
    @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;

    /**
     * Приложенный документ
     */
    @Schema(description = "Приложенный документ")
    private CompensationDocumentDTO compensationDocumentDTO;
}
