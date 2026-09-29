package ru.sberbank.ditsib.transport.request.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.request.dto.carsharing.CarsharingTripDTO;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({ "id" })
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "Заявка (Чтение)", description = "Данные заявки для каршеринга")
@SuperBuilder
public class GetCarsharingRequestDTO extends GetRequestDTO {
    
    @Schema(description = "Идентификатор контрагента")
    private UUID contractorId;
    
    @Schema(description = "Коментарий при отмене инженером")
    private String statusComment;
    
    @Schema(description = "Фактическая информация о поездке")
    private CarsharingTripDTO trip;
    
    @Schema(description = "Номер телефона, который использовался при создании заявки")
    private String phoneNumber;
    
}