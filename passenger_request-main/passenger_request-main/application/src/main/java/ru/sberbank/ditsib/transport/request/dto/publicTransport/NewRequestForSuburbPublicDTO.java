package ru.sberbank.ditsib.transport.request.dto.publicTransport;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;

import java.util.List;

/**
 * DTO с данными по новой заявке на компенсацию за <b>пригородный</b> транспорт
 **/
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(
        title = "DTO с данными по новой заявке на компенсацию за пригородный транспорт",
        description = "Данные по новой заявке"
)
@Deprecated
public class NewRequestForSuburbPublicDTO extends NewRequestForPublicDTO {
    
    /**
     * Тип заявки на компенсацию за общественный транспорт
     */
    @Schema(description = "Тип заявки на компенсацию за общественный транспорт")
    @Builder.Default
    private PublicCompensationType compensationType = PublicCompensationType.SUBURB_TRIP_COMPENSATION;
    
    /**
     * Список документов для подтверждения оплаты
     */
    @Schema(description = "Список документов для подтверждения оплаты")
    private List<@Valid CompensationDocumentDTO> compensationDocuments;
}
