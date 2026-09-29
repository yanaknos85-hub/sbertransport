package ru.sber.transport.trip.utils;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.trip.business.model.HasName;
import ru.sber.transport.trip.web.service.impl.file_resolvers.utils.StringHandlerUtil;

import static org.junit.jupiter.api.Assertions.*;
@DisplayName("Проверка обработчика строк")
@UnitTest
@Feature("app_platform_trips")
class StringHandlerUtilTest {

    @DisplayName("Проверка метода mapNotNull для null")
    @Test
    public void mapNotNullNullInputTest() {
        String result = StringHandlerUtil.mapNotNull(null);

        assertEquals("Н/Д", result);
    }

    @DisplayName("Проверка метода mapNotNull для строки c пробелами")
    @Test
    public void mapNotNullBlankInputTest() {
        String result = StringHandlerUtil.mapNotNull("   ");

        assertEquals("Н/Д", result);
    }

    @DisplayName("Проверка метода mapNotNull для пустой строки")
    @Test
    public void mapNotNullEmptyInputTest() {
        String result = StringHandlerUtil.mapNotNull("");

        assertEquals("Н/Д", result);
    }

    @DisplayName("Проверка метода mapNotNull для непустой строки")
    @Test
    public void mapNotNullNonEmptyInputTest() {
        String result = StringHandlerUtil.mapNotNull("Привет");

        assertEquals("Привет", result);
    }

    @DisplayName("Проверка метода append для пустой строки")
    @Test
    public void appendEmptyBuilderTest() {
        StringBuilder builder = new StringBuilder();
        StringHandlerUtil.append(builder, "Привет", ", ");

        assertEquals("Привет", builder.toString());
    }

    @DisplayName("Проверка метода append")
    @Test
    public void appendNonEmptyBuilderTest() {
        StringBuilder builder = new StringBuilder("Здравствуйте");
        StringHandlerUtil.append(builder, "Привет", ", ");

        assertEquals("Здравствуйте, Привет", builder.toString());
    }

    @DisplayName("Проверка метода append для null")
    @Test
    public void appendNullSourceTest() {
        StringBuilder builder = new StringBuilder("Текст");

        StringHandlerUtil.append(builder, null, ", ");

        assertEquals("Текст", builder.toString());
    }

    @DisplayName("Проверка метода append для пустой строки")
    @Test
    public void appendEmptySourceTest() {
        StringBuilder builder = new StringBuilder("Текст");

        StringHandlerUtil.append(builder, "", ", ");

        assertEquals("Текст, ", builder.toString());
    }

    @DisplayName("Проверка метода append")
    @Test
    public void appendMultipleAppendsTest() {
        StringBuilder builder = new StringBuilder();

        StringHandlerUtil.append(builder, "Имя", ", ");
        StringHandlerUtil.append(builder, "Фамилия", ", ");
        StringHandlerUtil.append(builder, "Отчество", ", ");

        assertEquals("Имя, Фамилия, Отчество", builder.toString());
    }

    @DisplayName("Проверка метода mapName")
    @Test
    public void mapNameFullNameTest() {
        HasName hasName = mockHasName("Иван", "Иванович", "Иванов");
        assertEquals("Иван Иванович И.", StringHandlerUtil.mapName(hasName));
    }

    private HasName mockHasName(String firstName, String patronymic, String lastName) {
        return new HasName() {
            @Override
            public String getFirstName() {
                return firstName;
            }

            @Override
            public String getPatronymic() {
                return patronymic;
            }

            @Override
            public String getLastName() {
                return lastName;
            }
        };
    }
}