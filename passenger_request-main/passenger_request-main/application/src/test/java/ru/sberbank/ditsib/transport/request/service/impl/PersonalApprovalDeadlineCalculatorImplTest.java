package ru.sberbank.ditsib.transport.request.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPersonal;
import ru.sberbank.ditsib.transport.request.service.ApprovalDeadlineCalculator;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Расчет контрольного срока на согласование заявки на ЛТ")
class PersonalApprovalDeadlineCalculatorImplTest {
    
    @Test
    @DisplayName("Желаемое время поездки через 1 минуту после создания")
    void getApprovalDeadline() {
        ApprovalDeadlineCalculator approvalDeadlineCalculator = new PersonalApprovalDeadlineCalculatorImpl();
        var creationTime = LocalDateTime.of(2024, 2, 22, 14, 0, 0);
        var desiredDate = LocalDateTime.of(2024, 2, 22, 14, 1, 0);
        
        var approvalDeadline = approvalDeadlineCalculator.getApprovalDeadline(creationTime, desiredDate, "GMT+03");
        
        assertEquals(LocalDateTime.of(2024, 2, 26, 14, 45),
                     approvalDeadline,
                     "На согласование заявки на личный транспорт дается не более 8 рабочих часов с момента создания заявки");
    }
    
    @Test
    @DisplayName("Контрольный срок согласования для пассажира. До начала поездки меньше 8 часов")
    void getApprovalDeadlineForPassLess8HoursLeft() {
        ApprovalDeadlineCalculator cut = new PersonalApprovalDeadlineCalculatorImpl();
        
        var creationTime = LocalDateTime.of(2024, 2, 22, 14, 0, 0,0);
        var desiredDate = LocalDateTime.of(2024, 2, 22, 14, 1, 0,0);
        
        var request = RequestForPersonal.builder()
                .creationTime(creationTime)
                .desiredDate(desiredDate)
                .coopTrip(true)
                .sharedRideOwner(false)
                .timeZone("GMT+03")
                .build();
        var result = cut.getApprovalDeadline(request);
        
        assertEquals(desiredDate.minusMinutes(15),
                     result,
                     "КС согласования срочной заявки пассажира должен быть за 15 минут до желаемого времени начала поездки водителем");
    }
}