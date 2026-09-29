package ru.sberbank.ditsib.transport.reports.model;

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
    
    /**
     * Код вида страховой оплаты
     */
    @Column(name = "payment_type_code_insurance")
    private Integer paymentTypeCodeInsurance;
    
    /**
     * Сумма страховой оплаты, коп
     */
    @Column(name = "payment_price_insurance")
    private Long paymentPriceInsurance;

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
    
    public PaymentTypeCode getPaymentTypeCodeInsurance () {
        return PaymentTypeCode.parse(this.paymentTypeCodeInsurance);
    }
    
    public void setPaymentTypeCodeInsurance(PaymentTypeCode code) {
        this.paymentTypeCodeInsurance = code.getCode();
    }

    public boolean thereIsOptionalPayment() {
        return paymentTypeCodeOptional != null && paymentPriceOptional != null && paymentPriceOptional > 0;
    }
    
    public boolean thereIsInsurancePayment() {
        return paymentTypeCodeInsurance != null && paymentPriceInsurance != null && paymentPriceInsurance > 0;
    }

}
