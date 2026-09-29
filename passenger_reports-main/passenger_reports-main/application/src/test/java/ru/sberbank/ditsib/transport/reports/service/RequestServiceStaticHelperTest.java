package ru.sberbank.ditsib.transport.reports.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sberbank.ditsib.transport.constants.PaymentTypeCode;
import ru.sberbank.ditsib.transport.reports.dto.PaymentDataDTO;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestServiceStaticHelperTest {
    
    @Test
    @DisplayName("Расчет суммы к выплате с учетом налогообложения")
    void getPaymentCost() {
        List<PaymentDataDTO> paymentDataList = new ArrayList<>();
        paymentDataList.add(PaymentDataDTO.builder().paymentTypeCode(PaymentTypeCode.CODE_4665).paymentPrice(2000L).build());
        paymentDataList.add(PaymentDataDTO.builder().paymentTypeCode(PaymentTypeCode.CODE_4664).paymentPrice(3000L).build());
        paymentDataList.add(PaymentDataDTO.builder().paymentTypeCode(PaymentTypeCode.CODE_4661).paymentPrice(5000L).build());
        
        var paymentCost = RequestServiceStaticHelper.getPaymentCost(paymentDataList);
        
        assertEquals(9610L, paymentCost, "Сумма к выплате должна быть равна Сумма4661 + Сумма4665 + 0,87*Сумма4664");
    }
}