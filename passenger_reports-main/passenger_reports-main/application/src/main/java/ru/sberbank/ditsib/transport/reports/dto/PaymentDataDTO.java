package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.transport.constants.PaymentTypeCode;

@Data
@Schema(title = "Данные по оплате", description = "Сумма по фактическим видам выплат")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDataDTO {
    @Schema(description = "Код вида оплаты")
    private PaymentTypeCode paymentTypeCode;

    @Schema(description = "Сумма оплаты, коп")
    private Long paymentPrice;
}
