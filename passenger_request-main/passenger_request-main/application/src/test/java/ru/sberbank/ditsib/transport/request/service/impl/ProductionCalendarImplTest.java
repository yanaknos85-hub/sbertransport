package ru.sberbank.ditsib.transport.request.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;

import java.time.LocalDateTime;
import java.time.ZoneId;

@UnitTest
@Isolated
@Feature("app_passenger_request")
class ProductionCalendarImplTest {
    
    @Test
    @DisplayName("Проверка добавления рабочего времени с учетом начала отчета с нерабочего времени, тайм-зоны, предпраздничных и праздничных дней")
    void addWorkingMinutes() {
        
        var cut = new ProductionCalendarImpl();
        
        var dateTime = LocalDateTime.of(2024, 2, 22, 1, 0, 0);
        
        var result = cut.addWorkingMinutes(dateTime, 480, "GMT+03");
        
        org.junit.jupiter.api.Assertions.assertEquals(LocalDateTime.of(2024, 2, 26, 6, 45, 0),
                                                      result,
                                                      "22.02.2024 - 7 часов 15 мин как у предпраздничного дня (не пятница), 23.02.2024 - " +
                                                      "праздничный день, значит итоговая дата должна быть равна 09:45 26.02.2024");
    }
    
    @Test
    @DisplayName("Проверка добавления рабочего времени с учетом обеда в пятницу")
    void addWorkingMinutes_friday() {
        
        var cut = new ProductionCalendarImpl();
        
        var dateTime = LocalDateTime.of(2024, 2, 2, 10, 0, 0, 0);
        
        var result = cut.addWorkingMinutes(dateTime, 480, "GMT+03");
        
        org.junit.jupiter.api.Assertions.assertEquals(
                LocalDateTime.of(2024, 2, 5, 11, 45, 0, 0),
                result,
                "02.02.2024 - после обеда в пятницу остается 3ч рабочего времени, значит итоговая дата с учетом выходных и обеда с 13:00 " +
                "по 13:45 понедельника должна быть равна 14:45 05.02.2024");
    }
}