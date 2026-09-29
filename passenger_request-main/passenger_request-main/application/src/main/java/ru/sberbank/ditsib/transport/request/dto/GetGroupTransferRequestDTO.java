package ru.sberbank.ditsib.transport.request.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.GroupTransferClass;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({ "id" })
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "Заявка (Чтение)", description = "Данные заявки для группового трансфера")
@SuperBuilder
public class GetGroupTransferRequestDTO extends GetRequestDTO {
    
    @Schema(description = "Идентификатор контрагента")
    private UUID contractorId;
    
    @Schema(description = "Признак VIP")
    private boolean vip;
    
    @Schema(description = "Класс группового трансфера")
    private GroupTransferClass groupTransferClass;
    
    @Schema(description = "Дополнительная информация о заявке")
    private GroupTransferRequestInformationDTO information;
    
}