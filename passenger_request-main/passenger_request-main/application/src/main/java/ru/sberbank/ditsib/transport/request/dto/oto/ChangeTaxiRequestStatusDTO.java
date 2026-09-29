package ru.sberbank.ditsib.transport.request.dto.oto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.validate.TripRequestStatusConstraint;

import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.TAXI_AWAITING_SEARCH;
import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.TAXI_CANCELLED;
import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.TAXI_DRIVER_FOUND;
import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.TAXI_TRIP_FINISHED;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Schema(title = "Изменение статуса заявки на Такси", description = "Изменение статуса заявки на Такси")
public class ChangeTaxiRequestStatusDTO {
    
    @Schema(description = "Статус заявки [TAXI_AWAITING_SEARCH | TAXI_DRIVER_FOUND | TAXI_TRIP_FINISHED | TAXI_CANCELLED] ",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @TripRequestStatusConstraint(allowed = { TAXI_AWAITING_SEARCH, TAXI_DRIVER_FOUND, TAXI_TRIP_FINISHED, TAXI_CANCELLED })
    private TripRequestStatus status;
    
    @Schema(description = "Код отмены заяки [TAXI_CANCELLED_BY_DRIVER | TAXI_CANCELLED_BY_EMPLOYEE]")
    private TripRequestStatus.TaxiStatusCode cancellationCode;
    
    @Schema(description = "Водитель найден, информация о машине")
    private CarInfoDTO carInfo;
    
}
