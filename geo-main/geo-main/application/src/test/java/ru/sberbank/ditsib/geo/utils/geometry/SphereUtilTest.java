package ru.sberbank.ditsib.geo.utils.geometry;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_geo")
@DisplayName("Проверка утилиты для работы с точками на сфере")
class SphereUtilTest {

    @Test
    @DisplayName("Получение дистанции между координатами в градусах")
    void test_distanceDeg() {
        double[] point1 = {55.67627623137461, 37.52115611035158};
        double[] point2 = {55.79521238365093, 37.493690290039076};
        var expectedDistance = 13353.923322612618;

        var result = new SphereUtil().distanceDeg(point1[0], point1[1], point2[0], point2[1]);
        assertTrue(Math.abs(expectedDistance - result) < 0.00001);
    }
}
