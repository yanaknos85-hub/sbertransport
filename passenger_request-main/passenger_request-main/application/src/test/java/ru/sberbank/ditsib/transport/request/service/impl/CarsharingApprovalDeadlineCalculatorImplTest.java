package ru.sberbank.ditsib.transport.request.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.request.service.ApprovalDeadlineCalculator;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Расчет контрольного срока на согласование заявки на каршеринг")
class CarsharingApprovalDeadlineCalculatorImplTest {
    
    @Test
    @DisplayName("Желаемое время поездки через 1 минуту после создания")
    void getApprovalDeadline() {
        ApprovalDeadlineCalculator approvalDeadlineCalculator = new CarsharingApprovalDeadlineCalculatorImpl();
        var creationTime = LocalDateTime.of(2024, 2, 9, 13, 45, 0,0);
        var desiredDate = LocalDateTime.of(2024, 2, 9, 13, 46, 0, 0);
        
        var approvalDeadline = approvalDeadlineCalculator.getApprovalDeadline(creationTime, desiredDate, "GMT+03");
        
        assertEquals(LocalDateTime.of(2024, 2, 12, 14, 45,0),
                     approvalDeadline,
                     "На согласование заявки на каршеринг дается не более 8 рабочих часов с момента создания заявки");
    }
}