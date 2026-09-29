package ru.sberbank.ditsib.transport.request.dto.publicTransport;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * DTO с данными по новой заявке на компенсацию за общественный транспорт
 **/
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Компенсация за проезд", description = "Заявка на компенсацию за общественный транспорт")
public class NewRequestForCompensationDTO extends NewRequestForPublicDTO implements Serializable {
    
    /**
     * Заявки на компенсацию
     */
    @NotNull
    @Schema(description = "Список транспортных затрат", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@Valid NewTransportCompensationDTO> transportCompensation;
    
    /**
     * Список документов для подтверждения оплаты
     */
    @Schema(description = "Список документов для подтверждения оплаты")
    private List<@Valid CompensationDocumentDTO> compensationDocuments;
    
    private String employeeDeviceTimeZone;
}
