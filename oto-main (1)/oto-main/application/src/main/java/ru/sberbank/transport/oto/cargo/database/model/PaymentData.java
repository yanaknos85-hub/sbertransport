package ru.sberbank.transport.oto.cargo.database.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.transport.constants.PaymentTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentData {

    /**
     * Код вида основной оплаты
     */
    @Column(name = "payment_type_code_main")
    private Integer paymentTypeCodeMain;

    /**
     * Сумма основной оплаты, коп
     */
    @Column(name = "payment_price_main")
    private Long paymentPriceMain;

    /**
     * Код вида дополнительной оплаты
     */
    @Column(name = "payment_type_code_optional")
    private Integer paymentTypeCodeOptional;

    /**
     * Сумма дополнительной оплаты, коп
     */
    @Column(name = "payment_price_optional")
    private Long paymentPriceOptional;

    public PaymentTypeCode getPaymentTypeCodeMain () {
        return PaymentTypeCode.parse(this.paymentTypeCodeMain);
    }

    public void setPaymentTypeCodeMain(PaymentTypeCode code) {
        this.paymentTypeCodeMain = code.getCode();
    }

    public PaymentTypeCode getPaymentTypeCodeOptional () {
        return PaymentTypeCode.parse(this.paymentTypeCodeOptional);
    }

    public void setPaymentTypeCodeOptional(PaymentTypeCode code) {
        this.paymentTypeCodeOptional = code.getCode();
    }

}
