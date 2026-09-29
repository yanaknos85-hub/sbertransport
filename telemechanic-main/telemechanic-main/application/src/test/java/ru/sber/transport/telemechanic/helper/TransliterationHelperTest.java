package ru.sber.transport.telemechanic.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Проверка сервиса транслитерации")
class TransliterationHelperTest {

    @Test
    void transliterationNumber() {
        var actualList = List.of("A555XY33",
                "GT124ASF2",
                "0123456789abcehkmopstxy",
                "H 704 YK159",
                "А   373   EC999",
                "А120ОО90",
                "в832ут198");
        var expectList = List.of("А555ХУ33",
                "Т124АС2",
                "0123456789АВСЕНКМОРСТХУ",
                "Н704УК159",
                "А373ЕС999",
                "А120ОО90",
                "В832УТ198");

        for (int i = 0; i < actualList.size(); i++) {
            assertEquals(expectList.get(i), TransliterationHelper.transliterateNumber(actualList.get(i)));
        }
    }
}