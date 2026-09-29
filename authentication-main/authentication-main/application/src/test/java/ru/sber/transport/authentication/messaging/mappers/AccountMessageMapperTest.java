package ru.sber.transport.authentication.messaging.mappers;

import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@Slf4j
@DisplayName("Проверка маппера сообщений аккаунтов")
public class AccountMessageMapperTest {

    private final AccountMessageMapper accountMessageMapper = new AccountMessageMapperImpl();

    @Test
    @DisplayName("Проверка генерирования логина")
    void generateUserLoginTest(){
        var firstName = " Им я ";
        var lastName = " Фам ил-ия ";
        var patronymic = " От чество ";
        var expected = "Famil-ijaI-O";

        var result = accountMessageMapper.generateUserLogin(firstName, lastName, patronymic);
        assertEquals(expected, result);
    }
}
