package ru.sberbank.ditsib.transport.request.dto.oto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Schema(title = "Краткая информация по контрагенту", description = "Краткая информация по контрагенту")
public class ContractorShortDTO {

    /**
     * Идентификатор контрагента
     */
    @Schema(description = "Идентификатор контрагента")
    UUID id;

    /**
     * Имя контрагента
     */
    @Schema(description = "Имя контрагента")
    String name;
}
