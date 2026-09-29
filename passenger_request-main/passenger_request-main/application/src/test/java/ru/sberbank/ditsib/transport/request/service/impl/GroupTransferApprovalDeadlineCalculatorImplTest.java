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
@DisplayName("Расчет контрольного срока на согласование заявки на групповой трансфер")
class GroupTransferApprovalDeadlineCalculatorImplTest {
    
    @Test
    @DisplayName("Желаемое время поездки через 1 минуту после создания")
    void getApprovalDeadline_1min() {
        ApprovalDeadlineCalculator approvalDeadlineCalculator = new GroupTransferApprovalDeadlineCalculatorImpl();
        var creationTime = LocalDateTime.of(2023, 11, 18, 10, 0, 0);
        var desiredDate = LocalDateTime.of(2023, 11, 18, 10, 1, 0);
        
        var approvalDeadline = approvalDeadlineCalculator.getApprovalDeadline(creationTime, desiredDate, "GMT+03");
        
        assertEquals(LocalDateTime.of(2023, 11, 18, 10, 15),
                     approvalDeadline,
                     "На согласование заявки на групповой трансфер, созданной менее чем за 2 часа до поездки, дается не более 15 минут с момента " +
                     "создания заявки");
    }
    
    @Test
    @DisplayName("Желаемое время поездки через 2 часа после создания")
    void getApprovalDeadline_2hour() {
        ApprovalDeadlineCalculator approvalDeadlineCalculator = new GroupTransferApprovalDeadlineCalculatorImpl();
        var creationTime = LocalDateTime.of(2023, 11, 18, 10, 0, 0);
        var desiredDate = LocalDateTime.of(2023, 11, 18, 12, 0, 0);
        
        var approvalDeadline = approvalDeadlineCalculator.getApprovalDeadline(creationTime, desiredDate, "GMT+03");
        
        assertEquals(LocalDateTime.of(2023, 11, 18, 11, 0),
                     approvalDeadline,
                     "На согласование заявки на групповой трансфер, созданной за 2 часа и более до поездки, дается не более 1 часа до начала " +
                     "поездки");
    }
}