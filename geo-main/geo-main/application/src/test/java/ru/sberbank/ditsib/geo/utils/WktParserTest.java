package ru.sberbank.ditsib.geo.utils;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.geo.utils.geometry.PolyLine;
import ru.sberbank.ditsib.geo.utils.geometry.WktParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_geo")
@DisplayName("Проверка WKT-парсера")
class WktParserTest {
    
    @Test
    @DisplayName("Парсинг линии")
    void test_parse() {
        var actual = WktParser.<PolyLine>parseWkt("LINESTRING(1 2, 3 4)");
        
        assertThat(actual.getLines()).hasSize(2);
        assertThat(actual.getLines().get(0).getX()).isEqualTo(1);
        assertThat(actual.getLines().get(0).getY()).isEqualTo(2);
        assertThat(actual.getLines().get(1).getX()).isEqualTo(3);
        assertThat(actual.getLines().get(1).getY()).isEqualTo(4);
    }
    
    @Test
    @DisplayName("Парсинг линии. Неверный префикс")
    void test_wrongPrefix() {
        assertThatThrownBy(() -> WktParser.<PolyLine>parseWkt("WRONG(1 2, 3 4)"))
                .isInstanceOf(RuntimeException.class).hasMessage("Unknown WKT type for string 'WRONG(1 2, 3 4)'");
    }
    
    @Test
    @DisplayName("Парсинг линии. Неверное тело")
    void test_wrongBody() {
        assertThatThrownBy(() -> WktParser.<PolyLine>parseWkt("LINESTRING(1 2 3 4)"))
                .isInstanceOf(RuntimeException.class).hasMessage("Unparcelable linestring '1 2 3 4'");
    }
    
}